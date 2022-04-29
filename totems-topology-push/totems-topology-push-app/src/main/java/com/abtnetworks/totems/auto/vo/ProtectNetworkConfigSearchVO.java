package com.abtnetworks.totems.auto.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @desc
 * @author liuchanghao
 * @date 2021-06-11 10:52
 */
@Data
@ApiModel("防护网段配置查询VO")
public class ProtectNetworkConfigSearchVO{

    @ApiModelProperty("查询IP")
    private String ip;

    @ApiModelProperty("IPV4防护网段IP起始数据")
    private Long ipv4Start;

    @ApiModelProperty("IPV4防护网段IP终止数据")
    private Long ipv4End;

    @ApiModelProperty("IPV6防护网段IP起始数据")
    private String ipv6Start;

    @ApiModelProperty("IPV6防护网段IP终止数据")
    private String ipv6End;

    @ApiModelProperty("ip类型  0：ipv4; 1:ipv6; 2:url")
    private Integer ipType;

    @ApiModelProperty("防火墙IP")
    private String deviceIp;

    @ApiModelProperty("设备名称")
    private String deviceName;

    @ApiModelProperty("Nat类型（S:源Nat；D:目的Nat）")
    private String natType;

    @ApiModelProperty("转换前ip")
    private String preIp;

    @ApiModelProperty("转换后ip")
    private String postIp;

    /**
     * 页数
     */
    private Integer page = 1;

    /**
     * 每页条数
     */
    private Integer limit = 20;

}