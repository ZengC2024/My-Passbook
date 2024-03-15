package com.imooc.passbook.service;

import com.imooc.passbook.vo.Pass;
import com.imooc.passbook.vo.Response;

/**
 * <h1>获取用户个人优惠券信息</h1>
 * 最核心的功能*/
public interface IUserPassService {
    /**
     * <h2>获取用户个人未使用的优惠券信息，我的优惠券功能</h2>*/
    Response getUserPassInfo(Long userId) throws Exception;
    /**
     * <h2>获取用户已经消费的优惠券，</h2>*/
    Response getUserUsedPassInfo(Long userId) throws Exception;
    /**
     * <h2>获取用户所有的优惠券，</h2>*/
    Response getUserAllPassInfo(Long userId) throws Exception;

    /**
     * <h2>用户使用优惠券，</h2>*/
    Response userUsePass(Pass pass);
}
