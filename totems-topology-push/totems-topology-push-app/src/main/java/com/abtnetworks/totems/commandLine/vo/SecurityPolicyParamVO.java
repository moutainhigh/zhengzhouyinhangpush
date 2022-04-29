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
@ApiModel("原子化命令行HTTP接口>>安全策略参数")
@Data
public class SecurityPolicyParamVO {


    @ApiModelProperty("设备型号")
    private String modelNumber;


    @ApiModelProperty("操作状态 1:新增(包含ipv4和ipv6) 2:修改(包含ipv4和ipv6) 3:删除(包含ipv4和ipv6) " +
            "4:新增(ipv4) 5:修改(ipv4) 6:删除(ipv4) " +
            "7:新增(ipv6) 8:修改(ipv6) 9:删除(ipv6)")
    private Integer statusTypeEnumCode;

    @ApiModelProperty("策略集")
    private String groupName;

    @ApiModelProperty("策略名称")
    private String name;

    @ApiModelProperty("策略id")
    private String id;

    @ApiModelProperty("动作")
    private String action;

    @ApiModelProperty("备注说明")
    private String description;

    @ApiModelProperty("开启日志")
    private String logFlag;

    @ApiModelProperty("老化时间")
    private String ageingTime;

    @ApiModelProperty("引用病毒库")
    private String refVirusLibrary;

    @ApiModelProperty("移动位置")
    private Integer moveSeatEnumCode;

    @ApiModelProperty("交换位置的规则名或id")
    private String swapRuleNameId;

    @ApiModelProperty("源ip")
    private IpAddressObjectParamVO srcIp;

    @ApiModelProperty("目的ip")
    private IpAddressObjectParamVO dstIp;

    @ApiModelProperty("服务")
    private ServiceParamVO[] service;

    @ApiModelProperty("时间")
    private TimeCommandLineParamVO timeCommandLineParamVO;

    @ApiModelProperty("源域")
    private String[] srcZoneArray;

    @ApiModelProperty("目的域")
    private String[] dstZoneArray;

    @ApiModelProperty("进接口")
    private String[] inInterfaceArray;

    @ApiModelProperty("出接口")
    private String[] outInterfaceArray;

    @ApiModelProperty("引用 源地址对象")
    private String[] srcRefIpAddressObject;

    @ApiModelProperty("引用 源地址组对象")
    private String[] srcRefIpAddressObjectGroup;

    @ApiModelProperty("引用 目的地址对象")
    private String[] dstRefIpAddressObject;

    @ApiModelProperty("引用 目的地址组对象")
    private String[] dstRefIpAddressObjectGroup;

    @ApiModelProperty("引用服务对象")
    private String[] refServiceObject;

    @ApiModelProperty("引用服务组对象")
    private String[] refServiceObjectGroup;

    @ApiModelProperty("引用时间对象")
    private String[] refTimeObject;

    @ApiModelProperty("扩展参数 key-value String:Object类型")
    private Map<String, Object> map;

    @ApiModelProperty("扩展参数 String[] 类型")
    private String[] args;
}
