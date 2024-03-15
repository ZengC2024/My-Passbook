package com.imooc.passbook.service.impl;

import com.imooc.passbook.constant.Constants;
import com.imooc.passbook.dao.MerchantsDao;
import com.imooc.passbook.entity.Merchants;
import com.imooc.passbook.mapper.PassTemplateRowMapper;
import com.imooc.passbook.service.IInventoryService;
import com.imooc.passbook.service.IUserPassService;
import com.imooc.passbook.utils.RowKeyGenUtil;
import com.imooc.passbook.vo.InventoryResponse;
import com.imooc.passbook.vo.PassInfo;
import com.imooc.passbook.vo.PassTemplate;
import com.imooc.passbook.vo.PassTemplateInfo;
import com.imooc.passbook.vo.Response;
import com.spring4all.spring.boot.starter.hbase.api.HbaseTemplate;
import lombok.extern.slf4j.Slf4j;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.filter.CompareFilter;
import org.apache.hadoop.hbase.filter.FilterList;
import org.apache.hadoop.hbase.filter.LongComparator;
import org.apache.hadoop.hbase.filter.SingleColumnValueFilter;
import org.apache.hadoop.hbase.util.Bytes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * <h1>获取库存信息, 只返回用户没有领取的</h1>
 * Created by zc
 */
@Slf4j
@Service
public class InventoryServiceImpl implements IInventoryService {

    /** Hbase 客户端  用于获取可用的优惠券信息*/
    private final HbaseTemplate hbaseTemplate;

    /** Merchants Dao 接口  用于获取对应的商户信息*/
    private final MerchantsDao merchantsDao;
    /**由于我们需要去掉用户已经领取的优惠券，可以调用UserPassService服务获取用户已经领取的所有优惠券*/
    private final IUserPassService userPassService;

    @Autowired
    public InventoryServiceImpl(HbaseTemplate hbaseTemplate,
                                MerchantsDao merchantsDao,
                                IUserPassService userPassService) {
        this.hbaseTemplate = hbaseTemplate;
        this.merchantsDao = merchantsDao;
        this.userPassService = userPassService;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Response getInventoryInfo(Long userId) throws Exception {
        /**调用接口*/
        Response allUserPass =userPassService.getUserAllPassInfo(userId);
        /**强制转换，由于Response返回的是Object类型（pass,passTemplate,merchants）*/
        List<PassInfo> passInfos = (List<PassInfo>)allUserPass.getData();
        /**从PassInfo中获取passTemplate这一对象属性*/
        List<PassTemplate> excludeObject =passInfos.stream().map(PassInfo::getPassTemplate)
                .collect(Collectors.toList());
        /**获取其中的TemplateId */
        List<String> excludeIds =new ArrayList<>();
        /**我们这里需要获取已经领取的优惠券的rowKey（rowKey生成工具类 RowKeyGenUtil)*/
        excludeObject.forEach(e ->excludeIds.add(RowKeyGenUtil.genPassTemplateRowKey(e)));

        /**返回包括userId和这个用户可以看到的库存信息*/
        return new Response(new InventoryResponse(userId,buildPassTemplateInfo
                (getAvailablePassTemplate(excludeIds))));
    }

    /**
     * <h2>获取系统中可用的优惠券</h2>
     * @param excludeIds 需要排除的优惠券 ids
     * @return {@link PassTemplate}
     * */
    private List<PassTemplate> getAvailablePassTemplate(List<String> excludeIds) {

        FilterList filterList = new FilterList(FilterList.Operator.MUST_PASS_ONE);
        /**过滤器，满足 LIMIT>0*/
        filterList.addFilter(
                new SingleColumnValueFilter(
                        Bytes.toBytes(Constants.PassTemplateTable.FAMILY_C),
                        Bytes.toBytes(Constants.PassTemplateTable.LIMIT),
                        CompareFilter.CompareOp.GREATER,
                        new LongComparator(0L)
                )
        );
        /**或者满足LIMIT=-1*/
        filterList.addFilter(
                new SingleColumnValueFilter(
                        Bytes.toBytes(Constants.PassTemplateTable.FAMILY_C),
                        Bytes.toBytes(Constants.PassTemplateTable.LIMIT),
                        CompareFilter.CompareOp.EQUAL,
                        Bytes.toBytes("-1")
                )
        );

        Scan scan = new Scan();
        scan.setFilter(filterList);

        List<PassTemplate> validTemplates = hbaseTemplate.find(
                Constants.PassTemplateTable.TABLE_NAME, scan, new PassTemplateRowMapper());
        List<PassTemplate> availablePassTemplates = new ArrayList<>();

        Date cur = new Date();

        for (PassTemplate validTemplate : validTemplates) {

            if (excludeIds.contains(RowKeyGenUtil.genPassTemplateRowKey(validTemplate))) {
                continue;
            }

            if (cur.getTime() >= validTemplate.getStart().getTime()
                    && cur.getTime() <= validTemplate.getEnd().getTime()) {
                availablePassTemplates.add(validTemplate);
            }
        }

        return availablePassTemplates;
    }

    /**
     * <h2>构造优惠券的信息  包括优惠券模版信息和商户信息</h2>
     * @param passTemplates {@link PassTemplate}
     * @return {@link PassTemplateInfo}
     * */
    private
    List<PassTemplateInfo> buildPassTemplateInfo(List<PassTemplate> passTemplates) {

        Map<Integer, Merchants> merchantsMap = new HashMap<>();
        /**从passTemplate中提取出id这一属性*/
        List<Integer> merchantsIds = passTemplates.stream().map(
                PassTemplate::getId
        ).collect(Collectors.toList());
        /**根据ids 查询mysql获取商户对象*/
        List<Merchants> merchants = merchantsDao.findByIdIn(merchantsIds);
        /**java8语法*/
        merchants.forEach(m -> merchantsMap.put(m.getId(), m));

        List<PassTemplateInfo> result = new ArrayList<>(passTemplates.size());

        for (PassTemplate passTemplate : passTemplates) {

            Merchants mc = merchantsMap.getOrDefault(passTemplate.getId(),
                    null);
            if (null == mc) {
                log.error("Merchants Error: {}", passTemplate.getId());
                continue;
            }

            result.add(new PassTemplateInfo(passTemplate, mc));
        }

        return result;
    }
}
