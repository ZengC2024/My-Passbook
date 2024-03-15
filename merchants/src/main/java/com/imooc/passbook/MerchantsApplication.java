package com.imooc.passbook;

import com.imooc.passbook.security.AuthCheckInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import javax.annotation.Resource;

@SpringBootApplication
public class MerchantsApplication extends WebMvcConfigurerAdapter {


    /**将拦截器注入进来*/
    @Resource   //用resource注入 javabean
    private AuthCheckInterceptor authCheckInterceptor;
    public static void main(String[] args) {
        SpringApplication.run(MerchantsApplication.class, args);
    }


    @Override
    public void addInterceptors(InterceptorRegistry registry){
        /**将拦截器注册进来*/
        registry.addInterceptor(authCheckInterceptor).addPathPatterns("/merchants/**");
        /**在原来的方法重写进去*/
        super.addInterceptors(registry);
    }

}
