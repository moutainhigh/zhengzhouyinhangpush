package com.abtnetworks.totems.recommend.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

@Data
@ApiModel("命令行数据对象")
public class CommandVO {
    @ApiModelProperty("路径信息id")
    Integer taskId;

    @ApiModelProperty("策略下发信息id")
    Integer id;

    @ApiModelProperty("设备名称")
    String deviceName;

    @ApiModelProperty("命令行")
    String command;

    @ApiModelProperty("修改用户")
    String editUserName;

    @ApiModelProperty("设备UUID")
    String deviceUuid;

    @ApiModelProperty("修改时间")
    Date editTime;

    @ApiModelProperty("下发结果")
    String pushResult;

    @ApiModelProperty("是否能编辑")
    boolean editable;

    @ApiModelProperty("回滚命令行")
    String revert;

    @ApiModelProperty("回滚命令行回显")
    String revertEcho;

    @ApiModelProperty("回滚是否能编辑")
    boolean editableRevert;

    @ApiModelProperty("回滚编辑用户")
    private String editRevertUserName;
    @ApiModelProperty("回滚编辑时间")
    private Date revertModifiedTime;

    @ApiModelProperty("下发状态")
    private Integer pushStatus;
}
