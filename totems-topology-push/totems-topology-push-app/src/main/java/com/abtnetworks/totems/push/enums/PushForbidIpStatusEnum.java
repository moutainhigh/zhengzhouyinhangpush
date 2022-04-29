package com.abtnetworks.totems.push.enums;

import java.util.HashMap;
import java.util.Map;

/**
 * @desc    封禁IP状态枚举
 * @author liuchanghao
 * @date 2020-09-11 09:52
 */
public enum PushForbidIpStatusEnum {

    INIT(0,"初始化"),
    GENERATING(1, "命令行生成中"),
    GENERATE_FAIL(2, "命令行生成失败"),
    PRE_PUSH(3, "待下发"),
    PUSHING(4, "下发中"),
    PUSH_FAIL(5, "下发失败"),
    PUSH_SUCCESS(6, "下发成功"),
    UPDATED(7, "已更新"),
    ENABLE(8, "已启用"),
    DISABLE(9, "已禁用")
    ;

    private static Map<Integer, PushForbidIpStatusEnum> map = new HashMap<>();

    private Integer code;

    private String desc;

    static {
        for(PushForbidIpStatusEnum statusEnum : PushForbidIpStatusEnum.values()){
            map.put(statusEnum.getCode(), statusEnum);
        }
    }

    PushForbidIpStatusEnum() {
    }

    PushForbidIpStatusEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public Integer getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

    public static Map<Integer, PushForbidIpStatusEnum> getMap() {
        return map;
    }

    public static String getDescByCode(Integer code) {
        String desc = "";
        PushForbidIpStatusEnum statusEnum = map.get(code);
        if(statusEnum != null){
            desc = statusEnum.getDesc();
        }
        return desc;
    }

}
