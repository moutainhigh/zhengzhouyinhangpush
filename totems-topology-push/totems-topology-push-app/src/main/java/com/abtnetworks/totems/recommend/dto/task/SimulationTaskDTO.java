package com.abtnetworks.totems.recommend.dto.task;

import com.abtnetworks.totems.common.dto.commandline.ServiceDTO;
import com.alibaba.fastjson.JSONObject;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;
import java.util.List;
import java.util.Map;

@Data
@ApiModel("策略仿真任务数据DTO")
public class SimulationTaskDTO {
    @ApiModelProperty("任务id")
    private Integer id;

    @ApiModelProperty("主题（工单号）")
    private String theme;

    @ApiModelProperty("用户名称")
    private String userName;

    @ApiModelProperty("流水号")
    private String orderNumber;

    @ApiModelProperty("描述")
    private String description;

    @ApiModelProperty("源IP")
    private String srcIp;

    @ApiModelProperty("源IP")
    private String dstIp;

    @ApiModelProperty("服务")
    private List<ServiceDTO> serviceList;

    @ApiModelProperty("开始时间")
    private Date startTime;

    @ApiModelProperty("结束时间")
    private Date endTime;

    @ApiModelProperty("任务类型")
    private int taskType;

    @ApiModelProperty("入口子网")
    String srcSubnet;

    @ApiModelProperty("出口子网")
    String dstSubnet;

    @ApiModelProperty("模拟变更场景UUID")
    String whatIfCaseUuid;

    @ApiModelProperty("设备模拟变更数据（青提使用）")
    JSONObject deviceWhatifs = new JSONObject();

    @ApiModelProperty("需要合并的策略对象列表")
    Map<String, List<RecommendPolicyDTO>> DevicePolicyMap;

    @ApiModelProperty("需生成命令行策略列表")
    List<RecommendPolicyDTO> policyList;

    @ApiModelProperty("长链接超时时间")
    Integer idleTimeout;

    @ApiModelProperty("源地址描述")
    String srcIpSystem;

    @ApiModelProperty("目的地址描述")
    String dstIpSystem;

    @ApiModelProperty("ip类型  0：ipv4; 1:ipv6; 2:url ")
    private Integer ipType;

    @ApiModelProperty("起点标签")
    private String startLabel;
    @ApiModelProperty("标签模式 or，and")
    private String  labelModel;

    @ApiModelProperty("目的地址域名对应解析IP地址,key为解析地址，value为域名")
    private Map<String, String> domainConvertIp;

    @ApiModelProperty("路径分析状态，格式(路径分析状态code1:num,路径分析状态code2:num)")
    private String pathAnalyzeStatus;

    @ApiModelProperty("合并检查")
    private Boolean mergeCheck;
    @ApiModelProperty("移动冲突前")
    private Boolean beforeConflict;

    @ApiModelProperty("关联nat")
    String relevancyNat;

    @ApiModelProperty("是否匹配到NAT")
    Boolean matchNat = false;
}
