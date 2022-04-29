package com.abtnetworks.totems.command.line.enums;

import org.apache.commons.lang3.StringUtils;

/**
 * @author luwei
 * @date 2020/12/10
 */
public enum RuleIPTypeEnum {

    /** 安全策略使用 **/
    IP4("1","IP4", ""),
    IP6("2","IP6", ""),
    IP46("3","IP46", ""),

    /** NAT策略使用 **/
    /**IPv4-to-IPv4**/
    NAT44("4","NAT44", ""),
    /**IPv6-to-IPv6**/
    NAT66("5","NAT66", ""),
    /**IPv4-to-IPv6**/
    NAT46("6","NAT46", ""),
    /**IPv6-to-IPv4**/
    NAT64("7","NAT64", ""),

    /** 未知类型 **/
    UNKNOWN("0","UNKNOWN", "");

    private String code;
    private String name;
    private String description;

    RuleIPTypeEnum(String code, String name, String description) {
        this.code = code;
        this.name = name;
        this.description = description;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public static RuleIPTypeEnum getByCode(String code){
        if(StringUtils.isBlank(code)){
            return null;
        }
        for (RuleIPTypeEnum ruleIPTypeEnum:RuleIPTypeEnum.values()) {
            if(ruleIPTypeEnum.getCode().equals(code)){
                return ruleIPTypeEnum;
            }
        }
        return null;
    }
}
