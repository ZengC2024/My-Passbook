package com.imooc.passbook.security;


import com.imooc.passbook.constant.Constants;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Component;

import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * <h1>权限拦截器</h1>
 * Created by zc
 */
@Component //通用组件注解
public class AuthCheckInterceptor implements HandlerInterceptor {


    @Override
    public boolean preHandle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o) throws Exception {
        //拦截的基本方法  获得常量中定义的TOKEN_STRING
        String token =httpServletRequest.getHeader(Constants.TOKEN_STRING);
        if(StringUtils.isEmpty(token)){
            throw new Exception("Header 中缺少 "+ Constants.TOKEN_STRING+"!");
        }
        if(!token.equals(Constants.TOKEN)){
            throw new Exception("Header 中 "+Constants.TOKEN+"错误");
        }
        //在本项目中，所有的商户会用到同一个token，但在企业级开发中，我们会在数据库中保存每个商户的token映射
        System.out.println("ZYZYZYZYZC");
        AccessContext.setToken(token);
        /**因为这个return false，搞得我的postman请求发送成功，但是接收不到消息*/
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o, ModelAndView modelAndView) throws Exception {

    }

    @Override
    public void afterCompletion(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o, Exception e) throws Exception {
        //http 连接完成，即使是抛出异常也会执行
        AccessContext.clearAccessKey();
    }
}
