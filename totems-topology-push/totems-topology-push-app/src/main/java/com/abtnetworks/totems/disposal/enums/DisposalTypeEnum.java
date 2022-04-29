package com.abtnetworks.totems.disposal.enums;

import java.util.HashMap;
import java.util.Map;

/**
 * 工单类型
 * @author luwei
 * @date 2019/11/12
 */
public enum DisposalTypeEnum {

    MANUAL(1,"场景"),
    BLACK_IP(2, "黑IP"),
    PATH(3, "路径"),
    ;

    private static Map<Integer, DisposalTypeEnum> map = new HashMap<>();

    private Integer code;

    private String desc;

    static {
        for(DisposalTypeEnum typeEnum : DisposalTypeEnum.values()){
            map.put(typeEnum.getCode(), typeEnum);
        }
    }

    DisposalTypeEnum() {
    }

    DisposalTypeEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public Integer getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

    public static Map<Integer, DisposalTypeEnum> getMap() {
        return map;
    }

    public static String getDescByCode(Integer code) {
        String desc = "";
        DisposalTypeEnum typeEnum = map.get(code);
        if(typeEnum != null){
            desc = typeEnum.getDesc();
        }
        return desc;
    }



}
