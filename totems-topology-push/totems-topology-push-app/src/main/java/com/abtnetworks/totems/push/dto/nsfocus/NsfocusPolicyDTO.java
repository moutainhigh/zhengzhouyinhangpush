package com.abtnetworks.totems.push.dto.nsfocus;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author lifei
 * @since 2021/3/10
 **/
@ApiModel("绿盟策略对象DTO")
@Data
public class NsfocusPolicyDTO {

    @ApiModelProperty("服务id")
    String service;

    @ApiModelProperty("源地址id,可为多个eg:'100001,100002'")
    String srcObject;

    @ApiModelProperty("目的地址id,可为多个")
    String dstObject;

    @ApiModelProperty("源域")
    String srcZone;

    @ApiModelProperty("目的域")
    String dstZone;

    @ApiModelProperty("策略名称")
    String name;

    @ApiModelProperty("时间对象id")
    String time;

    @ApiModelProperty("动作:accept 允许,drop 拒绝")
    String action;

    @ApiModelProperty("老化时间,即会话超时时间")
    String sessionTimeout;
}
