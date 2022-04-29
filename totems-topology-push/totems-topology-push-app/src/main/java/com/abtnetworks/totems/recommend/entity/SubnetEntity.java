package com.abtnetworks.totems.recommend.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel("子网对象数据")
public class SubnetEntity {

    @ApiModelProperty("id")
    String id;

    @ApiModelProperty("域UUID")
    String zoneUuid;

    @ApiModelProperty("子网UUID")
    String subnetUuid;

    @ApiModelProperty("ip地址")
    String ipAddress;

    @ApiModelProperty("子网掩码")
    String maskLength;

    @ApiModelProperty("接口名")
    String interfaceName;

    @ApiModelProperty("子网名称")
    String sunbetName;

    @ApiModelProperty("子网状态")
    String sunbetStatus;

    @ApiModelProperty("设备名称")
    String deviceName;

    @ApiModelProperty("子网名称")
    String subnetName;
}
