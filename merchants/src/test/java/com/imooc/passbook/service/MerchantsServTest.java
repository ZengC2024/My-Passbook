package com.imooc.passbook.service;

import com.alibaba.fastjson.JSON;
import com.imooc.passbook.vo.PassTemplate;
import org.apache.commons.lang.time.DateUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import com.imooc.passbook.vo.CreateMerchantsRequest;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;


/**   排查debug：查看报错所有日志的Caused by，原因往往在最后一个Caused by。*/
/**
 * <h1> 商户服务测试类</h1>
 */
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
public class MerchantsServTest {
    @Autowired  //自动注入进来
    private IMerchantsServ merchantsServ;
    @Test
    @Transactional // 仅仅测试的话数据库会回滚，数据库不会保存提交的数据
    public void testCreateMerchantsServ(){

        CreateMerchantsRequest request =new CreateMerchantsRequest();
        request.setName("zy");
        request.setLogoUrl("www.zy.com");
        request.setBusinessLicenseUrl("www.zy.com");
        request.setPhone("15651785275");
        request.setAddress("上海");

        System.out.println(JSON.toJSONString(merchantsServ.createMerchants(request)));

    }
    /**
     {"data":{"address":"上海",
     "businessLicenseUrl":"www.zy.com","id":19,
     "isAudit":false,"logoUrl":"www.zy.com","name":"ZY","phone":"15651785275"},
     "errorCode":0,"errorMsg":""}
     * */
    @Test
    public void testBuildMerchantsInfoById(){
        System.out.println(JSON.toJSONString(merchantsServ.buildMerchantsInfoById(19)));
    }
    @Test
    public void testDropPassTemplate(){
        PassTemplate passTemplate = new PassTemplate();
        passTemplate.setId(17);
        passTemplate.setTitle("慕课-1");
        passTemplate.setSummary("简介: 慕课");
        passTemplate.setDesc("详情: 慕课");
        passTemplate.setLimit(10000L);
        passTemplate.setHasToken(false);
        passTemplate.setBackground(2);
        passTemplate.setStart(DateUtils.addDays(new Date(), -10));
        passTemplate.setEnd(DateUtils.addDays(new Date(), 10));
        System.out.println(JSON.toJSONString(
                merchantsServ.dropPassTemplate(passTemplate)
        ));
    }

}
