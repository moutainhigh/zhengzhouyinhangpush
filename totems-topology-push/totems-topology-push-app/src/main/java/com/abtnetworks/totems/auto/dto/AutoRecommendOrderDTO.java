package com.abtnetworks.totems.auto.dto;

import com.abtnetworks.totems.common.dto.commandline.ServiceDTO;
import com.abtnetworks.totems.common.entity.NodeEntity;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @desc    自动开通任务详情-最小粒度的工单状态DTO
 * @author liuchanghao
 * @date 2021-08-19 14:40
 */
@Data
public class AutoRecommendOrderDTO {

    @ApiModelProperty("源地址")
    private String srcIp;

    @ApiModelProperty("目的地址")
    private String dstIp;

    @ApiModelProperty("服务")
    private List<ServiceDTO> serviceList;

    @ApiModelProperty("防火墙信息")
    private NodeEntity entity;

}
