package com.abtnetworks.totems.auto.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @desc
 * @author liuchanghao
 * @date 2021-11-16 11:22
 */
@Data
@ApiModel("新建或修改生成策略域限制配置VO")
public class PushZoneLimitConfigVO {

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

    /**
     * 页数
     */
    private Integer page = 1;

    /**
     * 每页条数
     */
    private Integer limit = 20;

}