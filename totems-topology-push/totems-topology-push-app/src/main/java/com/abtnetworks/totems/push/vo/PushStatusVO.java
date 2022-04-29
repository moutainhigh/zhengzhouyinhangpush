package com.abtnetworks.totems.push.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel("下发任务状态对象")
public class PushStatusVO {

    @ApiModelProperty("任务类型")
    Integer type;

    @ApiModelProperty("下发任务类型")
    String name;

    @ApiModelProperty("下发未开始")
    Integer notStart = 0;

    @ApiModelProperty("下发已完成")
    Integer finished = 0;

    @ApiModelProperty("下发失败")
    Integer failed = 0;

    @ApiModelProperty("下发停止")
    Integer reverted = 0;

    @ApiModelProperty("下发等待中")
    Integer revertFailed = 0;

    @ApiModelProperty("总数")
    Integer total = 0;
}
