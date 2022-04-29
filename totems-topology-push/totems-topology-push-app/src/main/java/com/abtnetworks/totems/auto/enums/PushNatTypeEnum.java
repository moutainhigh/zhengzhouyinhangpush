package com.abtnetworks.totems.auto.enums;

import java.util.HashMap;
import java.util.Map;

/**
 * @desc    自动开通防护配置NAT类型枚举
 * @author liuchanghao
 * @date 2021-08-09 10:26
 */
public enum PushNatTypeEnum {

    NAT_TYPE_N("N","无Nat"),
    NAT_TYPE_S("S","源Nat"),
    NAT_TYPE_D("D", "目的Nat")
    ;

    private static Map<String, PushNatTypeEnum> map = new HashMap<>();
    private static Map<String, PushNatTypeEnum> codeMap = new HashMap<>();

    private String code;

    private String desc;

    static {
        for(PushNatTypeEnum statusEnum : PushNatTypeEnum.values()){
            map.put(statusEnum.getCode(), statusEnum);
            codeMap.put(statusEnum.getDesc(), statusEnum);
        }
    }

    PushNatTypeEnum() {
    }

    PushNatTypeEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public String getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

    public static Map<String, PushNatTypeEnum> getMap() {
        return map;
    }

    public static String getDescByCode(String code) {
        String desc = "";
        PushNatTypeEnum statusEnum = map.get(code);
        if(statusEnum != null){
            desc = statusEnum.getDesc();
        }
        return desc;
    }

    public static String getCodeByDesc(String desc) {
        String code = "";
        PushNatTypeEnum statusEnum = codeMap.get(desc);
        if(statusEnum != null){
            code = statusEnum.getCode();
        }
        return code;
    }

}
