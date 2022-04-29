package com.abtnetworks.totems.auto.dto;

import com.abtnetworks.totems.recommend.entity.CommandTaskEditableEntity;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * @desc 即将过期策略邮件DTO
 * @author zhoumuhua
 * @date 2021-07-08
 */
@Data
public class WillExpirePolicyEmailDTO {

    @ApiModelProperty("主题（工单号）")
    private String theme;

    @ApiModelProperty("策略ID")
    private String policyId;

    @ApiModelProperty("策略名称")
    private String policyName;

    @ApiModelProperty("源地址")
    private String srcIp;

    @ApiModelProperty("目的地址")
    private String dstIp;

    @ApiModelProperty("服务列表")
    private String serviceList;

    @ApiModelProperty("开始时间")
    private Date startTime;

    @ApiModelProperty("结束时间")
    private Date endTime;

    @ApiModelProperty("申请描述")
    private String description;

    @ApiModelProperty("申请人")
    private String applicant;

    @ApiModelProperty("申请人邮箱")
    private String applicantEmail;

    @ApiModelProperty("设备命令行list")
    private List<CommandTaskEditableEntity> taskEditableEntityList;
}
