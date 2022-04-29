package com.abtnetworks.totems.commandLine.vo;

import com.abtnetworks.totems.command.line.enums.RuleIPTypeEnum;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Map;

/**
 * @Author: WangCan
 * @Description
 * @Date: 2021/8/10
 */
@ApiModel("原子化命令行HTTP接口>>删除地址对象参数")
@Data
public class DeleteIpAddressObjectParamVO {

    @ApiModelProperty("设备型号")
    private String modelNumber;

    @ApiModelProperty("地址(组)对象名")
    private String name;

    @ApiModelProperty("IP类型 1:IP4 2:IP6 3:IP46 " +
            "4:NAT44 5:NAT66 6:NAT46 " +
            "7:NAT64 0:UNKNOWN")
    private Integer ruleIpTypeEnumCode;

    @ApiModelProperty("删除，失效标记")
    String delStr;

    @ApiModelProperty("扩展参数 key-value String:Object类型")
    private Map<String, Object> map;

    @ApiModelProperty("扩展参数 String[] 类型")
    private String[] args;
}
