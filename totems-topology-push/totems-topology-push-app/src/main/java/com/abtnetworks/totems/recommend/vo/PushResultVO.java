package com.abtnetworks.totems.recommend.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel("命令行下发数据结构")
public class PushResultVO {

    @ApiModelProperty("任务id")
    Integer taskId;

    @ApiModelProperty("设备名称")
    String deviceName;

    @ApiModelProperty("下发结果")
    String pushResult;
}
