package com.abtnetworks.totems.common.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * @Description 该类保存了生成策略过程中创建的对象的名称，用于回滚命令行使用
 * @Author Wen Jiachang
 */
@Data
@ApiModel("策略生成对象的名称")
public class GeneratedObjectDTO {

    @ApiModelProperty("创建的策略名称")
    String policyName;

    @ApiModelProperty("创建的服务对象名称")
    List<String> serviceObjectNameList;

    @ApiModelProperty("创建的服务对象名称")
    List<String> serviceObjectGroupNameList;

    @ApiModelProperty("创建的地址对象的名称")
    List<String> addressObjectNameList;

    @ApiModelProperty("创建的地址组对象的名称")
    List<String> addressObjectGroupNameList;

    @ApiModelProperty("创建的时间对象的名称")
    List<String> timeObjectNameList;

    @ApiModelProperty("地址对象的名称 用于华三v7 静态策略回滚的时候对象名称参数")
    List<String> addressNameList;

    @ApiModelProperty("创建的源地址对象名称,若有多个，则存储第一个,当前使用场景，封禁")
    String srcObjectName;
    @ApiModelProperty("ACL的策略命令行")
    String aclPolicyCommand;

    @ApiModelProperty("是否为虚设备")
    boolean isVsys;

    @ApiModelProperty("虚设备名称")
    String vsysName;

    @ApiModelProperty("是否为虚墙")
    boolean hasVsys;

    @ApiModelProperty("回滚查询命令行")
    String rollbackShowCmd;
    @ApiModelProperty("ip类型")
    Integer ipType;

    @ApiModelProperty("用于不同类型地址回滚命令行。比如每种地址类型都不同，拼接不用规则的命令行地址。 地址类型map ，key为地址名称，value为地址的类型 host主机，sub子网 rang 范围")
    private Map<String,String> addressTypeMap;

    @ApiModelProperty("回滚命令行  解释：这个对于回滚的执行器又去调nat创建接口 获取回滚命令行 每次都生成了随机数的对象，导致命令行生成的和回滚中的的对象名称对不上")
    String rollbackCommandLine;
}
