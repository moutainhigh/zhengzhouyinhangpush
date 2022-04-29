package com.abtnetworks.totems.push.dto.forti;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author lifei
 * @since 2021/2/18
 **/
@Data
@ApiModel("飞塔平台登陆对象")
public class FortiLoginDTO {

    @ApiModelProperty("用戶名")
    String  user;

    @ApiModelProperty("密码")
    String  passwd;

}
