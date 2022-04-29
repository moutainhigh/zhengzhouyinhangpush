package com.abtnetworks.totems.recommend.dto.task;

import com.abtnetworks.totems.common.dto.commandline.ServiceDTO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

@Data
@ApiModel("模拟变更NAT策略对象")
public class WhatIfNatDTO {

    @ApiModelProperty("策略名称")
    String name;

    @ApiModelProperty("设备UUID")
    String deviceUuid;

    @ApiModelProperty("NAT作用范围：SRC源NAT，DST目的NAT")
    String natField;

    @ApiModelProperty("Nat类型")
    String natType;

    @ApiModelProperty("源地址转换前")
    String preSrcAddress;

    @ApiModelProperty("源地址转换后")
    String postSrcAddress;

    @ApiModelProperty("目的地址转换前")
    String preDstAddress;

    @ApiModelProperty("目的地址转换后")
    String postDstAddress;

    @ApiModelProperty("入接口安全域")
    String srcZone;

    @ApiModelProperty("出接口安全域")
    String dstZone;

    @ApiModelProperty("入接口")
    String inDevItf;

    @ApiModelProperty("出接口")
    String outDevItf;

    @ApiModelProperty("申请人")
    String userName;

    @ApiModelProperty("解析后转换前服务对象")
    List<ServiceDTO> preServiceList;

    @ApiModelProperty("解析后转换后服务对象")
    List<ServiceDTO> postServiceList;

    @ApiModelProperty("IP类型，可用值有 IP4 或 IP6")
    private String ipType;

    @ApiModelProperty("源地址转换前")
    String preIp6SrcAddress;

    @ApiModelProperty("源地址转换后")
    String postIp6SrcAddress;

    @ApiModelProperty("目的地址转换前")
    String preIp6DstAddress;

    @ApiModelProperty("目的地址转换后")
    String postIp6DstAddress;
}
