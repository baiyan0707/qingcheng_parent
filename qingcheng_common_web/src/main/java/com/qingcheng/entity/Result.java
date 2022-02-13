package com.qingcheng.entity;

import java.io.Serializable;

/**
 * 返回前端的消息封装
 */
public class Result implements Serializable {

    //返回的业务码  0：成功执行  1：发生错误
    private int code;
    //信息
    private String message;
    //接受对象
    private Object other;

    public Result(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public Result() {
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Object getOther() {
        return other;
    }

    public void setOther(Object other) {
        this.other = other;
    }
}
