package com.abtnetworks.totems.recommend.entity;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel("设备详情数据")
public class PathDeviceDetailEntity {
    @ApiModelProperty("主键id")
    private Integer id;

    @ApiModelProperty("路径信息id")
    private Integer pathInfoId;

    @ApiModelProperty("是否验证数据")
    private Integer isVerifyData;

    @ApiModelProperty("设备UUID")
    private String deviceUuid;

    @ApiModelProperty("设备详情数据")
    private String deviceDetail;

    @ApiModelProperty("所在路径序号")
    private Integer pathIndex;
}