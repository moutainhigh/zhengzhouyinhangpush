package com.abtnetworks.totems.auto.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Set;

/**
 * @desc    新的自动开通任务详情-工单状态和工单信息DTO
 * @author liuchanghao
 * @date 2021-12-08 14:40
 */
@Data
public class AutoRecommendOrderStatusNewDTO {

    @ApiModelProperty("工单信息集合")
    private Set<AutoRecommendOrderDTO> orderDTOSet;

    @ApiModelProperty("工单状态")
    private Integer orderStatus;

}
