package com.abtnetworks.totems.recommend.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

@Data
@ApiModel("策略下发任务详情数据")
public class PushTaskDetailVO {

    @ApiModelProperty("主题（工单号）")
    private String theme;

    @ApiModelProperty("用户名")
    private String userName;

    @ApiModelProperty("策略描述")
    private String description;

    @ApiModelProperty("工单备注")
    private String remarks;

    @ApiModelProperty("源地址")
    private String srcIp;

    @ApiModelProperty("源地址所属系统")
    String srcIpSystem;

    @ApiModelProperty("目的地址")
    private String dstIp;

    @ApiModelProperty("目的地址所属系统")
    String dstIpSystem;

    @ApiModelProperty("服务列表")
    private String serviceList;

    @ApiModelProperty("开始时间")
    private Date startTime;

    @ApiModelProperty("结束时间")
    private Date endTime;

    @ApiModelProperty("任务类型")
    private Integer taskType;

    @ApiModelProperty("任务状态")
    private Integer status;

    @ApiModelProperty("设备名称")
    private String deviceName;

    @ApiModelProperty("源域")
    private String srcZone;

    @ApiModelProperty("目的域")
    private String dstZone;

    @ApiModelProperty("入接口")
    private String inDevItf;

    @ApiModelProperty("出接口")
    private String outDevItf;

    @ApiModelProperty("入接口子网")
    private String entrySubnet;

    @ApiModelProperty("出接口子网")
    private String exitSubnet;

    @ApiModelProperty("长连接时间")
    private Integer idleTimeout;

    @ApiModelProperty("关联nat")
    String relevancyNat;

    @ApiModelProperty("创建时间")
    private Date createTime;

    @ApiModelProperty("行为")
    private String action;

    @ApiModelProperty("起点标签")
    private String startLabel;

    @ApiModelProperty("ip类型  0：ipv4; 1:ipv6; 2:url ")
    private Integer ipType;

    @ApiModelProperty("标签模式 or，and")
    private String  labelModel;

    @ApiModelProperty("转换后源地址所属系统")
    String postSrcIpSystem;

    @ApiModelProperty("转换后目的地址所属系统")
    String postDstIpSystem;

    @ApiModelProperty("转换后源地址")
    private String postSrcIp;

    @ApiModelProperty("转换后目的地址")
    private String postDstIp;

    @ApiModelProperty("范围过滤")
    private Boolean rangeFilter;
    @ApiModelProperty("合并检查")
    private Boolean mergeCheck;
    @ApiModelProperty("移动冲突前")
    private Boolean beforeConflict;
}
