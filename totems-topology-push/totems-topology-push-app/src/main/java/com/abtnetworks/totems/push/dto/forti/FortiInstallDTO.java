package com.abtnetworks.totems.push.dto.forti;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;


/**
 * @author lifei
 * @since 2021/2/25
 **/
@Data
@ApiModel("飞塔平台安装对象")
public class FortiInstallDTO {

    @ApiModelProperty("策略包名称")
    String  adom;

    @ApiModelProperty("策略包名称")
    String  pkg;

}
