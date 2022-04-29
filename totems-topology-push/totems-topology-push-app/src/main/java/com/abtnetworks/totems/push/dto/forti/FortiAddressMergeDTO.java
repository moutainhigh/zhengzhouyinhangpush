package com.abtnetworks.totems.push.dto.forti;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @author lifei
 * @since 2021/3/5
 **/
@Data
@ApiModel("飞塔平台创建地址构建时的合并对象")
public class FortiAddressMergeDTO {

    @ApiModelProperty("地址对象集合")
    List<FortiAddressDTO> fortiAddressDTOs;

    @ApiModelProperty("已经存在的复用对象名称")
    List<String> existNames;
}
