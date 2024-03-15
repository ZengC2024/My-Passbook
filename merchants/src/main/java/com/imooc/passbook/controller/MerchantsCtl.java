package com.imooc.passbook.controller;

import com.alibaba.fastjson.JSON;
import com.imooc.passbook.service.IMerchantsServ;
import com.imooc.passbook.vo.CreateMerchantsRequest;
import com.imooc.passbook.vo.PassTemplate;
import com.imooc.passbook.vo.Response;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * <h1>商户服务Controller</h1>
 * 实现Http方法，能够通过http调用我们的后端服务接口
 * */
@Slf4j  //打印日志用的
@RestController //表示一个java Bean 且是rest风格的api
@RequestMapping("/merchants") //URI的前缀
public class MerchantsCtl {
    /**商户服务接口*/
    private IMerchantsServ merchantsServ;
    @Autowired
    public MerchantsCtl(IMerchantsServ merchantsServ) {
        this.merchantsServ = merchantsServ;
    }

    /**ImerchantsServ里面有多少服务，controller里面就应该有多少服务，否则service定义没有意义*/
    @ResponseBody  //返回一个json对象
    @PostMapping("/create")
    public Response createMerchants(@RequestBody CreateMerchantsRequest request){
        log.info("CreateMerchants:{}", JSON.toJSON(request));
        return merchantsServ.createMerchants(request);
    }
    @ResponseBody
    @GetMapping("/{id}")  //将形参作为URL的一部分，动态的路径变量
    public Response buildMerchantsInfo(@PathVariable Integer id){
        System.out.println("ZZZZYYYYY");
        log.info("BuildMerchantsInfo:{}",id);
        return merchantsServ.buildMerchantsInfoById(id);
    }

    @ResponseBody
    @PostMapping("/drop")
    public Response dropPassTemplate(@RequestBody PassTemplate passTemplate) {

        log.info("DropPassTemplate: {}", passTemplate);
        return merchantsServ.dropPassTemplate(passTemplate);
    }
}
