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
@ApiModel("原子化命令行HTTP接口>>地址对象参数")
@Data
public class IpAddressObjectParamVO {

    @ApiModelProperty("设备型号")
    private String modelNumber;

    @ApiModelProperty("地址对象名")
    private String name;

    @ApiModelProperty("地址对象id")
    private String id;

    @ApiModelProperty("附加字符串")
    private String attachStr;

    @ApiModelProperty("操作状态 1:新增(包含ipv4和ipv6) 2:修改(包含ipv4和ipv6) 3:删除(包含ipv4和ipv6) " +
            "4:新增(ipv4) 5:修改(ipv4) 6:删除(ipv4) " +
            "7:新增(ipv6) 8:修改(ipv6) 9:删除(ipv6)")
    private Integer statusTypeEnumCode;

    @ApiModelProperty("IP类型 1:IP4 2:IP6 3:IP46 " +
            "4:NAT44 5:NAT66 6:NAT46 " +
            "7:NAT64 0:UNKNOWN")
    private Integer ruleIpTypeEnumCode;

    @ApiModelProperty("单个ip")
    String[] singleIpArray;

    @ApiModelProperty("范围ip")
    IpAddressRangeVO[] rangeIpArray;

    @ApiModelProperty("子网ip 掩码int")
    IpAddressSubnetIntVO[] subnetIntIpArray;

    @ApiModelProperty("子网ip 掩码str")
    IpAddressSubnetStrVO[] subnetStrIpArray;

    @ApiModelProperty("接口集合")
    String[] interfaceArray;

    @ApiModelProperty("域名集合")
    String[] fqdnArray;

    @ApiModelProperty("引用对象名称集合")
    String[] objectNameRefArray;

    @ApiModelProperty("引用对象组名称集合")
    String[] objectGroupNameRefArray;

    @ApiModelProperty("备注")
    String description;

    @ApiModelProperty("删除，失效标记")
    String delStr;

    @ApiModelProperty("扩展参数 key-value String:Object类型")
    private Map<String, Object> map;

    @ApiModelProperty("扩展参数 String[] 类型")
    private String[] args;
}
