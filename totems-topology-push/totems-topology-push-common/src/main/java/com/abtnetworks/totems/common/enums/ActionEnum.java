package com.abtnetworks.totems.common.enums;

public enum ActionEnum {

    PERMIT(0, "Permit", "允许"),
    DENY(1, "Deny", "禁止");


    private int code;

    private String desc;

    private String key;

    ActionEnum(int code, String key, String desc) {
        this.code = code;
        this.desc = desc;
        this.key = key;
    }

    public int getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

    public String getKey() {
        return key;
    }
}
