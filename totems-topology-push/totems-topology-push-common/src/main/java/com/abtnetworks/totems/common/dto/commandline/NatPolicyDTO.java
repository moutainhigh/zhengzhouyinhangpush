package com.abtnetworks.totems.common.dto.commandline;

import com.abtnetworks.totems.common.dto.ExistObjectRefDTO;
import com.abtnetworks.totems.common.enums.MoveSeatEnum;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
@ApiModel("Nat策略对象")
public class NatPolicyDTO {

    @ApiModelProperty("命令行下发任务主键id")
    Integer id;

    @ApiModelProperty("任务ID")
    Integer taskId;

    @ApiModelProperty("主题（工单号）")
    String theme;

    @ApiModelProperty("设备UUID")
    String deviceUuid;

    @ApiModelProperty("源地址")
    String srcIp;

    @ApiModelProperty("目的地址")
    String dstIp;

    @ApiModelProperty("服务")
    List<ServiceDTO> serviceList;

    @ApiModelProperty("转换后服务")
    List<ServiceDTO> postServiceList;

    @ApiModelProperty("转换后源地址")
    String postSrcIp;

    @ApiModelProperty("转换后目的地址")
    String postDstIp;

    @ApiModelProperty("转换后端口")
    String postPort;

    @ApiModelProperty("源域")
    String srcZone;

    @ApiModelProperty("入接口")
    String srcItf;

    @ApiModelProperty("目的域")
    String dstZone;

    @ApiModelProperty("出接口")
    String dstItf;

    @ApiModelProperty("开始时间")
    String startTime;

    @ApiModelProperty("结束时间")
    String endTime;

    @ApiModelProperty("策略描述")
    String description;
    @ApiModelProperty("工单备注")
    String remarks;
    @ApiModelProperty("现有服务对象名称")
    String serviceObjectName;

    @ApiModelProperty("现有源地址对象名称")
    String srcAddressObjectName;

    @ApiModelProperty("现有目的地址对象名称")
    String dstAddressObjectName;

    @ApiModelProperty("转换后地址对象名称")
    String postSrcAddressObjectName;

    @ApiModelProperty("转换后目的地址对象名称")
    String postDstAddressObjectName;

    @ApiModelProperty("转换后服务对象名称")
    String postServiceObjectName;

    @ApiModelProperty("是否为虚墙")
    boolean isVsys;

    @ApiModelProperty("虚墙名称")
    String vsysName;

    @ApiModelProperty("是否为虚墙")
    boolean hasVsys;

    @ApiModelProperty("是否创建对象,true标识创建, false不创建，直接引用")
    boolean createObjFlag;

    @ApiModelProperty("移动位置")
    MoveSeatEnum moveSeatEnum;

    @ApiModelProperty("交换位置的规则名或id")
    String swapRuleNameId;

    @ApiModelProperty("需要新建的服务列表")
    List<ServiceDTO> restServiceList = new ArrayList<>();

    @ApiModelProperty("已存在服务名称列表")
    List<String> existServiceNameList = new ArrayList<>();

    @ApiModelProperty("已存在转换后服务名称列表")
    List<String> existPostServiceNameList = new ArrayList<>();

    @ApiModelProperty("需要新建转换后的服务列表")
    List<ServiceDTO> restPostServiceList = new ArrayList<>();

    @ApiModelProperty("特殊：已存在对象数据，当前仅思科8.6以上版本使用")
    ExistObjectRefDTO specialExistObject = new ExistObjectRefDTO();

    @ApiModelProperty("默认static，勾选后为dynamic")
    boolean dynamic = false;

    @ApiModelProperty("是否执行回滚")
    boolean isRollback = false;
    @ApiModelProperty("还需要创建转换后源地址对象列表")
    List<String> restPostSrcAddressList = new ArrayList<>();

    @ApiModelProperty("已存在转换后源地址对象列表")
    List<String> existPostSrcAddressList = new ArrayList<>();

    @ApiModelProperty("已存在转换后目的地址对象列表")
    List<String> existPostDstAddressList = new ArrayList<>();

    @ApiModelProperty("还需要创建转换后目的地址对象列表")
    List<String> restPostDstAddressList = new ArrayList<>();

    @ApiModelProperty("入接口别名 ")
    private String inDevItfAlias;

    @ApiModelProperty("出接口别名 ")

    private String outDevItfAlias;

    @ApiModelProperty("本条策略创建的服务对象名称集合")
    List<String> serviceObjectNameList;

    @ApiModelProperty("本条策略创建的服务组对象名称集合")
    List<String> serviceObjectGroupNameList;

    @ApiModelProperty("本条策略创建的地址对象名称集合")
    List<String> addressObjectNameList;

    @ApiModelProperty("本条策略创建的地址组对象名称集合")
    List<String> addressObjectGroupNameList;


    @ApiModelProperty("已存在源地址对象列表")
    List<String> existSrcAddressList = new ArrayList<>();

    @ApiModelProperty("还需要创建源地址对象列表")
    List<String> restSrcAddressList = new ArrayList<>();

    @ApiModelProperty("已存在目的地址对象列表")
    List<String> existDstAddressList = new ArrayList<>();

    @ApiModelProperty("还需要创建目的地址对象列表")
    List<String> restDstAddressList = new ArrayList<>();

    @ApiModelProperty("源地址描述")
    String srcIpSystem;

    @ApiModelProperty("目的地址描述")
    String dstIpSystem;

    @ApiModelProperty("转换后源地址所属系统")
    String postSrcIpSystem;

    @ApiModelProperty("转换后源地址所属系统")
    String postDstIpSystem;

    @ApiModelProperty("ip类型  0：ipv4; 1:ipv6; 2:url ")
    private Integer ipType;

    @ApiModelProperty("华3V7当前地址池id")
    String currentAddressGroupId;

    @ApiModelProperty("当前策略id")
    String currentId;

    @ApiModelProperty("bothNat回滚命令行（思科）  解释：这个对于回滚的执行器又去调nat创建接口 获取回滚命令行 每次都生成了随机数的对象，导致命令行生成的和回滚中的的对象名称对不上")
    String rollbackCommandLine;

    @ApiModelProperty("回滚查询命令行")
    String rollbackShowCmd;

    @ApiModelProperty("命令行中的策略名称  策略名称在生成命令的时候要根据工单号_AO_随机数,要传到后面流程使用")
    String policyName;

    @ApiModelProperty("复用查到的已经存在的虚拟ip名称")
    String existVirtualIpName;

    @ApiModelProperty("用于不同类型地址回滚命令行。比如每种地址类型都不同，拼接不用规则的命令行地址。 地址类型map ，key为地址名称，value为地址的类型 host主机，sub子网 rang 范围")
    Map<String,String> addressTypeMap = new HashMap<>();
}
