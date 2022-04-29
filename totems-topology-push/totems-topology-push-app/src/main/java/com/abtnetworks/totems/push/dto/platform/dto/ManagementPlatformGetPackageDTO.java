package com.abtnetworks.totems.push.dto.platform.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author lifei
 * @since 2021/2/23
 **/
@ApiModel("管理平台获取包名称对象")
@Data
public class ManagementPlatformGetPackageDTO {

    @ApiModelProperty("设备名称")
    String deviceName;

    @ApiModelProperty("设备uuid")
    String deviceUuid;

    @ApiModelProperty("深信服与checkPoint地址标记")
    String webUrl;

    @ApiModelProperty("虚拟设备名称")
    String vsysName;
}
