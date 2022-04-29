package com.abtnetworks.totems.recommend.dto.recommend;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author Administrator
 * @Title:
 * @Description: 请写注释类
 * @date 2021/3/16
 */
@Api("编辑命令行参数")
@Data
public class EditCommandDTO {
    @ApiModelProperty("任务id")
    Integer taskId;
    @ApiModelProperty("设备uuid")
    String deviceUuid;
    @ApiModelProperty("命令行")
    String command;
    @ApiModelProperty("类型")
    Integer type;
}
