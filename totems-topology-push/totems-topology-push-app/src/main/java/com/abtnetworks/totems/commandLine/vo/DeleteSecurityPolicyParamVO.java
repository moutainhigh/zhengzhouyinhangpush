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
@ApiModel("原子化命令行HTTP接口>>删除安全策略参数")
@Data
public class DeleteSecurityPolicyParamVO {

    @ApiModelProperty("设备型号")
    private String modelNumber;

    @ApiModelProperty("IP类型 1:IP4 2:IP6 3:IP46 " +
            "4:NAT44 5:NAT66 6:NAT46 " +
            "7:NAT64 0:UNKNOWN")
    private Integer RuleIPTypeEnumCode;

    @ApiModelProperty("安全策略ID")
    private String id;

    @ApiModelProperty("安全策略名")
    private String name;
    @ApiModelProperty("扩展参数 key-value String:Object类型")
    private Map<String, Object> map;

    @ApiModelProperty("扩展参数 String[] 类型")
    private String[] args;
}
