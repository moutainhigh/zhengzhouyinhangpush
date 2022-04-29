package com.abtnetworks.totems.auto.dto;

import com.abtnetworks.totems.common.dto.commandline.ServiceDTO;
import com.abtnetworks.totems.common.entity.NodeEntity;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;
import java.util.Set;

/**
 * @desc    自动开通任务详情-工单状态DTO
 * @author liuchanghao
 * @date 2021-08-19 14:40
 */
@Data
public class AutoRecommendOrderStatusDTO {

    @ApiModelProperty("源地址集合")
    private Set<String> srcIpList;

    @ApiModelProperty("目的地址集合")
    private Set<String> dstIpList;

    @ApiModelProperty("服务")
    private Set<List<ServiceDTO>> serviceList;

    @ApiModelProperty("工单状态")
    private Integer orderStatus;

    @ApiModelProperty("防火墙信息集合")
    private Set<NodeEntity> entityList;

}
