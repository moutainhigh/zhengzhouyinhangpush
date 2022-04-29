package com.abtnetworks.totems.push.dto.nsfocus;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author lifei
 * @since 2021/3/8
 **/
@ApiModel("绿盟公共请求DTO")
@Data
public class NsfocusCommonRequestDTO {

    @ApiModelProperty("签名")
    String signature;

    @ApiModelProperty("时间戳")
    String timestamp;

    @ApiModelProperty("随机数")
    String nonce;

    @ApiModelProperty("用户id")
    String accountId;
}
