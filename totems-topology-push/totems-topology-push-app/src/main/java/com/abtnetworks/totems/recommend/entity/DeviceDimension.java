package com.abtnetworks.totems.recommend.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel("合并策略的设备维度参数")
public class DeviceDimension {

    @ApiModelProperty("设备UUID")
    String deviceUuid;

    @ApiModelProperty("源域")
    String srcZone;

    @ApiModelProperty("目的域")
    String dstZone;

    @ApiModelProperty("入接口")
    String inDevItf;

    @ApiModelProperty("出接口")
    String outDevItf;
}
