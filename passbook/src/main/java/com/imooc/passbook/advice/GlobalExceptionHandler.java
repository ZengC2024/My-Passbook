package com.imooc.passbook.advice;

import com.imooc.passbook.vo.ErrorInfo;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;

/**
 * <h1> 全局异常处理</h1>
 * 在客户端或者前端调用一个controller的时候抛出一个异常，就会到这个ControllerAdvice中捕获
 * 捕获后就会对应到ExceptionHandler中进行处理，然后返回一个ResponseBody的JSON串给前端*/
@ControllerAdvice  //扫描到容器中，做异常处理
public class GlobalExceptionHandler {
    @ResponseBody
    @ExceptionHandler(value = Exception.class)
    public ErrorInfo<String> errorHandler(HttpServletRequest request, Exception ex) throws Exception {

        ErrorInfo<String> info = new ErrorInfo<String>();

        info.setCode(ErrorInfo.ERROR);
        info.setMessage(ex.getMessage());
        info.setData("Do Not Have Return Data");
        info.setUrl(request.getRequestURL().toString());

        return info;
    }
}
