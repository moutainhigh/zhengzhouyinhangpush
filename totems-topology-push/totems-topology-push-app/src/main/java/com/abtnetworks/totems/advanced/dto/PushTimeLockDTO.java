package com.abtnetworks.totems.advanced.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

@Data
public class PushTimeLockDTO {
    @ApiModelProperty("绝对时间段集合")
    List<AbsoluteTimeDTO> absoluteTimeDTOList;

    @ApiModelProperty("周期时间")
    List<CycleTimeDTO> cycleTimeDTOList;
}
