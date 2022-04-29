package com.abtnetworks.totems.mapping.dto;

import com.abtnetworks.totems.common.entity.NodeEntity;
import com.abtnetworks.totems.mapping.enums.RuleTypeTaskEnum;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @desc    所有规则执行完后都返回此类，用于添加到仿真工单
 * @author liuchanghao
 * @date 2022-01-20 17:19
 */
@Data
@ApiModel("所有规则执行完后都返回此类，用于添加到仿真工单")
public class AutoMappingTaskResultDTO {

    @ApiModelProperty("状态")
    private Integer status;

    @ApiModelProperty("错误信息")
    private String errorMsg;

    @ApiModelProperty("nat策略的主题工单号")
    private String natTheme;

    @ApiModelProperty("nat策略的id")
    private Integer natId;

    @ApiModelProperty("静态路由的主题工单号")
    private String routeTheme;

    @ApiModelProperty("静态路由的id")
    private Integer routeId;

    @ApiModelProperty("转换后源地址")
    private String postSrcIp;

    @ApiModelProperty("转换前目的地址")
    private String preDstIp;

    @ApiModelProperty("设备IP")
    private String deviceIp;

    @ApiModelProperty("设备名称")
    private String deviceName;

    @ApiModelProperty("设备信息")
    private NodeEntity nodeEntity;

    @ApiModelProperty("当前要执行的规则任务类型,可能是降级后的任务类型")
    private RuleTypeTaskEnum ruleTypeTaskEnum;

}
