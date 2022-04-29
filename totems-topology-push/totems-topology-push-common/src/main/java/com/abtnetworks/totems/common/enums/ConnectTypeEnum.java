package com.abtnetworks.totems.common.enums;

/**
 * @author lifei
 * @desc 长短连接枚举
 * @date 2021/10/28 19:37
 */
public enum ConnectTypeEnum {


    LONG_CONNECT(1,"长连接"),
    SHORT_CONNECT(2, "短连接")
    ;

    private Integer code;

    private String desc;


    ConnectTypeEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public Integer getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }
}
