package com.abtnetworks.totems.common.dto;

import com.abtnetworks.totems.common.entity.NodeEntity;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class GeneratedDeviceDTO {

    @ApiModelProperty("虚设备信息DTO")
    private DeviceDTO disasterRecoveryDTO;

    @ApiModelProperty("设备节点信息push")
    private NodeEntity nodeEntity;
}
