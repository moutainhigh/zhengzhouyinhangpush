package com.abtnetworks.totems.advanced.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author lifei
 * @desc 单个设备DTO 为只配置设备uuid的高级设置配置项使用
 * @date 2021/10/28 21:27
 */
@Data
public class DeviceSingDTO {

    @ApiModelProperty("设备uuid")
    String deviceUuid;

    @ApiModelProperty("连接类型 1:长链接 2：短链接")
    String connectType;
}
