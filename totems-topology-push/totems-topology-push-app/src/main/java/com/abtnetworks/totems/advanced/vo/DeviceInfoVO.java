package com.abtnetworks.totems.advanced.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class DeviceInfoVO {
    @ApiModelProperty("设备名称")
    String name;

    @ApiModelProperty("管理IP")
    String manageIp;

    @ApiModelProperty("设备UUID")
    String uuid;

    @ApiModelProperty("设备品牌")
    String vendorName;

    @ApiModelProperty("设备相关策略")
    String relatedRule;

    @ApiModelProperty("天融信分组名称")
    String groupName;

    @ApiModelProperty("checkPoint网络分层名称")
    String layerName;
    @ApiModelProperty("checkPoint策略包名称")
    String policyPackage;

    @ApiModelProperty("连接类型 1:长链接 2：短链接")
    String connectType;

    @ApiModelProperty("是否是虚设备 true:是 false:否")
    Boolean isVsys;

    @ApiModelProperty("虚设备名称")
    String vsysName;

    @ApiModelProperty("设备对应的下发py文件名称")
    String fileName;
}
