package com.abtnetworks.totems.recommend.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@ApiModel("Nat策略补充数据")
@AllArgsConstructor
@NoArgsConstructor
public class NatAdditionalInfoEntity {

    @ApiModelProperty("NAT类型：保留后续使用")
    Integer type;

    @ApiModelProperty("转换后源地址")
    String postSrcIp;

    @ApiModelProperty("转换后目的地址")
    String postDstIp;

    @ApiModelProperty("转换后端口")
    String postPort;

    @ApiModelProperty("设备UUID")
    String deviceUuid;

    @ApiModelProperty("源域")
    String srcZone;

    @ApiModelProperty("目的域")
    String dstZone;

    @ApiModelProperty("出接口")
    String dstItf;

    @ApiModelProperty("入接口")
    String srcItf;

    @ApiModelProperty("默认static，勾选后为dynamic")
    boolean dynamic = false;

    @ApiModelProperty("入接口别名 ")
    private String inDevItfAlias;

    @ApiModelProperty("出接口别名 ")

    private String outDevItfAlias;
}
