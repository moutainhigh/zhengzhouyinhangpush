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
@ApiModel("原子化命令行HTTP接口>>基础命令行参数")
@Data
public class BaseCommandLineParamVO {
    @ApiModelProperty("设备型号")
    private String modelNumber;

    @ApiModelProperty("是否为虚墙 true:虚墙 false:实墙")
    Boolean isVsys;

    @ApiModelProperty("虚墙名")
    String vsysName;

    @ApiModelProperty("扩展参数 key-value String:Object类型")
    private Map<String, Object> map;

    @ApiModelProperty("扩展参数 String[] 类型")
    private String[] args;

}
