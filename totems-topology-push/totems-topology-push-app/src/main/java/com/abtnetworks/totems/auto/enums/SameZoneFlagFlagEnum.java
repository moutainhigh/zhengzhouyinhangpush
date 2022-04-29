package com.abtnetworks.totems.auto.enums;

import java.util.HashMap;
import java.util.Map;

/**
 * @desc    是否开启同域开通逻辑（Y：打开；N：未打开）
 * @author liuchanghao
 * @date 2021-11-07 16:52
 */
public enum SameZoneFlagFlagEnum {

    SAME_ZONE_RECOMMEND_Y("Y","开启同域开通逻辑"),
    SAME_ZONE_RECOMMEND_N("N", "关闭同域开通逻辑")
    ;

    private static Map<String, SameZoneFlagFlagEnum> map = new HashMap<>();

    private String code;

    private String desc;

    static {
        for(SameZoneFlagFlagEnum statusEnum : SameZoneFlagFlagEnum.values()){
            map.put(statusEnum.getCode(), statusEnum);
        }
    }

    SameZoneFlagFlagEnum() {
    }

    SameZoneFlagFlagEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public String getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

    public static Map<String, SameZoneFlagFlagEnum> getMap() {
        return map;
    }

    public static String getDescByCode(Integer code) {
        String desc = "";
        SameZoneFlagFlagEnum statusEnum = map.get(code);
        if(statusEnum != null){
            desc = statusEnum.getDesc();
        }
        return desc;
    }

}
