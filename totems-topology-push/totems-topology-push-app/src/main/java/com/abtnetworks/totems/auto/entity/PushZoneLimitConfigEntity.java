package com.abtnetworks.totems.auto.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @desc
 * @author liuchanghao
 * @date 2021-11-16 11:22
 */
@Data
@ApiModel("生成策略域限制配置实体类")
public class PushZoneLimitConfigEntity {

    @ApiModelProperty("主键id")
    private Integer id;

    @ApiModelProperty("uuid")
    private String uuid;

    @ApiModelProperty("源域")
    private String srcZone;

    @ApiModelProperty("目的域")
    private String dstZone;

    @ApiModelProperty("设备信息")
    private String deviceInfo;

}