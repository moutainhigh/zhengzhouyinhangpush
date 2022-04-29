package com.abtnetworks.totems.advanced.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class DeviceDTO {
    @ApiModelProperty("设备uuid")
    String deviceUuid;

    @ApiModelProperty("设备相关策略")
    String relatedRule;

    @ApiModelProperty("天融信分组名称")
    String groupName;
    @ApiModelProperty("checkPoint网络分层名称")
    String layerName;
    @ApiModelProperty("checkPoint策略包名称")
    String policyPackage;

    @ApiModelProperty("厂商名称")
    private String vendorName;

    @ApiModelProperty("设备ip")
    String deviceIp;

    @ApiModelProperty("连接类型 1:长链接 2：短链接")
    String connectType;

    @ApiModelProperty("设备name")
    String deviceName;
}
