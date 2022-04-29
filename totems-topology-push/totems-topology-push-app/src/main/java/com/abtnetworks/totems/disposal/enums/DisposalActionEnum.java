package com.abtnetworks.totems.disposal.enums;

import java.util.HashMap;
import java.util.Map;

/**
 * 处置动作
 * @author luwei
 * @date 2019/11/14
 */
public enum DisposalActionEnum {

    DENY("deny", "封堵"),
    PERMIT("permit", "解封"),

    ;


    private static Map<String, DisposalActionEnum> map = new HashMap<>();

    private String code;

    private String desc;

    static {
        for(DisposalActionEnum typeEnum : DisposalActionEnum.values()){
            map.put(typeEnum.getCode(), typeEnum);
        }
    }

    DisposalActionEnum() {
    }

    DisposalActionEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public String getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

    public static Map<String, DisposalActionEnum> getMap() {
        return map;
    }

    public static String getDescByCode(String code) {
        String desc = "";
        DisposalActionEnum typeEnum = map.get(code);
        if(typeEnum != null){
            desc = typeEnum.getDesc();
        }
        return desc;
    }

}
