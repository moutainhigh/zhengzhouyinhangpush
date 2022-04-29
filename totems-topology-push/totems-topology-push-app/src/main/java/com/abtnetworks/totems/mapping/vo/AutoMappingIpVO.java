package com.abtnetworks.totems.mapping.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author lifei
 * @desc 地址映射ip对应关系VO
 * @date 2022/2/10 19:29
 */
@Data
public class AutoMappingIpVO {


    @ApiModelProperty("工单类型（0：源nat；1：目的nat；）")
    private Integer natType;

    @ApiModelProperty("转换前地址")
    private String preIp;

    @ApiModelProperty("转换后地址")
    private String postIp;

    @ApiModelProperty("设备uuid")
    private String deviceUuid;
}
