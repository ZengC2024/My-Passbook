package com.imooc.passbook.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * User Object，与HBase中user表结构是一样的*/
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {
    /** 用户 id */
    private Long id;

    /** 用户基本信息 */
    private BaseInfo baseInfo;

    /** 用户额外信息 */
    private OtherInfo otherInfo;


    /**第一个列族 b列族*/
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BaseInfo {

        private String name;
        private Integer age;
        private String sex;
    }
    /**第二个列族  o列族*/
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OtherInfo {

        private String phone;
        private String address;
    }

}
