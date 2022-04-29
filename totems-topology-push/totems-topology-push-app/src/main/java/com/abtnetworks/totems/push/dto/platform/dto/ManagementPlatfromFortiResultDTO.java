package com.abtnetworks.totems.push.dto.platform.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author lifei
 * @since 2021/2/8
 **/
@Data
@ApiModel("飞塔管理平台公共接口返回对象")
public class ManagementPlatfromFortiResultDTO {

    @ApiModelProperty("请求返回的对象名称")
    String methodName;

    @ApiModelProperty("请求编码返回")
    String code;

    @ApiModelProperty("请求的消息描述返回")
    String message;

    @ApiModelProperty("请求返回的session")
    String session;
}
