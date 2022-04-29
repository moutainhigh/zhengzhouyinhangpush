package com.abtnetworks.totems.recommend.manager;

import com.abtnetworks.totems.common.dto.DeviceDTO;
import com.abtnetworks.totems.common.dto.PageDTO;
import com.abtnetworks.totems.common.dto.PolicyDTO;
import com.abtnetworks.totems.common.dto.SettingDTO;
import com.abtnetworks.totems.common.dto.commandline.NatPolicyDTO;
import com.abtnetworks.totems.common.dto.commandline.ServiceDTO;
import com.abtnetworks.totems.common.dto.generate.ExistAddressObjectDTO;
import com.abtnetworks.totems.common.enums.AddressPropertyEnum;
import com.abtnetworks.totems.common.enums.PolicyEnum;
import com.abtnetworks.totems.common.ro.ResultRO;
import com.abtnetworks.totems.external.vo.DeviceDetailRunVO;
import com.abtnetworks.totems.recommend.dto.risk.DeviceInterfaceDto;
import com.abtnetworks.totems.recommend.dto.task.DeviceForExistObjDTO;
import com.abtnetworks.totems.recommend.dto.task.PackFilterDTO;
import com.abtnetworks.totems.recommend.dto.task.PathInfoTaskDTO;
import com.abtnetworks.totems.recommend.dto.task.SimulationTaskDTO;
import com.abtnetworks.totems.whale.baseapi.dto.DetailPathSubnetDTO;
import com.abtnetworks.totems.whale.baseapi.ro.*;
import com.abtnetworks.totems.whale.policy.dto.*;
import com.abtnetworks.totems.whale.policy.ro.DeviceDetailRO;
import com.abtnetworks.totems.whale.policy.ro.PathAnalyzeRO;
import com.abtnetworks.totems.whale.policy.ro.PathDetailRO;
import com.abtnetworks.totems.whale.policy.ro.PathFlowRO;
import com.abtnetworks.totems.whale.policybasic.ro.FilterListsRO;
import com.abtnetworks.totems.whale.policyoptimize.ro.RuleCheckPolicyRO;
import com.abtnetworks.totems.whale.policyoptimize.ro.RuleCheckResultRO;

import java.util.List;

/**
 * @Author: wenjiachang
 * @Date: 2019/3/13 16:59
 */

/**
 * 调用Whale服务
 */
public interface WhaleManager {
    /**
     * 通过IP地址获取所对应的子网UUID列表
     * @param ipAddress ip地址
     * @return 子网UUID列表, 若为空，则获取失败
     */
    public List<String> getSubnetUuidList(String ipAddress);

    /**
     * 查询路径信息
     * @param pathAnalyzeDTO 查询路径参数
     * @return 查询路径结果数据
     */
    public PathAnalyzeRO queryPath(PathAnalyzeDTO pathAnalyzeDTO);

    /**
     * 通过设备UUID获取设备数据对象
     * @param deviceUuid 设备UUID
     * @return 设备数据对象
     */
    public DeviceRO getDeviceByUuid(String deviceUuid);

    /**
     * 根据设备UUID获取设备域对象
     * @param deviceUuid 设备UUID
     * @return 设备域对象
     */
    public ZoneRO getDeviceZoneVO(String deviceUuid);

    /**
     * 根据设备UUID获取设备域对象
     * @param deviceUuid 设备UUID
     * @return 设备域对象
     */
    public ZoneRO getDeviceZone(String deviceUuid) throws Exception;

    /**
     * 获取路径上设备详情
     * @param pathDetail 设备路径信息
     * @return 路径设备详情
     */
    public DeviceDetailRO getDeviceDetail(PathDetailRO pathDetail) throws Exception;

    public DeviceDetailRO getDeviceDetail(PathDetailRO pathDetail, String whatIfCaseUuid) throws Exception;

    /**
     * 开始全局拓扑分析
     * @return 全局拓扑分析任务UUID
     */
    public String startAnalysisAccess();

    /**
     * 查看全局拓扑分析结果
     * @param id
     * @return
     */
    String checkAnalysisAccessTask(String id);

    public PathAnalyzeDTO getAnylizePathDTO(String srcIp, String srcPort, String dstIp, String dstPort, String protocol);

    /**
     * 获取路径分析DTO
     * @param taskDTO 源IP
     * @return 路径分析DTO
     */
    public PathAnalyzeDTO getAnylizePathDTO(PathInfoTaskDTO taskDTO);

    public PathAnalyzeDTO getAnylizePathDTO(String srcIpListString, String dstIpListString, List<ServiceDTO> serviceList,
                                            String srcNodeUuid, String dstNodeUuid, String whatIfCaseUuid);
    public PathAnalyzeDTO getAnylizePathDTO(String srcIpListString, String dstIpListString, List<ServiceDTO> serviceList,
                                            String srcNodeUuid, String dstNodeUuid, String whatIfCaseUuid, Integer idleTimeout);

    public PathAnalyzeDTO getAnylizePathDTOByQT(PathInfoTaskDTO task, SimulationTaskDTO simulationTaskDTO);
    /**
     * 获取IP地址所在子网UUID
     * @param ipAddress ip地址
     * @return 子网UUID
     */
    String getNodeUuid(String ipAddress);

    /**
     * 根据子网获取域UUID
     * @param subnetUuid
     * @return
     */
    String getZoneUuid(String subnetUuid);

    /**
     * 获取子网对应uuid列表
     * @param ipAddress 地址信息
     * @return 子网信息
     */
    List<String> getNodeUuidList(String ipAddress);

    /**
     * 获取子网对象列表
     * @param subnetUuidList
     * @return
     */
    List<SubnetRO> getSubnetROList(List<String> subnetUuidList);

    /**
     * 根据huawei设备UUID，获取安全策略集UUID
     * @param devicUuid
     * @return
     */
    String getHuaweiRuleListUuid(String devicUuid);

    /**
     * 根据Juniper设备UUID，获取策略集UUID
     * @param deviceUuid
     * @return
     */
    String getJuniperSrxRuleListUuid(String deviceUuid);

    /**
     * 检测多地址的子网是否存在，只检测第一个IP地址是否存在对应子网
     * @param ipAddress 多个IP地址
     * @return
     */
    List<String> getMultiAddressNodeUuid(String ipAddress);

    /**
     * 根据迪普设备UUID，获取策略集UUID
     * @param deviceUuid
     * @return
     */
    String getDpTechRuleListUuid(String deviceUuid);

    String getFortinetTechRuleListUuid(String deviceUuid);

    String getAbtnetworksTechRuleListUuid(String deviceUuid, PolicyEnum policyType);

    String getAbtnetworksTechRule6ListUuid(String deviceUuid,PolicyEnum policyType);

    String getTopsecRuleListUuid(String deviceUuid);

    String getH3Cv7RuleListUuid(String deviceUuid);

    String getVenusVSOSRuleListUuid(String deviceUuid);

    /**
     * 根据思科设备UUID，获取策略集UUID
     * @param deviceUuid 设备UUID
     * @param aclDirection 策略方向
     * @return
     */
    String getCiscoRuleListUuid(String deviceUuid, Integer aclDirection);

    /**
     * 检测源地址和目的地址的子网
     * @param srcIp 源地址
     * @param dstIp 目的地址
     * @return 检测结果
     */
    int checkSubnet(String srcIp, String dstIp);

    /**
     * 获取zone信息
     * @param zoneRO 域对象
     * @param interfaceName 接口名称
     * @return 接口对应域数据对象
     */
    ZoneDataRO getZoneData(ZoneRO zoneRO, String interfaceName);

    /**
     * 获取设备地址对象
     * @param deviceUuid 设备UUID
     * @param name 地址对象名称
     * @return 地址对象
     */
    ObjectDetailRO queryIpByName(String deviceUuid, String name);

    /**
     * 获取设备策略对象地址
     * @param deviceUuid
     * @param ref
     * @return
     */
    String getRuleListUuidByRef(String deviceUuid, String ref);

    /**
     * 获取策略集
     * @param uuid 策略集UUID
     * @return 策略集对象
     */
    FilterListsRO getFilterListsByUuid(String uuid);

    /**
     * 根据域名获取域的所有接口
     * @param deviceUuid 设备uuid
     * @param zone 域名称
     * @return 接口数据列表
     */
    List<String> getInterfaces(String deviceUuid, String zone);

    /**
     * 预变更策略检查
     * @param ruleCheckPolicyRO 预变更策略对象
     * @param deviceUuid 设备UUID
     * @param ruleListUuid 策略集UUID
     * @return
     */
    RuleCheckResultRO getRuleCheckResult(RuleCheckPolicyRO ruleCheckPolicyRO, String deviceUuid, String ruleListUuid);

    /**
     * 获取落地子网信息
     * @param detailPathSubnetDTO 查询落地子网参数对象
     * @return
     */
    List<String> getDetailPathSubnet(DetailPathSubnetDTO detailPathSubnetDTO) throws Exception;

    /**
     * 获取落地子网uuid列表
     * @param srcNodeUuid 源子网UUID
     * @param srcIpListString 源地址
     * @param dstIpListString 目的地址
     * @param serviceList 服务列表
     * @return
     */
    List<String> getDetailPathSubnetList(String srcNodeUuid, String srcIpListString, String dstIpListString, List<ServiceDTO> serviceList, String whatIfCaseUuid) throws Exception;

    /**
     * 获取子网所在设备列表
     * @param subnetUuid 子网uuid
     * @return
     */
    List<String> getSubnetDeviceUuidList(String subnetUuid);

    /**
     * 获取子网关联设备列表
     * @param subnetUuid 子网UUID
     * @return 子网关联设备列表
     */
    String getSubnetDeviceList(String subnetUuid);

    /**
     * 获取子网信息
     * @param uuid 子网UUID
     * @return 子网信息字符串（例：192.168.0.0/24）
     */
    String getSubnetStringByUuid(String uuid);

    DeviceDetailRunVO parseDeviceDetail(DeviceDetailRO deviceDetailRO);

    /**
     * 包装策略列表
     * @param packFilterDTO 服务列表
     * @return  包装好FilterDTO列表
     */
    List<FilterDTO> packFilterDTO(PackFilterDTO packFilterDTO);

    /**
     * 此方法为至赛，ipv6等增量功能不加
     * @param srcIpList
     * @param dstIpList
     * @param serviceList
     * @return
     */
    List<PathFlowRO> packPathFlowRO(List<String> srcIpList, List<String> dstIpList, List<ServiceDTO> serviceList);

    List<SrcDstStringDTO> getSrcDstStringDTO(String ipListString);

    /***
     * 暂时保留，先做仿真的ipv6，后面有要求就用下面的方法替换
     * @param ipStringList
     * @return
     */
    List<SrcDstStringDTO> getSrcDstStringDTO(List<String> ipStringList);

    /**
     * 组装源与目的地址的，根据ip类型
     * @param ipList
     * @param ipType
     * @return
     */
    List<SrcDstStringDTO> getSrcDstStringDTO(List<String> ipList, Integer ipType);

    List<SrcDstIntegerDTO> getSrcDstIntegerDTOList(String portListString);

    List<SrcDstIntegerDTO> getSrcDstIntegerDTOList(List<String> portList);

    SrcDstIntegerDTO getSrcDstIntegerDTO(String port);

    ExistAddressObjectDTO getCurrentAddressObjectName(String ipAddress, DeviceDTO deviceDTO, AddressPropertyEnum addressPropertyEnum,PolicyEnum policyType, SettingDTO settingDTO, PolicyDTO policyDTO);

    String getCurrentServiceObjectName(List<ServiceDTO> serviceList, DeviceForExistObjDTO deviceDTO);

    String getCurrentServiceObjectName(List<ServiceDTO> serviceList, DeviceForExistObjDTO deviceDTO, Integer idleTimeout);

    /**
     * 针对于绿盟多服务对象,查询复用对象的。格式与其他厂商不一致问题，单独新开接口
     * @param serviceList
     * @param deviceDTO
     * @param idleTimeout
     * @return
     */
    String getCurrentServiceObjectNameForNsfocus(List<ServiceDTO> serviceList, DeviceForExistObjDTO deviceDTO, Integer idleTimeout);

    String getInterfacePolicyName(String deviceUuid, String interfaceAlias);

    String getInterfacePolicyName(String deviceUuid, String interfaceAlias, Boolean isInbound);

    WhatIfRO addWhatIfCase(WhatIfRO whatIfRO);

    ResultRO<List<DeviceFilterRuleListRO>> getFilterRuleList(String deviceUuid, String ruleListUuid);

    /**
     * 获取所有不可信子网的uuid
     **/
    List<String> getSubnetListByUntrusted();

    /**
     * 获取设备的策略集基本信息
     * @param deviceUuid
     * @return
     */
    List<DeviceFilterlistRO> getDeviceFilterListRO(String deviceUuid);

    /**
     * 根据设备uuid获取设备的路由表信息
     * @param deviceUuid
     * @return
     */
    List<RoutingtableRO> getRoutTable(String deviceUuid);

    /**
     * 根据设备uuid获取设备的所有的接口
     * @param deviceUuid
     * @return
     */
    List<DeviceInterfaceDto>  getDeviceInterfaces(String deviceUuid);

    /**
     * 根据设备uuid查询这个所属系统名称是否存在
     * @param deviceUuid
     * @param ipSystem
     * @return
     */
    boolean queryIpSystemHasExist(String deviceUuid,String ipSystem);


    /**
     * 查询nat策略列表
     * @param  dstIp, serviceList
     * @return
     */
    ResultRO<List<DeviceFilterRuleListRO>> getFilterRuleListSearch(String deviceUuid,String dstIp,List<ServiceDTO> serviceList);
}
