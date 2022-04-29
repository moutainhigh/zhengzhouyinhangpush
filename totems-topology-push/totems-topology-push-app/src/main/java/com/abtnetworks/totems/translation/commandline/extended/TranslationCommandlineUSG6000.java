package com.abtnetworks.totems.translation.commandline.extended;

import com.abtnetworks.totems.command.line.dto.*;
import com.abtnetworks.totems.command.line.enums.MaskTypeEnum;
import com.abtnetworks.totems.command.line.enums.RuleIPTypeEnum;
import com.abtnetworks.totems.command.line.enums.StatusTypeEnum;
import com.abtnetworks.totems.common.constant.Constants;
import com.abtnetworks.totems.common.dto.TranslationTaskProgressDTO;
import com.abtnetworks.totems.common.utils.IpUtils;
import com.abtnetworks.totems.translation.commandline.TranslationCommandline;
import com.abtnetworks.totems.vender.Usg.security.SecurityUsg6000Impl;
import com.abtnetworks.totems.whale.baseapi.ro.*;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @Author: Yyh
 * @Description
 * @Date: 2021/5/26
 */
@Slf4j
public class TranslationCommandlineUSG6000 extends TranslationCommandline {

    public TranslationCommandlineUSG6000() {
        this.generatorBean = new SecurityUsg6000Impl();
    }

    @Override
    public Map<String, String> generateSecurityPolicyCommandLine(String taskUuid, DeviceFilterlistRO deviceFilterlistRO, List<DeviceFilterRuleListRO> needAddFilter, Map<String, Object> parameterMap) throws Exception {
        Map<String,String> generateSecurityResult = new HashMap<>();
        StringBuffer policyCommandLine = new StringBuffer();
        StringBuffer warningCommandLine = new StringBuffer();
//        policyCommandLine.append(generatorBean.generatePolicyGroupCommandLine(deviceFilterlistRO.getName(),deviceFilterlistRO.getDescription(),null,null));
        for (DeviceFilterRuleListRO deviceFilterRuleListRO : needAddFilter) {
            //生成策略
            try {
                Map<String, Object> map = new HashMap<>();
                Map<String, ServiceGroupObjectRO> serviceNameObjectMap = (Map<String, ServiceGroupObjectRO>) parameterMap.get("serviceNameObjectMap");
                Map<String, NetWorkGroupObjectRO> srcIpNameObjectMap = (Map<String, NetWorkGroupObjectRO>) parameterMap.get("srcIpNameObjectMap");
                Map<String, NetWorkGroupObjectRO> dstIpNameObjectMap = (Map<String, NetWorkGroupObjectRO>) parameterMap.get("dstIpNameObjectMap");
                IpAddressParamDTO srcIpAddressParam=new IpAddressParamDTO();
                IpAddressParamDTO dstIpAddressParam=new IpAddressParamDTO();
                List<ServiceParamDTO> serviceParamDTOS=new ArrayList<>();
                List<IncludeItemsRO> includeItemsROS=new ArrayList<>();
                Map<String,List<ServiceParamDTO>> serviceObjectMap=new HashMap<>();
                Map<String,IpAddressParamDTO> srcIpObjectMap=new HashMap<>();
                Map<String,IpAddressParamDTO> dstIpObjectMap=new HashMap<>();

                JSONArray services = (JSONArray)deviceFilterRuleListRO.getMatchClause().get("services");
                JSONArray srcIps = (JSONArray)deviceFilterRuleListRO.getMatchClause().get("srcIp");
                JSONArray dstIps = (JSONArray)deviceFilterRuleListRO.getMatchClause().get("dstIp");
                String nameRef="";
                if (serviceNameObjectMap!=null && services!=null&& services.size() > 1) {
                    for (int i = 0; i < services.size(); i++) {
                        JSONObject service = services.getJSONObject(i);
                        if (service.containsKey("nameRef")) {
                            nameRef = service.getString("nameRef");
                            nameRef = nameRef.substring(0, nameRef.length() - 2);
                            ServiceGroupObjectRO serviceObjectInformation = serviceNameObjectMap.get(nameRef);
                            if(serviceObjectInformation!=null) {
                                serviceParamDTOS = buildServiceParamDTO(serviceObjectInformation);
                                serviceObjectMap.put(nameRef, serviceParamDTOS);
                                map.put("serviceNameObjectMap", serviceObjectMap);
                            }
                        }
                    }
                }
                JSONArray srcIpArray = (JSONArray) deviceFilterRuleListRO.getMatchClause().get("srcIp");
                JSONArray dstIpArray = (JSONArray) deviceFilterRuleListRO.getMatchClause().get("dstIp");
                if (srcIpNameObjectMap!=null && srcIps!=null&&srcIps.size() > 1) {
                    for (int i = 0; i < srcIpArray.size(); i++) {
                        JSONObject srcIp = srcIpArray.getJSONObject(i);
                        if (srcIp.containsKey("nameRef")) {
                            nameRef = srcIp.getString("nameRef");
                            nameRef = nameRef.substring(0, nameRef.length() - 2);
                            NetWorkGroupObjectRO srcIpNameObjectInformation = srcIpNameObjectMap.get(nameRef);
                            if(srcIpNameObjectInformation!=null) {
                                includeItemsROS.addAll(srcIpNameObjectInformation.getIncludeItems());
                            }
                        }
                    }
                    if(includeItemsROS!=null) {
                        srcIpAddressParam = buildIpAddressParam(includeItemsROS);
                        srcIpObjectMap.put(nameRef, srcIpAddressParam);
                        map.put("srcIpNameObjectMap", srcIpObjectMap);
                    }
                }
                    if ( dstIpNameObjectMap!=null&&dstIps!=null&&dstIps.size() > 1) {
                        for (int i = 0; i < dstIpArray.size(); i++) {
                            JSONObject dstIp = dstIpArray.getJSONObject(i);
                            if (dstIp.containsKey("nameRef")) {
                                nameRef = dstIp.getString("nameRef");
                                nameRef = nameRef.substring(0, nameRef.length() - 2);
                                NetWorkGroupObjectRO dstIpNameObjectInformation = dstIpNameObjectMap.get(nameRef);
                                if(dstIpNameObjectInformation!=null) {
                                    includeItemsROS.addAll(dstIpNameObjectInformation.getIncludeItems());
                                }
                            }
                        }
                        if(includeItemsROS!=null) {
                            dstIpAddressParam = buildIpAddressParam(includeItemsROS);
                            dstIpObjectMap.put(nameRef, dstIpAddressParam);
                            map.put("dstIpNameObjectMap", dstIpObjectMap);
                        }
                    }
                    policyCommandLine.append(generatorBean.generateSecurityPolicyCommandLine(StatusTypeEnum.ADD, buildPolicyParam(deviceFilterRuleListRO), map, null));
                    if (parameterMap != null && parameterMap.get("progress") != null) {
                        TranslationTaskProgressDTO progress = (TranslationTaskProgressDTO) parameterMap.get("progress");
                        progress.increOne();
                    }
            }catch (Exception e) {
                log.error("generateSecurityPolicyCommandLine error:",e);
                warningCommandLine.append(String.format("策略 %s 生成命令行异常，报错信息为：%s \n",deviceFilterRuleListRO.getName(),e.getMessage()));

            }
        }
        generateSecurityResult.put("commandLine",policyCommandLine.toString());
        generateSecurityResult.put("warning",warningCommandLine.toString());
        return generateSecurityResult;
    }

    /**
     * 构建地址对象原子化命令行参数
     * @param
     * @return
     */

    public IpAddressParamDTO buildIpAddressParam(List<IncludeItemsRO> includeItemsROS) {
//        List<IncludeItemsRO> includeItemsROS = netWorkGroupObjectRO.getIncludeItems();
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
    @Override
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
    @Override
    public String generateIpAddressObjectCommandLine(List<NetWorkGroupObjectRO> needAddNetWorkObjectList, Map<String, Object> parameterMap) throws Exception {
        StringBuffer objectCommandLine = new StringBuffer();
        for (NetWorkGroupObjectRO netWorkGroupObjectRO : needAddNetWorkObjectList) {
            IpAddressParamDTO paramDTO = buildIpAddressParam(netWorkGroupObjectRO);
            Map<String,Object> map = new HashMap<>();
            map.put(netWorkGroupObjectRO.getName(),parameterMap.get(netWorkGroupObjectRO.getName()));
            objectCommandLine.append(generatorBean.generateIpAddressObjectCommandLine(StatusTypeEnum.ADD, paramDTO.getIpTypeEnum(),
                    netWorkGroupObjectRO.getName(),null,paramDTO.getSingleIpArray(),paramDTO.getRangIpArray(),paramDTO.getSubnetIntIpArray(),paramDTO.getSubnetStrIpArray(),
                    null,paramDTO.getHosts(),paramDTO.getObjectNameRefArray(),netWorkGroupObjectRO.getDescription(),null,null,
                    map,null));
        }
        return objectCommandLine.toString();

    }


    @Override
    public String generateIpAddressObjectGroupCommandLine(List<NetWorkGroupObjectRO> needAddNetWorkObjectList, Map<String, Object> parameterMap) throws Exception {
        StringBuffer objectCommandLine = new StringBuffer();
        for (NetWorkGroupObjectRO netWorkGroupObjectRO : needAddNetWorkObjectList) {
            IpAddressParamDTO paramDTO = buildIpAddressParam(netWorkGroupObjectRO);
            Map<String,Object> map = new HashMap<>();
            //处理引用地址对象
            List<String> objectNameRefList = new ArrayList<>();
            List<String> includeItemNames = netWorkGroupObjectRO.getIncludeItemNames();
            map.put(netWorkGroupObjectRO.getName(),parameterMap.get(netWorkGroupObjectRO.getName())) ;
            if(CollectionUtils.isNotEmpty(includeItemNames)){
                objectNameRefList.addAll(includeItemNames);
            }
            if(ArrayUtils.isNotEmpty(paramDTO.getObjectNameRefArray())){
                objectNameRefList.addAll(Arrays.stream(paramDTO.getObjectNameRefArray()).collect(Collectors.toList()));
            }
            List<String> objectGroupNameRefArray = new ArrayList<>();
            List<String> includeGroupNames = netWorkGroupObjectRO.getIncludeGroupNames();
            if(CollectionUtils.isNotEmpty(includeGroupNames)){
                objectGroupNameRefArray.addAll(includeGroupNames);
            }
            if(ArrayUtils.isNotEmpty(paramDTO.getObjectGroupNameRefArray())){
                objectGroupNameRefArray.addAll(Arrays.stream(paramDTO.getObjectGroupNameRefArray()).collect(Collectors.toList()));
            }
            objectCommandLine.append(generatorBean.generateIpAddressObjectGroupCommandLine(StatusTypeEnum.ADD, paramDTO.getIpTypeEnum(),
                    netWorkGroupObjectRO.getName(),null,paramDTO.getSingleIpArray(),paramDTO.getRangIpArray(),paramDTO.getSubnetIntIpArray(),paramDTO.getSubnetStrIpArray(),
                    null,paramDTO.getHosts(),objectNameRefList.toArray(new String[objectNameRefList.size()]),paramDTO.getObjectGroupNameRefArray(),netWorkGroupObjectRO.getDescription(),null,null,
                    map,null));
        }
        return objectCommandLine.toString();
    }

    @Override
    public String generateServiceObjectCommandLine(List<ServiceGroupObjectRO> needAddServiceObjectList, Map<String, Object> parameterMap) throws Exception {
        StringBuffer serviceCommandLine = new StringBuffer();
        if(needAddServiceObjectList!=null) {
            for (ServiceGroupObjectRO serviceGroupObjectRO : needAddServiceObjectList) {
                Map<String, Object> map = new HashMap<>();
                List<ServiceParamDTO> serviceParamDTOS = buildServiceParamDTO(serviceGroupObjectRO);
                map.put(serviceGroupObjectRO.getName(), parameterMap.get(serviceGroupObjectRO.getName()));
                serviceCommandLine.append(generatorBean.generateServiceObjectCommandLine(StatusTypeEnum.ADD,
                        serviceGroupObjectRO.getName(), serviceGroupObjectRO.getId(), null, serviceParamDTOS,
                        serviceGroupObjectRO.getDescription(), parameterMap, null));
            }
        }
        return serviceCommandLine.toString();
    }

    @Override
    public String generateServiceObjectGroupCommandLine(List<ServiceGroupObjectRO> needAddServiceObjectGroupList, Map<String, Object> parameterMap) throws Exception {
        StringBuffer serviceCommandLine = new StringBuffer();
        for (ServiceGroupObjectRO serviceGroupObjectRO : needAddServiceObjectGroupList) {
            List<ServiceParamDTO> serviceParamDTOS = buildServiceParamDTO(serviceGroupObjectRO);
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



}
