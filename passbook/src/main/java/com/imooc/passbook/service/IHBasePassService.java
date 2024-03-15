package com.imooc.passbook.service;

import com.imooc.passbook.vo.PassTemplate;

/**
 * <h1> Pass HBase 服务</h1>
 * 商户通过Kafka投放优惠券到我们的平台上，我们取出来放到HBase表里面写入记录*/
public interface IHBasePassService {



    /**
     * <h2>将 PassTemplate 写入 HBase</h2>
     * @param passTemplate {@link PassTemplate}
     * @return true/false
     * */
    boolean dropPassTemplateToHBase(PassTemplate passTemplate);
}
