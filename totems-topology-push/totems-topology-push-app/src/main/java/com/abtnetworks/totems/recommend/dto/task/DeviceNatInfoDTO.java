package com.abtnetworks.totems.recommend.dto.task;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

@ApiModel("设备NAT对象数据")
@Data
public class DeviceNatInfoDTO {

    @ApiModelProperty("设备UUID")
    public String deviceUuid;

    @ApiModelProperty("是否带有相关NAT数据")
    private boolean natDevice = false;

    //使用list是为了适配有双向NAT的设备
    @ApiModelProperty("NAT信息列表")
    private List<NatInfoDTO> natList;
}
