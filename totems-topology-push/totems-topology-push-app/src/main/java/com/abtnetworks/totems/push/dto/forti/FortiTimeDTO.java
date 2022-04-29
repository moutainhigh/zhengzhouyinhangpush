package com.abtnetworks.totems.push.dto.forti;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author lifei
 * @since 2021/2/18
 **/
@Data
@ApiModel("飞塔管理平台时间对象DTO")
public class FortiTimeDTO {

    @ApiModelProperty("名称")
    String name;

    @ApiModelProperty("开始时间")
    String[] start;

    @ApiModelProperty("结束时间")
    String[]  end;
}
