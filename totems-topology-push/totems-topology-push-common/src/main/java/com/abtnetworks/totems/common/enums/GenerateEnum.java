package com.abtnetworks.totems.common.enums;

/**
 * @author Administrator
 * @Title:
 * @Description: 策略生成阶段需要的枚举
 * @date 2021/4/27
 */
public enum GenerateEnum {

    CMD_EXIST_ERROR(8, "该设备不生成命令行[已存在开通策略]");

    private int code;

    private String message;

    GenerateEnum(int code, String message) {
        this.code = code;
        this.message = message;
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
}
