package com.abtnetworks.totems.recommend.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel("前端状态显示数据对象")
public class TaskStatusVO {

    @ApiModelProperty("分析状态")
    private Integer analyzeStatus;

    @ApiModelProperty("建议状态")
    private Integer adviceStatus;

    @ApiModelProperty("检查状态")
    private Integer checkStatus;

    @ApiModelProperty("风险分析状态")
    private Integer riskStatus;

    @ApiModelProperty("命令行生成状态")
    private Integer cmdStatus;

    @ApiModelProperty("命令行下发状态")
    private Integer pushStatus;

    @ApiModelProperty("采集状态")
    private Integer gatherStatus;

    @ApiModelProperty("拓扑分析状态")
    private Integer accessAnalyzeStatus;

    @ApiModelProperty("验证状态")
    private Integer verifyStatus;
}
