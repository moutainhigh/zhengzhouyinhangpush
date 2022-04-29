package com.abtnetworks.totems.auto.enums;

import java.util.HashMap;
import java.util.Map;

/**
 * @desc    工单状态枚举
 * @author liuchanghao
 * @date 2021-08-19 11:52
 */
public enum AutoTaskOrderStatusEnum {

    POLICY_HAS_EXIST(0,"已开通"),
    POLICY_GENERATE_ERROR(1, "不开通"),
    POLICY_WAIT_CREATE(2, "待开通")
    ;

    private static Map<Integer, AutoTaskOrderStatusEnum> map = new HashMap<>();

    private static Map<String, AutoTaskOrderStatusEnum> descMap = new HashMap<>();

    private Integer code;

    private String desc;

    static {
        for(AutoTaskOrderStatusEnum statusEnum : AutoTaskOrderStatusEnum.values()){
            map.put(statusEnum.getCode(), statusEnum);
            descMap.put(statusEnum.getDesc(), statusEnum);
        }
    }

    AutoTaskOrderStatusEnum() {
    }

    AutoTaskOrderStatusEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public Integer getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

    public static Map<Integer, AutoTaskOrderStatusEnum> getMap() {
        return map;
    }

    public static String getDescByCode(Integer code) {
        String desc = "";
        AutoTaskOrderStatusEnum statusEnum = map.get(code);
        if(statusEnum != null){
            desc = statusEnum.getDesc();
        }
        return desc;
    }

    public static Integer getCodeByDesc(String desc) {
        AutoTaskOrderStatusEnum statusEnum = descMap.get(desc);
        if(null == statusEnum){
            return null;
        }
        return statusEnum.getCode();
    }


}
