package com.abtnetworks.totems.commandLine.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * @Author: WangCan
 * @Description
 * @Date: 2021/8/10
 */
@ApiModel("原子化命令行HTTP接口>>删除服务对象参数")
@Data
public class DeleteServiceObjectParamVO {

    @ApiModelProperty("设备型号")
    private String modelNumber;


    @ApiModelProperty("服务(组)对象名称")
    private String name;

    @ApiModelProperty("服务(组)对象id")
    private String id;

    @ApiModelProperty("附加字符串")
    private String attachStr;

    @ApiModelProperty("删除服务对象标记字符串")
    String delStr;

    @ApiModelProperty("扩展参数 key-value String:Object类型")
    private Map<String, Object> map;

    @ApiModelProperty("扩展参数 String[] 类型")
    private String[] args;
}
