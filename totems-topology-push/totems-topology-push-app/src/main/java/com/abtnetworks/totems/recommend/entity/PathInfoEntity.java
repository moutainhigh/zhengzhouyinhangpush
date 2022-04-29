package com.abtnetworks.totems.recommend.entity;

import com.abtnetworks.totems.common.constants.PolicyConstants;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel("路径信息数据")
public class PathInfoEntity {
    @ApiModelProperty("主键")
    private Integer id;

    @ApiModelProperty("任务id")
    private Integer taskId;

    @ApiModelProperty("源地址")
    private String srcIp;

    @ApiModelProperty("目的地址")
    private String dstIp;

    @ApiModelProperty("服务")
    private String service;

    @ApiModelProperty("路径验证结果状态")
    private Integer pathStatus = PolicyConstants.POLICY_INT_RECOMMEND_TASK_NO_STARTED;

    @ApiModelProperty("分析状态")
    private Integer analyzeStatus = PolicyConstants.POLICY_INT_RECOMMEND_TASK_NO_STARTED;

    @ApiModelProperty("建议状态")
    private Integer adviceStatus = PolicyConstants.POLICY_INT_RECOMMEND_TASK_NO_STARTED;

    @ApiModelProperty("检查状态")
    private Integer checkStatus = PolicyConstants.POLICY_INT_RECOMMEND_TASK_NO_STARTED;

    @ApiModelProperty("风险分析状态")
    private Integer riskStatus = PolicyConstants.POLICY_INT_RECOMMEND_TASK_NO_STARTED;

    @ApiModelProperty("命令行生成状态")
    private Integer cmdStatus = PolicyConstants.POLICY_INT_RECOMMEND_TASK_NO_STARTED;

    @ApiModelProperty("命令行下发状态")
    private Integer pushStatus = PolicyConstants.POLICY_INT_RECOMMEND_TASK_NO_STARTED;

    @ApiModelProperty("采集状态")
    private Integer gatherStatus = PolicyConstants.POLICY_INT_RECOMMEND_TASK_NO_STARTED;

    @ApiModelProperty("拓扑分析状态")
    private Integer accessAnalyzeStatus = PolicyConstants.POLICY_INT_RECOMMEND_TASK_NO_STARTED;

    @ApiModelProperty("验证状态")
    private Integer verifyStatus = PolicyConstants.POLICY_INT_RECOMMEND_TASK_NO_STARTED;

    @ApiModelProperty("源节点UUID")
    private String srcNodeUuid;

    @ApiModelProperty("目的子网UUID")
    private String dstNodeUuid;

    @ApiModelProperty("源节点子网")
    private String srcNodeDevice;

    @ApiModelProperty("目的节点子网")
    private String dstNodeDevice;

    @ApiModelProperty("是否启用路径")
    private Integer enablePath;

    @ApiModelProperty("源子网关联设备列表")
    private String srcSubnetDevices;

    @ApiModelProperty("目的子网关联设备列表")
    private String dstSubnetDevices;
}