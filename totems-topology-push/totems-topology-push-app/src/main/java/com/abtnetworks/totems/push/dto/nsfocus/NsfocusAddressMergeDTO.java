package com.abtnetworks.totems.push.dto.nsfocus;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @author lifei
 * @since 2021/3/5
 **/
@Data
@ApiModel("绿盟地址合并对象DTO")
public class NsfocusAddressMergeDTO {

    @ApiModelProperty("地址对象集合")
    List<NsfocusAddressDTO> nsAddressDTOs;

    @ApiModelProperty("已经存在的复用对象名称")
    List<String> existNames;
}
