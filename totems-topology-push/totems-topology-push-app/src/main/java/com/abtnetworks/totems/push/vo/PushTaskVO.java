package com.abtnetworks.totems.push.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.annotations.ApiParam;
import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * 策略开通任务前段展示数据结构
 */
@Data
@ApiModel("策略下发前端显示对象")
public class PushTaskVO {
    @ApiModelProperty("任务id")
    private Integer taskId;

    @ApiModelProperty("主题（工单号）")
    private String orderNo;

    @ApiModelProperty("工单类型")
    private int orderType;

    @ApiModelProperty("下发时间")
    private Date pushTime;

    @ApiModelProperty("创建时间")
    private Date createTime;

    @ApiModelProperty("状态")
    private int status;

    @ApiModelProperty("用户名称")
    private String userName;

    @ApiModelProperty("下发状态")
    private int pushStatus;

    @ApiModelProperty("回滚状态")
    private int revertStatus;

    @ApiModelProperty("下发计划")
    private Date pushSchedule;

    @ApiModelProperty("启用邮件通知")
    private Boolean enableEmail;

    @ApiModelProperty("收件人邮箱")
    private String receiverEmail;

    @ApiModelProperty("回滚时间")
    private Date revertTime;
}
