package com.abtnetworks.totems.mapping.enums;

import io.swagger.annotations.ApiModelProperty;

/**
 * @author lifei
 * @desc ip映射表nat类型枚举
 * @date 2022/2/16 11:20
 */
public enum AutoMappingNatTypeEnum {

    SNAT(0, "SNAT","源NAT"),
    DNAT(1, "DNAT","目的NAT");

    @ApiModelProperty("编码")
    private Integer code;

    @ApiModelProperty("名称")
    private String name;

    @ApiModelProperty("规则名称")
    private String desc;

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    AutoMappingNatTypeEnum(Integer code, String name, String desc) {
        this.code = code;
        this.name = name;
        this.desc = desc;
    }

    /**
     * 根据编码查枚举
     *
     * @param code
     * @return
     */
    public static AutoMappingNatTypeEnum getByCode(Integer code) {

        for (AutoMappingNatTypeEnum autoMappingNatTypeEnum : AutoMappingNatTypeEnum.values()) {
            if (autoMappingNatTypeEnum.getCode().equals(code)) {
                return autoMappingNatTypeEnum;
            }
        }
        return null;
    }

}
