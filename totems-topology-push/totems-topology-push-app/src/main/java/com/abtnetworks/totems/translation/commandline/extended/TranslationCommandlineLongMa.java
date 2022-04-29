package com.abtnetworks.totems.translation.commandline.extended;

import com.abtnetworks.totems.command.line.dto.*;
import com.abtnetworks.totems.command.line.enums.MaskTypeEnum;
import com.abtnetworks.totems.command.line.enums.ProtocolTypeEnum;
import com.abtnetworks.totems.command.line.enums.RuleIPTypeEnum;
import com.abtnetworks.totems.command.line.enums.StatusTypeEnum;
import com.abtnetworks.totems.common.constant.Constants;
import com.abtnetworks.totems.common.mapper.TotemsJsonMapper;
import com.abtnetworks.totems.common.network.TotemsIp4Utils;
import com.abtnetworks.totems.common.network.TotemsIp6Utils;
import com.abtnetworks.totems.common.utils.IpUtils;
import com.abtnetworks.totems.common.utils.ProtocolUtils;
import com.abtnetworks.totems.translation.commandline.TranslationCommandline;
import com.abtnetworks.totems.vender.longma.nat.NatLongMaImpl;
import com.abtnetworks.totems.vender.longma.routing.RoutingLongMaImpl;
import com.abtnetworks.totems.vender.longma.security.SecurityLongMaImpl;
import com.abtnetworks.totems.whale.baseapi.ro.*;
import com.abtnetworks.totems.whale.policy.ro.RoutingEntriesRO;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @Author: WangCan
 * @Description
 * @Date: 2021/9/3
 */
@Slf4j
public class TranslationCommandlineLongMa extends TranslationCommandline {
    public TranslationCommandlineLongMa() {
        this.generatorBean = new SecurityLongMaImpl();
        this.natGeneratorBean = new NatLongMaImpl();
        this.routingGeneratorBean = new RoutingLongMaImpl();
    }


    /** 生成地址对象命令行
     * @param needAddNetWorkObjectList
     * @param parameterMap
     * @return
     * @throws Exception
     */
    @Override
    public String generateIpAddressObjectCommandLine(List<NetWorkGroupObjectRO> needAddNetWorkObjectList, Map<String, Object> parameterMap) throws Exception{
        StringBuffer objectCommandLine = new StringBuffer();
        for (NetWorkGroupObjectRO netWorkGroupObjectRO : needAddNetWorkObjectList) {
            IpAddressParamDTO paramDTO = this.buildIpAddressParam(netWorkGroupObjectRO);
            if(netWorkGroupObjectRO.getDeviceNetworkType() != null){
                //地址池
                objectCommandLine.append(natGeneratorBean.generateIpAddressPoolCommandLine(StatusTypeEnum.ADD,
                        netWorkGroupObjectRO.getName(),paramDTO,paramDTO.getObjectNameRefArray(),null,parameterMap,null));
            } else {
                //地址对象
                objectCommandLine.append(generatorBean.generateIpAddressObjectCommandLine(StatusTypeEnum.ADD, paramDTO.getIpTypeEnum(),
                        netWorkGroupObjectRO.getName(),null,paramDTO.getSingleIpArray(),paramDTO.getRangIpArray(),paramDTO.getSubnetIntIpArray(),paramDTO.getSubnetStrIpArray(),
                        null,paramDTO.getHosts(),paramDTO.getObjectNameRefArray(),netWorkGroupObjectRO.getDescription(),null,null,
                        parameterMap,null));
            }
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
    @Override
    public String generateIpAddressObjectGroupCommandLine(List<NetWorkGroupObjectRO> needAddNetWorkObjectList,Map<String, Object> parameterMap) throws Exception {
        StringBuffer objectCommandLine = new StringBuffer();
        for (NetWorkGroupObjectRO netWorkGroupObjectRO : needAddNetWorkObjectList) {
            IpAddressParamDTO paramDTO = this.buildIpAddressParam(netWorkGroupObjectRO);
            if (netWorkGroupObjectRO.getDeviceNetworkType() != null) {
                //地址池
                objectCommandLine.append(natGeneratorBean.generateIpAddressPoolCommandLine(StatusTypeEnum.ADD,
                        netWorkGroupObjectRO.getName(), paramDTO, paramDTO.getObjectNameRefArray(), paramDTO.getObjectGroupNameRefArray(), parameterMap, null));
            } else {
                //地址组对象
                objectCommandLine.append(generatorBean.generateIpAddressObjectGroupCommandLine(StatusTypeEnum.ADD, paramDTO.getIpTypeEnum(),
                        netWorkGroupObjectRO.getName(), null, paramDTO.getSingleIpArray(), paramDTO.getRangIpArray(), paramDTO.getSubnetIntIpArray(), paramDTO.getSubnetStrIpArray(),
                        null, paramDTO.getHosts(), paramDTO.getObjectNameRefArray(), paramDTO.getObjectGroupNameRefArray(), netWorkGroupObjectRO.getDescription(), null, null,
                        parameterMap, null));
            }

        }
        return objectCommandLine.toString();
    }

    @Override
    public Map<String,String> generateNatPolicyCommandLine(String taskUuid, DeviceFilterlistRO deviceFilterlistRO, List<DeviceFilterRuleListRO> needAddFilter, Map<String, Object> parameterMap) throws Exception {
        Map<String,String> generateSecurityResult = new HashMap<>();
        StringBuffer policyCommandLine = new StringBuffer();
        StringBuffer warningCommandLine = new StringBuffer();

        if(STATIC_TYPE.equals(needAddFilter.get(0).getNatClause().getString("type"))){
            StatusTypeEnum statusTypeEnum = StatusTypeEnum.ADD;
            if("ip6".equals(needAddFilter.get(0).getIpType())){
                statusTypeEnum = StatusTypeEnum.ADD_IPV6;
            }
            IpAddressParamDTO insideAddress = new IpAddressParamDTO();
            IpAddressParamDTO globalAddress = new IpAddressParamDTO();
            List<String> insideRefIpAddressObject = new ArrayList();
            List<String> globalRefIpAddressObject = new ArrayList();
            List<ServiceParamDTO> globalServiceParam = new ArrayList<>();
            NatPolicyParamDTO natPolicyParamDTO = this.buildNatPolicyParam(needAddFilter.get(0));
            String srcZone = null;
            for (DeviceFilterRuleListRO deviceFilterRuleListRO : needAddFilter) {
                JSONObject natClause = deviceFilterRuleListRO.getNatClause();
                if(CollectionUtils.isNotEmpty(deviceFilterRuleListRO.getInInterfaceGroupRefs())){
                    String tempSrcZone = deviceFilterRuleListRO.getInInterfaceGroupRefs().get(0);
                    if(StringUtils.isNotBlank(tempSrcZone)){
                        srcZone = tempSrcZone;
                    }
                }
                if(natClause.containsKey("preDstPortSpec") || natClause.containsKey("preSrcPortSpec")){
                    throw new RuntimeException("目标设备的静态Nat暂不支持端口转换");
                }
                if(natClause.containsKey("postDstPortSpecAsList")){

                    JSONArray postDstPortSpec = natClause.getJSONArray("postDstPortSpecAsList");
                    for (int i = 0; i < postDstPortSpec.size(); i++) {
                        JSONObject dstPortSpec = postDstPortSpec.getJSONObject(i);
                        if (dstPortSpec.containsKey("portValues")) {
                            JSONArray portValues = dstPortSpec.getJSONArray("portValues");
                            List<String> portList = portValues.stream().map(t -> (String) t).collect(Collectors.toList());
                            String portOp = dstPortSpec.getString("portOp");
                            List<Integer> dstSinglePortArray = new ArrayList<>();
                            List<String> dstSinglePortStrArray = new ArrayList<>();
                            List<PortRangeDTO> dstRangePortArray = new ArrayList<>();
                            ServiceParamDTO globalDstServiceParam = new ServiceParamDTO();
                            getServicePort(portOp, portList, dstSinglePortArray, dstSinglePortStrArray, dstRangePortArray);
                            if (CollectionUtils.isNotEmpty(dstSinglePortArray)) {
                                globalDstServiceParam.setDstSinglePortArray(dstSinglePortArray.toArray(new Integer[dstSinglePortArray.size()]));
                            }
                            if (CollectionUtils.isNotEmpty(dstSinglePortStrArray)) {
                                globalDstServiceParam.setDstSinglePortStrArray(dstSinglePortStrArray.toArray(new String[dstSinglePortStrArray.size()]));
                            }
                            if (CollectionUtils.isNotEmpty(dstRangePortArray)) {
                                globalDstServiceParam.setDstRangePortArray(dstRangePortArray.toArray(new PortRangeDTO[dstRangePortArray.size()]));
                            }
                            globalServiceParam.add(globalDstServiceParam);
                        }
                    }
                }
                if(natClause.containsKey("preSrcIPItemsAsList")){
                    RuleIPTypeEnum typeEnum = RuleIPTypeEnum.IP4;
                    JSONArray preSrcIPItemsAsList = natClause.getJSONArray("preSrcIPItemsAsList");
                    List<String> hostList = new ArrayList<>();
                    List<String> singleIpList = new ArrayList<>();
                    List<IpAddressRangeDTO> rangeDTOS = new ArrayList<>();
                    List<IpAddressSubnetStrDTO> subnetStrDTOS = new ArrayList<>();
                    List<IpAddressSubnetIntDTO> subnetIntDTOS = new ArrayList<>();
                    for (int i = 0; i < preSrcIPItemsAsList.size(); i++) {
                        JSONObject srcIp = preSrcIPItemsAsList.getJSONObject(i);
                        typeEnum = getIpAddressParamDTO(insideRefIpAddressObject, hostList, singleIpList, rangeDTOS, subnetStrDTOS, subnetIntDTOS, srcIp);
                        if(typeEnum.getName().contains("6")){
                            throw new RuntimeException("目标设备暂不支持IPv6");
                        }
                    }
                    if(CollectionUtils.isNotEmpty(singleIpList) || CollectionUtils.isNotEmpty(rangeDTOS)
                            || CollectionUtils.isNotEmpty(subnetStrDTOS) || CollectionUtils.isNotEmpty(subnetIntDTOS)){
                        insideAddress = new IpAddressParamDTO();
                        insideAddress.setSingleIpArray(singleIpList.toArray(new String[singleIpList.size()]));
                        insideAddress.setRangIpArray(rangeDTOS.toArray(new IpAddressRangeDTO[rangeDTOS.size()]));
                        insideAddress.setSubnetStrIpArray(subnetStrDTOS.toArray(new IpAddressSubnetStrDTO[subnetStrDTOS.size()]));
                        insideAddress.setSubnetIntIpArray(subnetIntDTOS.toArray(new IpAddressSubnetIntDTO[subnetIntDTOS.size()]));
                        insideAddress.setHosts(hostList.toArray(new String[hostList.size()]));
                        insideAddress.setIpTypeEnum(typeEnum);
                    }
                } else if(natClause.containsKey("preDstIPItemsAsArray")){
                    RuleIPTypeEnum typeEnum = RuleIPTypeEnum.IP4;
                    JSONArray preDstIPItems = natClause.getJSONArray("preDstIPItems");
                    List<String> hostList = new ArrayList<>();
                    List<String> singleIpList = new ArrayList<>();
                    List<IpAddressRangeDTO> rangeDTOS = new ArrayList<>();
                    List<IpAddressSubnetStrDTO> subnetStrDTOS = new ArrayList<>();
                    List<IpAddressSubnetIntDTO> subnetIntDTOS = new ArrayList<>();
                    for (int i = 0; i < preDstIPItems.size(); i++) {
                        JSONObject dstIp = preDstIPItems.getJSONObject(i);

                        typeEnum = getIpAddressParamDTO(globalRefIpAddressObject, hostList, singleIpList, rangeDTOS, subnetStrDTOS, subnetIntDTOS, dstIp);
                        if(typeEnum.getName().contains("6")){
                            throw new RuntimeException("目标设备暂不支持IPv6");
                        }
                    }
                    if(CollectionUtils.isNotEmpty(singleIpList) || CollectionUtils.isNotEmpty(rangeDTOS)
                            || CollectionUtils.isNotEmpty(subnetStrDTOS) || CollectionUtils.isNotEmpty(subnetIntDTOS) || CollectionUtils.isNotEmpty(hostList)){
                        globalAddress = new IpAddressParamDTO();
                        globalAddress.setSingleIpArray(singleIpList.toArray(new String[singleIpList.size()]));
                        globalAddress.setRangIpArray(rangeDTOS.toArray(new IpAddressRangeDTO[rangeDTOS.size()]));
                        globalAddress.setSubnetStrIpArray(subnetStrDTOS.toArray(new IpAddressSubnetStrDTO[subnetStrDTOS.size()]));
                        globalAddress.setSubnetIntIpArray(subnetIntDTOS.toArray(new IpAddressSubnetIntDTO[subnetIntDTOS.size()]));
                        globalAddress.setHosts(hostList.toArray(new String[hostList.size()]));
                        globalAddress.setIpTypeEnum(typeEnum);
                    }
                }
            }
            if(ArrayUtils.isEmpty(insideAddress.getSingleIpArray())){
                if(ArrayUtils.isNotEmpty(insideAddress.getSubnetIntIpArray())){
                    for (IpAddressSubnetIntDTO ipAddressSubnetIntDTO : insideAddress.getSubnetIntIpArray()) {
                        if(ipAddressSubnetIntDTO.getMask() == 32){
                            insideAddress.setSingleIpArray(new String[]{ipAddressSubnetIntDTO.getIp()});
                            break;
                        }
                    }
                } else if (ArrayUtils.isNotEmpty(insideAddress.getSubnetStrIpArray())){
                    for (IpAddressSubnetStrDTO subnetStrDTO : insideAddress.getSubnetStrIpArray()) {
                        if("255".equals(subnetStrDTO.getMask())){
                            insideAddress.setSingleIpArray(new String[]{subnetStrDTO.getIp()});
                            break;
                        }
                    }
                }
            }
            if(ArrayUtils.isEmpty(globalAddress.getSingleIpArray())){
                if(ArrayUtils.isNotEmpty(globalAddress.getSubnetIntIpArray())){
                    for (IpAddressSubnetIntDTO ipAddressSubnetIntDTO : globalAddress.getSubnetIntIpArray()) {
                        if(ipAddressSubnetIntDTO.getMask() == 32){
                            globalAddress.setSingleIpArray(new String[]{ipAddressSubnetIntDTO.getIp()});
                            break;
                        }
                    }
                } else if (ArrayUtils.isNotEmpty(globalAddress.getSubnetStrIpArray())){
                    for (IpAddressSubnetStrDTO subnetStrDTO : globalAddress.getSubnetStrIpArray()) {
                        if("255".equals(subnetStrDTO.getMask())){
                            globalAddress.setSingleIpArray(new String[]{subnetStrDTO.getIp()});
                            break;
                        }
                    }
                }
            }
            if(ArrayUtils.isEmpty(insideAddress.getSingleIpArray())){
                if(ArrayUtils.isNotEmpty(globalAddress.getSubnetIntIpArray())){
                    throw new RuntimeException("目标设备静态Nat暂不支持子网转换");
                } else if(ArrayUtils.isNotEmpty(insideAddress.getSubnetStrIpArray())){
                    throw new RuntimeException("目标设备静态Nat暂不支持子网转换");
                } else if (ArrayUtils.isNotEmpty(insideAddress.getRangIpArray())){
                    throw new RuntimeException("目标设备静态Nat暂不支持范围转换");
                }
            }
            if(ArrayUtils.isEmpty(globalAddress.getSingleIpArray())){
                if(ArrayUtils.isNotEmpty(globalAddress.getSubnetIntIpArray())){
                    throw new RuntimeException("目标设备静态Nat暂不支持子网转换");
                } else if(ArrayUtils.isNotEmpty(globalAddress.getSubnetStrIpArray())){
                    throw new RuntimeException("目标设备静态Nat暂不支持子网转换");
                } else if (ArrayUtils.isNotEmpty(globalAddress.getRangIpArray())){
                    throw new RuntimeException("目标设备静态Nat暂不支持范围转换");
                }
            }

            if(StringUtils.isNotBlank(srcZone)){
                natPolicyParamDTO.setInInterface(new InterfaceParamDTO(srcZone));
            }

            natPolicyParamDTO.setInsideAddress(insideAddress);
            natPolicyParamDTO.setGlobalAddress(globalAddress);
            natPolicyParamDTO.setInsideRefIpAddressObject(insideRefIpAddressObject.toArray(insideRefIpAddressObject.toArray(new String[0])));
            natPolicyParamDTO.setGlobalRefIpAddressObject(globalRefIpAddressObject.toArray(globalRefIpAddressObject.toArray(new String[0])));
            natPolicyParamDTO.setGlobalServiceParam(globalServiceParam.toArray(new ServiceParamDTO[0]));
            policyCommandLine.append(natGeneratorBean.generateStaticNatPolicyCommandLine(statusTypeEnum, natPolicyParamDTO, parameterMap, null));
        } else {
            Map<String, ServiceGroupObjectRO> targetPredefinedServiceMap = (Map<String, ServiceGroupObjectRO>) parameterMap.get("targetPredefinedServiceMap");
            Set<String> targetServiceObjectNames = (Set<String>) parameterMap.get("targetServiceObjectNames");
            Set<String> currentServiceObjectNames = (Set<String>) parameterMap.get("currentServiceObjectNames");
            Set<String> targetServiceGroupObjectNames = (Set<String>) parameterMap.get("targetServiceGroupObjectNames");
            Set<String> currentServiceGroupObjectNames = (Set<String>) parameterMap.get("currentServiceGroupObjectNames");


            Set<String> targetAddressPoolNames = (Set<String>) parameterMap.get("targetAddressPoolNames");
            Set<String> targetAddressObjectNames = (Set<String>) parameterMap.get("targetAddressObjectNames");
            Set<String> targetAddressGroupObjectNames = (Set<String>) parameterMap.get("targetAddressGroupObjectNames");
            Set<String> currentAddressPoolNames = (Set<String>) parameterMap.get("currentAddressPoolNames");
            Set<String> currentAddressObjectNames = (Set<String>) parameterMap.get("currentAddressObjectNames");
            Set<String> currentAddressGroupObjectNames = (Set<String>) parameterMap.get("currentAddressGroupObjectNames");
            for (DeviceFilterRuleListRO deviceFilterRuleListRO : needAddFilter) {
                //生成策略
                StatusTypeEnum statusTypeEnum = StatusTypeEnum.ADD;
                if("ip6".equals(deviceFilterRuleListRO.getIpType())){
                    statusTypeEnum = StatusTypeEnum.ADD_IPV6;
                }
                NatPolicyParamDTO natPolicyParamDTO = this.buildNatPolicyParam(deviceFilterRuleListRO);
                JSONObject natClause = deviceFilterRuleListRO.getNatClause();
                String natCommandline = StringUtils.EMPTY;

                if(ArrayUtils.isNotEmpty(natPolicyParamDTO.getServiceParam())){
                    ServiceParamDTO[] serviceParam = natPolicyParamDTO.getServiceParam();
                    if(serviceParam.length == 1){
                        ServiceParamDTO serviceParamDTO = serviceParam[0];
                        if(serviceParamDTO.getProtocol() == null){
                            throw new RuntimeException("目标设备暂不支持无协议端口转换");
                        }
                    }
                    String serviceObjectName = natGeneratorBean.createServiceObjectName(Arrays.asList(serviceParam), null, null);
                    if(targetPredefinedServiceMap.containsKey(serviceObjectName) || targetServiceObjectNames.contains(serviceObjectName) || currentServiceObjectNames.contains(serviceObjectName)
                            || targetServiceGroupObjectNames.contains(serviceObjectName) || currentServiceGroupObjectNames.contains(serviceObjectName)){
                        natPolicyParamDTO.setServiceParam(null);
                        String[] refServiceObject = natPolicyParamDTO.getRefServiceObject();
                        if(refServiceObject == null){
                            refServiceObject = new String[0];
                        }
                        natPolicyParamDTO.setRefServiceObject(ArrayUtils.add(refServiceObject,serviceObjectName));
                    } else {
                        currentServiceObjectNames.add(serviceObjectName);
                    }
                }

                if(natPolicyParamDTO.getSrcIp() != null){
                    IpAddressParamDTO srcIp = natPolicyParamDTO.getSrcIp();
                    String srcAddressName = natGeneratorBean.createIpAddressObjectNameByParamDTO(srcIp.getSingleIpArray(), srcIp.getRangIpArray(), srcIp.getSubnetIntIpArray(), srcIp.getSubnetStrIpArray(), null, null, null, null);
                    if( targetAddressObjectNames.contains(srcAddressName) || targetAddressGroupObjectNames.contains(srcAddressName) || currentAddressObjectNames.contains(srcAddressName) || currentAddressGroupObjectNames.contains(srcAddressName)){
                        natPolicyParamDTO.setSrcIp(null);
                        String[] srcRefIpAddressObject = natPolicyParamDTO.getSrcRefIpAddressObject();
                        if(srcRefIpAddressObject == null){
                            srcRefIpAddressObject = new String[0];
                        }
                        natPolicyParamDTO.setSrcRefIpAddressObject(ArrayUtils.add(srcRefIpAddressObject,srcAddressName));
                    }else {
                        currentAddressObjectNames.add(srcAddressName);
                    }
                }
                if ("SRC".equals(natClause.getString("natField"))) {
                    if(natPolicyParamDTO.getDstIp() != null){
                        IpAddressParamDTO dstIp = natPolicyParamDTO.getDstIp();
                        String dstAddressName = natGeneratorBean.createIpAddressObjectNameByParamDTO(dstIp.getSingleIpArray(), dstIp.getRangIpArray(), dstIp.getSubnetIntIpArray(), dstIp.getSubnetStrIpArray(), null, null, null, null);
                        if(targetAddressObjectNames.contains(dstAddressName) || targetAddressGroupObjectNames.contains(dstAddressName) || currentAddressObjectNames.contains(dstAddressName) || currentAddressGroupObjectNames.contains(dstAddressName)){
                            natPolicyParamDTO.setDstIp(null);
                            String[] dstRefIpAddressObject = natPolicyParamDTO.getDstRefIpAddressObject();
                            if(dstRefIpAddressObject == null){
                                dstRefIpAddressObject = new String[0];
                            }
                            natPolicyParamDTO.setDstRefIpAddressObject(ArrayUtils.add(dstRefIpAddressObject,dstAddressName));
                        } else {
                            currentAddressObjectNames.add(dstAddressName);
                        }
                    }
                    if(natPolicyParamDTO.getPostSrcIpAddress() != null){
                        IpAddressParamDTO postSrcIpAddress = natPolicyParamDTO.getPostSrcIpAddress();
                        String postSrcAddressName = natGeneratorBean.createIpAddressObjectNameByParamDTO(postSrcIpAddress.getSingleIpArray(), postSrcIpAddress.getRangIpArray(), postSrcIpAddress.getSubnetIntIpArray(), postSrcIpAddress.getSubnetStrIpArray(), null, null, null, null);
                        if(targetAddressPoolNames.contains(postSrcAddressName) || currentAddressPoolNames.contains(postSrcAddressName)){
                            natPolicyParamDTO.setPostSrcIpAddress(null);
                            String[] postSrcRefIpAddressObject = natPolicyParamDTO.getPostSrcRefIpAddressObject();
                            if(postSrcRefIpAddressObject == null){
                                postSrcRefIpAddressObject = new String[0];
                            }
                            natPolicyParamDTO.setPostSrcRefIpAddressObject(ArrayUtils.add(postSrcRefIpAddressObject,postSrcAddressName));
                        } else {
                            currentAddressPoolNames.add(postSrcAddressName);
                        }
                    }
                    natCommandline = natGeneratorBean.generateSNatPolicyCommandLine(statusTypeEnum, natPolicyParamDTO, parameterMap, null);
                } else if ("DST".equals(natClause.getString("natField"))) {
                    if(natPolicyParamDTO.getDstIp() != null){
                        IpAddressParamDTO dstIp = natPolicyParamDTO.getDstIp();
                        String dstAddressName = natGeneratorBean.createIpAddressObjectNameByParamDTO(dstIp.getSingleIpArray(), dstIp.getRangIpArray(), dstIp.getSubnetIntIpArray(), dstIp.getSubnetStrIpArray(), null, null, null, null);
                        if(targetAddressPoolNames.contains(dstAddressName) || currentAddressPoolNames.contains(dstAddressName)){
                            natPolicyParamDTO.setDstIp(null);
                            String[] dstRefIpAddressObject = natPolicyParamDTO.getDstRefIpAddressObject();
                            if(dstRefIpAddressObject == null){
                                dstRefIpAddressObject = new String[0];
                            }
                            natPolicyParamDTO.setDstRefIpAddressObject(ArrayUtils.add(dstRefIpAddressObject,dstAddressName));
                        } else {
                            currentAddressPoolNames.add(dstAddressName);
                        }
                    }
                    if(natPolicyParamDTO.getPostDstIpAddress() != null){
                        IpAddressParamDTO postDstIp = natPolicyParamDTO.getPostDstIpAddress();
                        String postDstAddressName = natGeneratorBean.createIpAddressObjectNameByParamDTO(postDstIp.getSingleIpArray(), postDstIp.getRangIpArray(), postDstIp.getSubnetIntIpArray(), postDstIp.getSubnetStrIpArray(), null, null, null, null);
                        if(targetAddressObjectNames.contains(postDstAddressName) || targetAddressGroupObjectNames.contains(postDstAddressName) || currentAddressObjectNames.contains(postDstAddressName) || currentAddressGroupObjectNames.contains(postDstAddressName)){
                            natPolicyParamDTO.setDstIp(null);
                            String[] postDstRefIpAddressObject = natPolicyParamDTO.getPostDstRefIpAddressObject();
                            if(postDstRefIpAddressObject == null){
                                postDstRefIpAddressObject = new String[0];
                            }
                            natPolicyParamDTO.setPostDstRefIpAddressObject(ArrayUtils.add(postDstRefIpAddressObject,postDstAddressName));
                        } else {
                            currentAddressObjectNames.add(postDstAddressName);
                        }
                    }

                    natCommandline = natGeneratorBean.generateDNatPolicyCommandLine(statusTypeEnum, natPolicyParamDTO, parameterMap, null);
                } else {
                    throw new RuntimeException("该策略暂不支持迁移");
                }
                policyCommandLine.append(natCommandline);
            }
        }

        generateSecurityResult.put("commandLine",policyCommandLine.toString());
        generateSecurityResult.put("warning",warningCommandLine.toString());
        return generateSecurityResult;
    }

    @Override
    public String generateRoutingCommandLine(RoutingEntriesRO routingEntriesRO) throws Exception {
        if(routingGeneratorBean == null){
            new RuntimeException("当前设备暂不支持");
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
            throw new RuntimeException("暂不支持IPv6静态路由的迁移");
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
    @Override
    public NatPolicyParamDTO buildNatPolicyParam(DeviceFilterRuleListRO ruleListRO){

        NatPolicyParamDTO natPolicyParamDTO = new NatPolicyParamDTO();

        if (ruleListRO.getInInterfaceGroupRefs() != null && ruleListRO.getInInterfaceGroupRefs().size() > 0) {
            natPolicyParamDTO.setInInterface(new InterfaceParamDTO(ruleListRO.getInInterfaceGroupRefs().get(0)));
        }
//        dto.setSrcIp("");
        if (ruleListRO.getOutInterfaceGroupRefs() != null && ruleListRO.getOutInterfaceGroupRefs().size() > 0) {
            natPolicyParamDTO.setOutInterface(new InterfaceParamDTO(ruleListRO.getOutInterfaceGroupRefs().get(0)));
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
                    if(typeEnum.getName().contains("6")){
                        throw new RuntimeException("目标设备暂不支持IPv6");
                    }

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
                    if(typeEnum.getName().contains("6")){
                        throw new RuntimeException("目标设备暂不支持IPv6");
                    }
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
                    if(typeEnum.getName().contains("6")){
                        throw new RuntimeException("目标设备暂不支持IPv6");
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
                    if(typeEnum.getName().contains("6")){
                        throw new RuntimeException("目标设备暂不支持IPv6");
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
                    String protocolName = protocolObj.getString("protocolName");
                    String protocolNum = protocolObj.getString("protocolNum");
                    if (StringUtils.isNotBlank(protocolName)) {
                        protocolNum = ProtocolUtils.getProtocolNumberByName(protocolName);
                    }
                    if(CollectionUtils.isNotEmpty(preServiceParamDTOS)){
                        for (ServiceParamDTO preServiceParamDTO : preServiceParamDTOS) {
                            preServiceParamDTO.setProtocol(ProtocolTypeEnum.getByCode(Integer.parseInt(protocolNum)));
                        }
                        continue;
                    }


                    if (protocolObj.containsKey("nameRef")) {
                        String nameRef = protocolObj.getString("nameRef");
                        nameRef = nameRef.substring(0, nameRef.length() - 2);
                        preExistServiceNameList.add(nameRef);
                        continue;
                    }
                    if (StringUtils.isNotBlank(protocolName)) {
                        protocolNum = ProtocolUtils.getProtocolNumberByName(protocolName);
                    }
                    if (StringUtils.isNotBlank(protocolNum)) {
                        if(CollectionUtils.isNotEmpty(preServiceParamDTOS)){
                            for (ServiceParamDTO preServiceParamDTO : preServiceParamDTOS) {
                                preServiceParamDTO.setProtocol(ProtocolTypeEnum.getByCode(Integer.parseInt(protocolNum)));
                            }
                        } else {
                            ServiceParamDTO serviceParamDTO = new ServiceParamDTO();
                            serviceParamDTO.setProtocol(ProtocolTypeEnum.getByCode(Integer.parseInt(protocolNum)));
                            preServiceParamDTOS.add(serviceParamDTO);
                        }

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

    /**
     * 构建地址对象原子化命令行参数
     * @param netWorkGroupObjectRO
     * @return
     */
    @Override
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
                    throw new RuntimeException("目标设备不支持域名策略");
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


    /**
     * 构建安全策略原子化命令行参数
     * @param ruleListRO
     * @return
     */
    @Override
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
        //自动生成id
        /*if(StringUtils.isNotBlank(ruleListRO.getRuleId())){
            policyParamDTO.setId(ruleListRO.getRuleId());
        } else {
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

                    if (service.containsKey("nameRef")) {
                        String nameRef = service.getString("nameRef");
                        nameRef = nameRef.substring(0, nameRef.length()-2);
                        existServiceNameList.add(nameRef);
                    }else if (service.containsKey("serviceValue")) {

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
                                List<String> portList = portValues.stream().map(t -> (String)t).collect(Collectors.toList());
                                String portOp = serviceValue.getString("portOp");
                                List<Integer> dstSinglePortArray = new ArrayList<>();
                                List<String> dstSinglePortStrArray = new ArrayList<>();
                                List<PortRangeDTO> dstRangePortArray = new ArrayList<>();
                                getServicePort(portOp, portList, dstSinglePortArray, dstSinglePortStrArray, dstRangePortArray);
                                if(CollectionUtils.isNotEmpty(dstSinglePortArray)){
                                    serviceParamDTO.setDstSinglePortArray(dstSinglePortArray.toArray(new Integer[dstSinglePortArray.size()]));
                                }
                                if(CollectionUtils.isNotEmpty(dstSinglePortStrArray)){
                                    serviceParamDTO.setDstSinglePortStrArray(dstSinglePortStrArray.toArray(new String[dstSinglePortStrArray.size()]));
                                }
                                if(CollectionUtils.isNotEmpty(dstRangePortArray)){
                                    serviceParamDTO.setDstRangePortArray(dstRangePortArray.toArray(new PortRangeDTO[dstRangePortArray.size()]));
                                }
                            }
                            serviceParamDTOS.add(serviceParamDTO);
                        }

                    }
                }
                policyParamDTO.setServiceParam(serviceParamDTOS.toArray(new ServiceParamDTO[0]));
                policyParamDTO.setRefServiceObject(existServiceNameList.toArray(new String[existServiceNameList.size()]));
            }


            if (matchClause.containsKey("srcIp")) {
                RuleIPTypeEnum typeEnum = RuleIPTypeEnum.IP4;
                JSONArray srcIpArray = matchClause.getJSONArray("srcIp");
                List<String> existSrcAddressList = new ArrayList<>();
                List<String> hostList = new ArrayList<>();
                List<String> singleIpList = new ArrayList<>();
                List<IpAddressRangeDTO> rangeDTOS = new ArrayList<>();
                List<IpAddressSubnetStrDTO> subnetStrDTOS = new ArrayList<>();
                List<IpAddressSubnetIntDTO> subnetIntDTOS = new ArrayList<>();
                for (int i = 0; i < srcIpArray.size(); i++) {
                    JSONObject srcIp = srcIpArray.getJSONObject(i);

                    if (srcIp.containsKey("nameRef")) {
                        String nameRef = srcIp.getString("nameRef");
                        nameRef = nameRef.substring(0, nameRef.length()-2);
                        existSrcAddressList.add(nameRef);
                    }
                    String type = srcIp.getString("type");
                    if (Constants.SUBNET.equals(type)) {
                        String ip4Prefix = srcIp.getString("ip4Prefix");
                        String ip4Length = srcIp.getString("ip4Length");
                        if(StringUtils.isNotBlank(ip4Prefix) && StringUtils.isNotBlank(ip4Length)){
                            IpAddressSubnetIntDTO subnetIntDTO = new IpAddressSubnetIntDTO();
                            subnetIntDTO.setIp(ip4Prefix);
                            subnetIntDTO.setMask(Integer.parseInt(ip4Length));
                            subnetIntDTO.setType(MaskTypeEnum.mask);
                            subnetIntDTOS.add(subnetIntDTO);
                        }

                        String ip6Prefix = srcIp.getString("ip6Prefix");
                        String ip6Length = srcIp.getString("ip6Length");
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
                        List<String> ip4Addresses = srcIp.getObject("ip4Addresses",List.class);
                        if (ip4Addresses != null && ip4Addresses.size() > 0) {
                            singleIpList.addAll(ip4Addresses);
                        }

                        List<String> ip6Addresses = srcIp.getObject("ip6Addresses",List.class);
                        if (ip6Addresses != null && ip6Addresses.size() > 0) {
                            typeEnum = RuleIPTypeEnum.IP6;
                            singleIpList.addAll(ip6Addresses);
                        }
                    }else if (Constants.RANGE.equals(type)) {
                        Ip4RangeRO ip4Range = srcIp.getObject("ip4Range",Ip4RangeRO.class);
                        Ip4RangeRO ip6Range = srcIp.getObject("ip6Range",Ip4RangeRO.class);
                        if (ip4Range != null) {
                            rangeDTOS.add(getStartEndByJsonObject(ip4Range));
                        }
                        if (ip6Range != null) {
                            typeEnum = RuleIPTypeEnum.IP6;
                            rangeDTOS.add(getStartEndByJsonObject(ip6Range));
                        }
                    }else if (Constants.FQDN.equals(type)) {
                        String fqdn = srcIp.getString("fqdn");
                        if(TotemsIp4Utils.isIp4(fqdn)){
                            singleIpList.add(fqdn);
                        } else if (TotemsIp6Utils.isIp6(fqdn)){
                            typeEnum = RuleIPTypeEnum.IP6;
                            singleIpList.add(fqdn);
                        } else {
                            hostList.add(srcIp.getString("fqdn"));
                        }
                    }else if (Constants.IP4WILDCARD.equals(type)) {
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
                policyParamDTO.setSrcRefIpAddressObject(existSrcAddressList.toArray(new String[existSrcAddressList.size()]));
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


}
