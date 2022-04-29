package com.abtnetworks.totems.recommend.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

@Data
@ApiModel("命令行下发任务数据")
public class CommandTaskEntity {
    @ApiModelProperty("主键id")
    private Integer id;

    @ApiModelProperty("相关任务id")
    private Integer taskId;

    @ApiModelProperty("相关路径id")
    private Integer pathInfoId;

    @ApiModelProperty("策略id")
    private Integer policyId;

    @ApiModelProperty("主题（工单号）")
    private String theme;

    @ApiModelProperty("用户名称")
    private String userName;

    @ApiModelProperty("任务类型")
    private Integer taskType;

    @ApiModelProperty("设备UUID")
    private String deviceUuid;

    @ApiModelProperty("创建时间")
    private Date createTime;

    @ApiModelProperty("下发时间")
    private Date pushTime;

    @ApiModelProperty("任务状态")
    private Integer status;

    @ApiModelProperty("生成命令行")
    private String commandline;

    @ApiModelProperty("回滚命令行")
    private String commandlineRevert;

    @ApiModelProperty("命令行下发结果")
    private String pushResult;

    @ApiModelProperty("匹配信息")
    private String matchMsg;
}