package com.abtnetworks.totems.advanced.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

@Data
public class DeviceListDTO {

    @ApiModelProperty("设备列表")
    List<String> devices;
}
