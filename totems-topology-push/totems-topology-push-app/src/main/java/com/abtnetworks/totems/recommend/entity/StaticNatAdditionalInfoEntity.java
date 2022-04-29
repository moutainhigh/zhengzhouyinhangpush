package com.abtnetworks.totems.recommend.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ApiModel("静态nat补充数据")
public class StaticNatAdditionalInfoEntity {

    @ApiModelProperty("设备UUID")
    String deviceUuid;

    @ApiModelProperty("源域")
    String fromZone;

    @ApiModelProperty("目的域")
    String toZone;

    @ApiModelProperty("入接口")
    String inDevItf;

    @ApiModelProperty("出接口")
    String outDevItf;

    @ApiModelProperty("公网地址")
    String globalAddress;

    @ApiModelProperty("内网地址")
    String insideAddress;

    @ApiModelProperty("公网端口")
    String globalPort;

    @ApiModelProperty("公网地址")
    String insidePort;

    @ApiModelProperty("协议")
    String protocol;

    @ApiModelProperty("入接口别名 ")
    private String inDevItfAlias;

    @ApiModelProperty("出接口别名 ")

    private String outDevItfAlias;
}
