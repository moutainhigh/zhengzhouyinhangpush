package com.abtnetworks.totems.retrieval.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;
@ApiModel("全局搜索：修改服务传递后台参数")
@Data
public class RetrievalServiceDto {

    @ApiModelProperty("设备uuid")
    private String deviceUuid;

    @ApiModelProperty("服务名称")
    private String serviceName;

    @ApiModelProperty("服务内容（可能包含服务名称和服务内容）")
    private String serviceContent;
}
