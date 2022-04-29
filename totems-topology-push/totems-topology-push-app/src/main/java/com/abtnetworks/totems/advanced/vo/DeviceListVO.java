package com.abtnetworks.totems.advanced.vo;


import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * 设备分组列表信息
 */
@Data
public class DeviceListVO {
    @ApiModelProperty("设备总数")
    int total;

    @ApiModelProperty("设备数据列表")
    List<DeviceInfoVO> data;
}
