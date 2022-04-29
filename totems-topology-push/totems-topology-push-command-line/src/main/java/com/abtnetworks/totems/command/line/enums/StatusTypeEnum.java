package com.abtnetworks.totems.command.line.enums;

import org.apache.commons.lang3.StringUtils;

/**
 * @author hw
 * @date
 */
public enum StatusTypeEnum {

    /** 策略操作状态 **/

    ADD("1","add", "ipv4/ipv6", "新增:包含ipv4和ipv6"),
    MODIFY("2","modify", "ipv4/ipv6", "修改:包含ipv4和ipv6"),
    DELETE("3","delete", "ipv4/ipv6", "删除:包含ipv4和ipv6"),

    ADD_IPV4("4","add", "ipv4", "新增:ipv4"),
    MODIFY_IPV4("5","modify", "ipv4", "修改:ipv4"),
    DELETE_IPV4("6","delete", "ipv4", "删除:ipv4"),

    ADD_IPV6("7","add", "ipv6", "新增:ipv6"),
    MODIFY_IPV6("8","modify", "ipv6", "修改:ipv6"),
    DELETE_IPV6("9","delete", "ipv6", "删除:ipv6");

    private String code;
    private String status;
    private String ruleType;
    private String description;

    StatusTypeEnum(String code, String status, String ruleType, String description) {
        this.code = code;
        this.status = status;
        this.ruleType = ruleType;
        this.description = description;
    }

    public String getCode() {
        return code;
    }

    public String getStatus() {
        return status;
    }

    public String getRuleType() {
        return ruleType;
    }

    public String getDescription() {
        return description;
    }

    public static StatusTypeEnum getByCode(String code){
        if(StringUtils.isBlank(code)){
            return null;
        }
        for (StatusTypeEnum statusTypeEnum:StatusTypeEnum.values()) {
            if(statusTypeEnum.getCode().equals(code)){
                return statusTypeEnum;
            }
        }
        return null;
    }
}
