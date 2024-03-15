package com.imooc.passbook;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * <h1>测试程序入口</h1>
 * 通过SpringBootApplication启动容器，然后注入相应的bean，然后实现相应的服务
 * */
@SpringBootApplication
public class PassbookApplication {

    public static void main(String[] args) {
        SpringApplication.run(PassbookApplication.class, args);
    }

}
