package com.imooc.passbook.service.impl;

import com.imooc.passbook.constant.Constants;
import com.imooc.passbook.service.IUserService;
import com.imooc.passbook.vo.Response;
import com.imooc.passbook.vo.User;
import com.spring4all.spring.boot.starter.hbase.api.HbaseTemplate;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.RandomStringUtils;
import org.apache.hadoop.hbase.client.Mutation;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.util.Bytes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class UserServiceImpl implements IUserService {

    /**
     * 实现这个功能，需要哪些bean呢？获得HBase的客户端，连接HBase服务器，  redis客户端， UserId的生成需要redis递增的Key*/
    /**Hbase客户端*/
    private final HbaseTemplate hbaseTemplate;
    /**redis客户端*/
    private final StringRedisTemplate redisTemplate;
    @Autowired
    public UserServiceImpl(HbaseTemplate hbaseTemplate, StringRedisTemplate redisTemplate) {
        this.hbaseTemplate = hbaseTemplate;
        this.redisTemplate = redisTemplate;
    }
    /**创建过程: 定义两个客户端，redis获取用户数，然后创建用户Id，之后创建put，填充对应的列族，
     * 然后使用Hbase客户端发送请求到Hbase服务器，最后返回一个带user的response*/
    @Override
    public Response createUser(User user) throws Exception {
        byte[] FAMILY_B = Constants.UserTable.FAMILY_B.getBytes();
        byte[] NAME = Constants.UserTable.NAME.getBytes();
        byte[] AGE = Constants.UserTable.AGE.getBytes();
        byte[] SEX = Constants.UserTable.SEX.getBytes();

        byte[] FAMILY_O = Constants.UserTable.FAMILY_O.getBytes();
        byte[] PHONE = Constants.UserTable.PHONE.getBytes();
        byte[] ADDRESS = Constants.UserTable.ADDRESS.getBytes();
        System.out.println("取redis数据前" );
        /**获取redis的用户数+1*/
        Long curCount = redisTemplate.opsForValue().increment(Constants.USE_COUNT_REDIS_KEY, 1);
        Long userId = genUserId(curCount);
        System.out.println("取redis数据后" );
        /**put和delete的子类，可以兼容两者 */
        List<Mutation> datas = new ArrayList<Mutation>();
        Put put = new Put(Bytes.toBytes(userId));

        put.addColumn(FAMILY_B, NAME, Bytes.toBytes(user.getBaseInfo().getName()));
        put.addColumn(FAMILY_B, AGE, Bytes.toBytes(user.getBaseInfo().getAge()));
        put.addColumn(FAMILY_B, SEX, Bytes.toBytes(user.getBaseInfo().getSex()));

        put.addColumn(FAMILY_O, PHONE, Bytes.toBytes(user.getOtherInfo().getPhone()));
        put.addColumn(FAMILY_O, ADDRESS, Bytes.toBytes(user.getOtherInfo().getAddress()));

        datas.add(put);
        System.out.println("访问hbase前" );
        hbaseTemplate.saveOrUpdates(Constants.UserTable.TABLE_NAME, datas);
        System.out.println("访问hbase后" );
        user.setId(userId);

        return new Response(user);
    }
    /**
     * <h2>生成 userId</h2>
     * @param prefix 当前用户数
     * @return 用户 id
     * */
    private Long genUserId(Long prefix) {

        String suffix = RandomStringUtils.randomNumeric(5);
        return Long.valueOf(prefix + suffix);
    }
}
