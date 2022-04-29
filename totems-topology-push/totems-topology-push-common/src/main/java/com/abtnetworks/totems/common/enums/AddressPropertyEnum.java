package com.abtnetworks.totems.common.enums;

/**
 * @author Administrator
 * @Title:
 * @Description: 地址属性枚举
 * @date 2021/3/3
 */
public enum AddressPropertyEnum {
    SRC(0,"源地址"),
    POST_SRC(1,"转换后源地址"),
    DST(0,"目的地址"),
    POST_DST(3,"转后目的地址");



    private Integer code;

    private String name;

    AddressPropertyEnum(Integer code, String name) {
        this.code = code;
        this.name = name;
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
