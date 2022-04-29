package com.abtnetworks.totems.auto.enums;

import java.util.HashMap;
import java.util.Map;

/**
 * @desc    自动开通状态枚举
 * @author liuchanghao
 * @date 2021-06-16 16:52
 */
public enum AddressStatusEnum {

    INIT_STATUS(0,"初始化状态"),
    ISSUED_STATUS(1, "待下发状态"),
    PUSHING(2, "下发中"),
    PUSH_SUCCESS(3, "下发成功"),
    PUSH_FAIL(4, "下发失败"),
    PUSH_SUCCESS_PARTS(5, "下发部分成功"),
    ;

    private static Map<Integer, AddressStatusEnum> map = new HashMap<>();

    private Integer code;

    private String desc;

    static {
        for(AddressStatusEnum statusEnum : AddressStatusEnum.values()){
            map.put(statusEnum.getCode(), statusEnum);
        }
    }

    AddressStatusEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public Integer getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

    public static Map<Integer, AddressStatusEnum> getMap() {
        return map;
    }

    public static String getDescByCode(Integer code) {
        String desc = "";
        AddressStatusEnum statusEnum = map.get(code);
        if(statusEnum != null){
            desc = statusEnum.getDesc();
        }
        return desc;
    }

}
