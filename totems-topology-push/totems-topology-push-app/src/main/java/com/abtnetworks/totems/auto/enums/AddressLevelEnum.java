package com.abtnetworks.totems.auto.enums;

import java.util.HashMap;
import java.util.Map;

public enum AddressLevelEnum {

    ADDRESS_PARENT(0,"顶级地址组对象"),
    ADDRESS_FIRST(1, "一级地址组对象"),
    ADDRESS_SECOND(2, "二级地址组对象"),
    ADDRESS_THIRD(3, "三级地址组对象"),
    ADDRESS_FOUR(4, "四级地址组对象"),
    ADDRESS_FIVE(5, "五级地址组对象"),
    ;

    private static Map<Integer, AddressLevelEnum> map = new HashMap<>();


    private Integer code;

    private String desc;

    static {
        for(AddressLevelEnum statusEnum : AddressLevelEnum.values()){
            map.put(statusEnum.getCode(), statusEnum);
        }
    }

    AddressLevelEnum() {
    }

    AddressLevelEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public Integer getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

    public static Map<Integer, AddressLevelEnum> getMap() {
        return map;
    }

    public static String getDescByCode(Integer code) {
        String desc = "";
        AddressLevelEnum statusEnum = map.get(code);
        if(statusEnum != null){
            desc = statusEnum.getDesc();
        }
        return desc;
    }

}
