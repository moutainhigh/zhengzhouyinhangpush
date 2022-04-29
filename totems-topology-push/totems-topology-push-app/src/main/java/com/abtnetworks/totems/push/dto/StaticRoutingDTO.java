package com.abtnetworks.totems.push.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class StaticRoutingDTO {

    /**
     *所属虚拟路由器
     */
    private String srcVirtualRouter;

    /**
     *目的虚拟路由器
     */
    private String dstVirtualRouter;

    /**
     *出接口
     */
    private String outInterface;

    /**
     *子网掩码
     */
    private Integer subnetMask;

    /**
     *下一跳
     */
    private String nextHop;

    /**
     *优先级
     */
    private String priority;

    /**
     *管理距离
     */
    private String managementDistance;
}
