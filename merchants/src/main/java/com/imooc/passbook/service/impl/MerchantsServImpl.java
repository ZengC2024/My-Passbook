package com.imooc.passbook.service.impl;

/**
 * <h1> 商户服务接口实现</h1>
 */

import com.alibaba.fastjson.JSON;
import com.imooc.passbook.constant.Constants;
import com.imooc.passbook.constant.ErrorCode;
import com.imooc.passbook.dao.MerchantsDao;
import com.imooc.passbook.entity.Merchants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.imooc.passbook.service.IMerchantsServ;
import com.imooc.passbook.vo.CreateMerchantsRequest;
import com.imooc.passbook.vo.CreateMerchantsResponse;
import com.imooc.passbook.vo.PassTemplate;
import com.imooc.passbook.vo.Response;

@Slf4j
@Service
public class MerchantsServImpl implements IMerchantsServ {
    /**注入一个bean，我们创建一个商户，我们需要数据库的dao接口
    Merchants 数据库接口
     */
    private final MerchantsDao merchantsDao;

    /**  Kafka 客户端*/
    /**md,下载这个kafka又用了一天，cnmd，因为在启动kafka自带的zookeeper的时候，之前下了一个单独的zookeeper，
     * 占用了端口2181，搞得自带的zookeeper无法启动，使用lsof查看端口情况，再使用sudo kill *杀死占用线程，cnmd*/
    private final KafkaTemplate<String,String> kafkaTemplate;
    @Autowired  //自动注入，以构造函数的方式注入
    public MerchantsServImpl(MerchantsDao merchantsDao, KafkaTemplate<String, String> kafkaTemplate) {
        this.merchantsDao = merchantsDao;
        this.kafkaTemplate = kafkaTemplate;
    }


    @Override
    @Transactional //保存成功才提交，要不然回滚
    public Response createMerchants(CreateMerchantsRequest request) {
        Response response =new Response();
        CreateMerchantsResponse merchantsResponse =new CreateMerchantsResponse();

        /**先校验*/
        ErrorCode errorCode =request.validate(merchantsDao);
        if(errorCode!=ErrorCode.SUCCESS){
            merchantsResponse.setId(-1);
            response.setErrorCode(errorCode.getCode());
            response.setErrorMsg(errorCode.getDesc());
        }else{
            merchantsResponse.setId(merchantsDao.save(request.toMerchants()).getId());
        }
        response.setData(merchantsResponse);


        return response;
    }

    @Override
    public Response buildMerchantsInfoById(Integer id) {
        System.out.println("ZYZC");
        Response response =new Response();
        Merchants merchants =merchantsDao.findById(id);
        if(merchants ==null){
            response.setErrorCode(ErrorCode.MERCHANTS_NOT_EXIST.getCode());
            response.setErrorMsg(ErrorCode.MERCHANTS_NOT_EXIST.getDesc());
        }
        response.setData(merchants);
        return response;
    }
    /**
     * 商户投放优惠卷  商户发送消息到kafka，用户消费掉这样的一条消息，
     * 然后入库这样一条信息到HBase中
     * */
    @Override
    public Response dropPassTemplate(PassTemplate template) {
        Response response =new Response();
        ErrorCode errorCode =template.validate(merchantsDao);
        if(errorCode!=ErrorCode.SUCCESS){
            response.setErrorCode(errorCode.getCode());
            response.setErrorMsg(errorCode.getDesc());
        }else{
            String passTemplate = JSON.toJSONString(template);
            kafkaTemplate.send(
                    /**这个topic就是kafka客户端的topic*/
                    Constants.TEMPLATE_TOPIC,
                    Constants.TEMPLATE_TOPIC,
                    passTemplate
            );
            log.info("DropPassTemplate:{}",passTemplate);
        }

        return response;
    }
}
