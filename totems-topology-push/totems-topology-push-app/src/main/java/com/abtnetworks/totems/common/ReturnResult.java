package com.abtnetworks.totems.common;

import java.io.Serializable;

/**
 * @Author: Administrator
 * @Date: 2019/11/12
 * @desc: 请写类注释
 */
public class ReturnResult<T> implements Serializable {
    private static final long serialVersionUID = 2143771883265392131L;

    public static final int SUCCESS_CODE = 200;
    public static final int FAIL_CODE = 500;

    public static final ReturnResult<String> SUCCESS = new ReturnResult<String>(null);
    public static final ReturnResult<String> FAIL = new ReturnResult<String>(FAIL_CODE, null);

    private int code;
    private String msg;
    private T content;

    public ReturnResult() {
    }

    public ReturnResult(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public ReturnResult(T content) {
        this.code = SUCCESS_CODE;
        this.content = content;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public T getContent() {
        return content;
    }

    public void setContent(T content) {
        this.content = content;
    }

    @Override
    public String toString() {
        return "ReturnT [code=" + code + ", msg=" + msg + ", content=" + content + "]";
    }

}
