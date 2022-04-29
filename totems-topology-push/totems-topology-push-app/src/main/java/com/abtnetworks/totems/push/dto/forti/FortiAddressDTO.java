package com.abtnetworks.totems.push.dto.forti;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author lifei
 * @since 2021/2/18
 **/
@Data
@ApiModel("飞塔平台创建地址对象")
public class FortiAddressDTO {

    @ApiModelProperty("地址名称")
    String name;

    @ApiModelProperty("子网")
    String[] subnet;

    @ApiModelProperty("类型 0：单个ip ,1:网段,2:域名")
    Integer type;

    @ApiModelProperty("开始ip")
    String startIp;

    @ApiModelProperty("结束ip")
    String endIp;

    @ApiModelProperty("相关接口")
    String associatedInterface;

    @ApiModelProperty("描述")
    String comment;

    @ApiModelProperty("域名")
    String fqdn;
}
