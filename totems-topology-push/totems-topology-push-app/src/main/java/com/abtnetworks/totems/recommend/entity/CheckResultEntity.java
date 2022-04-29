package com.abtnetworks.totems.recommend.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel("策略检查数据")
public class CheckResultEntity {

    @ApiModelProperty("主键id")
    private Integer id;

    @ApiModelProperty("策略Id")
    private Integer policyId;

    @ApiModelProperty("策略检查结果")
    private String checkResult;
}