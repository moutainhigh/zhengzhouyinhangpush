package com.abtnetworks.totems.push.dto.platform.dto;

import com.abtnetworks.totems.common.dto.commandline.ServiceDTO;
import com.abtnetworks.totems.common.enums.ActionEnum;
import com.abtnetworks.totems.common.enums.MoveSeatEnum;
import com.abtnetworks.totems.common.enums.PolicyEnum;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @desc    管理平台生成策略入参
 * @author liuchanghao
 * @date 2021-02-03 10:09
 */
@Data
@ApiModel("管理平台创建策略")
public class ManagementPlatformCreatePolicyDTO {

    @ApiModelProperty("策略类型")
    PolicyEnum type;

    @ApiModelProperty("源地址")
    String srcIp;

    @ApiModelProperty("转换后源地址")
    String postSrcIp;

    @ApiModelProperty("目的地址")
    String dstIp;

    @ApiModelProperty("转换后目的地址")
    String postDstIp;

    @ApiModelProperty("转换后目的端口")
    String postPort;

    @ApiModelProperty("服务列表")
    List<ServiceDTO> serviceList;

    @ApiModelProperty("转换后服务列表")
    List<ServiceDTO> postServiceList;

    @ApiModelProperty("源域")
    String srcZone;

    @ApiModelProperty("目的域")
    String dstZone;

    @ApiModelProperty("入接口")
    String srcItf;

    @ApiModelProperty("出接口")
    String dstItf;

    @ApiModelProperty("入接口别名")
    String srcItfAlias;

    @ApiModelProperty("出接口别名")
    String dstItfAlias;

    @ApiModelProperty("开始时间")
    String startTime;

    @ApiModelProperty("结束时间")
    String endTime;

    @ApiModelProperty("描述")
    String description;

    @ApiModelProperty("行为")
    ActionEnum action;

    @ApiModelProperty("长连接")
    Integer idleTimeout;

    @ApiModelProperty("源地址描述")
    String srcIpSystem;

    @ApiModelProperty("目的地址描述")
    String dstIpSystem;

    @ApiModelProperty("转换后源地址所属系统")
    String postSrcIpSystem;

    @ApiModelProperty("转换后源地址所属系统")
    String postDstIpSystem;

    @ApiModelProperty("ip类型  0：ipv4; 1:ipv6; 2:url ")
    private Integer ipType;

    @ApiModelProperty("设备Uuid")
    String deviceUuid;

    @ApiModelProperty("主机名称")
    String hostName;

    @ApiModelProperty("源地址名称")
    List<String> srcaddrs;

    @ApiModelProperty("目的地址名称")
    List<String> dstaddrs;

    @ApiModelProperty("源地址转换之后的名称")
    List<String> postSrcaddrs;

    @ApiModelProperty("目的地址转换之后的名称")
    List<String> postDstaddrs;

    @ApiModelProperty("服务对象名称")
    List<String> serviceNames;

    @ApiModelProperty("时间对象名称")
    List<String> scheduleNames;

    @ApiModelProperty("策略id")
    String policyId;

    @ApiModelProperty("深信服与checkPoint地址标记")
    String webUrl;

    @ApiModelProperty("工单号")
    String ticket;

    @ApiModelProperty("虚拟设备名称")
    String vsysName;

    @ApiModelProperty("策略包名称")
    String packageName;

    @ApiModelProperty("目标策略id")
    String targetPolicyId;

    @ApiModelProperty("移动位置")
    MoveSeatEnum moveSeatEnum;

    @ApiModelProperty("用户名")
    String userName;

    @ApiModelProperty("用户密码")
    String password;
}