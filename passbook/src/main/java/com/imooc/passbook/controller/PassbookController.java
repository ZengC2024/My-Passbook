package com.imooc.passbook.controller;

import com.imooc.passbook.log.LogConstants;
import com.imooc.passbook.log.LogGenerator;
import com.imooc.passbook.service.IFeedbackService;
import com.imooc.passbook.service.IGainPassTemplateService;
import com.imooc.passbook.service.IInventoryService;
import com.imooc.passbook.service.IUserPassService;
import com.imooc.passbook.vo.Feedback;
import com.imooc.passbook.vo.GainPassTemplateRequest;
import com.imooc.passbook.vo.Pass;
import com.imooc.passbook.vo.Response;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.lang.annotation.Repeatable;

/**
 * <h1> PassBook RestController</h1>
 * 为passbook一个总的controller,将之前实现的所有的子服务放在一起，一起注入*/
@Slf4j
@RestController
@RequestMapping("/passbook")
public class PassbookController {
    /** 用户优惠券服务 */
    private final IUserPassService userPassService;

    /** 优惠券库存服务 */
    private final IInventoryService inventoryService;

    /** 领取优惠券服务 */
    private final IGainPassTemplateService gainPassTemplateService;

    /** 反馈服务 */
    private final IFeedbackService feedbackService;

    /** HttpServletRequest
     * 为HTTP ServiceRequest 请求的封装，获取IP地址，用来打印日志*/
    private final HttpServletRequest httpServletRequest;

    @Autowired
    public PassbookController(IUserPassService userPassService,
                              IInventoryService inventoryService,
                              IGainPassTemplateService gainPassTemplateService,
                              IFeedbackService feedbackService,
                              HttpServletRequest httpServletRequest) {
        this.userPassService = userPassService;
        this.inventoryService = inventoryService;
        this.gainPassTemplateService = gainPassTemplateService;
        this.feedbackService = feedbackService;
        this.httpServletRequest = httpServletRequest;
    }

    /**获取用户个人未使用的优惠券信息*/
    @ResponseBody
    @GetMapping("/userpassinfo")
    Response userPassInfo(Long userId) throws Exception{
        /**打日志*/
        LogGenerator.genLog(
                httpServletRequest,
                userId,
                LogConstants.ActionName.USER_PASS_INFO,
                null
        );
        return userPassService.getUserPassInfo(userId);
    }
    /**获取用户已使用的优惠卷信息*/
    @ResponseBody
    @GetMapping("/userusedpassinfo")
    Response userUsedPassInfo(Long userId)throws Exception{
        /**打日志*/
        LogGenerator.genLog(
                httpServletRequest,
                userId,
                LogConstants.ActionName.USER_USED_PASS_INFO,
                null
        );
        return userPassService.getUserUsedPassInfo(userId);
    }
    /**用户使用优惠券*/
    @ResponseBody
    @PostMapping("/userusepass") /**由于这是一个Post服务，且我们将请求对象封装成了一个对象，所以做序列化的时候
     需要给它一个@RequestBody标识这是一个对象，需要用到JSON将它序列化*/
    Response userUsePass(@RequestBody  Pass pass) throws Exception{
        LogGenerator.genLog(
                httpServletRequest,
                pass.getUserId(),
                LogConstants.ActionName.USER_USE_PASS,
                pass
        );
        return userPassService.userUsePass(pass);
    }

    /**获取库存信息*/
    @ResponseBody
    @GetMapping("/inventoryinfo")
    Response inventoryInfo(Long userId) throws Exception{
        LogGenerator.genLog(
                httpServletRequest, /** 获取到ip地址*/
                userId,
                LogConstants.ActionName.INVENTORY_INFO,null
        );
        return inventoryService.getInventoryInfo(userId);
    }

    /**用户领取优惠券*/
    @ResponseBody
    @PostMapping("/gainpasstemplate")  /**由于这是一个Post服务，且我们将请求对象封装成了一个对象，所以做序列化的时候
     需要给它一个@RequestBody标识这是一个对象，需要用到JSON将它序列化*/
    Response gainPassTemplate(@RequestBody GainPassTemplateRequest request) throws Exception{
        LogGenerator.genLog(
                httpServletRequest,
                request.getUserId(),
                LogConstants.ActionName.GAIN_PASS_TEMPLATE,
                request
        );
        return gainPassTemplateService.gainPassTemplate(request);
    }
    /**
     * <h2>用户创建评论</h2>
     * @param feedback {@link Feedback}
     * @return {@link Response}
     * */
    @ResponseBody
    @PostMapping("/createfeedback") /**由于这是一个Post服务，且我们将请求对象封装成了一个对象，所以做序列化的时候
     需要给它一个@RequestBody标识这是一个对象，需要用到JSON将它序列化*/
    Response createFeedback(@RequestBody Feedback feedback) {

        LogGenerator.genLog(
                httpServletRequest,
                feedback.getUserId(),
                LogConstants.ActionName.CREATE_FEEDBACK,
                feedback
        );
        return feedbackService.createFeedback(feedback);
    }
    /**
     * <h2>用户获取评论信息</h2>
     * @param userId 用户 id
     * @return {@link Response}
     * */
    @ResponseBody
    @GetMapping("/getfeedback")
    Response getFeedback(Long userId) {

        LogGenerator.genLog(
                httpServletRequest,
                userId,
                LogConstants.ActionName.GET_FEEDBACK,
                null
        );
        return feedbackService.getFeedback(userId);
    }
    /**
     * <h2>异常演示接口</h2>
     * @return {@link Response}
     * */
    @ResponseBody
    @GetMapping("/exception")
    Response exception() throws Exception {
        throw new Exception("Welcome To IMOOC");
    }

}
