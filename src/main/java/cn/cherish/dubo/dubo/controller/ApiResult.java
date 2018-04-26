package cn.cherish.dubo.dubo.controller;

import lombok.Data;

/**
 * @author Cherish
 * @version 1.0
 * @date 2018/4/26 20:14
 */
@Data
public class ApiResult<T> {

    private Integer code;
    private boolean success;
    private String msg;
    private T data;


    public ApiResult(T data) {
        this.data = data;
        this.code = 200;
        this.success = true;
    }

}
