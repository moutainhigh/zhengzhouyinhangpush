package com.abtnetworks.totems.translation.vo;

import io.swagger.annotations.ApiModelProperty;

/**
 * @Author: WangCan
 * @Description
 * @Date: 2021/8/19
 */
public class DeviceZoneInterfaceVO {

    @ApiModelProperty("域名/接口名")
    private String name;

    @ApiModelProperty("设备uuid")
    private String deviceUuid;

    @ApiModelProperty("域uuid")
    private String zoneUuid;

    @ApiModelProperty("类型 1:域  2:接口")
    private Integer type;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDeviceUuid() {
        return deviceUuid;
    }

    public void setDeviceUuid(String deviceUuid) {
        this.deviceUuid = deviceUuid;
    }

    public String getZoneUuid() {
        return zoneUuid;
    }

    public void setZoneUuid(String zoneUuid) {
        this.zoneUuid = zoneUuid;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }
}
