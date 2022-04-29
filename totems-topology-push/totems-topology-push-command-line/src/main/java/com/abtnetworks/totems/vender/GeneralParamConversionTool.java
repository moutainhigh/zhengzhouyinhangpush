package com.abtnetworks.totems.vender;

import com.abtnetworks.totems.command.line.abs.OverAllGeneratorAbstractBean;
import com.abtnetworks.totems.command.line.dto.*;
import com.abtnetworks.totems.command.line.enums.*;
import com.abtnetworks.totems.common.constant.Constants;
import com.abtnetworks.totems.common.network.TotemsIpUtils;
import com.abtnetworks.totems.common.utils.PortFormatUtil;
import com.abtnetworks.totems.whale.baseapi.ro.*;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.activemq.util.TimeUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @Author: WangCan
 * @Description
 * @Date: 2021/4/30
 */
@Slf4j
public class GeneralParamConversionTool {

    /** 生成地址对象命令行
     * @param needAddNetWorkObjectList
     * @param parameterMap
     * @return
     * @throws Exception
     */
    public static String generateIpAddressObjectCommandLine(List<NetWorkGroupObjectRO> needAddNetWorkObjectList, Map<String, Object> parameterMap, OverAllGeneratorAbstractBean generatorBean) throws Exception{
        StringBuffer objectCommandLine = new StringBuffer();
        for (NetWorkGroupObjectRO netWorkGroupObjectRO : needAddNetWorkObjectList) {
            IpAddressParamDTO paramDTO = buildIpAddressParam(netWorkGroupObjectRO.getIncludeItems());
            Map<String,Object> map = new HashMap<>();
            objectCommandLine.append(generatorBean.generateIpAddressObjectCommandLine(StatusTypeEnum.ADD, paramDTO.getIpTypeEnum(),
                    netWorkGroupObjectRO.getName(),null,paramDTO.getSingleIpArray(),paramDTO.getRangIpArray(),paramDTO.getSubnetIntIpArray(),paramDTO.getSubnetStrIpArray(),
                    null,paramDTO.getHosts(),paramDTO.getObjectNameRefArray(),netWorkGroupObjectRO.getDescription(),null,null,
                    map,null));
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
    public static String generateIpAddressObjectGroupCommandLine(List<NetWorkGroupObjectRO> needAddNetWorkObjectList,Map<String, Object> parameterMap,OverAllGeneratorAbstractBean generatorBean) throws Exception{
        StringBuffer objectCommandLine = new StringBuffer();
        for (NetWorkGroupObjectRO netWorkGroupObjectRO : needAddNetWorkObjectList) {
            IpAddressParamDTO paramDTO = buildIpAddressParam(netWorkGroupObjectRO.getIncludeItems());
            Map<String,Object> map = new HashMap<>();
            objectCommandLine.append(generatorBean.generateIpAddressObjectGroupCommandLine(StatusTypeEnum.ADD, paramDTO.getIpTypeEnum(),
                    netWorkGroupObjectRO.getName(),null,paramDTO.getSingleIpArray(),paramDTO.getRangIpArray(),paramDTO.getSubnetIntIpArray(),paramDTO.getSubnetStrIpArray(),
                    null,paramDTO.getHosts(),paramDTO.getObjectNameRefArray(),paramDTO.getObjectGroupNameRefArray(),netWorkGroupObjectRO.getDescription(),null,null,
                    map,null));
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
    public static String generateServiceObjectCommandLine(List<ServiceGroupObjectRO> needAddServiceObjectList, Map<String, Object> parameterMap,OverAllGeneratorAbstractBean generatorBean) throws Exception {
        StringBuffer serviceCommandLine = new StringBuffer();
        for (ServiceGroupObjectRO serviceGroupObjectRO : needAddServiceObjectList) {
            List<ServiceParamDTO> serviceParamDTOS = buildServiceParamDTO(serviceGroupObjectRO.getIncludeFilterServices());
            serviceCommandLine.append(generatorBean.generateServiceObjectCommandLine(StatusTypeEnum.ADD,
                    serviceGroupObjectRO.getName(), serviceGroupObjectRO.getId(), null,serviceParamDTOS,
                    serviceGroupObjectRO.getDescription(),null,null));
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
    public static String generateServiceObjectGroupCommandLine(List<ServiceGroupObjectRO> needAddServiceObjectGroupList, Map<String, Object> parameterMap,OverAllGeneratorAbstractBean generatorBean) throws Exception {
        StringBuffer serviceCommandLine = new StringBuffer();
        for (ServiceGroupObjectRO serviceGroupObjectRO : needAddServiceObjectGroupList) {
            List<ServiceParamDTO> serviceParamDTOS = buildServiceParamDTO(serviceGroupObjectRO.getIncludeFilterServices());
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
                    null,null));
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
    public static String generateTimeCommandLine(List<TimeObjectRO> needAddTimeObjectList, Map<String, Object> parameterMap, OverAllGeneratorAbstractBean generatorBean) throws Exception {
        StringBuffer timeCommandLine = new StringBuffer();
        for (TimeObjectRO timeObjectRO : needAddTimeObjectList) {
            String start = null;
            String end = null;
            Map<String,Object> map = new HashMap<>();
            for (TimeItemsRO timeItem : timeObjectRO.getTimeItems()) {
                if (StringUtils.isNoneBlank(timeItem.getStart(), timeItem.getEnd())) {
                    start = dealDateFormat(timeItem.getStart());
                    end = dealDateFormat(timeItem.getEnd());
                    AbsoluteTimeParamDTO absoluteTimeParamDTO=new AbsoluteTimeParamDTO();
                    absoluteTimeParamDTO.setStartTime(start);
                    absoluteTimeParamDTO.setEndTime(end);
                    timeCommandLine.append(generatorBean.generateAbsoluteTimeCommandLine(timeObjectRO.getName(),null, absoluteTimeParamDTO,null,null));
                } else if(StringUtils.isNotBlank(timeItem.getPeriodicSetting())){
                    String periodicSetting = timeItem.getPeriodicSetting();
                    //TODO 每个厂家的周期计划格式不一致，暂无法转换，如华为：08:00:00 to 18:00:00 working-day
                }
            }

        }
        return timeCommandLine.toString();
    }

    /**
     * 生成安全策略命令行
     * @param deviceFilterlistRO 策略集
     * @param needAddFilter 策略
     * @param parameterMap  扩展参数
     * @param generatorBean 设备实现类
     * @return
     */
    public static Map<String,String> generateSecurityPolicyCommandLine(DeviceFilterlistRO deviceFilterlistRO,List<DeviceFilterRuleListRO> needAddFilter, Map<String, Object> parameterMap, OverAllGeneratorAbstractBean generatorBean) throws Exception {
        Map<String,String> generateSecurityResult = new HashMap<>();
        StringBuffer policyCommandLine = new StringBuffer();
        StringBuffer warningCommandLine = new StringBuffer();
        policyCommandLine.append(generatorBean.generatePolicyGroupCommandLine(deviceFilterlistRO.getName(),deviceFilterlistRO.getDescription(),null,null));
        for (DeviceFilterRuleListRO deviceFilterRuleListRO : needAddFilter) {
            //生成策略
            try {
                policyCommandLine.append(generatorBean.generateSecurityPolicyCommandLine(StatusTypeEnum.ADD, buildPolicyParam(deviceFilterRuleListRO), null, null));
                if(parameterMap != null && parameterMap.get("progress") != null){
                    TranslationTaskProgressDTO progress = (TranslationTaskProgressDTO) parameterMap.get("progress");
                    progress.increOne();
                }
            } catch (Exception e) {
                log.error("generateSecurityPolicyCommandLine error:",e);
                warningCommandLine.append(String.format("策略 %s 生成命令行异常，报错信息为：%s \n",deviceFilterRuleListRO.getName(),e.getMessage()));

            }
        }
        generateSecurityResult.put("commandLine",policyCommandLine.toString());
        generateSecurityResult.put("warning",warningCommandLine.toString());
        return generateSecurityResult;
    }

    /**
     * 删除安全策略
     * @param id
     * @param name
     * @param generatorBean
     * @return
     */
    public static String generateDeleteSecurityPolicyCommandLine(String id,String name,OverAllGeneratorAbstractBean generatorBean){
        return generatorBean.deleteSecurityPolicyByIdOrName(RuleIPTypeEnum.IP4,id,name,null,null);
    }


    public static PolicyParamDTO buildPolicyParam(DeviceFilterRuleListRO ruleListRO){

        PolicyParamDTO policyParamDTO = new PolicyParamDTO();

        if (ruleListRO.getInInterfaceGroupRefs() != null && ruleListRO.getInInterfaceGroupRefs().size() > 0) {
            String in= ruleListRO.getInInterfaceGroupRefs().get(0);
            ZoneParamDTO zoneParamDTO = new ZoneParamDTO(in);
            policyParamDTO.setSrcZone(zoneParamDTO);
        }
//        dto.setSrcIp("");
        if (ruleListRO.getOutInterfaceGroupRefs() != null && ruleListRO.getOutInterfaceGroupRefs().size() > 0) {
            String out= ruleListRO.getOutInterfaceGroupRefs().get(0);
            ZoneParamDTO zoneParamDTO = new ZoneParamDTO(out);
            policyParamDTO.setDstZone(zoneParamDTO);
        }
//        dto.setDstItf("");

        String name = StringUtils.isBlank(ruleListRO.getName())?ruleListRO.getRuleId():ruleListRO.getName();
        policyParamDTO.setName(name);
        if(StringUtils.isNotBlank(ruleListRO.getRuleId())){
            policyParamDTO.setId(ruleListRO.getRuleId());
        }
        policyParamDTO.setAction(StringUtils.isBlank(ruleListRO.getAction())?"permit":ruleListRO.getAction());
        policyParamDTO.setDescription(ruleListRO.getDescription());

        policyParamDTO.setMoveSeatEnum(MoveSeatEnum.AFTER);

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
                for (int i = 0; i < services.size(); i++) {
                    JSONObject service = services.getJSONObject(i);

                    if (service.containsKey("nameRef")) {
                        String nameRef = service.getString("nameRef");
                        nameRef = nameRef.substring(0, nameRef.length()-2);
                        existServiceNameList.add(nameRef);
                    }

                }
                policyParamDTO.setRefServiceObject(existServiceNameList.toArray(new String[existServiceNameList.size()]));
            }

            List<IpAddressSubnetIntDTO> subnetIntDTOS = new ArrayList<>();


            if (matchClause.containsKey("srcIp")) {
                RuleIPTypeEnum typeEnum = RuleIPTypeEnum.IP4;
                JSONArray srcIpArray = matchClause.getJSONArray("srcIp");
                List<String> existSrcAddressList = new ArrayList<>();
                List<String> hostList = new ArrayList<>();
                List<String> singleIpList = new ArrayList<>();
                List<IpAddressRangeDTO> rangeDTOS = new ArrayList<>();
                List<IpAddressSubnetStrDTO> subnetStrDTOS = new ArrayList<>();
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
                    }else if (type.equals(Constants.FQDN)) {
                        hostList.add(srcIp.getString("fqdn"));
                    }else if (Constants.IP4WILDCARD.equals(type)) {
                        String ip4WildCardMask = srcIp.getString("ip4WildCardMask");
                        if (StringUtils.isNumeric(ip4WildCardMask)) {
                            ip4WildCardMask = String.valueOf(TotemsIpUtils.IPv4NumToString(Long.parseLong(ip4WildCardMask)));
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
                    }else if (type.equals(Constants.FQDN)) {
                        hostList.add(dstIp.getString("fqdn"));
                    }else if (Constants.IP4WILDCARD.equals(type)) {
                        String ip4WildCardMask = dstIp.getString("ip4WildCardMask");
                        if (StringUtils.isNumeric(ip4WildCardMask)) {
                            ip4WildCardMask = String.valueOf(TotemsIpUtils.IPv4NumToString(Long.parseLong(ip4WildCardMask)));
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

    public static List<ServiceParamDTO> buildServiceParamDTO(List<IncludeFilterServicesRO> includeFilterServicesROS) {
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

    private static void getServicePort(String portOp, List<String> portValues, List<Integer> singlePortArray, List<String> singlePortStrArray, List<PortRangeDTO> rangePortArray) {
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


    public static IpAddressParamDTO buildIpAddressParam(List<IncludeItemsRO> includeItemsROS) {

        //获取类型
        IpAddressParamDTO paramDTO = new IpAddressParamDTO();
        if(CollectionUtils.isEmpty(includeItemsROS)){
            return new IpAddressParamDTO();
        }
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
            }else if (type.equals(Constants.FQDN)) {
                hosts.add(ro.getFqdn());
//            formatIpStr += "域名: " + ro.getFqdn() + ";";
            }else if (Constants.IP4WILDCARD.equals(type)) {
                String ip4WildCardMask = ro.getIp4WildCardMask();
                if (StringUtils.isNumeric(ip4WildCardMask)) {
                    ip4WildCardMask = String.valueOf(TotemsIpUtils.IPv4NumToString(Long.parseLong(ip4WildCardMask)));
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
        if(CollectionUtils.isNotEmpty(objectGroupNameList)){
            paramDTO.setObjectGroupNameRefArray(objectGroupNameList.toArray(new String[objectGroupNameList.size()]));
        }
        if(CollectionUtils.isNotEmpty(objectNameList)){
            paramDTO.setObjectNameRefArray(objectNameList.toArray(new String[objectNameList.size()]));
        }
        if(CollectionUtils.isNotEmpty(hosts)){
            paramDTO.setHosts(hosts.toArray(new String[hosts.size()]));
        }
        paramDTO.setIpTypeEnum(typeEnum);
        return paramDTO;
    }

    private static IpAddressRangeDTO getStartEndByJsonObject(Ip4RangeRO rangeRO) {
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


    public static String dealDateFormat(String oldDateStr) throws ParseException {
        //此格式只有  jdk 1.7才支持  yyyy-MM-dd'T'HH:mm:ss.SSSXXX
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'+'SSSS");
        Date date = df.parse(oldDateStr);
        SimpleDateFormat df1 = new SimpleDateFormat ("EEE MMM dd HH:mm:ss Z yyyy", Locale.UK);
        Date date1 =  df1.parse(date.toString());
        DateFormat df2 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return df2.format(date1);
    }

}
