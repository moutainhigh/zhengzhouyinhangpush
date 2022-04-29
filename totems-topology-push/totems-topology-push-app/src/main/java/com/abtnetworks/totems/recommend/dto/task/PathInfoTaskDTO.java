package com.abtnetworks.totems.recommend.dto.task;

import com.abtnetworks.totems.common.dto.commandline.ServiceDTO;
import com.abtnetworks.totems.whale.policy.ro.PathAnalyzeRO;
import com.alibaba.fastjson.JSONObject;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
@ApiModel("路径信息数据对象")
public class PathInfoTaskDTO {

    @ApiModelProperty("主键id")
    private int id;

    @ApiModelProperty("路径对应任务id")
    private int taskId;

    @ApiModelProperty("主题（工单号）")
    private String theme;

    @ApiModelProperty("流水号")
    private String orderNumber;

    @ApiModelProperty("描述")
    private String description;

    @ApiModelProperty("源IP")
    private String srcIp;

    @ApiModelProperty("目的IP")
    private String dstIp;

    @ApiModelProperty("开始时间")
    private Date startTime;

    @ApiModelProperty("结束时间")
    private Date endTime;

    @ApiModelProperty("路经查询结果")
    private PathAnalyzeRO pathAnalyzeRO;

    @ApiModelProperty("生成策略数据对象")
    List<RecommendPolicyDTO> policyList;

    @ApiModelProperty("源地址子网UUID")
    String srcNodeUuid;

    @ApiModelProperty("目的地址子网uuid")
    String dstNodeUuid;

    @ApiModelProperty("源地址子网")
    String srcNodeSubnet;

    @ApiModelProperty("目的地址子网")
    String dstNodeSubnet;

    @ApiModelProperty("服务对象数据")
    List<ServiceDTO> serviceList;

    @ApiModelProperty("模拟变更场景UUID")
    String whatIfCaseUuid;

    @ApiModelProperty("设备模拟变更数据（青提使用）")
    JSONObject deviceWhatifs = new JSONObject();

    @ApiModelProperty("长链接超时时间")
    Integer idleTimeout;

    @ApiModelProperty("ip类型  0：ipv4; 1:ipv6; 2:url ")
    private Integer ipType;

    @ApiModelProperty("是否匹配到NAT")
    Boolean matchNat = false;
}
