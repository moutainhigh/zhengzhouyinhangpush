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
@ApiModel("原子化命令行HTTP接口>>服务对象参数")
@Data
public class ServiceObjectParamVO {
    @ApiModelProperty("设备型号")
    private String modelNumber;

    @ApiModelProperty("操作状态 1:新增(包含ipv4和ipv6) 2:修改(包含ipv4和ipv6) 3:删除(包含ipv4和ipv6) " +
            "4:新增(ipv4) 5:修改(ipv4) 6:删除(ipv4) " +
            "7:新增(ipv6) 8:修改(ipv6) 9:删除(ipv6)")
    private Integer statusTypeEnumCode;

    @ApiModelProperty("服务对象名称")
    private String name;

    @ApiModelProperty("服务对象id")
    private String id;

    @ApiModelProperty("附加字符串")
    private String attachStr;

    @ApiModelProperty("服务详情")
    private List<ServiceParamVO> serviceParamVOList;

    @ApiModelProperty("备注")
    private String description;

    @ApiModelProperty("引用服务对象名称")
    String[] serviceObjectNameRefArray;

    @ApiModelProperty("引用服务组对象名称")
    String[] serviceObjectGroupNameRefArray;

    @ApiModelProperty("扩展参数 key-value String:Object类型")
    private Map<String, Object> map;

    @ApiModelProperty("扩展参数 String[] 类型")
    private String[] args;
}
