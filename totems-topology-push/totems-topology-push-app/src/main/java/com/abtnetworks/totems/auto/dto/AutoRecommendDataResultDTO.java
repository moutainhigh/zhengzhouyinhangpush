package com.abtnetworks.totems.auto.dto;

import com.abtnetworks.totems.common.entity.NodeEntity;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @desc    自动开通任务结果报告页详情DTO
 * @author liuchanghao
 * @date 2021-06-09 18:48
 */
@Data
public class AutoRecommendDataResultDTO {

    @ApiModelProperty("设备uuid")
    private String deviceUuid;

    @ApiModelProperty("设备信息")
    private NodeEntity nodeEntity;

    @ApiModelProperty("策略信息")
    private AutoRecommendResultDTO autoRecommendResultDTO;

}
