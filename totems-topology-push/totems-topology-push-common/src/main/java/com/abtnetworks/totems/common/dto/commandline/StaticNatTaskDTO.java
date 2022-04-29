package com.abtnetworks.totems.common.dto.commandline;

import com.abtnetworks.totems.common.enums.MoveSeatEnum;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

@Data
@ApiModel("静态NAT生成数据对象")
public class StaticNatTaskDTO {
    @ApiModelProperty("命令行下发任务主键id")
    private Integer id;

    @ApiModelProperty("任务id")
    private Integer taskId;

    @ApiModelProperty("主题（工单号）")
    private String theme;

    @ApiModelProperty("设备UUID")
    private String deviceUuid;

    @ApiModelProperty("策略名称")
    private String policyName;

    @ApiModelProperty("入口域")
    private String fromZone;

    @ApiModelProperty("出口域")
    private String toZone;

    @ApiModelProperty("入接口")
    private String inDevItf;

    @ApiModelProperty("出接口")
    private String outDevItf;

    @ApiModelProperty("内网地址")
    private String insideAddress;

    @ApiModelProperty("外网地址")
    private String globalAddress;

    @ApiModelProperty("内网端口")
    private String insidePort;

    @ApiModelProperty("已存在内网端口")
    private String existInsidePort;

    @ApiModelProperty("外网端口")
    private String globalPort;

    @ApiModelProperty("已存在外网端口")
    private String existGlobaPort;

    @ApiModelProperty("指定协议")
    private String protocol;

    @ApiModelProperty("策略描述")
    String description;

    @ApiModelProperty("是否为虚设备")
    boolean isVsys;

    @ApiModelProperty("虚墙名称")
    String vsysName;

    @ApiModelProperty("是否为虚墙")
    boolean hasVsys;

    @ApiModelProperty("cisco是否需要Enable")
    boolean ciscoEnable = true;

    @ApiModelProperty("外部地址对象名称")
    String globalAddressName;

    @ApiModelProperty("内部地址对象名称")
    String insideAddressName;

    @ApiModelProperty("是否创建对象")
    boolean createObject;

    @ApiModelProperty("飞塔当前策略id")
    String currentId;

    @ApiModelProperty("是否执行回滚")
    boolean isRollback = false;

    @ApiModelProperty("入接口别名 ")
    private String inDevItfAlias;

    @ApiModelProperty("出接口别名 ")

    private String outDevItfAlias;

    @ApiModelProperty("本条策略创建的服务对象名称集合")
    List<String> serviceObjectNameList;

    @ApiModelProperty("本条策略创建的地址对象名称集合")
    List<String> addressObjectNameList;

    @ApiModelProperty("本条策略创建的服务组对象名称集合")
    List<String> serviceObjectGroupNameList;

    @ApiModelProperty("本条策略创建的地址组对象名称集合")
    List<String> addressObjectGroupNameList;

    @ApiModelProperty("静态nat回滚命令行（思科）  解释：这个对于回滚的执行器又去调nat创建接口 获取回滚命令行 每次都生成了随机数的对象，导致命令行生成的和回滚中的的对象名称对不上")
    String rollbackCommandLine;

    @ApiModelProperty("移动位置")
    MoveSeatEnum moveSeatEnum;

    @ApiModelProperty("交换位置的规则名或id")
    String swapRuleNameId;

}
