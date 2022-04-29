package com.abtnetworks.totems.recommend.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;
@Data
@ApiModel("可编辑命令行下发任务数据")
public class CommandTaskEditableEntity {
    @ApiModelProperty("主键id")
    private Integer id;

    @ApiModelProperty("任务id")
    private Integer taskId;

    @ApiModelProperty("主题")
    private String theme;

    @ApiModelProperty("用户名")
    private String userName;

    @ApiModelProperty("编辑用户名")
    private String editUserName;

    @ApiModelProperty("任务类型")
    private Integer taskType;

    @ApiModelProperty("设备UUID")
    private String deviceUuid;

    @ApiModelProperty("创建时间")
    private Date createTime;

    @ApiModelProperty("下发时间")
    private Date pushTime;

    @ApiModelProperty("修改时间")
    private Date modifiedTime;

    @ApiModelProperty("自动下发")
    private Integer autoPush;

    @ApiModelProperty("状态")
    private Integer status;

    @ApiModelProperty("下发结果")
    private String pushResult;

    @ApiModelProperty("命令行")
    private String commandline;

    @ApiModelProperty("回滚命令行")
    private String commandlineRevert;

    @ApiModelProperty("命令行下发回显")
    private String commandlineEcho;

    @ApiModelProperty("回滚命令行下发回显")
    private String commandlineRevertEcho;

    @ApiModelProperty("下发状态")
    private Integer pushStatus = 0;

    @ApiModelProperty("回滚状态")
    private Integer revertStatus = 0;

    @ApiModelProperty("下发计划")
    private Date pushSchedule;

    @ApiModelProperty("启用邮件通知")
    private String enableEmail;

    @ApiModelProperty("收件人邮箱")
    private String receiverEmail;

    @ApiModelProperty("回滚时间")
    private Date revertTime;
    @ApiModelProperty("回滚编辑用户")
    private String editRevertUserName;
    @ApiModelProperty("回滚编辑时间")
    private Date revertModifiedTime;
    @ApiModelProperty("分组")
    private String branchLevel;
    @ApiModelProperty("策略合并信息")
    private String mergeInfo;
    @ApiModelProperty("策略移动位置")
    private String movePosition;
    @ApiModelProperty("错误信息")
    private String errorMsg;
    @ApiModelProperty("匹配信息")
    private String matchMsg;

    @ApiModelProperty("验证回显")
    private String verifyEcho;
    @ApiModelProperty("地址新增值")
    private String addressAdd;
    @ApiModelProperty("地址删除值")
    private String addressDel;
}