package com.abtnetworks.totems.command.line.enums;

/**
 * @Author: WangCan
 * @Description 公共枚举
 * @Date: 2021/7/12
 */
public enum CommonEnums {

    H3C_OP_OBJECT_POLICY(1,"<policySetName>", "h3sec po");

    private int code;
    private String value;
    private String description;

    CommonEnums(int code, String value, String description) {
        this.code = code;
        this.value = value;
        this.description = description;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
