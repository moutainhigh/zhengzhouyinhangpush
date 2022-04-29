package com.abtnetworks.totems.auto.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

/**
 * @desc
 * @author liuchanghao
 * @date 2021-06-10 15:52
 */
@Data
@ApiModel("自动开通任务实体类")
public class AutoRecommendTaskEntity {

    @ApiModelProperty("主键id")
    private Integer id;

    @ApiModelProperty("uuid")
    private String uuid;

    @ApiModelProperty("主题")
    private String theme;

    @ApiModelProperty("流水号")
    private String orderNumber;

    @ApiModelProperty("访问类型（0：内网互访；1：内网访问互联网；2：互联网访问内网） ")
    private Integer accessType;

    @ApiModelProperty("申请人")
    private String userName;

    @ApiModelProperty("申请描述")
    private String description;

    @ApiModelProperty("源IP组")
    private String srcIp;

    @ApiModelProperty("源地址所属系统")
    private String srcIpSystem;

    @ApiModelProperty("转换后源地址所属系统")
    private String postSrcIpSystem;

    @ApiModelProperty("目的IP组")
    private String dstIp;

    @ApiModelProperty("目的地址所属系统")
    private String dstIpSystem;

    @ApiModelProperty("转换后目的地址所属系统")
    private String postDstIpSystem;

    @ApiModelProperty("服务组")
    private String serviceList;

    @ApiModelProperty("创建时间")
    private Date createTime;

    @ApiModelProperty("开始时间")
    private Date startTime;

    @ApiModelProperty("结束时间")
    private Date endTime;

    @ApiModelProperty("任务状态")
    private Integer status;

    @ApiModelProperty("任务类型")
    private Integer taskType;

    @ApiModelProperty("附加信息")
    private String additionInfo;

    @ApiModelProperty("工单开始时间")
    private Date taskStart;

    @ApiModelProperty("工单结束时间")
    private Date taskEnd;

    @ApiModelProperty("批量开通任务id")
    private Integer batchId;

    @ApiModelProperty("模拟变更场景ID")
    private String whatIfCase;

    @ApiModelProperty("长链接超时时间")
    private Integer idleTimeout;

    @ApiModelProperty("关联nat信息")
    private String relevancyNat;

    @ApiModelProperty("组")
    private String branchLevel;

    @ApiModelProperty("ip类型  0：ipv4; 1:ipv6; 2:url")
    private Integer ipType;

    @ApiModelProperty("转换后源地址")
    private String postSrcIp;

    @ApiModelProperty("转换后目的地址")
    private String postDstIp;

    @ApiModelProperty("合并检查")
    private Boolean mergeCheck;

    @ApiModelProperty("策略分成允许流和禁止流两部分")
    private Boolean rangeFilter;

    @ApiModelProperty("移动到冲突前")
    private Boolean beforeConflict;

    @ApiModelProperty("申请人")
    private String applicant;

    @ApiModelProperty("申请人邮箱")
    private String applicantEmail;

    @ApiModelProperty("错误信息")
    private String errorMsg;

    @ApiModelProperty("冲突策略")
    private String conflictPolicy;

    @ApiModelProperty("创建时间-开始时间")
    private Date createStartTime;

    @ApiModelProperty("创建时间-结束时间")
    private Date createEndTime;

    @ApiModelProperty("源地址输入类型（0：IP类型；1：地址对象名称类型）")
    private Integer srcInputType;

    @ApiModelProperty("目的地址输入类型（2：IP类型；3：地址对象名称类型）")
    private Integer dstInputType;

    @ApiModelProperty("源地址对象名称")
    private String srcAddressObjectName;

    @ApiModelProperty("目的地址对象名称")
    private String dstAddressObjectName;

}