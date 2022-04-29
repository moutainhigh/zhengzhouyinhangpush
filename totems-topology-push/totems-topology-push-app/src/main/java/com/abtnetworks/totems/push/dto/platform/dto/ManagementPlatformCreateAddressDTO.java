package com.abtnetworks.totems.push.dto.platform.dto;

import com.abtnetworks.totems.common.dto.DeviceDTO;
import com.abtnetworks.totems.common.enums.AddressPropertyEnum;
import com.abtnetworks.totems.common.enums.PolicyEnum;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @desc    管理平台创建地址对象入参
 * @author liuchanghao
 * @date 2021-02-03 10:09
 */
@Data
@ApiModel("管理平台创建地址对象")
public class ManagementPlatformCreateAddressDTO {

    @ApiModelProperty("策略类型")
    PolicyEnum type;

    @ApiModelProperty("源地址")
    String srcIp;

    @ApiModelProperty("目的地址")
    String dstIp;

    @ApiModelProperty("源地址描述")
    String srcIpSystem;

    @ApiModelProperty("目的地址描述")
    String dstIpSystem;

    @ApiModelProperty("转换后源地址所属系统")
    String postSrcIpSystem;

    @ApiModelProperty("转换后源地址所属系统")
    String postDstIpSystem;

    @ApiModelProperty("源地址转换之后的地址")
    String postSrcIp;

    @ApiModelProperty("目的地址转换之后的地址")
    String postDstIp;

    @ApiModelProperty("源域")
    String srcZone;

    @ApiModelProperty("目的域")
    String dstZone;

    @ApiModelProperty("ip类型  0：ipv4; 1:ipv6; 2:url ")
    private Integer ipType;

    @ApiModelProperty("创建地址名称")
    String addressName;

    @ApiModelProperty("接口名称")
    String itf;

    @ApiModelProperty("ip地址类型:0:单个ip,1:网段,2:域名")
    Integer ipAddreessType;

    @ApiModelProperty("单ip")
    String  subnet;

    @ApiModelProperty("域名")
    String  fqdn;

    @ApiModelProperty("设备uuid")
    String  deviceUuid;

    @ApiModelProperty("深信服与checkPoint地址标记")
    String webUrl;

    @ApiModelProperty("工单号")
    String ticket;

    @ApiModelProperty("虚拟设备名称")
    String vsysName;

    @ApiModelProperty("设备数据")
    DeviceDTO deviceDTO;

    @ApiModelProperty("地址属性枚举")
    AddressPropertyEnum addressPropertyEnum;

    @ApiModelProperty("用户名")
    String userName;

    @ApiModelProperty("用户密码")
    String password;

    @ApiModelProperty("地址组id 格式:'110002,110003,100001'")
    String addrGroupId;
}