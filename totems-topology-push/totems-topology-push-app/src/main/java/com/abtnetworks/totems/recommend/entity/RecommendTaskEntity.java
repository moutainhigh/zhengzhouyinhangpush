package com.abtnetworks.totems.recommend.entity;

import com.alibaba.fastjson.JSONObject;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springframework.data.annotation.Transient;

import java.util.Date;

@Data
@ApiModel("策略开通任务数据")
public class RecommendTaskEntity {
    @ApiModelProperty("主键")
    private Integer id;

    @ApiModelProperty("主题（工单号）")
    private String theme;

    @ApiModelProperty("流水号")
    private String orderNumber;

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

    @ApiModelProperty("创建时间")
    private Date createTime;

    @ApiModelProperty("开始时间")
    private Date startTime;

    @ApiModelProperty("结束时间")
    private Date endTime;

    @ApiModelProperty("任务状态:物理+云")
    private Integer status;

    @ApiModelProperty("任务类型")
    private Integer taskType;

    @ApiModelProperty("附加信息")
    private String additionInfo;

    @ApiModelProperty("任务开始时间")
    private Date taskStart;

    @ApiModelProperty("任务完成时间")
    private Date taskEnd;

    @ApiModelProperty("批量开通任务id")
    private Integer batchId;

    @ApiModelProperty("模拟变更场景Id")
    private String whatIfCase;

    @ApiModelProperty("长链接超时时间")
    Integer idleTimeout;

    @ApiModelProperty("设备IP")
    String deviceIp;

    @ApiModelProperty("结果明细")
    Integer resultDetail = 0;

    @ApiModelProperty("关联nat")
    String relevancyNat;
    @Transient
    @ApiModelProperty("设备模拟变更数据（青提使用）")
    JSONObject deviceWhatifs = new JSONObject();

    @ApiModelProperty("分组")
    String branchLevel;

    @ApiModelProperty("ip类型  0：ipv4; 1:ipv6; 2:url ")
    private Integer ipType;

    @ApiModelProperty("选择域名策略的ip类型用于生成命令行  0：ipv4; 1:ipv6; ")
    private Integer urlType;

    @ApiModelProperty("转换后源地址所属系统")
    String postSrcIpSystem;

    @ApiModelProperty("转换后目的地址所属系统")
    String postDstIpSystem;

    @ApiModelProperty("起点标签")
    private String startLabel;
    @ApiModelProperty("标签模式 or，and")
    private String  labelModel;

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

    @ApiModelProperty("路径分析状态，格式(路径分析状态code1:num,路径分析状态code2:num)")
    private String pathAnalyzeStatus;

    @ApiModelProperty("东西仿真任务ID")
    private Integer weTaskId;
    @ApiModelProperty("物理仿真任务状态")
    private Integer nsStatus;
    @ApiModelProperty("东西向任务状态")
    private Integer weStatus;

    @ApiModelProperty("飞塔dnat转换关系名称")
    private String fortinetDnatMipName;

    @ApiModelProperty("源地址对象名称")
    private String srcAddressObjectName;

    @ApiModelProperty("目的地址对象名称")
    private String dstAddressObjectName;
}