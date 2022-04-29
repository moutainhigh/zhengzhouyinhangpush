package com.abtnetworks.totems.advanced.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class CycleTimeDTO {
    @ApiModelProperty("周期类型：0每周，1每天")
    private Integer cycleType;
    @ApiModelProperty("周几 逗号分割 如 1,2,5")
    private String week;
    @ApiModelProperty("开始小时")
    private Integer startHour;
    @ApiModelProperty("开始分钟")
    private Integer startMinute;
    @ApiModelProperty("结束小时")
    private Integer endHour;
    @ApiModelProperty("结束分钟")
    private Integer endMinute;
}
