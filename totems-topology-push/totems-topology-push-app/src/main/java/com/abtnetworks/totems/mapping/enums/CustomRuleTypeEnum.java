package com.abtnetworks.totems.mapping.enums;

/**
 * @desc    地址映射自动匹配规则类型类
 * @author liuchanghao
 * @date 2022-01-20 10:03
 */
public enum CustomRuleTypeEnum {
    DEFAULT(0,"DEFAULT"),
    CUSTOM_V2(1, "CUSTOM_V2")
            ;

    private Integer code;

    private String desc;


    CustomRuleTypeEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public Integer getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

    /**
     * 根据描述查枚举
     * @param desc
     * @return
     */
    public static CustomRuleTypeEnum getRuleTypeEnumByDesc(String desc){
        for (CustomRuleTypeEnum ipTypeEnum: CustomRuleTypeEnum.values() ) {
            if(ipTypeEnum.getDesc().equalsIgnoreCase(desc)){
                return ipTypeEnum;
            }
        }
        return DEFAULT;
    }


}
