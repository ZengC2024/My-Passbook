package com.imooc.passbook.service;

import org.junit.Before;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

/**服务测试抽象基类*/
@RunWith(SpringRunner.class)
@SpringBootTest
public class AbstractServiceTest {
    Long userId;

    @Before
    public void init() {

        userId = 149276L;
    }
}
