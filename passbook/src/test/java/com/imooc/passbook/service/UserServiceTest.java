package com.imooc.passbook.service;

import com.alibaba.fastjson.JSON;
import com.imooc.passbook.vo.User;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Get;
import org.apache.hadoop.hbase.util.Bytes;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import com.spring4all.spring.boot.starter.hbase.api.HbaseTemplate;

/**用户服务测试*/
@RunWith(SpringRunner.class)
@SpringBootTest
public class UserServiceTest {


    @Autowired
    private IUserService userService;

    @Autowired
    private HbaseTemplate hbaseTemplate;

    @Test
    public void testCreateUser() throws Exception{
        User user =new User();
        user.setBaseInfo(
                new User.BaseInfo("imooc", 10, "m")
        );
        user.setOtherInfo(
                new User.OtherInfo("123456", "北京市朝阳区")
        );
        System.out.println("NMDNMDNMDND");
        System.out.println(JSON.toJSONString(userService.createUser(user)));
    }
}
