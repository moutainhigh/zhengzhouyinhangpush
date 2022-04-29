package com.abtnetworks.totems.recommend.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ApiModel("SNAT补充数据")
public class SNatAdditionalInfoEntity {

    @ApiModelProperty("设备UUID")
    String deviceUuid;

    @ApiModelProperty("转换后地址")
    String postIpAddress;

    @ApiModelProperty("域")
    String srcZone;

    @ApiModelProperty("接口")
    String srcItf;

    @ApiModelProperty("目的域")
    String dstZone;

    @ApiModelProperty("出接口")
    String dstItf;

    @ApiModelProperty("模式")
    String mode;
    @ApiModelProperty("入接口别名 ")
    private String inDevItfAlias;

    @ApiModelProperty("出接口别名 ")

    private String outDevItfAlias;
}
