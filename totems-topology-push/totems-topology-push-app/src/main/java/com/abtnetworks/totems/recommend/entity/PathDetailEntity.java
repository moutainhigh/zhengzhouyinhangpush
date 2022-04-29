package com.abtnetworks.totems.recommend.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel("路径详情数据")
public class PathDetailEntity {
    @ApiModelProperty("主键ID")
    private Integer id;

    @ApiModelProperty("路径信息Id")
    private Integer pathInfoId;

    @ApiModelProperty("路径分析详细路径")
    private String analyzePath;

    @ApiModelProperty("路径验证详细路径")
    private String verifyPath;
}