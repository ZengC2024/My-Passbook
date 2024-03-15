package com.imooc.passbook.service;


import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.context.junit4.SpringRunner;

/**<h1>redis 客户端测试</h1>
 * */
@SpringBootTest    /**这两个为测试类的声明*/
@RunWith(SpringRunner.class)
public class RedisTemplateTest {
    @Autowired
    private StringRedisTemplate redisTemplate;

    @Test
    public void testRedisTemplate(){
        //redis flushall
        redisTemplate.execute((RedisCallback<? extends Object>) connection->{
            connection.flushAll();
            return null;
        });
        /**断言*/
        assert redisTemplate.opsForValue().get("name")==null;
        redisTemplate.opsForValue().set("name","imooc");

        /**再次断言*/
        assert redisTemplate.opsForValue().get("name")!=null;
        /**结果正常*/
        System.out.println(redisTemplate.opsForValue().get("name"));
    }
}
