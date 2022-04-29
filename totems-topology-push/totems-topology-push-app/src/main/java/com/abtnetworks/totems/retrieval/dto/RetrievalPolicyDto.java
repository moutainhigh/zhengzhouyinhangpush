package com.abtnetworks.totems.retrieval.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

@ApiModel("全局搜索：修改策略传递后台参数")
@Data
public class RetrievalPolicyDto {
    @ApiModelProperty("设备uuid")
    private String deviceUuid;

    @ApiModelProperty("策略集uuid")
    private String policyUuid;

    @ApiModelProperty("策略id")
    private String policyId;

    @ApiModelProperty("策略名称")
    private String policyName;

    @ApiModelProperty("源地址")
    private String srcIp;

    @ApiModelProperty("目的地址")
    private String dstIp;

    @ApiModelProperty("服务")
    private String service;
}
