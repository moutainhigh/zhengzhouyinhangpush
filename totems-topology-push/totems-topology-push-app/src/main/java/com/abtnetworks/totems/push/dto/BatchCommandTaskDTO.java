package com.abtnetworks.totems.push.dto;

import com.abtnetworks.totems.recommend.entity.CommandTaskEditableEntity;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @author lifei
 * @desc 批量下发命令行任务DTO
 * @date 2021/9/10 17:38
 */
@Data
public class BatchCommandTaskDTO {

    @ApiModelProperty("设备uuiid")
    String deviceUuid;

    @ApiModelProperty("设备型号")
    String modelNumber;

    @ApiModelProperty("命令行下发任务ids")
    List<Integer> taskIds;

    @ApiModelProperty("是否下发回滚命令行")
    boolean revert;

    @ApiModelProperty("命令行下发任务列表")
    List<CommandTaskEditableEntity> list;
}
