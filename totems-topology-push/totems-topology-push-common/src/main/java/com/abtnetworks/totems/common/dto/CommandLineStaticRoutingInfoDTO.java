package com.abtnetworks.totems.common.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author lb
 * @desc 静态路由信息命令行所需数据
 */
@Data
public class CommandLineStaticRoutingInfoDTO {

    @ApiModelProperty("所属虚拟路由器")
    private String srcVirtualRouter;

    @ApiModelProperty("目的虚拟路由器")
    private String dstVirtualRouter;

    @ApiModelProperty("出接口")
    private String outInterface;

    @ApiModelProperty("下一跳")
    private String nextHop;

    @ApiModelProperty("优先级")
    private String priority;

    @ApiModelProperty("子网掩码")
    private Integer subnetMask;

    @ApiModelProperty("管理距离")
    private String managementDistance;
}
