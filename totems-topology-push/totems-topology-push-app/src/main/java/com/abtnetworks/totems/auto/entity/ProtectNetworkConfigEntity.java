package com.abtnetworks.totems.auto.entity;

import com.abtnetworks.totems.disposal.BaseEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @desc
 * @author liuchanghao
 * @date 2021-06-10 15:52
 */
@Data
@ApiModel("防护网段配置实体类")
public class ProtectNetworkConfigEntity extends BaseEntity {

    @ApiModelProperty("主键id")
    private Long id;

    @ApiModelProperty("uuid")
    private String uuid;

    @ApiModelProperty("防火墙IP")
    private String deviceIp;

    @ApiModelProperty("设备uuid")
    private String deviceUuid;

    @ApiModelProperty("设备名称")
    private String deviceName;

    @ApiModelProperty("防护网段")
    private String protectNetwork;

    @ApiModelProperty("是否打开Nat映射（Y：打开；N：未打开）")
    private String natFlag;

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

    @ApiModelProperty("Nat类型（N：无Nat；S:源Nat；D:目的Nat）")
    private String natType;

    @ApiModelProperty("nat映射关系数据")
    private List<ProtectNetworkNatMappingEntity> natMappingDTOList;

    @ApiModelProperty("比较并取交集之后转换的IP,子网形式")
    private String convertIp;

    @ApiModelProperty("比较并取交集之后转换的IP,范围形式")
    private String convertRangeIp;

    @ApiModelProperty("转换前ip")
    private String outsideIp;

    @ApiModelProperty("转换后ip")
    private String insideIp;

    @ApiModelProperty("是否开启同域不开通逻辑（Y：打开；N：未打开）")
    private String sameZoneFlag;

}