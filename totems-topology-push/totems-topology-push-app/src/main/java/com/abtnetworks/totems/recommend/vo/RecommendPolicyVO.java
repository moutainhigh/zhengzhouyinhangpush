package com.abtnetworks.totems.recommend.vo;

import com.abtnetworks.totems.common.dto.commandline.ServiceDTO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
@ApiModel("策略开通生成策略前端显示结果")
public class RecommendPolicyVO {

    @ApiModelProperty("设备名称")
    private String name;

    @ApiModelProperty("设备UUID")
    private String deviceUuid;

    @ApiModelProperty("设备建议生成策略列表")
    List<PolicyVO> list;
}
