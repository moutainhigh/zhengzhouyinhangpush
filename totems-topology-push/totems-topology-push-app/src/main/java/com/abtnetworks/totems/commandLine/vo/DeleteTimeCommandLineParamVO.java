package com.abtnetworks.totems.commandLine.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Map;

/**
 * @Author: WangCan
 * @Description
 * @Date: 2021/8/11
 */
@ApiModel("原子化命令行HTTP接口>>删除时间对象参数")
@Data
public class DeleteTimeCommandLineParamVO {

    @ApiModelProperty("设备型号")
    private String modelNumber;

    @ApiModelProperty("时间对象名")
    private String name;

    @ApiModelProperty("附加字符串")
    private String attachStr;

    @ApiModelProperty("时间对象类型 0:绝对计划 1:周期计划")
    private Integer timeObjectType;

    @ApiModelProperty("扩展参数 key-value String:Object类型")
    private Map<String, Object> map;

    @ApiModelProperty("扩展参数 String[] 类型")
    private String[] args;
}
