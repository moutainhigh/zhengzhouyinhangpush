package com.abtnetworks.totems.retrieval.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

@ApiModel("全局搜索：修改地址对象传递后台参数")
@Data
public class RetrievalAddressDto {

    @ApiModelProperty("设备uuid")
    private String deviceUuid;

    @ApiModelProperty("地址名称")
    private String addressName;

    @ApiModelProperty("地址类型：地址对象：NETWORK_OBJECT 地址组对象：NETWORK_GROUP_OBJECT")
    private String addressType;

    @ApiModelProperty("地址内容（可能包含地址名称和地址内容）")
    private String addressContent;

}
