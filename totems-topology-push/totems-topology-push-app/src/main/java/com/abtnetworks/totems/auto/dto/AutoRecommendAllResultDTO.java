package com.abtnetworks.totems.auto.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * @desc    自动开通任务结果报告页详情DTO，包含异常信息提示和相关策略建议、命令行等信息
 * @author liuchanghao
 * @date 2021-07-15 14:48
 */
@Data
public class AutoRecommendAllResultDTO {

    @ApiModelProperty("开通成功数据信息")
    private Map<String, AutoRecommendResultDTO> dataMap;

    @ApiModelProperty("任务状态")
    private Integer demandStatus;

    @ApiModelProperty("错误信息集合")
    private  List<AutoRecommendErrorDetailDTO> errorDetailDTOList;

    @ApiModelProperty("工单状态集合")
    private  List<AutoRecommendOrderStatusDTO> orderStatusDTOList;

    @ApiModelProperty("详情界面跳转url")
    private String detailPageUrl;

}
