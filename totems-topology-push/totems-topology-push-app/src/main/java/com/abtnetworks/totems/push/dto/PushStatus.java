package com.abtnetworks.totems.push.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel("下发状态")
public class PushStatus {

    @ApiModelProperty("下发任务数量")
    Integer count;

    @ApiModelProperty("下发任务类型")
    Integer taskType;

    @ApiModelProperty("下发状态")
    Integer pushStatus;

    @ApiModelProperty("回滚状态")
    Integer revertStatus;
}
