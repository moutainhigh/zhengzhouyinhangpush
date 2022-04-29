package com.abtnetworks.totems.push.entity;


import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @desc    绿盟防火墙API下发响应数据
 * @author lifei
 * @date 2021-03-12 15:06
 */
@Data
public class PushNsfocusApiResponseEntity {

    @ApiModelProperty("源地址对象id")
    private String srcIdList;

    @ApiModelProperty("目的地址对象id")
    private String dstIdList;

    @ApiModelProperty("服务对象id")
    private String serviceNames;

    @ApiModelProperty("源域")
    private String srcZone;

    @ApiModelProperty("目的域")
    private String dstZone;

    @ApiModelProperty("时间id")
    private String time;


}
