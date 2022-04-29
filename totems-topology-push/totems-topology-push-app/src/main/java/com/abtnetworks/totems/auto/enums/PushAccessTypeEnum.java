package com.abtnetworks.totems.auto.enums;

import java.util.HashMap;
import java.util.Map;

/**
 * @desc    访问类型枚举
 * @author liuchanghao
 * @date 2021-06-11 11:52
 */
public enum PushAccessTypeEnum {

    INSIDE_TO_INSIDE(0,"内网互访"),
    INSIDE_TO_OUTSIDE(1, "内网访问互联网"),
    OUTSIDE_TO_INSIDE(2, "互联网访问内网")
    ;

    private static Map<Integer, PushAccessTypeEnum> map = new HashMap<>();

    private static Map<String, PushAccessTypeEnum> descMap = new HashMap<>();

    private Integer code;

    private String desc;

    static {
        for(PushAccessTypeEnum statusEnum : PushAccessTypeEnum.values()){
            map.put(statusEnum.getCode(), statusEnum);
            descMap.put(statusEnum.getDesc(), statusEnum);
        }
    }

    PushAccessTypeEnum() {
    }

    PushAccessTypeEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public Integer getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

    public static Map<Integer, PushAccessTypeEnum> getMap() {
        return map;
    }

    public static String getDescByCode(Integer code) {
        String desc = "";
        PushAccessTypeEnum statusEnum = map.get(code);
        if(statusEnum != null){
            desc = statusEnum.getDesc();
        }
        return desc;
    }

    public static Integer getCodeByDesc(String desc) {
        PushAccessTypeEnum statusEnum = descMap.get(desc);
        if(null == statusEnum){
            return null;
        }
        return statusEnum.getCode();
    }


}
