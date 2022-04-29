package com.abtnetworks.totems.recommend.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel("风险分析数据")
public class PolicyRiskEntity {
    @ApiModelProperty("主键")
    private Integer id;

    @ApiModelProperty("路径信息id")
    private Integer pathInfoId;

    @ApiModelProperty("策略id")
    private Integer policyId;

    @ApiModelProperty("风险规则id")
    private String ruleId;

    @ApiModelProperty("补充数据")
    private String objJson;
}