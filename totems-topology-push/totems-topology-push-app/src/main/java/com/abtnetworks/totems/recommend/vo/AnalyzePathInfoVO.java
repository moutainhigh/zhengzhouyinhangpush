package com.abtnetworks.totems.recommend.vo;

import com.abtnetworks.totems.common.dto.commandline.ServiceDTO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel("路径分析信息前端展示数据")
public class AnalyzePathInfoVO {

    @ApiModelProperty("路径信息ID")
    private Integer pathInfoId;

    @ApiModelProperty("源地址")
    private String srcIp;

    @ApiModelProperty("目的地址")
    private String dstIp;

    @ApiModelProperty("服务对象")
    private ServiceDTO service;

    @ApiModelProperty("路径状态")
    private Integer analyzeStatus;

    @ApiModelProperty("策略检查状态")
    private Integer checkStatus;

    @ApiModelProperty("风险分析状态")
    private Integer riskStatus;
}
