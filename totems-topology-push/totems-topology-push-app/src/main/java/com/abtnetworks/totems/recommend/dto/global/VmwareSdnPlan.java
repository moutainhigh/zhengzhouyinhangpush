package com.abtnetworks.totems.recommend.dto.global;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class VmwareSdnPlan implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("规划id")
    private Integer id;

    @ApiModelProperty("东西向业务id")
    private Integer taskId;

    @ApiModelProperty("规划名称")
    private String planName;

    @ApiModelProperty("源地址")
    private String srcIp;

    @ApiModelProperty("源虚机对象")
    private String srcVirtualMachine;

    @ApiModelProperty("目的地址")
    private String dstIp;

    @ApiModelProperty("目的虚机对象")
    private String dstVirtualMachine;

    @ApiModelProperty("服务")
    private String service;

    @ApiModelProperty("状态")
    private Integer status;

    @ApiModelProperty("动作")
    private String action;

    @ApiModelProperty("未匹配到源虚机的IP")
    private String srcLossIps;
    @ApiModelProperty("未匹配到目的虚机的IP")
    private String dstLossIps;
    @ApiModelProperty("是否是大网段 0：否 1:是 ")
    private Integer isIpSegment;
    @ApiModelProperty("备注")
    private String remark;

    @ApiModelProperty("服务集合")
    private List<PlanServiceVO> serviceList;

}

