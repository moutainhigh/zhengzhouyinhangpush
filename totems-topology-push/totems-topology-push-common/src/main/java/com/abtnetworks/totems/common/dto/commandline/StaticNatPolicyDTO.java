package com.abtnetworks.totems.common.dto.commandline;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel("静态NAT数据类")
public class StaticNatPolicyDTO {

    @ApiModelProperty("主题（工单号）")
    private String theme;

    @ApiModelProperty("设备UUID")
    private String deviceUuid;

    @ApiModelProperty("策略名称")
    private String policyName;

    @ApiModelProperty("源域")
    private String srcZone;

    @ApiModelProperty("目的域")
    private String dstZone;

    @ApiModelProperty("源接口")
    private String inDevItf;

    @ApiModelProperty("目的接口")
    private String outDevItf;

    @ApiModelProperty("转换前地址")
    private String preIpAddress;

    @ApiModelProperty("转换后地址")
    private String postIpAddress;

    @ApiModelProperty("转换前端口")
    private String prePort;

    @ApiModelProperty("转换后端口")
    private String postPort;

    @ApiModelProperty("指定协议")
    private String protocol;

    @ApiModelProperty("入接口别名 ")
    private String inDevItfAlias;

    @ApiModelProperty("出接口别名 ")

    private String outDevItfAlias;

    @ApiModelProperty("ip类型  0：ipv4; 1:ipv6; 2:url ")
    private Integer ipType;

    @ApiModelProperty("工单备注")
    String remarks;

    @ApiModelProperty("策略描述")
    private String description;
}
