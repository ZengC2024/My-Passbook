package com.imooc.passbook.entity;
/* 商户对象模型*/

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Data // get,set 方法
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name="merchants")
public class Merchants {
    /** 商户id 需要是全局唯一的**/
    @Id
    @GeneratedValue //自动生成
    @Column(name="id",nullable = false)
    private Integer id;

    /**商户名称，需要是全局唯一的**/
    @Basic //默认的，表示是表的一个基本列 如果是@Transient表示表中可以不存在这个属性
    @Column(name="name",unique = true,nullable = false)
    private String name;

    /** 商户 logo */
    @Basic
    @Column(name = "logo_url", nullable = false)
    private String logoUrl;
    /** 商户营业执照 */
    @Basic
    @Column(name = "business_license_url", nullable = false)
    private String businessLicenseUrl;

    /** 商户的联系电话 */
    @Basic
    @Column(name = "phone", nullable = false)
    private String phone;

    /** 商户地址 */
    @Basic
    @Column(name = "address", nullable = false)
    private String address;

    /** 商户是否通过审核 */
    @Basic
    @Column(name = "is_audit", nullable = false)
    private Boolean isAudit = false;
}
