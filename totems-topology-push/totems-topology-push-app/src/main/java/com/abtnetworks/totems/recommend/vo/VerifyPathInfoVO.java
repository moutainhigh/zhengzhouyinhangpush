package com.abtnetworks.totems.recommend.vo;

import com.abtnetworks.totems.common.dto.commandline.ServiceDTO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel("路径验证信息前端展示数据")
public class VerifyPathInfoVO {

    @ApiModelProperty("路径信息ID")
    private Integer pathInfoId;

    @ApiModelProperty("源地址")
    private String srcIp;

    @ApiModelProperty("目的地址")
    private String dstIp;

    @ApiModelProperty("服务对象")
    private ServiceDTO service;

    @ApiModelProperty("路径验证结果状态")
    private Integer pathStatus;
}
