package com.abtnetworks.totems.common.dto.commandline;

import com.abtnetworks.totems.common.dto.ExistObjectRefDTO;
import com.abtnetworks.totems.common.dto.GeneratedObjectDTO;
import com.abtnetworks.totems.common.enums.DeviceModelNumberEnum;
import com.abtnetworks.totems.common.enums.MoveSeatEnum;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;


@Data
public class EditCommandlineDTO {


    String srcIp;

    String srcZone;

    String srcItf;

    String srcItfAlias;

    int srcZonePriority;

    String dstIp;

    String dstZone;

    String dstItf;

    String dstItfAlias;

    int dstZonePriority;

    String startTime;

    String endTime;

    String action;

    String name;

    String description;

    String deviceUuid;

    String ruleListUuid;

    /**
     设备管理IP
     */
    String ip;

    @ApiModelProperty("ip类型  0：ipv4; 1:ipv6; 2: url ")
    private Integer ipType;

    @ApiModelProperty("待合并的字段属性")
    private Integer mergeProperty;

    @ApiModelProperty("业务主题名称，对应：策略建议新建的业务主题")
    String businessName;

    @ApiModelProperty("是否创建对象,true标识创建, false不创建，直接引用")
    boolean createObjFlag;

    @ApiModelProperty("是否强制新建策略，true为强制，即使有合并策略，也不合并")
    boolean mustCreateFlag;

    @ApiModelProperty("移动位置")
    MoveSeatEnum moveSeatEnum;

    @ApiModelProperty("交换位置的规则名或id")
    String swapRuleNameId;

    boolean isTopFlag;

    @ApiModelProperty("服务列表")
    List<ServiceDTO> serviceList;

    @ApiModelProperty("合并服务")
    PolicyMergeDTO mergeDTO;

    @ApiModelProperty("第一条策略名称")
    String firstPolicyName;

    @ApiModelProperty("现有服务对象名称")
    String serviceName;

    @ApiModelProperty("源地址对象名称")
    String srcAddressName;

    @ApiModelProperty("目的地址对象名称")
    String dstAddressName;

    @ApiModelProperty("思科设备接口上已存在策略集名称")
    String ciscoInterfacePolicyName;

    @ApiModelProperty("思科设备上接口策略集是否新建")
    boolean isCiscoInterfaceCreate = false;

    @ApiModelProperty("经过业务处理，接口最后挂在out方向")
    boolean outBound = false;

    @ApiModelProperty("飞塔当前策略id")
    String currentId;

    @ApiModelProperty("cisco是否需要Enable")
    boolean ciscoEnable;

    @ApiModelProperty("是否为虚设备")
    boolean isVsys;

    @ApiModelProperty("虚设备名称")
    String vsysName;

    @ApiModelProperty("是否为虚墙")
    boolean hasVsys;

    @ApiModelProperty("需要新建的服务列表")
    List<ServiceDTO> restServiceList = new ArrayList<>();

    @ApiModelProperty("已存在服务名称列表")
    List<String> existServiceNameList = new ArrayList<>();

    @ApiModelProperty("已存在地址对象名称列表")
    List<String> existSrcAddressList = new ArrayList<>();

    @ApiModelProperty("需要建立对象地址列表")
    List<String> restSrcAddressList = new ArrayList<>();

    @ApiModelProperty("已存在目的地址对象列表")
    List<String> existDstAddressList = new ArrayList<>();

    @ApiModelProperty("需要建立对象地址列表")
    List<String> restDstAddressList = new ArrayList<>();

    @ApiModelProperty("长链接超时时间")
    Integer idleTimeout;

    @ApiModelProperty()
    DeviceInfoDTO deviceInfoDTO;

    @ApiModelProperty("特殊：已存在对象数据，当前仅思科8.6以上版本使用")
    ExistObjectRefDTO specialExistObject = new ExistObjectRefDTO();

    @ApiModelProperty("设备型号")
    DeviceModelNumberEnum modelNumber;

    @ApiModelProperty("天融信分组名称")
    String groupName;

    @ApiModelProperty("源地址描述")
    String srcIpSystem;

    @ApiModelProperty("目的地址描述")
    String dstIpSystem;

    @ApiModelProperty("生成数据")
    GeneratedObjectDTO generatedObject = new GeneratedObjectDTO();

    @ApiModelProperty("被合并的策略的数据信息")
    PolicyRecommendSecurityPolicyDTO securityPolicy = new PolicyRecommendSecurityPolicyDTO();

    @ApiModelProperty("juniper生成地址对象是根据安全域还是全局地址，true：安全域，false：全局地址")
    Boolean addressType = true;
}

