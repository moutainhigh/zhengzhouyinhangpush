package com.abtnetworks.totems.generate.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel("命令行生成结果数据")
public class CommandResultEntity {
    @ApiModelProperty("设备IP")
    String deviceIp;

    @ApiModelProperty("设备名称")
    String deviceName;

    @ApiModelProperty("生成命令行")
    String command;

    @ApiModelProperty("命令行类型，0为安全策略，1为nat策略")
    int type;
}
