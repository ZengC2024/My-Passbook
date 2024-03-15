package com.imooc.passbook.service.impl;

import com.alibaba.fastjson.JSON;
import com.imooc.passbook.constant.Constants;
import com.imooc.passbook.mapper.PassTemplateRowMapper;
import com.imooc.passbook.service.IGainPassTemplateService;
import com.imooc.passbook.utils.RowKeyGenUtil;
import com.imooc.passbook.vo.GainPassTemplateRequest;
import com.imooc.passbook.vo.PassTemplate;
import com.imooc.passbook.vo.Response;
import com.spring4all.spring.boot.starter.hbase.api.HbaseTemplate;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.time.DateFormatUtils;
import org.apache.hadoop.hbase.client.Mutation;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.util.Bytes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * <h1> 用户领取优惠券功能实现</h1>
 * 过程：
 * 1. gainPassTemplate(GainPassTemplateRequest request)函数接收到 用户发起的领取优惠券的请求request
 * 2. 首先判断一下这张优惠券是否可以被领取: 查询HBase中该优惠券的信息，包括limit和time，
 * 3. 判断成功，则更新HBase中该优惠券（limit-1）
 * 4. 然后调用addPassForUser(request,passTemplate.getId(),passTemplateId)，
 * passTemplateId为该记录在HBase存储的rowKey,将该优惠卷保存到用户领取优惠券表
 *    4.1 准备写入一条记录到用户领取优惠券表，在HBase中，包括生成该记录在Hbase中的rowKey，
 *    4.2 构造USER_ID,TEMPLATE_ID,TOKEN,ASSIGN_DATE,CON_DATE等
 *    4.3 其中，token需要判断，判断优惠券是否有token在redis中,
 *    调用redisTemplate.opsForSet().pop(passTemplateId)，其中passTemplateId为token在redis存储的key,
 *      如果有表明在redis中有该优惠券的token，将存储的redis token文件的第一条记录弹出，
 *          然后我们需要将这条已使用的token记录到本地文件中
 *          4.3.1 调用recordTokenToFile(Integer merchantsId,String passTemplateId,
 *                                   String token)即可写本地文件，是带后缀_的文件
 *      如果没有表明还没放到redis中，直接更新HBase中该用户的领取优惠券表即可*/
@Service
@Slf4j
public class GainPassTemplateServiceImpl implements IGainPassTemplateService {

    private HbaseTemplate hbaseTemplate;

    private StringRedisTemplate redisTemplate;
    @Autowired
    public GainPassTemplateServiceImpl(HbaseTemplate hbaseTemplate, StringRedisTemplate redisTemplate) {
        this.hbaseTemplate = hbaseTemplate;
        this.redisTemplate = redisTemplate;
    }

    @Override
    public Response gainPassTemplate(GainPassTemplateRequest request) throws Exception {
        /**request对象中包括优惠券的一些信息，
         * 首先判断一下这张优惠券是否可以被领取，判断成功之后给用户添加优惠券*/
        PassTemplate passTemplate;

        /**获取优惠券的rowKey,之前生成按照工具类的方式生成的rowKey(商户id+优惠券title），为PassTemplate在HBase中的存储位置*/
        String passTemplateId = RowKeyGenUtil.genPassTemplateRowKey(request.getPassTemplate());
        /**查询HBase优惠券*/
        try{
            passTemplate =hbaseTemplate.get(
                    Constants.PassTemplateTable.TABLE_NAME,passTemplateId,
                    new PassTemplateRowMapper()
            );
        }catch (Exception ex){
            log.error("Gain PassTemplate Error:{}", JSON.toJSONString(request.getPassTemplate()));
            return Response.failure("Gain PassTemplate Error!");
        }
        /**不能被领取*/
        if(passTemplate.getLimit()<=1&&passTemplate.getLimit()!=-1){
            log.error("PassTemplate Limit Max:{}",JSON.toJSONString(request.getPassTemplate()));
            return Response.failure("PassTemplate Limit Max!");
        }
        /**日期是否在可领取的范围*/
        Date cur =new Date();

        if(!(cur.getTime()>=passTemplate.getStart().getTime() &&
                cur.getTime()<passTemplate.getEnd().getTime())){
            log.error("PassTemplate ValidTime Error:{}",JSON.toJSONString(request.getPassTemplate()));
            return Response.failure("PassTemplate ValidTime Error!");
        }

        /**减去优惠券*/
        if(passTemplate.getLimit()!=-1){
            /**修改Hbase中的优惠券*/
            List<Mutation> datas =new ArrayList<>();
            byte[] FAMILY_C = Constants.PassTemplateTable.FAMILY_C.getBytes();
            byte[] LIMIT = Constants.PassTemplateTable.LIMIT.getBytes();

            Put put = new Put(Bytes.toBytes(passTemplateId));
            put.addColumn(FAMILY_C, LIMIT,
                    Bytes.toBytes(passTemplate.getLimit() - 1));
            datas.add(put);

            hbaseTemplate.saveOrUpdates(Constants.PassTemplateTable.TABLE_NAME,
                    datas);

        }
        /**将优惠卷保存到用户优惠券表*/
        if(!addPassForUser(request,passTemplate.getId(),passTemplateId)){
            return Response.failure("GainPassTemplate Failure!");
        }
        return Response.success();

    }

    /**
     * <h2>给用户添加优惠券</h2>
     * @param request {@link GainPassTemplateRequest}
     * @param merchantsId 商户 id
     * @param passTemplateId 优惠券 id (rowKey)
     * @return true/false
     *
     * */
    public boolean addPassForUser(GainPassTemplateRequest request,
                                  Integer merchantsId,String passTemplateId) throws Exception{
        byte[] FAMILY_I = Constants.PassTable.FAMILY_I.getBytes();
        byte[] USER_ID = Constants.PassTable.USER_ID.getBytes();
        byte[] TEMPLATE_ID = Constants.PassTable.TEMPLATE_ID.getBytes();
        byte[] TOKEN = Constants.PassTable.TOKEN.getBytes();
        byte[] ASSIGNED_DATE = Constants.PassTable.ASSIGNED_DATE.getBytes();
        byte[] CON_DATE = Constants.PassTable.CON_DATE.getBytes();

        List<Mutation> datas =new ArrayList<>();
        /**生成这条领取记录在hbase中的rowKey*/
        Put put = new Put(Bytes.toBytes(RowKeyGenUtil.genPassRowKey(request)));

        put.addColumn(FAMILY_I, USER_ID, Bytes.toBytes(request.getUserId()));
        put.addColumn(FAMILY_I, TEMPLATE_ID, Bytes.toBytes(passTemplateId));

        /**判断优惠券是否有token在redis中  passTemplateId为token在redis存储的key
         * 如果有表明在redis中有该优惠券的token，我们需要将这条已使用的token记录到本地文件中
         * 没有表明还没放到redis中，直接更新该用户的优惠券表即可*/
        if(request.getPassTemplate().getHasToken()){
            /**redis中只存储token的<key,token> */
            String token =redisTemplate.opsForSet().pop(passTemplateId);
            if(null==token){
                log.error("Token not exist:{}",passTemplateId);
                return false;
            }
            /**如果有则将已使用的token记录到本地文件中，带后缀_的文件*/
            recordTokenToFile(merchantsId,passTemplateId,token);
            put.addColumn(FAMILY_I, TOKEN, Bytes.toBytes(token));
        }else{
            put.addColumn(FAMILY_I, TOKEN, Bytes.toBytes("-1"));
        }

        put.addColumn(FAMILY_I, ASSIGNED_DATE,
                Bytes.toBytes(DateFormatUtils.ISO_DATE_FORMAT.format(new Date())));
        put.addColumn(FAMILY_I, CON_DATE, Bytes.toBytes("-1"));

        datas.add(put);

        hbaseTemplate.saveOrUpdates(Constants.PassTable.TABLE_NAME, datas);

        return true;
    }
    /**
     * <h2>将已使用的 token 记录到文件中</h2>
     * @param merchantsId 商户 id
     * @param passTemplateId 优惠券 id
     * @param token 分配的优惠券 token
     * */
    public void recordTokenToFile(Integer merchantsId,String passTemplateId,
                                  String token) throws Exception{
        Files.write(Paths.get(Constants.TOKEN_DIR,String.valueOf(merchantsId),passTemplateId
        +Constants.USED_TOKEN_SUFFIX),(token+"\n").getBytes(), StandardOpenOption.CREATE,StandardOpenOption.APPEND);
    }
}
