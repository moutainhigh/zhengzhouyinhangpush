package com.abtnetworks.totems.disposal.enums;

import java.util.HashMap;
import java.util.Map;

/**
 * 处置单状态
 * @author luwei
 * @date 2019/11/12
 */
public enum DisposalHandleStatusEnum {

    UN_PROCESS(1,"未处置"),
    COMPLETED(2, "已处置"),
    AUTO_COMPLETED(3, "自动处置"),
    ;

    private static Map<Integer, DisposalHandleStatusEnum> map = new HashMap<>();

    private Integer code;

    private String desc;

    static {
        for(DisposalHandleStatusEnum statusEnum : DisposalHandleStatusEnum.values()){
            map.put(statusEnum.getCode(), statusEnum);
        }
    }

    DisposalHandleStatusEnum() {
    }

    DisposalHandleStatusEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public Integer getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

    public static Map<Integer, DisposalHandleStatusEnum> getMap() {
        return map;
    }

    public static String getDescByCode(Integer code) {
        String desc = "";
        DisposalHandleStatusEnum statusEnum = map.get(code);
        if(statusEnum != null){
            desc = statusEnum.getDesc();
        }
        return desc;
    }

}
