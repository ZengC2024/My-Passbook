package com.imooc.passbook.service.impl;

import com.alibaba.fastjson.JSON;
import com.imooc.passbook.constant.Constants;
import com.imooc.passbook.constant.PassStatus;
import com.imooc.passbook.dao.MerchantsDao;
import com.imooc.passbook.entity.Merchants;
import com.imooc.passbook.mapper.PassRowMapper;
import com.imooc.passbook.service.IUserPassService;
import com.imooc.passbook.vo.Pass;
import com.imooc.passbook.vo.PassInfo;
import com.imooc.passbook.vo.PassTemplate;
import com.imooc.passbook.vo.Response;
import com.spring4all.spring.boot.starter.hbase.api.HbaseTemplate;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.time.DateFormatUtils;
import org.apache.commons.lang.time.DateUtils;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.*;
import org.apache.hadoop.hbase.filter.*;
import org.apache.hadoop.hbase.util.Bytes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * <h1> 用户优惠券相关功能实现</h1>*/
@Service
@Slf4j
public class UserPassServiceImpl implements IUserPassService {
    /**hbase 客户端*/
    private HbaseTemplate hbaseTemplate;

    private MerchantsDao merchantsDao;

    @Autowired
    public UserPassServiceImpl(HbaseTemplate hbaseTemplate, MerchantsDao merchantsDao) {
        this.hbaseTemplate = hbaseTemplate;
        this.merchantsDao = merchantsDao;
    }


    /**
     * <h2>通过获取的 优惠券Pass 对象构造优惠券模版信息 Map</h2>
     * @param passes {@link Pass}
     * @return Map {@link PassTemplate}
     * */
    private Map<String, PassTemplate> buildPassTemplateMap(List<Pass> passes) throws Exception {

        String[] patterns = new String[] {"yyyy-MM-dd"};

        byte[] FAMILY_B = Bytes.toBytes(Constants.PassTemplateTable.FAMILY_B);
        byte[] ID = Bytes.toBytes(Constants.PassTemplateTable.ID);
        byte[] TITLE = Bytes.toBytes(Constants.PassTemplateTable.TITLE);
        byte[] SUMMARY = Bytes.toBytes(Constants.PassTemplateTable.SUMMARY);
        byte[] DESC = Bytes.toBytes(Constants.PassTemplateTable.DESC);
        byte[] HAS_TOKEN = Bytes.toBytes(Constants.PassTemplateTable.HAS_TOKEN);
        byte[] BACKGROUND = Bytes.toBytes(Constants.PassTemplateTable.BACKGROUND);

        byte[] FAMILY_C = Bytes.toBytes(Constants.PassTemplateTable.FAMILY_C);
        byte[] LIMIT = Bytes.toBytes(Constants.PassTemplateTable.LIMIT);
        byte[] START = Bytes.toBytes(Constants.PassTemplateTable.START);
        byte[] END = Bytes.toBytes(Constants.PassTemplateTable.END);

        /**根据passes获取所有的templateId（即rowKey，template在Hbase中存储位置）*/
        List<String> templateIds = passes.stream().map(
                Pass::getTemplateId
        ).collect(Collectors.toList());
        /**构造一个所有优惠券Get的list */
        List<Get> templateGets = new ArrayList<>(templateIds.size());
        /**java8的语法   将String类型的id转化成Get对象（Bytes型的）*/
        templateIds.forEach(t -> templateGets.add(new Get(Bytes.toBytes(t))));

        /**利用优惠券id的字节码构造的查询请求，通过Hbase获取所有的优惠券模版信息*/
        Result[] templateResults = hbaseTemplate.getConnection()
                .getTable(TableName.valueOf(Constants.PassTemplateTable.TABLE_NAME))
                .get(templateGets);

        // 构造 PassTemplateId -> PassTemplate Object 的 Map, 用于构造 PassInfo
        Map<String, PassTemplate> templateId2Object = new HashMap<>();
        for (Result item : templateResults) {
            PassTemplate passTemplate = new PassTemplate();
            /**前面构造的内容是Bytes数组*/
            passTemplate.setId(Bytes.toInt(item.getValue(FAMILY_B, ID)));
            passTemplate.setTitle(Bytes.toString(item.getValue(FAMILY_B, TITLE)));
            passTemplate.setSummary(Bytes.toString(item.getValue(FAMILY_B, SUMMARY)));
            passTemplate.setDesc(Bytes.toString(item.getValue(FAMILY_B, DESC)));
            passTemplate.setHasToken(Bytes.toBoolean(item.getValue(FAMILY_B, HAS_TOKEN)));
            passTemplate.setBackground(Bytes.toInt(item.getValue(FAMILY_B, BACKGROUND)));

            passTemplate.setLimit(Bytes.toLong(item.getValue(FAMILY_C, LIMIT)));
            passTemplate.setStart(DateUtils.parseDate(
                    Bytes.toString(item.getValue(FAMILY_C, START)), patterns));
            passTemplate.setEnd(DateUtils.parseDate(
                    Bytes.toString(item.getValue(FAMILY_C, END)), patterns
            ));
            /**构建了一个templateId到template的map映射*/
            templateId2Object.put(Bytes.toString(item.getRow()), passTemplate);
        }

        return templateId2Object;
    }
    /**
     * <h2> 通过获取的PassTemplate优惠券模版 对象构造商户Merchants对象  Map</h2>
     * * @param passTemplates {@link PassTemplate}
     *      * @return {@link Merchants}*/
    private Map<Integer, Merchants> buildMerchantsMap(List<PassTemplate> passTemplates){
        Map<Integer, Merchants> merchantsMap = new HashMap<>();
        List<Integer> merchantsIds = passTemplates.stream().map(
                PassTemplate::getId
        ).collect(Collectors.toList());
        /**通过商户Id获取商户信息*/
        List<Merchants> merchants = merchantsDao.findByIdIn(merchantsIds);

        /**java8的语法  构建商户id ->商户 的map映射*/
        merchants.forEach(m -> merchantsMap.put(m.getId(), m));

        return merchantsMap;

    }

    /**
     * <h2>根据优惠券状态和用户id获取某个用户特定状态的优惠券信息（即某个用户已使用的优惠券 和  某个用户未使用的优惠券）</h2>
     * @param userId 用户 id
     * @param status {@link PassStatus}
     * @return {@link Response}
     * */
    private Response getPassInfoByStatus(Long userId, PassStatus status) throws Exception {

        // 根据 userId 构造行键前缀
        byte[] rowPrefix =Bytes.toBytes(new StringBuilder(String.valueOf(userId)).reverse().toString());

        CompareFilter.CompareOp compareOp =
                status == PassStatus.UNUSED ?
                        CompareFilter.CompareOp.EQUAL : CompareFilter.CompareOp.NOT_EQUAL;

        Scan scan = new Scan();
        /**默认多个过滤器是 与and 的*/
        List<Filter> filters = new ArrayList<>();
        /**两步过滤，1.根据前缀过滤特定用户的优惠券 2.根据状态过滤相应的优惠券*/
        // 1. 行键前缀过滤器, 找到特定用户的优惠券
        filters.add(new PrefixFilter(rowPrefix));
        // 2. 基于列单元值的过滤器, 找到未使用或者使用的的优惠券
        if (status != PassStatus.ALL) {
            filters.add(
                    new SingleColumnValueFilter(
                            Constants.PassTable.FAMILY_I.getBytes(),
                            Constants.PassTable.CON_DATE.getBytes(), compareOp,
                            Bytes.toBytes("-1")) //这是一个过滤器，过滤CON_DATE这一列，配合CompareOp获取UNUSED或者USED列单元
            );
        }
        scan.setFilter(new FilterList(filters));

        /**使用两个过滤器， 通过Hbase获取用户领取的特定状态的优惠券Pass对象*/
        List<Pass> passes =hbaseTemplate.find(Constants.PassTable.TABLE_NAME,scan,new PassRowMapper());
        /**利用优惠券pass对象 通过HBase请求获取优惠券模版信息，构建 templateId (rowKey) 到template模版的映射 */
        Map<String,PassTemplate> passTemplateMap =buildPassTemplateMap(passes);
        /**再通过优惠券模版信息（商户Id），通过mysql请求获取商户信息，构建商户Id到商户的映射*/
        Map<Integer,Merchants> merchantsMap =buildMerchantsMap(
                new ArrayList<>(passTemplateMap.values())
        );
        List<PassInfo> result = new ArrayList<>();

        for(Pass pass:passes){
            PassTemplate passTemplate =passTemplateMap.getOrDefault(pass.getTemplateId(),null);
            if(null==passTemplate){
                log.error("PassTemplate Null:{}",pass.getTemplateId());
                continue;
            }
            Merchants merchants =merchantsMap.getOrDefault(passTemplate.getId(),null);
            if(null==merchants){
                log.error("Merchants Null:{}",passTemplate.getId());
                continue;
            }
            result.add(new PassInfo(pass,passTemplate,merchants));
        }
        return new Response(result);

    }

    /**获取用户未使用优惠券的功能实现  这三个服务的实现，体现了封装的思想，抽象出它们的共同点*/
    @Override
    public Response getUserPassInfo(Long userId) throws Exception {
        return getPassInfoByStatus(userId, PassStatus.UNUSED);
    }

    @Override
    public Response getUserUsedPassInfo(Long userId) throws Exception {
        return getPassInfoByStatus(userId,PassStatus.USED);
    }

    @Override
    public Response getUserAllPassInfo(Long userId) throws Exception {
        return getPassInfoByStatus(userId,PassStatus.ALL);
    }

    @Override
    public Response userUsePass(Pass pass) {
        /**因为传进来的pass对象，是用户给的，肯定不知道该pass的rowKey，我们这个服务目的也就是为了获得该pass的rowKey*/
        /**根据 userId 构造行键前缀 ，因为我们使用了翻转的userId构建 pass在Hbase中的rowKey*/
        byte[] rowPrefix = Bytes.toBytes(new StringBuilder(
                String.valueOf(pass.getUserId())).reverse().toString());
        Scan scan = new Scan();
        List<Filter> filters = new ArrayList<>();
        /**行键前缀过滤器*/
        filters.add(new PrefixFilter(rowPrefix));
        /**优惠券Id过滤器*/
        filters.add(new SingleColumnValueFilter(
                Constants.PassTable.FAMILY_I.getBytes(),
                Constants.PassTable.TEMPLATE_ID.getBytes(),
                CompareFilter.CompareOp.EQUAL,
                Bytes.toBytes(pass.getTemplateId())
        ));
        /**消费日期为-1的过滤器*/
        filters.add(new SingleColumnValueFilter(
                Constants.PassTable.FAMILY_I.getBytes(),
                Constants.PassTable.CON_DATE.getBytes(),
                CompareFilter.CompareOp.EQUAL,
                Bytes.toBytes("-1")
        ));

        scan.setFilter(new FilterList(filters));

        List<Pass> passes = hbaseTemplate.find(Constants.PassTable.TABLE_NAME,
                scan, new PassRowMapper());
        /**用户传进来的pass为空或者有多个，则报错， 正确的是只查询到一个*/
        if (null == passes || passes.size() != 1) {
            log.error("UserUsePass Error: {}", JSON.toJSONString(pass));
            return Response.failure("UserUsePass Error");
        }

        byte[] FAMILY_I = Constants.PassTable.FAMILY_I.getBytes();
        byte[] CON_DATE = Constants.PassTable.CON_DATE.getBytes();
        /**Mutation 为put和delete对象的子类*/
        List<Mutation> datas = new ArrayList<>();
        /**put对象填充行键即rowKey*/
        Put put = new Put(passes.get(0).getRowKey().getBytes());
        /**put再填充CON_DATE这一列*/
        put.addColumn(FAMILY_I, CON_DATE,
                Bytes.toBytes(DateFormatUtils.ISO_DATE_FORMAT.format(new Date()))
        );
        datas.add(put);

        hbaseTemplate.saveOrUpdates(Constants.PassTable.TABLE_NAME, datas);

        return Response.success();
    }
}
