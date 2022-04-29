package com.abtnetworks.totems.common.dto;

import com.abtnetworks.totems.common.enums.TaskTypeEnum;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel("任务对象相关数据")
public class TaskDTO {

    @ApiModelProperty("命令行下发任务主键id")
    private Integer id;

    @ApiModelProperty("任务id")
    private Integer taskId;

    @ApiModelProperty("主题")
    private String theme;

    @ApiModelProperty("用户名")
    private String userName;

    @ApiModelProperty("任务类型枚举")
    private TaskTypeEnum taskTypeEnum;

    @ApiModelProperty("范围过滤")
    private Boolean rangeFilter;
    @ApiModelProperty("合并检查")
    private Boolean mergeCheck;
    @ApiModelProperty("移动冲突前")
    private Boolean beforeConflict;

    @ApiModelProperty("任务类型")
    private Integer taskType;
}
