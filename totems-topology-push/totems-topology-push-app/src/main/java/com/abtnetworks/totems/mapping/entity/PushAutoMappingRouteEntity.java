package com.abtnetworks.totems.mapping.entity;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

/**
 * @desc    静态路由自动匹配表实体
 * @author liuchanghao
 * @date 2022-01-21 10:31
 */
@Data
public class PushAutoMappingRouteEntity {

    @ApiModelProperty("主键id")
    private Integer id;

    @ApiModelProperty("UUID")
    private String uuid;

    @ApiModelProperty("描述")
    private String description;

    @ApiModelProperty("目的IP组")
    private String dstIp;

    @ApiModelProperty("设备uuid")
    private String deviceUuid;

    @ApiModelProperty("设备名称")
    private String deviceName;

    @ApiModelProperty("所属虚拟路由器")
    private String srcVirtualRouter;

    @ApiModelProperty("目的虚拟路由器")
    private String dstVirtualRouter;

    @ApiModelProperty("出接口")
    private String outInterface;

    @ApiModelProperty("子网掩码")
    private Integer subnetMask;

    @ApiModelProperty("下一跳")
    private String nextHop;

    @ApiModelProperty("优先级")
    private String priority;

    @ApiModelProperty("管理距离")
    private String managementDistance;

    @ApiModelProperty("创建人")
    private String createUser;

    @ApiModelProperty("创建时间")
    private Date createTime;

    @ApiModelProperty("更新人")
    private String updateUser;

    @ApiModelProperty("更新时间")
    private Date updateTime;
}