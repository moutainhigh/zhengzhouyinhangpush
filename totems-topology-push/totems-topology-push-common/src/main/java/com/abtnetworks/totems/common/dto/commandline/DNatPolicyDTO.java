package com.abtnetworks.totems.common.dto.commandline;

import com.abtnetworks.totems.common.dto.AutoRecommendFortinetDnatSpecialDTO;
import com.abtnetworks.totems.common.enums.MoveSeatEnum;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
@ApiModel("DNat数据实体")
public class DNatPolicyDTO {

    @ApiModelProperty("命令行下发任务主键id")
    Integer id;

    @ApiModelProperty("任务ID")
    Integer taskId;

    @ApiModelProperty("主题（工单号）")
    String theme;

    @ApiModelProperty("设备UUID")
    String deviceUuid;

    @ApiModelProperty("开始时间")
    String startTime;

    @ApiModelProperty("结束时间")
    String endTime;

    @ApiModelProperty("源地址")
    String srcIp;

    @ApiModelProperty("源地址描述")
    String srcIpSystem;

    @ApiModelProperty("目的地址")
    String dstIp;

    @ApiModelProperty("目的地址描述")
    String dstIpSystem;

    @ApiModelProperty("服务")
    List<ServiceDTO> serviceList;

    @ApiModelProperty("转换后地址")
    String postIpAddress;

    @ApiModelProperty("转换后目的地址描述")
    String postDstIpSystem;

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
    @ApiModelProperty("策略描述")
    String description;
    @ApiModelProperty("工单备注")
    String remarks;
    @ApiModelProperty("现有服务对象名称")
    String serviceObjectName;

    @ApiModelProperty("转换后服务对象名称")
    String postServiceObjectName;

    @ApiModelProperty("现有源地址对象名称")
    String srcAddressObjectName;

    @ApiModelProperty("现有目的地址对象名称")
    String dstAddressObjectName;

    @ApiModelProperty("转换后地址对象名称")
    String postAddressObjectName;

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

    @ApiModelProperty("已存在源地址对象列表")
    List<String> existSrcAddressList = new ArrayList<>();

    @ApiModelProperty("还需要创建源地址对象列表")
    List<String> restSrcAddressList = new ArrayList<>();

    @ApiModelProperty("已存在转换后源地址对象列表")
    List<String> existPostSrcAddressList = new ArrayList<>();

    @ApiModelProperty("还需要创建转换后地址对象列表")
    List<String> restPostSrcAddressList = new ArrayList<>();

    @ApiModelProperty("已存在目的地址对象列表")
    List<String> existDstAddressList = new ArrayList<>();

    @ApiModelProperty("还需要创建目的地址对象列表")
    List<String> restDstAddressList = new ArrayList<>();

    @ApiModelProperty("飞塔当前策略id")
    String currentId;

    @ApiModelProperty("入接口别名 ")
    private String inDevItfAlias;

    @ApiModelProperty("出接口别名 ")

    private String outDevItfAlias;

    @ApiModelProperty("已存在转换后目的地址对象列表")
    List<String> existPostDstAddressList = new ArrayList<>();

    @ApiModelProperty("还需要创建转换后目的地址对象列表")
    List<String> restPostDstAddressList = new ArrayList<>();

    @ApiModelProperty("ip类型  0：ipv4; 1:ipv6; 2:url ")
    private Integer ipType;

    @ApiModelProperty("华3V7当前地址池id")
    String currentAddressGroupId;

    @ApiModelProperty("本条策略创建的服务对象名称集合")
    List<String> serviceObjectNameList;

    @ApiModelProperty("本条策略创建的地址对象名称集合")
    List<String> addressObjectNameList;

    @ApiModelProperty("本条策略创建的地址组对象名称集合")
    List<String> addressObjectGroupNameList;

    @ApiModelProperty("本条策略创建的服务组对象名称集合")
    List<String> serviceObjectGroupNameList;

    @ApiModelProperty("命令行中的策略名称  策略名称在生成命令的时候要根据工单号_AO_随机数,要传到后面流程使用")
    String policyName;

    @ApiModelProperty("回滚查询命令行")
    String rollbackShowCmd;

    @ApiModelProperty("用于不同类型地址回滚命令行。比如每种地址类型都不同，拼接不用规则的命令行地址。 地址类型map ，key为地址名称，value为地址的类型 host主机，sub子网 rang 范围")
    Map<String,String> addressTypeMap = new HashMap<>();

    @ApiModelProperty("自动开通生成飞塔目的NAT处理")
    AutoRecommendFortinetDnatSpecialDTO fortinetDnatSpecialDTO;
}
