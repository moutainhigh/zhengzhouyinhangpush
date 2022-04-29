package com.abtnetworks.totems.recommend.entity;

import com.abtnetworks.totems.disposal.dto.DisposalScenesDTO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

@Data
@ApiModel("策略下发任务补充信息")
public class PushAdditionalInfoEntity {

    @ApiModelProperty("场景UUID")
    String scenesUuid;

    @ApiModelProperty("场景详细设备信息")
    List<DisposalScenesDTO> scenesDTOList;

    @ApiModelProperty("设备UUID")
    String deviceUuid;

    @ApiModelProperty("设备源域")
    String srcZone;

    @ApiModelProperty("设备目的域")
    String dstZone;

    @ApiModelProperty("入接口")
    String inDevItf;

    @ApiModelProperty("设备出接口")
    String outDevItf;

    @ApiModelProperty("设备入接口别名")
    String inDevItfAlias;

    @ApiModelProperty("设备出接口别名")
    String outDevItfAlias;

    @ApiModelProperty("行为")
    String action;
}
