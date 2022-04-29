package com.abtnetworks.totems.recommend.entity;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @Author: wenjiachang
 * @Date: 2018/12/17 10:49
 */
@Data
public class RiskRuleInfoEntity {

    @ApiModelProperty("自定义规则id")
    String ruleId;

    @ApiModelProperty("一级分类ID")
    int sortId;

    @ApiModelProperty("二级分类ID")
    int secondSortId;

    @ApiModelProperty("风险级别")
    int ruleLevel;

    @ApiModelProperty("规则名称")
    String ruleName;

    @ApiModelProperty("规则描述")
    String ruleDesc;
}
