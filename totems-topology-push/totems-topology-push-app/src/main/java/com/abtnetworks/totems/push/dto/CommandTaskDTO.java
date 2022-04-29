package com.abtnetworks.totems.push.dto;

import com.abtnetworks.totems.recommend.entity.CommandTaskEditableEntity;
import com.abtnetworks.totems.recommend.entity.CommandTaskEntity;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

@Data
@ApiModel("命令行任务对象")
public class CommandTaskDTO {

    @ApiModelProperty("任务id")
    private int taskId;

    @ApiModelProperty("主题（工单号）")
    private String theme;

    @ApiModelProperty("是否下发回滚命令行")
    private boolean revert;

    @ApiModelProperty("命令行下发任务列表")
    List<CommandTaskEditableEntity> list;
}
