package com.abtnetworks.totems.commandLine.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Map;

/**
 * @Author: WangCan
 * @Description
 * @Date: 2021/8/10
 */
@ApiModel("原子化命令行HTTP接口>>时间对象参数")
@Data
public class TimeCommandLineParamVO {

    @ApiModelProperty("设备型号")
    private String modelNumber;

    @ApiModelProperty("时间对象名")
    private String name;

    @ApiModelProperty("附加字符串")
    private String attachStr;

    @ApiModelProperty("时间对象类型 0:绝对计划 1:周期计划")
    private int timeObjectType;

    @ApiModelProperty("开始时间 yyyy-MM-dd HH:mm:ss")
    private String startTime;

    @ApiModelProperty("结束时间 yyyy-MM-dd HH:mm:ss")
    private String endTime;

    @ApiModelProperty("周期：如 daily weekdays weekend monday...sunday")
    private String[] cycle;

    @ApiModelProperty("周期类型")
    private String cycleType;

    @ApiModelProperty("周期开始")
    private String cycleStart;

    @ApiModelProperty("周期结束")
    private String cycleEnd;

    @ApiModelProperty("扩展参数 key-value String:Object类型")
    private Map<String, Object> map;

    @ApiModelProperty("扩展参数 String[] 类型")
    private String[] args;
}
