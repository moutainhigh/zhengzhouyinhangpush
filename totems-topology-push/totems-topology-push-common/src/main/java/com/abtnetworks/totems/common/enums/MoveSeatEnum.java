package com.abtnetworks.totems.common.enums;

/**
 * @author luwei
 * @date 2019/5/27
 */
public enum  MoveSeatEnum {

    FIRST(1, "最前面"),
    LAST(2,"最后面"),
    BEFORE(3, "在之前",  "before"),
    AFTER(4, "在之后", "after"),

    ;


    private int code;

    private String desc;

    private String key;

    MoveSeatEnum(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    MoveSeatEnum(int code, String desc, String key) {
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
    }}
