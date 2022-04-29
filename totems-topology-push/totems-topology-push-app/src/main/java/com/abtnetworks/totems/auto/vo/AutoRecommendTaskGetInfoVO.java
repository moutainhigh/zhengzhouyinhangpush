package com.abtnetworks.totems.auto.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @desc
 * @author liuchanghao
 * @date 2021-06-11 10:52
 */
@Data
@ApiModel("查看策略信息VO")
public class AutoRecommendTaskGetInfoVO {

    @ApiModelProperty("任务ID")
    private Integer taskId;

    @ApiModelProperty("策略类型  3：查看安全策略; 6:查看源nat策略；7:查看目的nat策略")
    private Integer taskType;


}