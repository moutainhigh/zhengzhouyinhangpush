package com.abtnetworks.totems.advanced.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class AbsoluteTimeDTO {
    @ApiModelProperty("开始时间-时间戳格式")
    private Long startTime;
    @ApiModelProperty("结束时间-时间戳格式")
    private Long endTime;
}
