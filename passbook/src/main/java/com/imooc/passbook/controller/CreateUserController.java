package com.imooc.passbook.controller;

import com.imooc.passbook.log.LogConstants;
import com.imooc.passbook.log.LogGenerator;
import com.imooc.passbook.service.IUserPassService;
import com.imooc.passbook.service.IUserService;
import com.imooc.passbook.vo.Response;
import com.imooc.passbook.vo.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;

/**
 * 创建用户服务*/
@Slf4j
@RestController
@RequestMapping("/passbaook")
public class CreateUserController {
    private final IUserService userService;
    private HttpServletRequest httpServletRequest;
    @Autowired
    public CreateUserController(IUserService userService,
                                HttpServletRequest httpServletRequest) {
        this.userService = userService;
        this.httpServletRequest = httpServletRequest;
    }
    /** <h2> 创建用户
     * </h2>*/
    @ResponseBody
    @PostMapping("/createuser")
    Response createUser(@RequestBody User user) throws Exception{
        LogGenerator.genLog(
                httpServletRequest,
                -1L,   /**暂时没有用户id*/
                LogConstants.ActionName.CREATE_USER,
                user
        );
        return userService.createUser(user);
    }
}
