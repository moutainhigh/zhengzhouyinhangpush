package com.abtnetworks.totems.push.dto.forti;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @author lifei
 * @since 2021/2/18
 **/
@Data
@ApiModel("飞塔管理平台服务创建DTO")
public class FortiServiceDTO {

    @ApiModelProperty("名称")
    String name;

    @ApiModelProperty("协议类型 tcp/udp/sctp:5   icmp:1")
    Integer protocol;

    @ApiModelProperty("类型为tcp 端口范围")
    List<String> tcpPortrange;

    @ApiModelProperty("类型为udp 端口范围")
    List<String> udpPortrange;

    @ApiModelProperty("类型为sctp 端口范围")
    List<String> sctpPortrange;
}
