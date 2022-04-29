package com.abtnetworks.totems.auto.enums;

import java.util.HashMap;
import java.util.Map;

/**
 * @desc    自动开通状态枚举
 * @author liuchanghao
 * @date 2021-06-16 16:52
 */
public enum AutoRecommendStatusEnum {

    GENERATE_COMMANDLINE_NOT_START(0,"生成命令行未开始"),
    GENERATE_COMMANDLINE_SUCCESS(1,"生成命令行成功"),
    GENERATE_COMMANDLINE_FAIL(2, "生成命令行失败"),
    PUSH_NOT_START(3, "下发未开始"),
    PUSH_WAITING(4, "下发等待中"),
    PUSHING(5, "下发中"),
    PUSH_SUCCESS(6, "下发成功"),
    PUSH_FAIL(7, "下发失败"),
    PUSH_SUCCESS_PARTS(8, "下发部分成功"),
    SECURITY_POLICY_HAS_EXIST(9, "安全策略已开通"),
    NAT_POLICY_HAS_EXIST(10, "Nat策略已开通"),
    GENERATE_COMMANDLINE_SUCCESS_PARTS(11,"命令行生成部分成功"),
    GENERATING_COMMANDLINE(12,"命令行生成中")
    ;

    private static Map<Integer, AutoRecommendStatusEnum> map = new HashMap<>();

    private Integer code;

    private String desc;

    static {
        for(AutoRecommendStatusEnum statusEnum : AutoRecommendStatusEnum.values()){
            map.put(statusEnum.getCode(), statusEnum);
        }
    }

    AutoRecommendStatusEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public Integer getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

    public static Map<Integer, AutoRecommendStatusEnum> getMap() {
        return map;
    }

    public static String getDescByCode(Integer code) {
        String desc = "";
        AutoRecommendStatusEnum statusEnum = map.get(code);
        if(statusEnum != null){
            desc = statusEnum.getDesc();
        }
        return desc;
    }

}
