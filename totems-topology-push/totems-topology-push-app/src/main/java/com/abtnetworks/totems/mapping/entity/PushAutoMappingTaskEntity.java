package com.abtnetworks.totems.mapping.entity;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

/**
 * @desc    工单检测自动匹配任务表实体
 * @author liuchanghao
 * @date 2022-01-21 10:31
 */

@Data
public class PushAutoMappingTaskEntity {

    @ApiModelProperty("主键id")
    private Integer id;

    @ApiModelProperty("UUID")
    private String uuid;

    @ApiModelProperty("申请主题（工单号）")
    private String theme;

    @ApiModelProperty("规则类型,类型可多选（0：目的Nat一对一；1：源Nat多对一；2：源Nat一对一；3：静态路由；）")
    private String ruleType;

    @ApiModelProperty("申请人")
    private String userName;

    @ApiModelProperty("源IP组")
    private String srcIp;

    @ApiModelProperty("是否指定转换后源IP，0：不指定；1：指定")
    private Integer isAppointPostSrcIp;

    @ApiModelProperty("源地址转换后IP")
    private String appointPostSrcIp;

    @ApiModelProperty("源地址所属系统")
    private String srcIpSystem;

    @ApiModelProperty("目的IP组")
    private String dstIp;

    @ApiModelProperty("目的地址所属系统")
    private String dstIpSystem;

    @ApiModelProperty("服务组")
    private String serviceList;

    @ApiModelProperty("开始时间")
    private Date startTime;

    @ApiModelProperty("结束时间")
    private Date endTime;

    @ApiModelProperty("任务状态（0：等待仿真；1：已加入仿真任务；2：异常）")
    private Integer status;

    @ApiModelProperty("任务类型")
    private Integer taskType;

    @ApiModelProperty("附加信息")
    private String additionInfo;

    @ApiModelProperty("仿真开通任务id")
    private Integer recommendTaskId;

    @ApiModelProperty("长链接超时时间")
    private Integer idleTimeout;

    @ApiModelProperty("关联nat信息")
    private String relevancyNat;

    @ApiModelProperty("关联静态路由信息")
    private String relevancyRoute;

    @ApiModelProperty("ip类型  0：ipv4; 1:ipv6; 2:url")
    private Integer ipType;

    @ApiModelProperty("转换后源地址")
    private String postSrcIp;

    @ApiModelProperty("转换后目的地址")
    private String postDstIp;

    @ApiModelProperty("合并检查")
    private Integer mergeCheck;

    @ApiModelProperty("策略分成允许流和禁止流两部分")
    private Integer rangeFilter;

    @ApiModelProperty("移动到冲突前")
    private Integer beforeConflict;

    @ApiModelProperty("错误信息")
    private String errorMsg;

    @ApiModelProperty("创建人")
    private String createUser;

    @ApiModelProperty("创建时间")
    private Date createTime;

    @ApiModelProperty("更新人")
    private String updateUser;

    @ApiModelProperty("更新时间")
    private Date updateTime;

    @ApiModelProperty("描述")
    private String description;

}