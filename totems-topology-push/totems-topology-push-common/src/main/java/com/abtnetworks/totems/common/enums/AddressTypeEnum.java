package com.abtnetworks.totems.common.enums;

/**
 * @author lifei
 * @desc 地址类型枚举
 * @date 2021/7/1 16:27
 */
public enum AddressTypeEnum {

    HOST("host", "主机/单ip"),
    SUB("subnet", "子网"),
    RANG("range", "范围");

    private String code;

    private String desc;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    AddressTypeEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}
