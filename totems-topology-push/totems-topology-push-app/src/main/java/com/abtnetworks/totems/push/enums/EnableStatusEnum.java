package com.abtnetworks.totems.push.enums;

import java.util.HashMap;
import java.util.Map;

/**
 * @desc    启用禁用状态枚举
 * @author liuchanghao
 * @date 2020-09-11 09:52
 */
public enum EnableStatusEnum {

    ENABLE ("Y","启用"),
    DISABLE("N", "禁用")
    ;

    private static Map<String, EnableStatusEnum> map = new HashMap<>();

    private String code;

    private String desc;

    static {
        for(EnableStatusEnum statusEnum : EnableStatusEnum.values()){
            map.put(statusEnum.getCode(), statusEnum);
        }
    }

    EnableStatusEnum() {
    }

    EnableStatusEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public String getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

    public static Map<String, EnableStatusEnum> getMap() {
        return map;
    }

    public static String getDescByCode(String code) {
        String desc = "";
        EnableStatusEnum statusEnum = map.get(code);
        if(statusEnum != null){
            desc = statusEnum.getDesc();
        }
        return desc;
    }

}
