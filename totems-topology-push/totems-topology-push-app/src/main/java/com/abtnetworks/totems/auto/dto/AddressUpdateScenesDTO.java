package com.abtnetworks.totems.auto.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

@Data
@ApiModel("对象管理更新场景")
public class AddressUpdateScenesDTO {

    @ApiModelProperty(value = "主键")
    private List<Integer> idList;

    @ApiModelProperty(value = "场景id")
    private String scenesUuid;
}
