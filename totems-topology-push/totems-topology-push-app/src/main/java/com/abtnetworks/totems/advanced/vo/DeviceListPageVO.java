package com.abtnetworks.totems.advanced.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

@Data
public class DeviceListPageVO {
    @ApiModelProperty("设备总数")
    int total;

    @ApiModelProperty("设备列表")
    List<DeviceInfoVO> list;

    @ApiModelProperty("主备双活设备列表")
    List<List<DeviceInfoVO>> acticeStandbyList;

    @ApiModelProperty("设备列表+业务参数")
    List<DeviceInfoAndBusinessVO> deviceList;
}
