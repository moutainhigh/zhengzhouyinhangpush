package com.abtnetworks.totems.auto.enums;

import java.util.HashMap;
import java.util.Map;

/**
 * @desc    NAT映射枚举
 * @author liuchanghao
 * @date 2021-06-11 16:52
 */
public enum PushNatFlagEnum {

    NAT_FLAG_Y("Y","开启NAT映射"),
    NAT_FLAG_N("N", "关闭NAT映射")
    ;

    private static Map<String, PushNatFlagEnum> map = new HashMap<>();

    private String code;

    private String desc;

    static {
        for(PushNatFlagEnum statusEnum : PushNatFlagEnum.values()){
            map.put(statusEnum.getCode(), statusEnum);
        }
    }

    PushNatFlagEnum() {
    }

    PushNatFlagEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public String getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

    public static Map<String, PushNatFlagEnum> getMap() {
        return map;
    }

    public static String getDescByCode(Integer code) {
        String desc = "";
        PushNatFlagEnum statusEnum = map.get(code);
        if(statusEnum != null){
            desc = statusEnum.getDesc();
        }
        return desc;
    }

}
