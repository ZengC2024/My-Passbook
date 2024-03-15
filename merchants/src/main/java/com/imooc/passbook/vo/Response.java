package com.imooc.passbook.vo;

/* 通用的相应对象**/

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Response {
    /** 错误码, 正确返回 0 */
    private Integer errorCode = 0;

    /** 错误信息, 正确返回空字符串 */
    private String errorMsg = "";

    /** 返回值对象 */
    private Object data;

    /**
     * <h2>正确的响应构造函数</h2>
     * @param data 返回值对象
     * */
    public Response(Object data) {
        this.data = data;
    }
}
