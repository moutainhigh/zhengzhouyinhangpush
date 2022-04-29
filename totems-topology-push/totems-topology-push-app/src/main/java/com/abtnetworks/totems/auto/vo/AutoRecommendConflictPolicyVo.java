package com.abtnetworks.totems.auto.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class AutoRecommendConflictPolicyVo {
    @ApiModelProperty("主键id")
    private Long id;
    @ApiModelProperty("自动开通工单主键id")
    private Integer autoTaskId;
    @ApiModelProperty("主题")
    private String theme;
    @ApiModelProperty("命令行")
    private String commandline;

    @ApiModelProperty("设备uuid ")
    private String deviceUuid;
    @ApiModelProperty("设备名称")
    private String deviceName;
    @ApiModelProperty("设备ip")
    private String deviceIp;
    @ApiModelProperty("序号")
    private String number;
    @ApiModelProperty("策略集名称")
    private String ruleListName;
    @ApiModelProperty("策略集uuid")
    private String ruleListUuid;
    @ApiModelProperty("策略Uuid")
    private String policyUuid;
    @ApiModelProperty("策略ID")
    private String policyId;
    @ApiModelProperty("策略名称")
    private String policyName;
    @ApiModelProperty("所在行号")
    private String lineNum;
    @ApiModelProperty("源域")
    private String firstSrcDomain;
    @ApiModelProperty("源域所有")
    private String srcDomain;
    @ApiModelProperty("源ip")
    private String srcIp;
    @ApiModelProperty("排除源ip")
    private String excludeSrcIp;
    @ApiModelProperty("目的域")
    private String firstDstDomain;
    @ApiModelProperty("目的域所有")
    private String dstDomain;
    @ApiModelProperty("目的ip")
    private String dstIp;
    @ApiModelProperty("排除目的ip")
    private String excludeDstIp;
    @ApiModelProperty("目的端口")
    private String port;
    @ApiModelProperty("服务")
    private String service;
    @ApiModelProperty("协议")
    private String protocol;
    @ApiModelProperty("应用")
    private String application;
    @ApiModelProperty("源用户")
    private String srcUser;
    @ApiModelProperty("排除服务")
    private String excludeServices;
    @ApiModelProperty("时间")
    private String time;
    @ApiModelProperty("老化时间")
    private String idleTimeout;
    @ApiModelProperty("动作")
    private String action;
    @ApiModelProperty("是否生效")
    private String isAble;
    @ApiModelProperty("描述")
    private String description;
    @ApiModelProperty("策略命中日志数")
    private String policyHit;
    @ApiModelProperty("策略宽松度")
    private String policyLoose;
    @ApiModelProperty("源IP转换前")
    private String preSrcIp;
    @ApiModelProperty("源IP转换后")
    private String postSrcIp;
    @ApiModelProperty("目的IP转换前")
    private String preDstIp;
    @ApiModelProperty("目的IP转换后")
    private String postDstIp;
    @ApiModelProperty("转换前服务")
    private String preService;
    @ApiModelProperty("转换前排除服务")
    private String preExcludeServices;
    @ApiModelProperty("转换前服务")
    private String postService;
    @ApiModelProperty("NAT类型")
    private String natType;
    @ApiModelProperty("掩码")
    private String mask;
    @ApiModelProperty("下一跳")
    private String nextStep;
    @ApiModelProperty("流出网口")
    private String netDoor;
    @ApiModelProperty("度量值")
    String metric;
    @ApiModelProperty("管理距离")
    String distance;
    @ApiModelProperty("优先级")
    String priority;
    @ApiModelProperty("VRF")
    private String vrf;
    @ApiModelProperty("权重")
    private String weight;
    @ApiModelProperty("策略利用率")
    private String policyUseRate;
    @ApiModelProperty("厂商")
    private String vendor;
    @ApiModelProperty("厂商名称")
    private String vendorName;
    @ApiModelProperty("是否虚拟设备 ")
    private String isVsys;
    @ApiModelProperty("虚拟设备名称")
    private String vsysName;
    @ApiModelProperty("主墙设备uuid")
    private String rootDeviceUuid;
    @ApiModelProperty("策略备注--来源人工输入")
    private String remark;
    @ApiModelProperty("创建时间")
    String createdTime;
    @ApiModelProperty("最后一次修改时间")
    String lastModifiedTime;
    @ApiModelProperty("策略是否跳过检查")
    private Boolean skipCheck;
    @ApiModelProperty("策略日志")
    private String ruleLog;
    @ApiModelProperty("命中数，从配置文件中读取")
    private String hitCount;
    @ApiModelProperty("到期时间")
    private String expireTime;
    @ApiModelProperty("设备显示名称")
    private String deviceShowName;
    @ApiModelProperty("设备类型")
    private String deviceType;
    @ApiModelProperty("变更类型 ADDED/UPDATED/DELETED")
    private String optType;
    @ApiModelProperty("cisco acl 策略establish字段")
    private boolean establish;
    @ApiModelProperty("策略类型")
    private String policyType;
}