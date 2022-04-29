package com.abtnetworks.totems.translation.commandline;

import com.abtnetworks.totems.command.line.abs.OverAllGeneratorAbstractBean;
import com.abtnetworks.totems.command.line.dto.*;
import com.abtnetworks.totems.command.line.enums.*;
import com.abtnetworks.totems.common.constant.Constants;
import com.abtnetworks.totems.common.mapper.TotemsJsonMapper;
import com.abtnetworks.totems.common.network.TotemsIp4Utils;
import com.abtnetworks.totems.common.network.TotemsIp6Utils;
import com.abtnetworks.totems.common.utils.*;
import com.abtnetworks.totems.translation.service.impl.TranslationDeviceCommandLineServiceImpl;
import com.abtnetworks.totems.vender.longma.nat.NatLongMaImpl;
import com.abtnetworks.totems.whale.baseapi.ro.*;
import com.abtnetworks.totems.whale.policy.ro.RoutingEntriesRO;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @Author: WangCan
 * @Description 策略迁移命令行生成基类，当该类的方法无法满足生成原子化命令行时，在厂家实现类中重写方法
 * @Date: 2021/5/26
 */
@Slf4j
public abstract class TranslationCommandline {

    public static final String DYNAMIC_TYPE="DYNAMIC";
    public static final String STATIC_TYPE="STATIC";

    /**
     * 原子化命令行实现类
     */
    protected OverAllGeneratorAbstractBean generatorBean;
    protected OverAllGeneratorAbstractBean natGeneratorBean;
    protected OverAllGeneratorAbstractBean routingGeneratorBean;

    /**
     * 前置命令行
     * @param isVsys 是否为虚墙
     * @param vsysName  虚墙名
     * @return
     */
    public String generatePreCommandLine(Boolean isVsys,String vsysName){
        return generatorBean.generatePreCommandline(isVsys,vsysName,null,null);
    }

    /**
     * 后置命令行
     * @return
     */
    public String generatePostCommandLine(){
        return generatorBean.generatePostCommandline(null,null);
    }

    /** 生成地址对象命令行
     * @param needAddNetWorkObjectList
     * @param parameterMap
     * @return
     * @throws Exception
     */
    public String generateIpAddressObjectCommandLine(List<NetWorkGroupObjectRO> needAddNetWorkObjectList, Map<String, Object> parameterMap) throws Exception{
        StringBuffer objectCommandLine = new StringBuffer();
        for (NetWorkGroupObjectRO netWorkGroupObjectRO : needAddNetWorkObjectList) {
            IpAddressParamDTO paramDTO = this.buildIpAddressParam(netWorkGroupObjectRO);
            objectCommandLine.append(generatorBean.generateIpAddressObjectCommandLine(StatusTypeEnum.ADD, paramDTO.getIpTypeEnum(),
                    netWorkGroupObjectRO.getName(),null,paramDTO.getSingleIpArray(),paramDTO.getRangIpArray(),paramDTO.getSubnetIntIpArray(),paramDTO.getSubnetStrIpArray(),
                    null,paramDTO.getHosts(),paramDTO.getObjectNameRefArray(),netWorkGroupObjectRO.getDescription(),null,null,
                    parameterMap,null));
        }
        return objectCommandLine.toString();
    }

    /**
     * 生成地址组命令行
     * @param needAddNetWorkObjectList
     * @param parameterMap
     * @return
     * @throws Exception
     */
    public String generateIpAddressObjectGroupCommandLine(List<NetWorkGroupObjectRO> needAddNetWorkObjectList,Map<String, Object> parameterMap) throws Exception{
        StringBuffer objectCommandLine = new StringBuffer();
        for (NetWorkGroupObjectRO netWorkGroupObjectRO : needAddNetWorkObjectList) {
            IpAddressParamDTO paramDTO = this.buildIpAddressParam(netWorkGroupObjectRO);
            objectCommandLine.append(generatorBean.generateIpAddressObjectGroupCommandLine(StatusTypeEnum.ADD, paramDTO.getIpTypeEnum(),
                    netWorkGroupObjectRO.getName(),null,paramDTO.getSingleIpArray(),paramDTO.getRangIpArray(),paramDTO.getSubnetIntIpArray(),paramDTO.getSubnetStrIpArray(),
                    null,paramDTO.getHosts(),paramDTO.getObjectNameRefArray(),paramDTO.getObjectGroupNameRefArray(),netWorkGroupObjectRO.getDescription(),null,null,
                    parameterMap,null));
        }
        return objectCommandLine.toString();
    }

    /**
     * 生成服务命令行
     * @param needAddServiceObjectList
     * @param parameterMap
     * @return
     * @throws Exception
     */
    public String generateServiceObjectCommandLine(List<ServiceGroupObjectRO> needAddServiceObjectList, Map<String, Object> parameterMap) throws Exception {
        StringBuffer serviceCommandLine = new StringBuffer();
        for (ServiceGroupObjectRO serviceGroupObjectRO : needAddServiceObjectList) {
            List<ServiceParamDTO> serviceParamDTOS = this.buildServiceParamDTO(serviceGroupObjectRO);
            serviceCommandLine.append(generatorBean.generateServiceObjectCommandLine(StatusTypeEnum.ADD,
                    serviceGroupObjectRO.getName(), serviceGroupObjectRO.getId(), null,serviceParamDTOS,
                    serviceGroupObjectRO.getDescription(),parameterMap,null));
        }
        return serviceCommandLine.toString();
    }

    /**
     * 生成服务组命令行
     * @param needAddServiceObjectGroupList
     * @param parameterMap
     * @return
     * @throws Exception
     */
    public String generateServiceObjectGroupCommandLine(List<ServiceGroupObjectRO> needAddServiceObjectGroupList, Map<String, Object> parameterMap) throws Exception {
        StringBuffer serviceCommandLine = new StringBuffer();
        for (ServiceGroupObjectRO serviceGroupObjectRO : needAddServiceObjectGroupList) {
            List<ServiceParamDTO> serviceParamDTOS = this.buildServiceParamDTO(serviceGroupObjectRO);
            String[] serviceObjectNameRefArray = null;
            if(CollectionUtils.isNotEmpty(serviceGroupObjectRO.getIncludeFilterServiceNames())){
                serviceObjectNameRefArray = serviceGroupObjectRO.getIncludeFilterServiceNames().toArray(new String[serviceGroupObjectRO.getIncludeFilterServiceNames().size()]);
            }
            String[] serviceObjectGroupNameRefArray = null;
            if(CollectionUtils.isNotEmpty(serviceGroupObjectRO.getIncludeFilterServiceGroupNames())){
                serviceObjectGroupNameRefArray = serviceGroupObjectRO.getIncludeFilterServiceGroupNames().toArray(new String[serviceGroupObjectRO.getIncludeFilterServiceGroupNames().size()]);
            }
            serviceCommandLine.append(generatorBean.generateServiceObjectGroupCommandLine(StatusTypeEnum.ADD,
                    serviceGroupObjectRO.getName(), serviceGroupObjectRO.getId(), null,
                    serviceParamDTOS,
                    serviceGroupObjectRO.getDescription(),
                    serviceObjectNameRefArray, serviceObjectGroupNameRefArray,
                    parameterMap,null));
        }
        return serviceCommandLine.toString();
    }

    /**
     * 生成服务命令行
     * @param needAddTimeObjectList
     * @param parameterMap
     * @return
     * @throws Exception
     */
    public String generateTimeCommandLine(List<TimeObjectRO> needAddTimeObjectList, Map<String, Object> parameterMap) throws Exception {
        StringBuffer timeCommandLine = new StringBuffer();
        for (TimeObjectRO timeObjectRO : needAddTimeObjectList) {
            AbsoluteTimeParamDTO absoluteTimeParamDTO = null;
            PeriodicTimeParamDTO periodicTimeParamDTO = null;
            for (TimeItemsRO timeItem : timeObjectRO.getTimeItems()) {
                if (StringUtils.isNoneBlank(timeItem.getStart(), timeItem.getEnd())) {
                    String start = TimeUtils.dealDateFormat(timeItem.getStart());
                    String[] startArr = start.split(" ");
                    String  end = TimeUtils.dealDateFormat(timeItem.getEnd());
                    String[] endArr = end.split(" ");
                    absoluteTimeParamDTO = new AbsoluteTimeParamDTO(startArr[0],startArr[1],endArr[0],endArr[1]);
                } else if(StringUtils.isNotBlank(timeItem.getPeriodicSetting())){
                    String periodicSetting = timeItem.getPeriodicSetting();
                    //TODO 每个厂家的周期计划格式不一致，暂无法转换，如华为：08:00:00 to 18:00:00 working-day
                    throw new RuntimeException("暂不支持周期时间对象迁移:"+timeObjectRO.getName());
                }
            }
            if(absoluteTimeParamDTO != null){
                timeCommandLine.append(generatorBean.generateAbsoluteTimeCommandLine(timeObjectRO.getName(), null, absoluteTimeParamDTO,parameterMap,null));
            } else {
                timeCommandLine.append(generatorBean.generatePeriodicTimeCommandLine(timeObjectRO.getName(), null, periodicTimeParamDTO,parameterMap,null));
            }
        }
        return timeCommandLine.toString();
    }

    /**
     * 生成安全策略命令行
     * @param deviceFilterlistRO 策略集
     * @param needAddFilter 策略
     * @param parameterMap  扩展参数
     * @return
     */
    public Map<String,String> generateSecurityPolicyCommandLine(String taskUuid,DeviceFilterlistRO deviceFilterlistRO, List<DeviceFilterRuleListRO> needAddFilter, Map<String, Object> parameterMap) throws Exception {
        Map<String,String> generateSecurityResult = new HashMap<>();
        StringBuffer policyCommandLine = new StringBuffer();
        StringBuffer warningCommandLine = new StringBuffer();
        // 不生成策略集命令行
//        policyCommandLine.append(generatorBean.generatePolicyGroupCommandLine(deviceFilterlistRO.getName(),deviceFilterlistRO.getDescription(),parameterMap,null));
        for (DeviceFilterRuleListRO deviceFilterRuleListRO : needAddFilter) {
            //生成策略
            StatusTypeEnum statusTypeEnum = StatusTypeEnum.ADD;
            if("ip6".equals(deviceFilterRuleListRO.getIpType())){
                statusTypeEnum = StatusTypeEnum.ADD_IPV6;
            }
            policyCommandLine.append(generatorBean.generateSecurityPolicyCommandLine(statusTypeEnum, this.buildPolicyParam(deviceFilterRuleListRO), parameterMap, null));
        }
        generateSecurityResult.put("commandLine",policyCommandLine.toString());
        generateSecurityResult.put("warning",warningCommandLine.toString());
        return generateSecurityResult;
    }

    public Map<String,String> generateNatPolicyCommandLine(String taskUuid, DeviceFilterlistRO deviceFilterlistRO, List<DeviceFilterRuleListRO> needAddFilter, Map<String, Object> parameterMap) throws Exception {
        Map<String,String> generateSecurityResult = new HashMap<>();
        StringBuffer policyCommandLine = new StringBuffer();
        StringBuffer warningCommandLine = new StringBuffer();
        // 不生成策略集命令行
        for (DeviceFilterRuleListRO deviceFilterRuleListRO : needAddFilter) {
            //生成策略
            if(natGeneratorBean == null){
                throw new RuntimeException("目标设备暂不支持迁移");
            }
            StatusTypeEnum statusTypeEnum = StatusTypeEnum.ADD;
            if("ip6".equals(deviceFilterRuleListRO.getIpType())){
                statusTypeEnum = StatusTypeEnum.ADD_IPV6;
            }
            NatPolicyParamDTO natPolicyParamDTO = this.buildNatPolicyParam(deviceFilterRuleListRO);
            JSONObject natClause = deviceFilterRuleListRO.getNatClause();
            String natCommandline ;
            if(DYNAMIC_TYPE.equals(natClause.getString("type"))){
                if("SRC".equals(natClause.getString("natField"))){
                    natCommandline = natGeneratorBean.generateSNatPolicyCommandLine(statusTypeEnum,natPolicyParamDTO,parameterMap,null);
                } else if("DST".equals(natClause.getString("natField"))){
                    natCommandline = natGeneratorBean.generateDNatPolicyCommandLine(statusTypeEnum,natPolicyParamDTO,parameterMap,null);
                } else {
                    natCommandline = natGeneratorBean.generateBothNatPolicyCommandLine(statusTypeEnum,natPolicyParamDTO,parameterMap,null);
                }

            } else if(STATIC_TYPE.equals(natClause.getString("type"))){
                natCommandline = natGeneratorBean.generateStaticNatPolicyCommandLine(statusTypeEnum,natPolicyParamDTO,parameterMap,null);
            } else {
                natCommandline = StringUtils.EMPTY;
                throw new RuntimeException("目标设备暂不支持迁移");
            }
            policyCommandLine.append(natCommandline);
        }
        generateSecurityResult.put("commandLine",policyCommandLine.toString());
        generateSecurityResult.put("warning",warningCommandLine.toString());
        return generateSecurityResult;
    }

    /**
     * 删除安全策略
     * @param id
     * @param name
     * @return
     */
    public String generateDeleteSecurityPolicyCommandLine(String id,String name,Map<String, Object> parameterMap){
        return generatorBean.deleteSecurityPolicyByIdOrName(null,id,name,null,null);
    }

    public String generateRoutingCommandLine(RoutingEntriesRO routingEntriesRO) throws Exception {
        if(routingGeneratorBean == null){
            throw new RuntimeException("当前设备暂不支持");
        }
        log.info("路由信息为:{}",  TotemsJsonMapper.toJson(routingEntriesRO));
        String nextHop = null;
        if(StringUtils.isNotBlank(routingEntriesRO.getNextHop().getIp6Gateway())){
            nextHop = routingEntriesRO.getNextHop().getIp6Gateway();
        } else {
            nextHop = routingEntriesRO.getNextHop().getIp4Gateway();
        }
        String routingCommandLine ;
        if(StringUtils.isNotBlank(routingEntriesRO.getIp6Prefix())){
            routingCommandLine = routingGeneratorBean.generateIpv6RoutingCommandLine(routingEntriesRO.getIp6Prefix(),routingEntriesRO.getMaskLength(),nextHop,routingEntriesRO.getNextHop().getInterfaceName(),
                    routingEntriesRO.getDistance()==null?StringUtils.EMPTY:String.valueOf(routingEntriesRO.getDistance()),routingEntriesRO.getWeight(),routingEntriesRO.getDescription(),new HashMap<>(),new String[0]);
        } else {
            routingCommandLine = routingGeneratorBean.generateIpv4RoutingCommandLine(routingEntriesRO.getIp4Prefix(),routingEntriesRO.getMaskLength(),nextHop,routingEntriesRO.getNextHop().getInterfaceName(),
                    routingEntriesRO.getDistance()==null?StringUtils.EMPTY:String.valueOf(routingEntriesRO.getDistance()),routingEntriesRO.getWeight(),routingEntriesRO.getDescription(),new HashMap<>(),new String[0]);
        }
        return routingCommandLine;
    }

    /**
     * 构建NAT策略原子化命令行参数
     * @param ruleListRO
     * @return
     */
    public NatPolicyParamDTO buildNatPolicyParam(DeviceFilterRuleListRO ruleListRO){

        NatPolicyParamDTO natPolicyParamDTO = new NatPolicyParamDTO();

        if (ruleListRO.getInInterfaceGroupRefs() != null && ruleListRO.getInInterfaceGroupRefs().size() > 0) {
            natPolicyParamDTO.setSrcZone(new ZoneParamDTO(ruleListRO.getInInterfaceGroupRefs().get(0)));
        }
//        dto.setSrcIp("");
        if (ruleListRO.getOutInterfaceGroupRefs() != null && ruleListRO.getOutInterfaceGroupRefs().size() > 0) {
            natPolicyParamDTO.setDstZone(new ZoneParamDTO(ruleListRO.getOutInterfaceGroupRefs().get(0)));
        }
//        dto.setDstItf("");

        String name = StringUtils.isBlank(ruleListRO.getName())?ruleListRO.getRuleId():ruleListRO.getName();
        natPolicyParamDTO.setName(name);
        if(StringUtils.isNotBlank(ruleListRO.getRuleId())){
            natPolicyParamDTO.setId(ruleListRO.getRuleId());
        }/* else {
            policyParamDTO.setId(IdGen.getRandomNumberString());
        }*/
        natPolicyParamDTO.setAction(StringUtils.isBlank(ruleListRO.getAction())?"permit":ruleListRO.getAction());
        natPolicyParamDTO.setDescription(ruleListRO.getDescription());

        natPolicyParamDTO.setAgeingTime(ruleListRO.getIdleTimeout());
        String filterTimeGroupName = ruleListRO.getFilterTimeGroupName();
        if(StringUtils.isNotBlank(filterTimeGroupName)){
            natPolicyParamDTO.setRefTimeObject(new String[]{filterTimeGroupName});
        }
        JSONObject natClause = ruleListRO.getNatClause();
        if (natClause != null) {
            List<String> preExistServiceNameList = new ArrayList<>();
            List<ServiceParamDTO> preServiceParamDTOS = new ArrayList<>();
            List<String> postExistServiceNameList = new ArrayList<>();
            List<ServiceParamDTO> postServiceParamDTOS = new ArrayList<>();
            if (natClause.containsKey("preServices")) {
                JSONArray services = natClause.getJSONArray("preServices");
                for (int i = 0; i < services.size(); i++) {
                    JSONObject service = services.getJSONObject(i);

                    getServiceParamDTO(preExistServiceNameList, preServiceParamDTOS, service);
                }
            }
            if (natClause.containsKey("postServices")) {
                JSONArray services = natClause.getJSONArray("postServices");
                for (int i = 0; i < services.size(); i++) {
                    JSONObject service = services.getJSONObject(i);
                    getServiceParamDTO(postExistServiceNameList, preServiceParamDTOS, service);
                }
            }

            if (natClause.containsKey("preSrcIPItems")) {
                RuleIPTypeEnum typeEnum = RuleIPTypeEnum.IP4;
                JSONArray preSrcIPItems = natClause.getJSONArray("preSrcIPItems");
                List<String> existSrcAddressList = new ArrayList<>();
                List<String> hostList = new ArrayList<>();
                List<String> singleIpList = new ArrayList<>();
                List<IpAddressRangeDTO> rangeDTOS = new ArrayList<>();
                List<IpAddressSubnetStrDTO> subnetStrDTOS = new ArrayList<>();
                List<IpAddressSubnetIntDTO> subnetIntDTOS = new ArrayList<>();
                for (int i = 0; i < preSrcIPItems.size(); i++) {
                    JSONObject srcIp = preSrcIPItems.getJSONObject(i);

                    typeEnum = getIpAddressParamDTO(existSrcAddressList, hostList, singleIpList, rangeDTOS, subnetStrDTOS, subnetIntDTOS, srcIp);

                }
                if(CollectionUtils.isNotEmpty(singleIpList) || CollectionUtils.isNotEmpty(rangeDTOS)
                        || CollectionUtils.isNotEmpty(subnetStrDTOS) || CollectionUtils.isNotEmpty(subnetIntDTOS)){
                    IpAddressParamDTO srcIpParam = new IpAddressParamDTO();
                    srcIpParam.setSingleIpArray(singleIpList.toArray(new String[singleIpList.size()]));
                    srcIpParam.setRangIpArray(rangeDTOS.toArray(new IpAddressRangeDTO[rangeDTOS.size()]));
                    srcIpParam.setSubnetStrIpArray(subnetStrDTOS.toArray(new IpAddressSubnetStrDTO[subnetStrDTOS.size()]));
                    srcIpParam.setSubnetIntIpArray(subnetIntDTOS.toArray(new IpAddressSubnetIntDTO[subnetIntDTOS.size()]));
                    srcIpParam.setHosts(hostList.toArray(new String[hostList.size()]));
                    srcIpParam.setIpTypeEnum(typeEnum);
                    natPolicyParamDTO.setSrcIp(srcIpParam);
                }
                natPolicyParamDTO.setSrcRefIpAddressObject(existSrcAddressList.toArray(new String[existSrcAddressList.size()]));
            }
            if (natClause.containsKey("postSrcIPItems")) {
                RuleIPTypeEnum typeEnum = RuleIPTypeEnum.IP4;
                JSONArray postSrcIPItems = natClause.getJSONArray("postSrcIPItems");
                List<String> existSrcAddressList = new ArrayList<>();
                List<String> hostList = new ArrayList<>();
                List<String> singleIpList = new ArrayList<>();
                List<IpAddressRangeDTO> rangeDTOS = new ArrayList<>();
                List<IpAddressSubnetStrDTO> subnetStrDTOS = new ArrayList<>();
                List<IpAddressSubnetIntDTO> subnetIntDTOS = new ArrayList<>();
                for (int i = 0; i < postSrcIPItems.size(); i++) {
                    JSONObject srcIp = postSrcIPItems.getJSONObject(i);

                    typeEnum = getIpAddressParamDTO(existSrcAddressList, hostList, singleIpList, rangeDTOS, subnetStrDTOS, subnetIntDTOS, srcIp);

                }
                if(CollectionUtils.isNotEmpty(singleIpList) || CollectionUtils.isNotEmpty(rangeDTOS)
                        || CollectionUtils.isNotEmpty(subnetStrDTOS) || CollectionUtils.isNotEmpty(subnetIntDTOS)){
                    IpAddressParamDTO srcIpParam = new IpAddressParamDTO();
                    srcIpParam.setSingleIpArray(singleIpList.toArray(new String[singleIpList.size()]));
                    srcIpParam.setRangIpArray(rangeDTOS.toArray(new IpAddressRangeDTO[rangeDTOS.size()]));
                    srcIpParam.setSubnetStrIpArray(subnetStrDTOS.toArray(new IpAddressSubnetStrDTO[subnetStrDTOS.size()]));
                    srcIpParam.setSubnetIntIpArray(subnetIntDTOS.toArray(new IpAddressSubnetIntDTO[subnetIntDTOS.size()]));
                    srcIpParam.setHosts(hostList.toArray(new String[hostList.size()]));
                    srcIpParam.setIpTypeEnum(typeEnum);
                    natPolicyParamDTO.setPostSrcIpAddress(srcIpParam);
                }
                natPolicyParamDTO.setPostSrcRefIpAddressObject(existSrcAddressList.toArray(new String[existSrcAddressList.size()]));
            }

            if (natClause.containsKey("preDstIPItems")) {
                RuleIPTypeEnum typeEnum = RuleIPTypeEnum.IP4;
                JSONArray preDstIPItems = natClause.getJSONArray("preDstIPItems");
                List<String> existDstAddressList = new ArrayList<>();
                List<String> hostList = new ArrayList<>();
                List<String> singleIpList = new ArrayList<>();
                List<IpAddressRangeDTO> rangeDTOS = new ArrayList<>();
                List<IpAddressSubnetStrDTO> subnetStrDTOS = new ArrayList<>();
                List<IpAddressSubnetIntDTO> subnetIntDTOS = new ArrayList<>();
                for (int i = 0; i < preDstIPItems.size(); i++) {
                    JSONObject dstIp = preDstIPItems.getJSONObject(i);

                    typeEnum = getIpAddressParamDTO(existDstAddressList, hostList, singleIpList, rangeDTOS, subnetStrDTOS, subnetIntDTOS, dstIp);
                }
                if(CollectionUtils.isNotEmpty(singleIpList) || CollectionUtils.isNotEmpty(rangeDTOS)
                        || CollectionUtils.isNotEmpty(subnetStrDTOS) || CollectionUtils.isNotEmpty(subnetIntDTOS) || CollectionUtils.isNotEmpty(hostList)){
                    IpAddressParamDTO dstIpParam = new IpAddressParamDTO();
                    dstIpParam.setSingleIpArray(singleIpList.toArray(new String[singleIpList.size()]));
                    dstIpParam.setRangIpArray(rangeDTOS.toArray(new IpAddressRangeDTO[rangeDTOS.size()]));
                    dstIpParam.setSubnetStrIpArray(subnetStrDTOS.toArray(new IpAddressSubnetStrDTO[subnetStrDTOS.size()]));
                    dstIpParam.setSubnetIntIpArray(subnetIntDTOS.toArray(new IpAddressSubnetIntDTO[subnetIntDTOS.size()]));
                    dstIpParam.setHosts(hostList.toArray(new String[hostList.size()]));
                    dstIpParam.setIpTypeEnum(typeEnum);
                    natPolicyParamDTO.setDstIp(dstIpParam);
                }
                natPolicyParamDTO.setDstRefIpAddressObject(existDstAddressList.toArray(new String[existDstAddressList.size()]));
            }

            if (natClause.containsKey("postDstIPItems")) {
                RuleIPTypeEnum typeEnum = RuleIPTypeEnum.IP4;
                JSONArray postDstIPItems = natClause.getJSONArray("postDstIPItems");
                List<String> existDstAddressList = new ArrayList<>();
                List<String> hostList = new ArrayList<>();
                List<String> singleIpList = new ArrayList<>();
                List<IpAddressRangeDTO> rangeDTOS = new ArrayList<>();
                List<IpAddressSubnetStrDTO> subnetStrDTOS = new ArrayList<>();
                List<IpAddressSubnetIntDTO> subnetIntDTOS = new ArrayList<>();
                for (int i = 0; i < postDstIPItems.size(); i++) {
                    JSONObject dstIp = postDstIPItems.getJSONObject(i);

                    typeEnum = getIpAddressParamDTO(existDstAddressList, hostList, singleIpList, rangeDTOS, subnetStrDTOS, subnetIntDTOS, dstIp);
                }
                if(CollectionUtils.isNotEmpty(singleIpList) || CollectionUtils.isNotEmpty(rangeDTOS)
                        || CollectionUtils.isNotEmpty(subnetStrDTOS) || CollectionUtils.isNotEmpty(subnetIntDTOS) || CollectionUtils.isNotEmpty(hostList)){
                    IpAddressParamDTO dstIpParam = new IpAddressParamDTO();
                    dstIpParam.setSingleIpArray(singleIpList.toArray(new String[singleIpList.size()]));
                    dstIpParam.setRangIpArray(rangeDTOS.toArray(new IpAddressRangeDTO[rangeDTOS.size()]));
                    dstIpParam.setSubnetStrIpArray(subnetStrDTOS.toArray(new IpAddressSubnetStrDTO[subnetStrDTOS.size()]));
                    dstIpParam.setSubnetIntIpArray(subnetIntDTOS.toArray(new IpAddressSubnetIntDTO[subnetIntDTOS.size()]));
                    dstIpParam.setHosts(hostList.toArray(new String[hostList.size()]));
                    dstIpParam.setIpTypeEnum(typeEnum);
                    natPolicyParamDTO.setPostDstIpAddress(dstIpParam);
                }
                natPolicyParamDTO.setPostDstRefIpAddressObject(existDstAddressList.toArray(new String[existDstAddressList.size()]));
            }
            if(natClause.containsKey("preSrcPortSpec")){
                JSONArray preSrcPortSpec = natClause.getJSONArray("preSrcPortSpec");
                for (int i = 0; i < preSrcPortSpec.size(); i++) {
                    JSONObject srcPortSpec = preSrcPortSpec.getJSONObject(i);
                    if (srcPortSpec.containsKey("portValues")) {
                        JSONArray portValues = srcPortSpec.getJSONArray("portValues");
                        List<String> portList = portValues.stream().map(t -> (String) t).collect(Collectors.toList());
                        String portOp = srcPortSpec.getString("portOp");
                        List<Integer> srcSinglePortArray = new ArrayList<>();
                        List<String> srcSinglePortStrArray = new ArrayList<>();
                        List<PortRangeDTO> srcRangePortArray = new ArrayList<>();
                        ServiceParamDTO serviceParamDTO = new ServiceParamDTO();
                        getServicePort(portOp, portList, srcSinglePortArray, srcSinglePortStrArray, srcRangePortArray);
                        if (CollectionUtils.isNotEmpty(srcSinglePortArray)) {
                            serviceParamDTO.setSrcSinglePortArray(srcSinglePortArray.toArray(new Integer[srcSinglePortArray.size()]));
                        }
                        if (CollectionUtils.isNotEmpty(srcSinglePortStrArray)) {
                            serviceParamDTO.setSrcSinglePortStrArray(srcSinglePortStrArray.toArray(new String[srcSinglePortStrArray.size()]));
                        }
                        if (CollectionUtils.isNotEmpty(srcRangePortArray)) {
                            serviceParamDTO.setSrcRangePortArray(srcRangePortArray.toArray(new PortRangeDTO[srcRangePortArray.size()]));
                        }
                        preServiceParamDTOS.add(serviceParamDTO);
                    }
                }
            }

            if(natClause.containsKey("postSrcPortSpec")){
                JSONArray postSrcPortSpec = natClause.getJSONArray("postSrcPortSpec");
                for (int i = 0; i < postSrcPortSpec.size(); i++) {
                    JSONObject srcPortSpec = postSrcPortSpec.getJSONObject(i);
                    if (srcPortSpec.containsKey("portValues")) {
                        JSONArray portValues = srcPortSpec.getJSONArray("portValues");
                        List<String> portList = portValues.stream().map(t -> (String) t).collect(Collectors.toList());
                        String portOp = srcPortSpec.getString("portOp");
                        List<Integer> srcSinglePortArray = new ArrayList<>();
                        List<String> srcSinglePortStrArray = new ArrayList<>();
                        List<PortRangeDTO> srcRangePortArray = new ArrayList<>();
                        ServiceParamDTO serviceParamDTO = new ServiceParamDTO();
                        getServicePort(portOp, portList, srcSinglePortArray, srcSinglePortStrArray, srcRangePortArray);
                        if (CollectionUtils.isNotEmpty(srcSinglePortArray)) {
                            serviceParamDTO.setSrcSinglePortArray(srcSinglePortArray.toArray(new Integer[srcSinglePortArray.size()]));
                        }
                        if (CollectionUtils.isNotEmpty(srcSinglePortStrArray)) {
                            serviceParamDTO.setSrcSinglePortStrArray(srcSinglePortStrArray.toArray(new String[srcSinglePortStrArray.size()]));
                        }
                        if (CollectionUtils.isNotEmpty(srcRangePortArray)) {
                            serviceParamDTO.setSrcRangePortArray(srcRangePortArray.toArray(new PortRangeDTO[srcRangePortArray.size()]));
                        }
                        postServiceParamDTOS.add(serviceParamDTO);
                    }
                }
            }

            if(natClause.containsKey("preDstPortSpec")){
                JSONArray preDstPortSpec = natClause.getJSONArray("preDstPortSpec");
                for (int i = 0; i < preDstPortSpec.size(); i++) {
                    JSONObject dstPortSpec = preDstPortSpec.getJSONObject(i);
                    if (dstPortSpec.containsKey("portValues")) {
                        JSONArray portValues = dstPortSpec.getJSONArray("portValues");
                        List<String> portList = portValues.stream().map(t -> (String) t).collect(Collectors.toList());
                        String portOp = dstPortSpec.getString("portOp");
                        List<Integer> dstSinglePortArray = new ArrayList<>();
                        List<String> dstSinglePortStrArray = new ArrayList<>();
                        List<PortRangeDTO> dstRangePortArray = new ArrayList<>();
                        ServiceParamDTO serviceParamDTO = new ServiceParamDTO();
                        getServicePort(portOp, portList, dstSinglePortArray, dstSinglePortStrArray, dstRangePortArray);
                        if (CollectionUtils.isNotEmpty(dstSinglePortArray)) {
                            serviceParamDTO.setDstSinglePortArray(dstSinglePortArray.toArray(new Integer[dstSinglePortArray.size()]));
                        }
                        if (CollectionUtils.isNotEmpty(dstSinglePortStrArray)) {
                            serviceParamDTO.setDstSinglePortStrArray(dstSinglePortStrArray.toArray(new String[dstSinglePortStrArray.size()]));
                        }
                        if (CollectionUtils.isNotEmpty(dstRangePortArray)) {
                            serviceParamDTO.setDstRangePortArray(dstRangePortArray.toArray(new PortRangeDTO[dstRangePortArray.size()]));
                        }
                        preServiceParamDTOS.add(serviceParamDTO);
                    }
                }
            }

            if(natClause.containsKey("postDstPortSpec")){
                JSONArray postDstPortSpec = natClause.getJSONArray("postDstPortSpec");
                for (int i = 0; i < postDstPortSpec.size(); i++) {
                    JSONObject dstPortSpec = postDstPortSpec.getJSONObject(i);
                    if (dstPortSpec.containsKey("portValues")) {
                        JSONArray portValues = dstPortSpec.getJSONArray("portValues");
                        List<String> portList = portValues.stream().map(t -> (String) t).collect(Collectors.toList());
                        String portOp = dstPortSpec.getString("portOp");
                        List<Integer> dstSinglePortArray = new ArrayList<>();
                        List<String> dstSinglePortStrArray = new ArrayList<>();
                        List<PortRangeDTO> dstRangePortArray = new ArrayList<>();
                        ServiceParamDTO serviceParamDTO = new ServiceParamDTO();
                        getServicePort(portOp, portList, dstSinglePortArray, dstSinglePortStrArray, dstRangePortArray);
                        if (CollectionUtils.isNotEmpty(dstSinglePortArray)) {
                            serviceParamDTO.setDstSinglePortArray(dstSinglePortArray.toArray(new Integer[dstSinglePortArray.size()]));
                        }
                        if (CollectionUtils.isNotEmpty(dstSinglePortStrArray)) {
                            serviceParamDTO.setDstSinglePortStrArray(dstSinglePortStrArray.toArray(new String[dstSinglePortStrArray.size()]));
                        }
                        if (CollectionUtils.isNotEmpty(dstRangePortArray)) {
                            serviceParamDTO.setDstRangePortArray(dstRangePortArray.toArray(new PortRangeDTO[dstRangePortArray.size()]));
                        }
                        postServiceParamDTOS.add(serviceParamDTO);
                    }
                }
            }
            if(natClause.containsKey("protocols")){
                JSONArray protocols = natClause.getJSONArray("protocols");
                for (int i = 0; i < protocols.size(); i++) {
                    JSONObject protocolObj = protocols.getJSONObject(i);
                    if (protocolObj.containsKey("nameRef")) {
                        String nameRef = protocolObj.getString("nameRef");
                        nameRef = nameRef.substring(0, nameRef.length() - 2);
                        preExistServiceNameList.add(nameRef);
                        continue;
                    }
                    String protocolName = protocolObj.getString("protocolName");
                    String protocolNum = protocolObj.getString("protocolNum");
                    if (StringUtils.isNotBlank(protocolName)) {
                        protocolNum = ProtocolUtils.getProtocolNumberByName(protocolName);
                    }
                    if (StringUtils.isNotBlank(protocolNum)) {
                        ServiceParamDTO serviceParamDTO = new ServiceParamDTO();
                        serviceParamDTO.setProtocol(ProtocolTypeEnum.getByCode(Integer.parseInt(protocolNum)));
                        preServiceParamDTOS.add(serviceParamDTO);
                    }
                }
            }
            natPolicyParamDTO.setServiceParam(preServiceParamDTOS.toArray(new ServiceParamDTO[0]));
            natPolicyParamDTO.setRefServiceObject(preExistServiceNameList.toArray(new String[preExistServiceNameList.size()]));
            natPolicyParamDTO.setPostServiceParam(postServiceParamDTOS.toArray(new ServiceParamDTO[0]));
            natPolicyParamDTO.setPostRefServiceObject(postExistServiceNameList.toArray(new String[postExistServiceNameList.size()]));
        }
        return natPolicyParamDTO;
    }

    public void getServiceParamDTO(List<String> existServiceNameList, List<ServiceParamDTO> serviceParamDTOS, JSONObject service) {
        if (service.containsKey("nameRef")) {
            String nameRef = service.getString("nameRef");
            nameRef = nameRef.substring(0, nameRef.length() - 2);
            existServiceNameList.add(nameRef);
        } else if (service.containsKey("serviceValue")) {

            JSONObject serviceValue = (JSONObject) service.get("serviceValue");
            String type = serviceValue.getString("type");
            String protocolName = serviceValue.getString("protocolName");
            String protocolNum = serviceValue.getString("protocolNum");
            if (StringUtils.isNotBlank(protocolName)) {
                protocolNum = ProtocolUtils.getProtocolNumberByName(protocolName);
            }
            if (StringUtils.isNotBlank(protocolNum)) {
                ServiceParamDTO serviceParamDTO = new ServiceParamDTO();
                serviceParamDTO.setProtocol(ProtocolTypeEnum.getByCode(Integer.parseInt(protocolNum)));
                if (serviceValue.containsKey("portValues")) {
                    JSONArray portValues = serviceValue.getJSONArray("portValues");
                    List<String> portList = portValues.stream().map(t -> (String) t).collect(Collectors.toList());
                    String portOp = serviceValue.getString("portOp");
                    List<Integer> dstSinglePortArray = new ArrayList<>();
                    List<String> dstSinglePortStrArray = new ArrayList<>();
                    List<PortRangeDTO> dstRangePortArray = new ArrayList<>();
                    getServicePort(portOp, portList, dstSinglePortArray, dstSinglePortStrArray, dstRangePortArray);
                    if (CollectionUtils.isNotEmpty(dstSinglePortArray)) {
                        serviceParamDTO.setDstSinglePortArray(dstSinglePortArray.toArray(new Integer[dstSinglePortArray.size()]));
                    }
                    if (CollectionUtils.isNotEmpty(dstSinglePortStrArray)) {
                        serviceParamDTO.setDstSinglePortStrArray(dstSinglePortStrArray.toArray(new String[dstSinglePortStrArray.size()]));
                    }
                    if (CollectionUtils.isNotEmpty(dstRangePortArray)) {
                        serviceParamDTO.setDstRangePortArray(dstRangePortArray.toArray(new PortRangeDTO[dstRangePortArray.size()]));
                    }
                }
                serviceParamDTOS.add(serviceParamDTO);
            }

        }
    }

    /**
     * 构建安全策略原子化命令行参数
     * @param ruleListRO
     * @return
     */
    public PolicyParamDTO buildPolicyParam(DeviceFilterRuleListRO ruleListRO){

        PolicyParamDTO policyParamDTO = new PolicyParamDTO();

        if (ruleListRO.getInInterfaceGroupRefs() != null && ruleListRO.getInInterfaceGroupRefs().size() > 0) {
            policyParamDTO.setSrcZone(new ZoneParamDTO(ruleListRO.getInInterfaceGroupRefs().get(0)));
        }
//        dto.setSrcIp("");
        if (ruleListRO.getOutInterfaceGroupRefs() != null && ruleListRO.getOutInterfaceGroupRefs().size() > 0) {
            policyParamDTO.setDstZone(new ZoneParamDTO(ruleListRO.getOutInterfaceGroupRefs().get(0)));
        }
//        dto.setDstItf("");

        String name = StringUtils.isBlank(ruleListRO.getName())?ruleListRO.getRuleId():ruleListRO.getName();
        policyParamDTO.setName(name);
        if(StringUtils.isNotBlank(ruleListRO.getRuleId())){
            policyParamDTO.setId(ruleListRO.getRuleId());
        }/* else {
            policyParamDTO.setId(IdGen.getRandomNumberString());
        }*/
        policyParamDTO.setAction(StringUtils.isBlank(ruleListRO.getAction())?"permit":ruleListRO.getAction());
        policyParamDTO.setDescription(ruleListRO.getDescription());

        policyParamDTO.setAgeingTime(ruleListRO.getIdleTimeout());
        String filterTimeGroupName = ruleListRO.getFilterTimeGroupName();
        if(StringUtils.isNotBlank(filterTimeGroupName)){
            policyParamDTO.setRefTimeObject(new String[]{filterTimeGroupName});
        }
        JSONObject matchClause = ruleListRO.getMatchClause();
        if (matchClause != null) {
            if (matchClause.containsKey("services")) {
                JSONArray services = matchClause.getJSONArray("services");
                List<String> existServiceNameList = new ArrayList<>();
                List<ServiceParamDTO> serviceParamDTOS = new ArrayList<>();
                for (int i = 0; i < services.size(); i++) {
                    JSONObject service = services.getJSONObject(i);

                    getServiceParamDTO(existServiceNameList, serviceParamDTOS, service);
                }
                policyParamDTO.setServiceParam(serviceParamDTOS.toArray(new ServiceParamDTO[0]));
                policyParamDTO.setRefServiceObject(existServiceNameList.toArray(new String[existServiceNameList.size()]));
            }


            if (matchClause.containsKey("srcIp")) {
                RuleIPTypeEnum typeEnum = RuleIPTypeEnum.IP4;
                JSONArray srcIpArray = matchClause.getJSONArray("srcIp");
                List<String> preExistSrcAddressList = new ArrayList<>();
                List<String> hostList = new ArrayList<>();
                List<String> singleIpList = new ArrayList<>();
                List<IpAddressRangeDTO> rangeDTOS = new ArrayList<>();
                List<IpAddressSubnetStrDTO> subnetStrDTOS = new ArrayList<>();
                List<IpAddressSubnetIntDTO> subnetIntDTOS = new ArrayList<>();
                for (int i = 0; i < srcIpArray.size(); i++) {
                    JSONObject srcIp = srcIpArray.getJSONObject(i);
                    typeEnum = getIpAddressParamDTO(preExistSrcAddressList, hostList, singleIpList, rangeDTOS, subnetStrDTOS, subnetIntDTOS, srcIp);

                }
                if(CollectionUtils.isNotEmpty(singleIpList) || CollectionUtils.isNotEmpty(rangeDTOS)
                        || CollectionUtils.isNotEmpty(subnetStrDTOS) || CollectionUtils.isNotEmpty(subnetIntDTOS)){
                    IpAddressParamDTO srcIpParam = new IpAddressParamDTO();
                    srcIpParam.setSingleIpArray(singleIpList.toArray(new String[singleIpList.size()]));
                    srcIpParam.setRangIpArray(rangeDTOS.toArray(new IpAddressRangeDTO[rangeDTOS.size()]));
                    srcIpParam.setSubnetStrIpArray(subnetStrDTOS.toArray(new IpAddressSubnetStrDTO[subnetStrDTOS.size()]));
                    srcIpParam.setSubnetIntIpArray(subnetIntDTOS.toArray(new IpAddressSubnetIntDTO[subnetIntDTOS.size()]));
                    srcIpParam.setHosts(hostList.toArray(new String[hostList.size()]));
                    srcIpParam.setIpTypeEnum(typeEnum);
                    policyParamDTO.setSrcIp(srcIpParam);
                }
                policyParamDTO.setSrcRefIpAddressObject(preExistSrcAddressList.toArray(new String[preExistSrcAddressList.size()]));
            }

            if (matchClause.containsKey("dstIp")) {
                RuleIPTypeEnum typeEnum = RuleIPTypeEnum.IP4;
                JSONArray dstIpArray = matchClause.getJSONArray("dstIp");
                List<String> existDstAddressList = new ArrayList<>();
                List<String> hostList = new ArrayList<>();
                List<String> singleIpList = new ArrayList<>();
                List<IpAddressRangeDTO> rangeDTOS = new ArrayList<>();
                List<IpAddressSubnetStrDTO> subnetStrDTOS = new ArrayList<>();
                List<IpAddressSubnetIntDTO> subnetIntDTOS = new ArrayList<>();
                for (int i = 0; i < dstIpArray.size(); i++) {
                    JSONObject dstIp = dstIpArray.getJSONObject(i);

                    if (dstIp.containsKey("nameRef")) {
                        String nameRef = dstIp.getString("nameRef");
                        nameRef = nameRef.substring(0, nameRef.length()-2);
                        existDstAddressList.add(nameRef);
                    }
                    String type = dstIp.getString("type");
                    if (Constants.SUBNET.equals(type)) {
                        String ip4Prefix = dstIp.getString("ip4Prefix");
                        String ip4Length = dstIp.getString("ip4Length");
                        if(StringUtils.isNotBlank(ip4Prefix) && StringUtils.isNotBlank(ip4Length)){
                            IpAddressSubnetIntDTO subnetIntDTO = new IpAddressSubnetIntDTO();
                            subnetIntDTO.setIp(ip4Prefix);
                            subnetIntDTO.setMask(Integer.parseInt(ip4Length));
                            subnetIntDTO.setType(MaskTypeEnum.mask);
                            subnetIntDTOS.add(subnetIntDTO);
                        }

                        String ip6Prefix = dstIp.getString("ip6Prefix");
                        String ip6Length = dstIp.getString("ip6Length");
                        if(StringUtils.isNotBlank(ip6Prefix) && StringUtils.isNotBlank(ip6Length)){
                            typeEnum = RuleIPTypeEnum.IP6;
                            IpAddressSubnetIntDTO subnetIntDTO = new IpAddressSubnetIntDTO();
                            subnetIntDTO.setIp(ip6Prefix);
                            subnetIntDTO.setMask(Integer.parseInt(ip6Length));
                            subnetIntDTO.setType(MaskTypeEnum.mask);
                            subnetIntDTOS.add(subnetIntDTO);
                        }

                    }else if ("INTERFACE".equals(type)) {

                    }else if (Constants.HOST_IP.equals(type)) {
                        List<String> ip4Addresses = dstIp.getObject("ip4Addresses",List.class);
                        if (ip4Addresses != null && ip4Addresses.size() > 0) {
                            singleIpList.addAll(ip4Addresses);
                        }

                        List<String> ip6Addresses = dstIp.getObject("ip6Addresses",List.class);
                        if (ip6Addresses != null && ip6Addresses.size() > 0) {
                            singleIpList.addAll(ip6Addresses);
                        }
                    }else if (Constants.RANGE.equals(type)) {
                        Ip4RangeRO ip4Range = dstIp.getObject("ip4Range",Ip4RangeRO.class);
                        Ip4RangeRO ip6Range = dstIp.getObject("ip6Range",Ip4RangeRO.class);
                        if (ip4Range != null) {
                            rangeDTOS.add(getStartEndByJsonObject(ip4Range));
                        }
                        if (ip6Range != null) {
                            typeEnum = RuleIPTypeEnum.IP6;
                            rangeDTOS.add(getStartEndByJsonObject(ip6Range));
                        }
                    }else if (Constants.FQDN.equals(type)) {
                        hostList.add(dstIp.getString("fqdn"));
                    }else if (Constants.IP4WILDCARD.equals(type)) {
                        String ip4WildCardMask = dstIp.getString("ip4WildCardMask");
                        if (StringUtils.isNumeric(ip4WildCardMask)) {
                            ip4WildCardMask = String.valueOf(IpUtils.IPv4NumToString(Long.parseLong(ip4WildCardMask)));
                        }
                        IpAddressSubnetStrDTO subnetStrDTO = new IpAddressSubnetStrDTO();
                        subnetStrDTO.setIp(dstIp.getString("ip4Base"));
                        subnetStrDTO.setMask(ip4WildCardMask);
                        subnetStrDTO.setIpTypeEnum(RuleIPTypeEnum.IP4);
                        subnetStrDTO.setType(MaskTypeEnum.mask);
                        subnetStrDTOS.add(subnetStrDTO);
                    }

                }
                if(CollectionUtils.isNotEmpty(singleIpList) || CollectionUtils.isNotEmpty(rangeDTOS)
                        || CollectionUtils.isNotEmpty(subnetStrDTOS) || CollectionUtils.isNotEmpty(subnetIntDTOS) || CollectionUtils.isNotEmpty(hostList)){
                    IpAddressParamDTO dstIpParam = new IpAddressParamDTO();
                    dstIpParam.setSingleIpArray(singleIpList.toArray(new String[singleIpList.size()]));
                    dstIpParam.setRangIpArray(rangeDTOS.toArray(new IpAddressRangeDTO[rangeDTOS.size()]));
                    dstIpParam.setSubnetStrIpArray(subnetStrDTOS.toArray(new IpAddressSubnetStrDTO[subnetStrDTOS.size()]));
                    dstIpParam.setSubnetIntIpArray(subnetIntDTOS.toArray(new IpAddressSubnetIntDTO[subnetIntDTOS.size()]));
                    dstIpParam.setHosts(hostList.toArray(new String[hostList.size()]));
                    dstIpParam.setIpTypeEnum(typeEnum);
                    policyParamDTO.setDstIp(dstIpParam);
                }
                policyParamDTO.setDstRefIpAddressObject(existDstAddressList.toArray(new String[existDstAddressList.size()]));
            }
        }
        return policyParamDTO;
    }

    public RuleIPTypeEnum getIpAddressParamDTO(List<String> existSrcAddressList, List<String> hostList, List<String> singleIpList, List<IpAddressRangeDTO> rangeDTOS, List<IpAddressSubnetStrDTO> subnetStrDTOS, List<IpAddressSubnetIntDTO> subnetIntDTOS, JSONObject srcIp) {
        RuleIPTypeEnum typeEnum = RuleIPTypeEnum.IP4;
        if (srcIp.containsKey("nameRef")) {
            String nameRef = srcIp.getString("nameRef");
            nameRef = nameRef.substring(0, nameRef.length() - 2);
            existSrcAddressList.add(nameRef);
        }
        String type = srcIp.getString("type");
        if (Constants.SUBNET.equals(type)) {
            String ip4Prefix = srcIp.getString("ip4Prefix");
            String ip4Length = srcIp.getString("ip4Length");
            if (StringUtils.isNotBlank(ip4Prefix) && StringUtils.isNotBlank(ip4Length)) {
                IpAddressSubnetIntDTO subnetIntDTO = new IpAddressSubnetIntDTO();
                subnetIntDTO.setIp(ip4Prefix);
                subnetIntDTO.setMask(Integer.parseInt(ip4Length));
                subnetIntDTO.setType(MaskTypeEnum.mask);
                subnetIntDTOS.add(subnetIntDTO);
            }

            String ip6Prefix = srcIp.getString("ip6Prefix");
            String ip6Length = srcIp.getString("ip6Length");
            if (StringUtils.isNotBlank(ip6Prefix) && StringUtils.isNotBlank(ip6Length)) {
                typeEnum = RuleIPTypeEnum.IP6;
                IpAddressSubnetIntDTO subnetIntDTO = new IpAddressSubnetIntDTO();
                subnetIntDTO.setIp(ip6Prefix);
                subnetIntDTO.setMask(Integer.parseInt(ip6Length));
                subnetIntDTO.setType(MaskTypeEnum.mask);
                subnetIntDTOS.add(subnetIntDTO);
            }

        } else if ("INTERFACE".equals(type)) {

        } else if (Constants.HOST_IP.equals(type)) {
            List<String> ip4Addresses = srcIp.getObject("ip4Addresses", List.class);
            if (ip4Addresses != null && ip4Addresses.size() > 0) {
                singleIpList.addAll(ip4Addresses);
            }

            List<String> ip6Addresses = srcIp.getObject("ip6Addresses", List.class);
            if (ip6Addresses != null && ip6Addresses.size() > 0) {
                typeEnum = RuleIPTypeEnum.IP6;
                singleIpList.addAll(ip6Addresses);
            }
        } else if (Constants.RANGE.equals(type)) {
            Ip4RangeRO ip4Range = srcIp.getObject("ip4Range", Ip4RangeRO.class);
            Ip4RangeRO ip6Range = srcIp.getObject("ip6Range", Ip4RangeRO.class);
            if (ip4Range != null) {
                rangeDTOS.add(getStartEndByJsonObject(ip4Range));
            }
            if (ip6Range != null) {
                typeEnum = RuleIPTypeEnum.IP6;
                rangeDTOS.add(getStartEndByJsonObject(ip6Range));
            }
        } else if (Constants.FQDN.equals(type)) {
            String fqdn = srcIp.getString("fqdn");
            if (TotemsIp4Utils.isIp4(fqdn)) {
                singleIpList.add(fqdn);
            } else if (TotemsIp6Utils.isIp6(fqdn)) {
                typeEnum = RuleIPTypeEnum.IP6;
                singleIpList.add(fqdn);
            } else {
                hostList.add(srcIp.getString("fqdn"));
            }
        } else if (Constants.IP4WILDCARD.equals(type)) {
            String ip4WildCardMask = srcIp.getString("ip4WildCardMask");
            if (StringUtils.isNumeric(ip4WildCardMask)) {
                ip4WildCardMask = String.valueOf(IpUtils.IPv4NumToString(Long.parseLong(ip4WildCardMask)));
            }
            IpAddressSubnetStrDTO subnetStrDTO = new IpAddressSubnetStrDTO();
            subnetStrDTO.setIp(srcIp.getString("ip4Base"));
            subnetStrDTO.setMask(ip4WildCardMask);
            subnetStrDTO.setIpTypeEnum(RuleIPTypeEnum.IP4);
            subnetStrDTO.setType(MaskTypeEnum.mask);
            subnetStrDTOS.add(subnetStrDTO);
        }
        return typeEnum;
    }

    /**
     * 构建服务对象原子化命令行参数
     * @param serviceGroupObjectRO
     * @return
     */
    public List<ServiceParamDTO> buildServiceParamDTO(ServiceGroupObjectRO serviceGroupObjectRO) {
        List<IncludeFilterServicesRO> includeFilterServicesROS = serviceGroupObjectRO.getIncludeFilterServices();
        List<ServiceParamDTO> serviceParamDTOList = new ArrayList<>();
        if(CollectionUtils.isEmpty(includeFilterServicesROS)){
            return serviceParamDTOList;
        }
        for (IncludeFilterServicesRO includeFilterServicesRO : includeFilterServicesROS) {
            ServiceParamDTO serviceParamDTO = new ServiceParamDTO();
            String type = includeFilterServicesRO.getType();

            if (Constants.SERVICE_OTHER.equals(type) || Constants.SERVICE_TCP_UDP.equals(type) || Constants.SERVICE_ICMP.equals(type)) {
                String protocolName = includeFilterServicesRO.getProtocolName();
                String protocolNum = includeFilterServicesRO.getProtocolNum();
                if (StringUtils.isBlank(protocolName) && StringUtils.isNotBlank(protocolNum)) {
                    serviceParamDTO.setProtocol(ProtocolTypeEnum.getByCode(Integer.parseInt(protocolNum)));
                } else if (StringUtils.isNotBlank(protocolName) && StringUtils.isBlank(protocolNum)) {
                    serviceParamDTO.setProtocol(ProtocolTypeEnum.getByType(protocolName));
                }
                if(serviceParamDTO.getProtocol() == null){
                    continue;
                }

                List<String> protocolAttachTypeArray = new ArrayList<>();
                String icmpCode = includeFilterServicesRO.getIcmpCode();
                String icmpStr = includeFilterServicesRO.getIcmpStr();
                if(StringUtils.isNotBlank(icmpCode)){
                    protocolAttachTypeArray.add(icmpCode);
                } else if(StringUtils.isNotBlank(icmpStr)){
                    protocolAttachTypeArray.add(icmpCode);
                }
                if(CollectionUtils.isNotEmpty(protocolAttachTypeArray)){
                    serviceParamDTO.setProtocolAttachTypeArray(protocolAttachTypeArray.toArray(new String[protocolAttachTypeArray.size()]));
                }

                List<String> protocolAttachCodeArray = new ArrayList<>();
                String icmpType = includeFilterServicesRO.getIcmpType();
                Integer icmpTypeNum = includeFilterServicesRO.getIcmpTypeNum();
                if(StringUtils.isNotBlank(icmpType)){
                    protocolAttachCodeArray.add(icmpType);
                } else if(icmpTypeNum != null){
                    protocolAttachCodeArray.add(icmpTypeNum.toString());
                }
                if(CollectionUtils.isNotEmpty(protocolAttachCodeArray)){
                    serviceParamDTO.setProtocolAttachCodeArray(protocolAttachCodeArray.toArray(new String[protocolAttachCodeArray.size()]));
                }

                String dstPortOp = includeFilterServicesRO.getDstPortOp();
                List<String> dstPortValues = includeFilterServicesRO.getDstPortValues();
                String dstPorts = PortFormatUtil.getPortValueStr(dstPortOp, dstPortValues);

                List<Integer> dstSinglePortArray = new ArrayList<>();
                List<String> dstSinglePortStrArray = new ArrayList<>();
                List<PortRangeDTO> dstRangePortArray = new ArrayList<>();
                getServicePort(dstPortOp, dstPortValues, dstSinglePortArray, dstSinglePortStrArray, dstRangePortArray);
                getServicePort(includeFilterServicesRO.getPortOp(), includeFilterServicesRO.getPortValues(), dstSinglePortArray, dstSinglePortStrArray, dstRangePortArray);
                if(CollectionUtils.isNotEmpty(dstSinglePortArray)){
                    serviceParamDTO.setDstSinglePortArray(dstSinglePortArray.toArray(new Integer[dstSinglePortArray.size()]));
                }
                if(CollectionUtils.isNotEmpty(dstSinglePortStrArray)){
                    serviceParamDTO.setDstSinglePortStrArray(dstSinglePortStrArray.toArray(new String[dstSinglePortStrArray.size()]));
                }
                if(CollectionUtils.isNotEmpty(dstRangePortArray)){
                    serviceParamDTO.setDstRangePortArray(dstRangePortArray.toArray(new PortRangeDTO[dstRangePortArray.size()]));
                }

                //源端口
                String srcPortOp = includeFilterServicesRO.getSrcPortOp();
                List<String> srcPortValues = includeFilterServicesRO.getSrcPortValues();
                String srcPort = PortFormatUtil.getPortValueStr(srcPortOp, srcPortValues);

                List<Integer> srcSinglePortArray = new ArrayList<>();
                List<String> srcSinglePortStrArray = new ArrayList<>();
                List<PortRangeDTO> srcRangePortArray = new ArrayList<>();
                getServicePort(srcPortOp, srcPortValues, srcSinglePortArray, srcSinglePortStrArray, srcRangePortArray);
                if(CollectionUtils.isNotEmpty(srcSinglePortArray)){
                    serviceParamDTO.setSrcSinglePortArray(srcSinglePortArray.toArray(new Integer[srcSinglePortArray.size()]));
                }
                if(CollectionUtils.isNotEmpty(srcSinglePortStrArray)){
                    serviceParamDTO.setSrcSinglePortStrArray(srcSinglePortStrArray.toArray(new String[srcSinglePortStrArray.size()]));
                }
                if(CollectionUtils.isNotEmpty(srcRangePortArray)){
                    serviceParamDTO.setSrcRangePortArray(srcRangePortArray.toArray(new PortRangeDTO[srcRangePortArray.size()]));
                }
                serviceParamDTOList.add(serviceParamDTO);
            }
        }
        return serviceParamDTOList;
    }

    public void getServicePort(String portOp, List<String> portValues, List<Integer> singlePortArray, List<String> singlePortStrArray, List<PortRangeDTO> rangePortArray) {
        if (portValues != null && portValues.size() != 0) {
            if (portValues.size() == 2 && portOp.equalsIgnoreCase("RANGE")) {
                PortRangeDTO rangeDTO = new PortRangeDTO(Integer.parseInt(portValues.get(0)), Integer.parseInt(portValues.get(1)));
                rangePortArray.add(rangeDTO);
            } else {
                int m;
                if (portOp.equalsIgnoreCase("MULTI_RANGE")) {
                    for (m = 0; m < portValues.size(); m += 2) {
                        String startPort = (String) portValues.get(m);
                        String endPort = (String) portValues.get(m + 1);
                        if (startPort.equals(endPort)) {
                            if (StringUtils.isNumeric(endPort)) {
                                singlePortArray.add(Integer.parseInt(endPort));
                            } else {
                                singlePortStrArray.add(endPort);
                            }
                        } else {
                            PortRangeDTO rangeDTO = new PortRangeDTO(Integer.parseInt(startPort), Integer.parseInt(endPort));
                            rangePortArray.add(rangeDTO);
                        }
                    }
                } else {
                    for (m = 0; m < portValues.size(); ++m) {
                        String portStr = portValues.get(m);
                        if (StringUtils.isNumeric(portStr)) {
                            singlePortArray.add(Integer.parseInt(portStr));
                        } else {
                            singlePortStrArray.add(portStr);
                        }
                    }
                }
            }
        }
    }

    /**
     * 构建地址对象原子化命令行参数
     * @param netWorkGroupObjectRO
     * @return
     */
    public IpAddressParamDTO buildIpAddressParam(NetWorkGroupObjectRO netWorkGroupObjectRO) {
        List<IncludeItemsRO> includeItemsROS = netWorkGroupObjectRO.getIncludeItems();
        List<String> includeItemsNames = netWorkGroupObjectRO.getIncludeItemNames();
        //获取类型
        IpAddressParamDTO paramDTO = new IpAddressParamDTO();
        if(CollectionUtils.isEmpty(includeItemsROS) && CollectionUtils.isEmpty(includeItemsNames)){
            return new IpAddressParamDTO();
        }

        if(CollectionUtils.isNotEmpty(includeItemsNames)){
            String[] names = new String[includeItemsNames.size()];
            for(int i=0;i<includeItemsNames.size();i++){
                names[i] = includeItemsNames.get(i);
            }
            paramDTO.setObjectNameRefArray(names);
        }
        if(CollectionUtils.isNotEmpty(includeItemsROS)){
            List<IpAddressSubnetIntDTO> subnetIntDTOS = new ArrayList<>();
            List<String> singleIpList = new ArrayList<>();
            List<IpAddressRangeDTO> rangeDTOS = new ArrayList<>();
            List<IpAddressSubnetStrDTO> subnetStrDTOS = new ArrayList<>();
            List<String> objectGroupNameList = new ArrayList<>();
            List<String> objectNameList = new ArrayList<>();
            List<String> hosts = new ArrayList<>();
            RuleIPTypeEnum typeEnum = RuleIPTypeEnum.IP4;
            for (IncludeItemsRO ro : includeItemsROS) {
                String type = ro.getType();
                if(type.equals(Constants.ANY4) || type.equals(Constants.ANY)) {
                    paramDTO.setName(type);
                    paramDTO.setIpTypeEnum(typeEnum);
                    return paramDTO;
                }else if (Constants.SUBNET.equals(type)) {
                    String ip4Prefix = ro.getIp4Prefix();
                    String ip4Length = ro.getIp4Length();
                    if(StringUtils.isNotBlank(ip4Prefix) && StringUtils.isNotBlank(ip4Length)){
                        IpAddressSubnetIntDTO subnetIntDTO = new IpAddressSubnetIntDTO();
                        subnetIntDTO.setIp(ip4Prefix);
                        subnetIntDTO.setMask(Integer.parseInt(ip4Length));
                        subnetIntDTO.setType(MaskTypeEnum.mask);
                        subnetIntDTOS.add(subnetIntDTO);
                    }

                    String ip6Prefix = ro.getIp6Prefix();
                    String ip6Length = ro.getIp6Length();
                    if(StringUtils.isNotBlank(ip6Prefix) && StringUtils.isNotBlank(ip6Length)){
                        typeEnum = RuleIPTypeEnum.IP6;
                        IpAddressSubnetIntDTO subnetIntDTO = new IpAddressSubnetIntDTO();
                        subnetIntDTO.setIp(ip6Prefix);
                        subnetIntDTO.setMask(Integer.parseInt(ip6Length));
                        subnetIntDTO.setType(MaskTypeEnum.mask);
                        subnetIntDTOS.add(subnetIntDTO);
                    }

                }else if ("INTERFACE".equals(type)) {

                }else if (Constants.HOST_IP.equals(type)) {
                    List<String> ip4Addresses = ro.getIp4Addresses();
                    if (ip4Addresses != null && ip4Addresses.size() > 0) {
                        singleIpList.addAll(ip4Addresses);
                    }

                    List<String> ip6Addresses = ro.getIp6Addresses();
                    if (ip6Addresses != null && ip6Addresses.size() > 0) {
                        typeEnum = RuleIPTypeEnum.IP6;
                        singleIpList.addAll(ip6Addresses);
                    }
                }else if (Constants.RANGE.equals(type)) {
                    Ip4RangeRO ip4Range = ro.getIp4Range();
                    Ip4RangeRO ip6Range = ro.getIp6Range();
                    if (ip4Range != null) {
                        rangeDTOS.add(getStartEndByJsonObject(ip4Range));
                    }
                    if (ip6Range != null) {
                        typeEnum = RuleIPTypeEnum.IP6;
                        rangeDTOS.add(getStartEndByJsonObject(ip6Range));
                    }
                }else if (Constants.FQDN.equals(type)) {
                    hosts.add(ro.getFqdn());
//            formatIpStr += "域名: " + ro.getFqdn() + ";";
                }else if (Constants.IP4WILDCARD.equals(type)) {
                    String ip4WildCardMask = ro.getIp4WildCardMask();
                    if (StringUtils.isNumeric(ip4WildCardMask)) {
                        ip4WildCardMask = String.valueOf(IpUtils.IPv4NumToString(Long.parseLong(ip4WildCardMask)));
                    }
                    IpAddressSubnetStrDTO subnetStrDTO = new IpAddressSubnetStrDTO();
                    subnetStrDTO.setIp(ro.getIp4Base());
                    subnetStrDTO.setMask(ip4WildCardMask);
                    subnetStrDTO.setIpTypeEnum(RuleIPTypeEnum.IP4);
                    subnetStrDTO.setType(MaskTypeEnum.mask);
                    subnetStrDTOS.add(subnetStrDTO);
                }else if (Constants.OBJECT_GROUP.equals(type)) {
                    objectNameList.add(ro.getNameRef());    //统一放到引用地址对象里面
                }else if (type.equals("interface") || type.equals("acl")) {

                }else if (Constants.OBJECT.equals(type)) {
                    objectNameList.add(ro.getNameRef());
                }
            }
            if(CollectionUtils.isNotEmpty(subnetIntDTOS)){
                paramDTO.setSubnetIntIpArray(subnetIntDTOS.toArray(new IpAddressSubnetIntDTO[subnetIntDTOS.size()]));
            }
            if(CollectionUtils.isNotEmpty(singleIpList)){
                paramDTO.setSingleIpArray(singleIpList.toArray(new String[singleIpList.size()]));
            }
            if(CollectionUtils.isNotEmpty(rangeDTOS)){
                paramDTO.setRangIpArray(rangeDTOS.toArray(new IpAddressRangeDTO[rangeDTOS.size()]));
            }
            if(CollectionUtils.isNotEmpty(subnetStrDTOS)){
                paramDTO.setSubnetStrIpArray(subnetStrDTOS.toArray(new IpAddressSubnetStrDTO[subnetStrDTOS.size()]));
            }
            List<String> includeGroupNames = netWorkGroupObjectRO.getIncludeGroupNames();
            if(CollectionUtils.isNotEmpty(includeGroupNames)){
                objectGroupNameList.addAll(includeGroupNames);
            }
            if(CollectionUtils.isNotEmpty(objectGroupNameList)){
                paramDTO.setObjectGroupNameRefArray(objectGroupNameList.toArray(new String[objectGroupNameList.size()]));
            }
            List<String> includeItemNames = netWorkGroupObjectRO.getIncludeItemNames();
            if(CollectionUtils.isNotEmpty(includeItemNames)){
                objectNameList.addAll(includeItemNames);
            }
            if(CollectionUtils.isNotEmpty(objectNameList)){
                paramDTO.setObjectNameRefArray(objectNameList.toArray(new String[objectNameList.size()]));
            }
            if(CollectionUtils.isNotEmpty(hosts)){
                paramDTO.setHosts(hosts.toArray(new String[hosts.size()]));
            }
            paramDTO.setIpTypeEnum(typeEnum);
        }

        return paramDTO;
    }

    public IpAddressRangeDTO getStartEndByJsonObject(Ip4RangeRO rangeRO) {
        IpAddressRangeDTO ipAddressRangeDTO = new IpAddressRangeDTO();
        String start = rangeRO.getStart();
        String end = rangeRO.getEnd();
        if (StringUtils.isNotBlank(start)) {
            ipAddressRangeDTO.setStart(start);
        }
        if (StringUtils.isNotBlank(end)) {
            ipAddressRangeDTO.setEnd(end);
        }
        return ipAddressRangeDTO;
    }

}
