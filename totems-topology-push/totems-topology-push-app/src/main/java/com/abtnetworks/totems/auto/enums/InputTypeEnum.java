package com.abtnetworks.totems.auto.enums;

import java.util.HashMap;
import java.util.Map;

/**
 * @desc    自动开通地址输入类型枚举
 * @author liuchanghao
 * @date 2021-06-11 11:52
 */
public enum InputTypeEnum {

    SRC_INPUT_TYPE_IP(0,"源地址输入IP类型"),
    SRC_INPUT_TYPE_OBJECT(1,"源地址输入地址对象类型"),
    DST_INPUT_TYPE_IP(2,"目的地址输入IP类型"),
    DST_INPUT_TYPE_OBJECT(3,"目的地址输入地址对象类型")
    ;

    private static Map<Integer, InputTypeEnum> map = new HashMap<>();

    private static Map<String, InputTypeEnum> descMap = new HashMap<>();


    private Integer code;

    private String desc;

    static {
        for(InputTypeEnum statusEnum : InputTypeEnum.values()){
            map.put(statusEnum.getCode(), statusEnum);
            descMap.put(statusEnum.getDesc(), statusEnum);
        }
    }

    InputTypeEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public Integer getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

    public static Map<Integer, InputTypeEnum> getMap() {
        return map;
    }

    public static String getDescByCode(Integer code) {
        String desc = "";
        InputTypeEnum statusEnum = map.get(code);
        if(statusEnum != null){
            desc = statusEnum.getDesc();
        }
        return desc;
    }
    public static Integer getCodeByDesc(String desc) {
        InputTypeEnum statusEnum = descMap.get(desc);
        if(null == statusEnum){
            return null;
        }
        return statusEnum.getCode();
    }


}
