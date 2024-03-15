package com.imooc.passbook.vo;

import com.imooc.passbook.entity.Merchants;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * <h1> 优惠券模版信息，包括优惠券模版和商户信息
 * 已经领取的是passTemplate</h1>*/
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PassTemplateInfo {
    /** 优惠券模板 */
    private PassTemplate passTemplate;

    /** 优惠券对应的商户 */
    private Merchants merchants;

}
