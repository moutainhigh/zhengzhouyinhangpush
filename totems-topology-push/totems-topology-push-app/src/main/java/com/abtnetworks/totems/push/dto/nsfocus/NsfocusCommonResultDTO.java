package com.abtnetworks.totems.push.dto.nsfocus;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author lifei
 * @since 2021/3/4
 **/
@ApiModel("绿盟公共返回结果DTO")
@Data
public class NsfocusCommonResultDTO {

    @ApiModelProperty("错误编码")
    String errorCode;

    @ApiModelProperty("错误描述")
    String errorMsg;

    @ApiModelProperty("状态")
    String status;

    @ApiModelProperty("名称")
    String name;

    @ApiModelProperty("id")
    String id;
}
