package com.abtnetworks.totems.recommend.dto.task;

import com.abtnetworks.totems.common.enums.RoutingEntryTypeEnum;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author lifei
 * @desc 模拟路由DTO
 * @date 2021/8/30 17:59
 */
@Data
public class WhatIfRouteDTO {

    @ApiModelProperty("策略名称")
    String name;

    @ApiModelProperty("设备UUID")
    String deviceUuid;

    @ApiModelProperty("路由类型")
    RoutingEntryTypeEnum routeType;

    @ApiModelProperty("ipv4目的地址")
    String ipv4DstIp;

    @ApiModelProperty("ipv6目的地址")
    String ipv6DstIp;

    @ApiModelProperty("掩码长度")
    Integer maskLength;

    @ApiModelProperty("路由表名称")
    String routingTable;

    @ApiModelProperty("出接口名称")
    String interfaceName;

    @ApiModelProperty("ipv4下一跳地址")
    String ip4Gateway;

    @ApiModelProperty("ipv6下一跳地址")
    String ip6Gateway;

    @ApiModelProperty("ip类型 0：ipv4  1：ipv6")
    Integer ipType;

    @ApiModelProperty("源vrf")
    String routingTableUuid;

    @ApiModelProperty("目的vrf")
    String dstRoutingTableUuid;

    @ApiModelProperty("优先级")
    Integer distance;
}
