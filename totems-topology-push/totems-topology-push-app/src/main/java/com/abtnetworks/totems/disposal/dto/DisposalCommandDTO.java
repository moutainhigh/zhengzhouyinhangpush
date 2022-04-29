package com.abtnetworks.totems.disposal.dto;

import com.abtnetworks.totems.disposal.BaseDto;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author zc
 * @date 2019/11/14
 */
@Data
public class DisposalCommandDTO {

    @ApiModelProperty("设备uuid")
    private String deviceUuid;

    @ApiModelProperty("新建命令行")
    private String commandLine;

    @ApiModelProperty("删除命令行")
    private String deleteCommandLine;

    @ApiModelProperty("是否为虚设备")
    private Boolean isVsys;

    @ApiModelProperty("主设备uuid")
    private String rootDeviceUuid;

    @ApiModelProperty("源ip")
    private String srcIp;

    @ApiModelProperty("目的ip")
    private String dstIp;

    @ApiModelProperty("服务")
    private String serviceList;

    @ApiModelProperty("黑洞路由ip")
    private String routingIp;

    @ApiModelProperty("设备型号")
    private String modelNumber;
    @ApiModelProperty("虚墙")
    private String vsysName;
    @ApiModelProperty("checkpoint中需使用的设备集群名")
    private String cpmiGatewayClusterName;

}
