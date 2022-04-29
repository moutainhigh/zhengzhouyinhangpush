package com.abtnetworks.totems.recommend.entity;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class RiskRuleDetailEntity {

    @ApiModelProperty("规则ID")
    String ruleId;

    @ApiModelProperty("源域级别")
    String srcZoneLevel;

    @ApiModelProperty("目的域级别")
    String dstZoneLevel;

    @ApiModelProperty("源IP")
    String srcIp;

    @ApiModelProperty("目的IP")
    String dstIp;

    @ApiModelProperty("协议")
    String protocol;

    @ApiModelProperty("端口")
    String port;

    @ApiModelProperty("策略类型")
    String policyType;
}
