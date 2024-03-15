package com.imooc.passbook.utils;

import com.imooc.passbook.vo.Feedback;
import com.imooc.passbook.vo.GainPassTemplateRequest;
import com.imooc.passbook.vo.PassTemplate;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;

/**
 * <h1> RowKey 生成器工具类</h1>*/
@Slf4j
public class RowKeyGenUtil {
    /**
     * <h2>根据提供的 PassTemplate 对象生成 RowKey,表示优惠券模版在Hbase中存储位置</h2>
     * @param passTemplate {@link PassTemplate}
     * @return String RowKey
     * */
    public static String genPassTemplateRowKey(PassTemplate passTemplate) {

        String passInfo = String.valueOf(passTemplate.getId()) + "_" + passTemplate.getTitle();
        /**md5使得数据更加分散，在HBase上一个rowKey存储在一块，这样分散存储，使得负载更加均衡*/
        String rowKey = DigestUtils.md5Hex(passInfo);
        log.info("GenPassTemplateRowKey: {}, {}", passInfo, rowKey);

        return rowKey;
    }

    /**
     * <h2>根据 Feedback 构造 RowKey，表示用户评论在Hbase中的存储位置</h2>
     * @param feedback {@link Feedback}
     * @return String RowKey
     * */
    public static String genFeedbackRowKey(Feedback feedback) {
        /**用户的所有的feedback存储在相近的位置是比较好的，便于扫面一个用户的所有评论，
         * reverse()的作用：当用户数很大时，用户的前缀是相同的，反转一下，就变成前缀随机的userId，利于数据的分散
         * long-current，越晚创建的feedback，它就在前面，同一个userId，最新的feedback在前面*/
        return new StringBuilder(String.valueOf(feedback.getUserId())).reverse().toString() +
                (Long.MAX_VALUE - System.currentTimeMillis());
    }

    /**
     * <h2>根据提供的领取优惠券请求生成 RowKey, 只可以在领取优惠券的时候使用, 表示领取优惠券的记录在Hbase中的存储位置</h2>
     *                  userId翻转         当前时间戳，越晚越小      优惠券模版RowKey，便于之后的扫描哪些用户领取某个passTemplate
     * Pass RowKey = reversed(userId) + inverse(timestamp) + PassTemplate RowKey
     * @param request {@link GainPassTemplateRequest}
     * @return String RowKey
     * */
    public static String genPassRowKey(GainPassTemplateRequest request) {

        return new StringBuilder(String.valueOf(request.getUserId())).reverse().toString()
                + (Long.MAX_VALUE - System.currentTimeMillis())
                + genPassTemplateRowKey(request.getPassTemplate());
    }
}
