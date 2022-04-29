package com.abtnetworks.totems.auto.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @desc    新增自动开通任务DTO
 * @author liuchanghao
 * @date 2021-06-09 18:48
 */
@Data
public class AutoRecommendNatMappingDTO {

    @ApiModelProperty("Nat类型（N：无Nat；S:源Nat；D:目的Nat）")
    private String natType;

    @ApiModelProperty("内网ip")
    private String insideIp;

    @ApiModelProperty("内网协议")
    private String insideProtocol;

    @ApiModelProperty("内网端口")
    private String insidePorts;

    @ApiModelProperty("外网IP")
    private String outsideIp;

    @ApiModelProperty("外网协议")
    private String outsideProtocol;

    @ApiModelProperty("外网端口")
    private String outsidePorts;

}
