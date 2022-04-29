package com.abtnetworks.totems.auto.dto;

import com.abtnetworks.totems.common.entity.NodeEntity;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;
import java.util.Set;

/**
 * @desc    自动开通任务结果  新的报告页详情DTO
 *          包含开通日志信息集合、安全策略工单状态集合、NAT策略工单状态集合、设备信息集合
 * @author liuchanghao
 * @date 2021-12-08 14:48
 */
@Data
public class AutoRecommendAllResultNewDTO {


    @ApiModelProperty("开通日志信息集合")
    private  List<AutoRecommendErrorDetailDTO> errorDetailDTOList;

    @ApiModelProperty("安全策略工单状态集合")
    private  List<AutoRecommendOrderStatusNewDTO> securityOrderStatusDTOList;

    @ApiModelProperty("NAT策略工单状态集合")
    private  List<AutoRecommendOrderStatusNewDTO> natOrderStatusDTOList;

    @ApiModelProperty("设备信息集合")
    private Set<NodeEntity> nodeEntitySet;

}
