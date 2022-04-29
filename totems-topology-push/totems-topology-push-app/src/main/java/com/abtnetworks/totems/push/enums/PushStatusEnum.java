package com.abtnetworks.totems.push.enums;

import java.util.HashMap;
import java.util.Map;

/**
 * @desc    下发状态枚举
 * @author liuchanghao
 * @date 2020-09-11 09:52
 */
public enum PushStatusEnum {

    PUSH_NOT_START (0,"下发未开始"),
    PUSHING(1, "下发中"),
    PUSH_STATUS_ENUM(2, "下发完成"),
    PUSH_FAILED(3, "下发失败"),
    PUSH_IN_QUEUE(4, "下发队列中")
    ;

    private static Map<Integer, PushStatusEnum> map = new HashMap<>();

    private Integer code;

    private String desc;

    static {
        for(PushStatusEnum statusEnum : PushStatusEnum.values()){
            map.put(statusEnum.getCode(), statusEnum);
        }
    }

    PushStatusEnum() {
    }

    PushStatusEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public Integer getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

    public static Map<Integer, PushStatusEnum> getMap() {
        return map;
    }

    public static String getDescByCode(Integer code) {
        String desc = "";
        PushStatusEnum statusEnum = map.get(code);
        if(statusEnum != null){
            desc = statusEnum.getDesc();
        }
        return desc;
    }

}
