package com.abtnetworks.totems.recommend.dto.task;

import com.abtnetworks.totems.common.constants.PolicyConstants;
import com.abtnetworks.totems.common.entity.NodeEntity;
import com.abtnetworks.totems.common.enums.MoveSeatEnum;
import com.abtnetworks.totems.common.dto.commandline.PolicyMergeDTO;
import com.abtnetworks.totems.common.dto.commandline.ServiceDTO;
import com.abtnetworks.totems.whale.baseapi.ro.ZoneDataRO;
import com.abtnetworks.totems.whale.policy.ro.DeviceRulesRO;
import com.abtnetworks.totems.whale.policyoptimize.ro.RuleCheckResultDataRO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * @Author: wenjiachang
 * @Date: 2018/12/19 11:42
 */
@ApiModel("策略任务对象")
@Data
public class RecommendTaskPolicyDTO {

    @ApiModelProperty("主键")
    private Integer id;

    @ApiModelProperty("任务ID")
    private Integer taskId;

    @ApiModelProperty("设备UUID")
    private String deviceUuid;

    @ApiModelProperty("设备管理IP")
    private String deviceManageIp;

    @ApiModelProperty("设备品牌ID")
    private String vendorId;

    @ApiModelProperty("设备品牌名称")
    private String vendorName;

    @ApiModelProperty("设备名称")
    private String name;

    @ApiModelProperty("工单号")
    private String orderNo;

    @ApiModelProperty("描述")
    private String description;

    @ApiModelProperty("源地址")
    private String srcIp;

    @ApiModelProperty("源域")
    private String srcZone;

    @ApiModelProperty("源域数据")
    private ZoneDataRO srcZoneData;

    @ApiModelProperty("入接口")
    private String inDevIf;

    @ApiModelProperty("目的地址")
    private String dstIp;

    @ApiModelProperty("目的域")
    private String dstZone;

    @ApiModelProperty("目的域数据")
    private ZoneDataRO dstZoneData;

    @ApiModelProperty("出接口")
    private String outDevIf;

    @ApiModelProperty("入接口别名")
    private String inDevIfAlias;

    @ApiModelProperty("出接口别名")
    private String outDevIfAlias;

    @ApiModelProperty("老化时间")
    private Integer elderTime;

    @ApiModelProperty("开始时间")
    private Date startTime;

    @ApiModelProperty("结束时间")
    private Date endTime;

    @ApiModelProperty("策略集UUID")
    private String ruleListUuid;

    @ApiModelProperty("行为")
    private String action = PolicyConstants.POLICY_STR_PERMISSION_PERMIT;

    @ApiModelProperty("是否开通")
    private String enable = PolicyConstants.POLICY_STR_CAPACITY_ENABLE;

    @ApiModelProperty("设备下发命令行")
    private String command;

    @ApiModelProperty("相关策略列表")
    private List<DeviceRulesRO> deviceRulesList;

    @ApiModelProperty("可合并策略列表")
    private List<RuleCheckResultDataRO> mergeRuleList;

    @ApiModelProperty("是否创建对象,true标识创建, false不创建，直接引用")
    boolean createObjFlag = true;

    @ApiModelProperty("是否强制新建策略，true为强制，即使有合并策略，也不合并")
    boolean mustCreateFlag = true;

    //为了与策略下发的新建策略兼容，默认值为false，因为策略下发中的新建策略无法移动策略
    @ApiModelProperty("是否置顶")
    boolean topFlag = false;

    @ApiModelProperty("移动位置")
    MoveSeatEnum moveSeatEnum;

    @ApiModelProperty("交换位置的规则名或id")
    String swapRuleNameId;

    @ApiModelProperty("合并策略数据对象")
    PolicyMergeDTO mergeDTO;

    @ApiModelProperty("服务对象")
    List<ServiceDTO> serviceList;

    @ApiModelProperty("是否为虚设备")
    boolean isVsys;

    @ApiModelProperty("虚拟设备名称")
    String vsysName;

    @ApiModelProperty("设备信息")
    NodeEntity node;

    @ApiModelProperty("长链接超时时间")
    Integer idleTimeout;

    @ApiModelProperty("相关策略集名称")
    private String ruleListName;

    @ApiModelProperty("匹配到的ruleId")
    private String matchRuleId;
}
