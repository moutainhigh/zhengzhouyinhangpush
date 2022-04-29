package com.abtnetworks.totems.push.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel("policy调用pushCmdDTO需要参数")
public class PushParamDTO {

    @ApiModelProperty("下发随机生成key")
    private String pushKey;

    @ApiModelProperty("deviceUuid")
    private String deviceUuid;

    @ApiModelProperty("需要下发的命令行")
    private String commandLine;

}
