package com.abtnetworks.totems.common.dto.commandline;

import com.abtnetworks.totems.common.constants.PolicyConstants;
import com.abtnetworks.totems.common.dto.AutoRecommendSpecialDTO;
import com.abtnetworks.totems.common.dto.ExistObjectRefDTO;
import com.abtnetworks.totems.common.dto.GeneratedObjectDTO;
import com.abtnetworks.totems.common.dto.SpecialNatDTO;
import com.abtnetworks.totems.common.enums.DeviceModelNumberEnum;
import com.abtnetworks.totems.common.enums.IPTypeEnum;
import com.abtnetworks.totems.common.enums.MoveSeatEnum;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @Author: wenjiachang
 * @Date: 2019/1/4 10:51
 */
@Data
public class CommandlineDTO {

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

    String randomNumberString;

    @ApiModelProperty("相关策略集名称")
    private String ruleListName;

    @ApiModelProperty("策略ID")
    String policyId;

    @ApiModelProperty("可用ruleId集合")
    List<Integer> usableRuleList;
    /**
     * 设备管理IP
     */
    String ip;

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

    @ApiModelProperty("交换位置的规则名 暂且迪普使用，迪普需要根据name回滚，但也可能根据id回滚，不影响主流程 添加此参数")
    String swapRuleName;

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

    @ApiModelProperty("设备信息")
    DeviceInfoDTO deviceInfoDTO;

    @ApiModelProperty("特殊：已存在对象数据，当前仅思科8.6以上版本使用")
    ExistObjectRefDTO specialExistObject = new ExistObjectRefDTO();

    @ApiModelProperty("设备型号")
    DeviceModelNumberEnum modelNumber;

    @ApiModelProperty("天融信分组名称")
    String groupName;

    @ApiModelProperty("checkPoint网络分层名称")
    String layerName;


    @ApiModelProperty("本次命令行操作，创建的源地址对象名称，作为值返回，当前使用场景：封禁")
    String returnSrcAddrObjectName;
    /**安徽电信的h3 acl策略专用**/
    @ApiModelProperty("ACL的zone pair id")
    String zonePairId;
    @ApiModelProperty("ACL的策略命令行")
    String aclPolicyCommand;

    @ApiModelProperty("源地址描述")
    String srcIpSystem;

    @ApiModelProperty("目的地址描述")
    String dstIpSystem;

    @ApiModelProperty("转换后源地址描述")
    String postSrcIpSystem;

    @ApiModelProperty("转换后目的地址描述")
    String postDstIpSystem;

    @ApiModelProperty("ip类型  0：ipv4; 1:ipv6; 2: url ")
    private Integer ipType;
    @ApiModelProperty("选择域名策略的ip类型用于生成命令行  0：ipv4; 1:ipv6; ")
    private Integer urlType;
    @ApiModelProperty("回滚所需要的参数 ")
    private GeneratedObjectDTO generatedDto;
    @ApiModelProperty("策略用户")
    private List<String> policyUserNames;
    @ApiModelProperty("策略应用")
    private List<String> policyApplications;
    @ApiModelProperty("被合并的策略的数据信息")
    PolicyRecommendSecurityPolicyDTO securityPolicy;

    @ApiModelProperty("本条策略创建的服务对象名称集合")
    List<String> serviceObjectNameList;

    @ApiModelProperty("本条策略创建的服务组对象名称集合")
    List<String> serviceObjectGroupNameList;

    @ApiModelProperty("本条策略创建的地址对象名称集合")
    List<String> addressObjectNameList;

    @ApiModelProperty("本条策略创建的地址组对象名称集合")
    List<String> addressObjectGroupNameList;

    @ApiModelProperty("本条策略创建的时间对象名称集合")
    List<String> timeObjectNameList;

    @ApiModelProperty("用于不同类型地址回滚命令行。比如每种地址类型都不同，拼接不用规则的命令行地址。 地址类型map ，key为地址名称，value为地址的类型 host主机，sub子网 rang 范围")
    private Map<String,String> addressTypeMap;

    @ApiModelProperty("juniper生成地址对象是根据安全域还是全局地址，true：安全域，false：全局地址")
    Boolean addressType = true;

    @ApiModelProperty("特殊：飞塔NAT场景仿真高级引用添加")
    SpecialNatDTO specialNatDTO = new SpecialNatDTO();

    @ApiModelProperty("自动开通逻辑：当墙上已存在转换关系时，复用转换关系，但需要生成安全策略建议，目的地址复用墙上已有的vip名称")
    private String vipName;

    @ApiModelProperty("自动开通适配地址对象特殊处理")
    AutoRecommendSpecialDTO autoRecommendSpecialDTO;

    public static CommandlineDTO getInstanceDemo() {
        CommandlineDTO dto = new CommandlineDTO();
        dto.setSrcZone("Trust");
        dto.setSrcItf("xq5");
        dto.setDstZone("Untrust");
        dto.setDstItf("out4");
        dto.setName("A20190131000000");
        dto.setBusinessName("A20190426中文主题");
        dto.setAction("permit");
        dto.setDescription("asdv");
        dto.setCreateObjFlag(true);
        dto.setMoveSeatEnum(MoveSeatEnum.BEFORE);
        dto.setSwapRuleNameId("交换策略名");
        dto.setIdleTimeout(180000);
        //dto.setServiceName("any");
        dto.setIpType(0);
        dto.setSrcIp("2.5.12.2");
        dto.setDstIp("2.5.12.23") ;
//        dto.setCurrentId("12");
       /* dto.setSrcAddressName("源对象名称123");
        dto.setDstAddressName("目的对象名称123");*/
        dto.setStartTime("2019-01-15 09:09:08");
        dto.setEndTime("2020-01-17 09:09:08");

        //已存在的对象名称
   /*     dto.setServiceName("exists_service_lw");
        dto.setSrcAddressName("exists_src_lw");
        dto.setDstAddressName("exists_dst_lw");*/
        //服务列表
  /*      dto.setServiceList(ServiceDTO.getServiceList());
        dto.setRestServiceList(dto.getServiceList());
        List<String> existServiceNameList = new ArrayList<>();
        existServiceNameList.add("已存在服务1");
        existServiceNameList.add("已存在服务2");
        existServiceNameList.add("已存在服务3");
        dto.setExistServiceNameList(existServiceNameList);



        List<String> restSrcAddressList = new ArrayList<>();
        List<String> restDstAddressList = new ArrayList<>();
        restSrcAddressList.add("1.1.1.1");
        restSrcAddressList.add("2.2.2.2/24");
        restSrcAddressList.add("4.2.5.5-4.2.5.8");
//        restSrcAddressList.add("4.2.5.10");
        dto.setRestSrcAddressList(restSrcAddressList);
        restSrcAddressList.add("3.3.3.3");
        restSrcAddressList.add("4.4.4.4");
//        restDstAddressList.add("1.2.3.5-1.2.3.50");
//        restDstAddressList.add("1.3.3.6");
        dto.setRestDstAddressList(restDstAddressList);
        dto.setRestServiceList(ServiceDTO.getServiceList());

        //合并策略
        PolicyMergeDTO mergeDTO = new PolicyMergeDTO();
        mergeDTO.setRuleName("edit_policy_name_lw");
        mergeDTO.setRuleId("1024_lw");
        mergeDTO.setMergeField(PolicyConstants.SERVICE);
//        dto.setMergeDTO(mergeDTO);

        dto.setGroupName("Lw组");*/
        List<String> srcList=new ArrayList<>();
        srcList.add("192.168.0.1");

        dto.setRestSrcAddressList(srcList);
        List<String> dstList=new ArrayList<>();
        dstList.add("192.168.0.2");
        dstList.add("192.168.0.4-192.168.0.6");

        dto.setRestDstAddressList(dstList);
        List<ServiceDTO> serverList=new ArrayList<>();
        ServiceDTO serviceDTO=new ServiceDTO();
        serviceDTO.setProtocol("6");
        serviceDTO.setSrcPorts("80");
        serviceDTO.setDstPorts("43");
        serverList.add(serviceDTO);
        dto.setRestServiceList(serverList);
        dto.setSrcIpSystem("this_src");
        dto.setDstIpSystem("this_dst");
        dto.setServiceList(serverList);
        SpecialNatDTO specialNatDTO = dto.getSpecialNatDTO();
//        specialNatDTO.setExistPoolName("pool-test1");
        specialNatDTO.setExistVipName("vip-test1");
        specialNatDTO.setPostSrcIp("19.19.19.19");
        specialNatDTO.setDstIp("28.28.28.28");
        specialNatDTO.setPostDstIp("29.29.29.29");
        specialNatDTO.setSrcItf("post3");


        List<ServiceDTO> preServiceList =new ArrayList<>();
        ServiceDTO serviceDTO3=new ServiceDTO();
        serviceDTO3.setProtocol("6");
        serviceDTO3.setDstPorts("43");
        preServiceList.add(serviceDTO3);
        specialNatDTO.setServiceList(preServiceList);

        List<ServiceDTO> postServiceList =new ArrayList<>();
        ServiceDTO serviceDTO4=new ServiceDTO();
        serviceDTO4.setProtocol("6");
        serviceDTO4.setDstPorts("63");
        postServiceList.add(serviceDTO4);
        specialNatDTO.setPostServiceList(postServiceList);

        return dto;
    }
}
