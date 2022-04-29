package com.abtnetworks.totems.common.commandline.security;

import com.abtnetworks.totems.common.commandline.PolicyGenerator;
import com.abtnetworks.totems.common.commandline.SecurityPolicyGenerator;
import com.abtnetworks.totems.common.constants.PolicyConstants;
import com.abtnetworks.totems.common.dto.*;
import com.abtnetworks.totems.common.dto.commandline.CommandlineDTO;
import com.abtnetworks.totems.common.dto.commandline.PolicyMergeDTO;
import com.abtnetworks.totems.common.dto.commandline.PolicyObjectDTO;
import com.abtnetworks.totems.common.dto.commandline.ServiceDTO;
import com.abtnetworks.totems.common.dto.commandline.device.TopSecFirstPolicyDTO;
import com.abtnetworks.totems.common.enums.ActionEnum;
import com.abtnetworks.totems.common.enums.AddressTypeEnum;
import com.abtnetworks.totems.common.enums.MoveSeatEnum;
import com.abtnetworks.totems.common.ro.ResultRO;
import com.abtnetworks.totems.common.utils.*;
import com.abtnetworks.totems.whale.baseapi.ro.DeviceFilterRuleListRO;
import com.abtnetworks.totems.whale.baseapi.service.WhaleDevicePolicyClient;
import com.alibaba.fastjson.JSON;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @Author: wenjiachang
 * @Date: 2019/1/29 10:16
 */

@Service
public class SecurityTopsec extends SecurityPolicyGenerator implements PolicyGenerator {

    private static Logger logger = Logger.getLogger(SecurityTopsec.class);

    public final int MAX_OBJECT_NAME_LENGTH = 30;

    @Autowired
    private WhaleDevicePolicyClient whaleDevicePolicyClient;

    @Override
    public String generate(CmdDTO cmdDTO) {
        CommandlineDTO dto = new CommandlineDTO();
        PolicyDTO policyDTO = cmdDTO.getPolicy();
        BeanUtils.copyProperties(policyDTO, dto);
        DeviceDTO deviceDTO = cmdDTO.getDevice();
        BeanUtils.copyProperties(deviceDTO, dto);
        SettingDTO settingDTO = cmdDTO.getSetting();
        BeanUtils.copyProperties(settingDTO, dto);
        if (policyDTO.getAction().equals(ActionEnum.PERMIT)) {
            dto.setAction(PolicyConstants.POLICY_STR_PERMISSION_PERMIT);
        } else {
            dto.setAction(PolicyConstants.POLICY_STR_PERMISSION_DENY);
        }
        dto.setSwapRuleNameId(settingDTO.getSwapNameId());
        dto.setCreateObjFlag(settingDTO.isCreateObject());

        TaskDTO taskDTO = cmdDTO.getTask();
        dto.setName(taskDTO.getTheme());
        dto.setBusinessName(taskDTO.getTheme());
        dto.setCreateObjFlag(settingDTO.isCreateObject());

        ExistObjectDTO existObjectDTO = cmdDTO.getExistObject();
        dto.setSrcAddressName(existObjectDTO.getSrcAddressObjectName());
        dto.setDstAddressName(existObjectDTO.getDstAddressObjectName());
        dto.setServiceName(existObjectDTO.getServiceObjectName());
        dto.setExistServiceNameList(existObjectDTO.getExistServiceNameList());
        dto.setRestServiceList(existObjectDTO.getRestServiceList());
        dto.setSwapRuleNameId(settingDTO.getSwapNameId());
        dto.setIdleTimeout(policyDTO.getIdleTimeout());

        dto.setCurrentId(settingDTO.getPolicyId());
        GeneratedObjectDTO generatedDto = cmdDTO.getGeneratedObject();
        generatedDto.setPolicyName(dto.getName());
        dto.setGeneratedDto(generatedDto);
        String commandLine =  composite(dto);
        generatedDto.setAddressTypeMap(dto.getAddressTypeMap());
        generatedDto.setServiceObjectNameList(dto.getServiceObjectNameList());
        generatedDto.setTimeObjectNameList(dto.getTimeObjectNameList());
        return commandLine;
    }

    @Override
    public String generatePreCommandline(CommandlineDTO dto) {
        return "";
    }

    @Override
    public String generateCommandline(CommandlineDTO dto) {
        if (dto.getMergeDTO() == null || dto.isMustCreateFlag()) {
            return createCommandLine(dto);
        } else {
            return editCommandLine(dto);
        }
    }

    public String createCommandLine(CommandlineDTO dto) {
        StringBuilder sb = new StringBuilder();
        String ticket = dto.getName();
        String srcIp = dto.getSrcIp();
        String dstIp = dto.getDstIp();
        String startTime = dto.getStartTime();
        String endTime = dto.getEndTime();
        String srcZoneJoin = StringUtils.isNotBlank(dto.getSrcZone()) ? "srcarea '" + dto.getSrcZone() + "' " : "";
        String dstZoneJoin = StringUtils.isNotBlank(dto.getDstZone()) ? "dstarea '" + dto.getDstZone() + "' " : "";

        PolicyObjectDTO srcAddress = generateAddressObject(srcIp, ticket, dto.getSrcAddressName(), dto.getSrcIpSystem(),dto.getIpType());
        PolicyObjectDTO dstAddress = generateAddressObject(dstIp, ticket, dto.getDstAddressName(), dto.getDstIpSystem(),dto.getIpType());
        PolicyObjectDTO service = generateServiceObject(dto.getRestServiceList(), dto.getExistServiceNameList(),dto.getIpType());
        PolicyObjectDTO time = generateTimeObject(startTime, endTime, ticket);

        // 设置对象名称到大对象中
        dto.setAddressTypeMap(srcAddress.getAddressTypeMap());
        dto.getAddressTypeMap().putAll(dstAddress.getAddressTypeMap());
        dto.setServiceObjectNameList(service.getCreateServiceObjectName());
        recordCreateTimeObjectName(dto,time);

        StringBuilder define = new StringBuilder();

        if (srcAddress.isObjectFlag() && StringUtils.isNotBlank(srcAddress.getCommandLine())) {
            define.append(String.format("%s", srcAddress.getCommandLine()));
        }
        if (dstAddress.isObjectFlag() && StringUtils.isNotBlank(dstAddress.getCommandLine())) {
            define.append(String.format("%s", dstAddress.getCommandLine()));
        }
        if (StringUtils.isNotBlank(service.getCommandLine())) {
            define.append(String.format("%s", service.getCommandLine()));
        }
        if (time != null) {
            define.append(String.format("%s", time.getCommandLine()));
        }

        if (define.length() > 0) {
            sb.append("define\n");
            sb.append(define.toString());
            sb.append("end\n");
        }

        sb.append("firewall\n");

        String srcAddressString = AliStringUtils.isEmpty(srcAddress.getJoin()) == true ? "" : String.format("src \'%s\' ", srcAddress.getJoin());
        String dstAddressString = AliStringUtils.isEmpty(dstAddress.getJoin()) == true ? "" : String.format("dst \'%s\' ", dstAddress.getJoin());
        String serviceString = AliStringUtils.isEmpty(service.getJoin()) == true ? "" : "IP".equalsIgnoreCase(service.getJoin()) ? "" : String.format("service \'%s\' ", service.getJoin());
        String name = dto.getBusinessName();
        sb.append(String.format("policy add action %s %s%s" +
                "%s%s%s",  dto.getAction().equalsIgnoreCase("permit") ? "accept" : "deny", srcZoneJoin, dstZoneJoin, srcAddressString, dstAddressString, serviceString));
        if(dto.getIdleTimeout()!=null){
            sb.append(String.format("permanent yes session-timeout %d ",dto.getIdleTimeout()));

        }

        if (time != null) {
            sb.append(String.format("schedule %s ", time.getJoin()));
        }


        StringBuffer rollbackShowCmd = new StringBuffer();

        rollbackShowCmd.append(srcZoneJoin).append(dstZoneJoin);
        if(StringUtils.isNotBlank(srcAddress.getFirstIpNameJoin())){
            String rollbackSrcIp = AliStringUtils.isEmpty(srcAddress.getJoin()) == true ? "" : String.format("src \'%s\' ", srcAddress.getFirstIpNameJoin());
            rollbackShowCmd.append(rollbackSrcIp);
        }
        if(StringUtils.isNotBlank(dstAddress.getFirstIpNameJoin())){
            String rollbackDstIp = AliStringUtils.isEmpty(dstAddress.getJoin()) == true ? "" : String.format("dst \'%s\' ", dstAddress.getFirstIpNameJoin());
            rollbackShowCmd.append(rollbackDstIp);
        }
        if(StringUtils.isNotBlank(service.getFirstServiceJoin())){
            String rollbackService = AliStringUtils.isEmpty(service.getJoin()) == true ? "" : "IP".equalsIgnoreCase(service.getJoin()) ? "" : String.format("service \'%s\' ", service.getFirstServiceJoin());
            rollbackShowCmd.append(rollbackService);
        }
        if (time != null) {
            rollbackShowCmd.append(String.format("schedule \'%s\' ", time.getJoin()));
        }

        if (!AliStringUtils.isEmpty(dto.getGroupName())) {
            sb.append(String.format("group_name %s ", dto.getGroupName()));
            rollbackShowCmd.append(String.format("group_name \'%s\'", dto.getGroupName()));
        }

        String swapRuleNameId = StringUtils.isNotBlank(dto.getSwapRuleNameId()) ? dto.getSwapRuleNameId() : "";
        int moveSeatCode = dto.getMoveSeatEnum().getCode();
        if (moveSeatCode == MoveSeatEnum.BEFORE.getCode()) {
            if (!AliStringUtils.isEmpty(dto.getDescription())) {
                sb.append(String.format("comment %s ", dto.getDescription()));
            }
            if(!AliStringUtils.isEmpty(swapRuleNameId)) {
                sb.append(String.format("before %s ", swapRuleNameId));
                sb.append("\n");
            }

        } else if (moveSeatCode == MoveSeatEnum.AFTER.getCode()) {
            if (!AliStringUtils.isEmpty(dto.getDescription())) {
                sb.append(String.format("comment %s ", dto.getDescription()));
            }
            if (!AliStringUtils.isEmpty(swapRuleNameId)) {
                sb.append("\n");
                sb.append("end\n");
                sb.append(String.format("firewall policy move <policyId> after %s\n", swapRuleNameId));
            }
        } else if(moveSeatCode == MoveSeatEnum.FIRST.getCode()) {
            TopSecFirstPolicyDTO  topSecFirstPolicyDTO = getFirstPolicyId(dto.getDeviceUuid(), dto.getRuleListUuid(), dto.getGroupName());
            if(StringUtils.isEmpty(dto.getGroupName()) && StringUtils.isNotEmpty(topSecFirstPolicyDTO.getGroupName())){
                sb.append(String.format("group_name %s ", topSecFirstPolicyDTO.getGroupName()));
                rollbackShowCmd.append(String.format("group_name \'%s\'", topSecFirstPolicyDTO.getGroupName()));
            }
            if (!AliStringUtils.isEmpty(dto.getDescription())) {
                sb.append(String.format("comment %s ", dto.getDescription()));
            }
            String ruleId = topSecFirstPolicyDTO.getRuleId();
            if (!AliStringUtils.isEmpty(ruleId)) {
                sb.append(String.format("before %s ", ruleId));
                sb.append("\n");
            }else{
                sb.append("\n");
            }
        }else{
            sb.append("\n");
        }


        sb.append("\n");
        sb.append("end\n");
        dto.getGeneratedDto().setRollbackShowCmd(rollbackShowCmd.toString());

        return sb.toString();
    }

    public TopSecFirstPolicyDTO getFirstPolicyId(String deviceUuid, String ruleListUuid, String groupName) {
        ResultRO<List<DeviceFilterRuleListRO>> resultRO = whaleDevicePolicyClient.getFilterRuleList(deviceUuid, ruleListUuid);
        if (resultRO == null) {
            return null;
        }
        logger.debug(String.format("天融信(%s)策略集(%s)相关策略数据为：\n-----------------------------------\n", deviceUuid, ruleListUuid)
                + JSON.toJSONString(resultRO) + "\n-----------------------------------\n");
        List<DeviceFilterRuleListRO> list = resultRO.getData();

        TopSecFirstPolicyDTO  topSecFirstPolicyDTO = new  TopSecFirstPolicyDTO();
        if (CollectionUtils.isNotEmpty(list)) {
            for (DeviceFilterRuleListRO ruleListRO : list) {
                if (StringUtils.isNotEmpty(groupName)) {
                    //查找策略列表描述中的*group_name:总部上网*，查找group_name为总部上网的所有策略，找到组内第一条策略的ID 12246，直接*before 12246*
                    String description = ruleListRO.getDescription();
                    if (StringUtils.isNotEmpty(description) && description.contains("Group:")) {
                        String name = description.substring(description.indexOf("Group:") + 6, description.indexOf(".")).trim();
                        if (StringUtils.isNotEmpty(name) && name.equals(groupName)) {
                            String ruleId = ruleListRO.getRuleId();
                            String ruleName = ruleListRO.getName();
                            topSecFirstPolicyDTO.setRuleId(ruleId);
                            topSecFirstPolicyDTO.setGroupName(groupName);
                            topSecFirstPolicyDTO.setRuleName(ruleName);
                            return topSecFirstPolicyDTO;
                        }
                    } else {
                        continue;
                    }
                } else {
                    //查找策略列表第一条策略的ID，直接before到第一条策略
                    logger.info("找到策略:" + ruleListRO.getRuleId());
                    String ruleId = ruleListRO.getRuleId();
                    String description = ruleListRO.getDescription();
                    topSecFirstPolicyDTO.setRuleId(ruleId);
                    topSecFirstPolicyDTO.setRuleName(ruleListRO.getName());
                    if( StringUtils.isNotEmpty(description) && description.contains("Group:")){
                        String name = description.substring(description.indexOf("Group:") + 6, description.indexOf(".")).trim();
                        topSecFirstPolicyDTO.setGroupName(name);
                    }
                    return topSecFirstPolicyDTO;
                }
            }
        }
        return topSecFirstPolicyDTO;
    }

    public String editCommandLine(CommandlineDTO dto) {
        PolicyMergeDTO mergeDTO = dto.getMergeDTO();
        if (mergeDTO == null || StringUtils.isBlank(mergeDTO.getRuleId()) || StringUtils.isBlank(mergeDTO.getMergeField())) {
            logger.info("进行修改策略命令时，合并信息ruleID、mergeField 有为空的");
            return createCommandLine(dto);
        }
        String ruleId = mergeDTO.getRuleId();
        String mergeField = mergeDTO.getMergeField();

        String ticket = dto.getName();

        //正式开始编辑
        PolicyObjectDTO srcAddress = generateAddressObject(dto.getSrcIp(), ticket, dto.getSrcAddressName(), dto.getSrcIpSystem(),dto.getIpType());
        PolicyObjectDTO dstAddress = generateAddressObject(dto.getDstIp(), ticket, dto.getDstAddressName(), dto.getDstIpSystem(),dto.getIpType());
        PolicyObjectDTO service = generateServiceObject(dto.getServiceList(), dto.getServiceName());

        StringBuilder sb = new StringBuilder();

        if (mergeField.equals(PolicyConstants.SRC) && srcAddress.isObjectFlag() && StringUtils.isNotBlank(srcAddress.getCommandLine())) {
            sb.append(String.format("%s\n", srcAddress.getCommandLine()));
        } else if (mergeField.equals(PolicyConstants.DST) && dstAddress.isObjectFlag() && StringUtils.isNotBlank(dstAddress.getCommandLine())) {
            sb.append(String.format("%s\n", dstAddress.getCommandLine()));
        } else if (mergeField.equals(PolicyConstants.SERVICE) && service.isObjectFlag() && StringUtils.isNotBlank(service.getCommandLine())) {
            sb.append(String.format("%s\n", service.getCommandLine()));
        }

        sb.append(String.format("policy modify %s ", ruleId));

        if (mergeField.equals(PolicyConstants.SRC)) {
            sb.append(String.format(" src %s\n", srcAddress.getJoin()));
        }
        if (mergeField.equals(PolicyConstants.DST)) {
            sb.append(String.format("dst %s\n", dstAddress.getJoin()));
        }
        if (mergeField.equals(PolicyConstants.SERVICE) && service != null && StringUtils.isNotBlank(service.getName())) {
            sb.append(String.format("service %s\n", service.getJoin()));
        }

        return sb.toString();
    }

    @Override
    public String generatePostCommandline(CommandlineDTO dto) {
        return "";
    }


    /**
     * 修改前 天融信地址构建方法
     * @param ipAddress
     * @param ticket
     * @param existsAddressName
     * @param ipSystem
     * @param ipType
     * @return
     */
    public PolicyObjectDTO generateAddressObject(String ipAddress, String ticket, String existsAddressName,String ipSystem,Integer ipType) {
        PolicyObjectDTO dto = new PolicyObjectDTO();
        dto.setObjectFlag(true);
        if (StringUtils.isNotBlank(existsAddressName)) {
            dto.setJoin(existsAddressName);
            dto.setFirstIpNameJoin(existsAddressName);
            return dto;
        }

        if (AliStringUtils.isEmpty(ipAddress) && AliStringUtils.isEmpty(existsAddressName)) {
            dto.setJoin("");
            return dto;
        }
        StringBuilder sb = new StringBuilder();

        //连接字符串，如果是多个对象，则用空格区分
        StringBuilder joinStr = new StringBuilder();

        String[] arr = ipAddress.split(",");
        int index = 1;
        // 统计地址对象
        Map<String,String> createObject = new HashMap<>();
        for (String address : arr) {
            StringBuilder serialNum = new StringBuilder();
            String name = getObjectName(ticket, ipSystem, arr, index, serialNum);
//            ipv6
            if(ipType!=null&&ipType==1){
                if (IpUtils.isIPv6Range(address)) {
                    String startIp = IpUtils.getRangeStartIPv6(address);
                    String endIp = IpUtils.getRangeEndIPv6(address);
                    sb.append(String.format("range add name %s ip1 %s ip2 %s\n", name, startIp, endIp));
                    createObject.put(name, AddressTypeEnum.RANG.getCode());
                } else if (IpUtils.isIPv6Subnet(address)) {
                    String ip = IpUtils.getIpFromIpSegment(address);
                    sb.append(String.format("subnet add name %s ipaddr %s \n", name, address));
                    createObject.put(name, AddressTypeEnum.SUB.getCode());
                } else {
                    sb.append(String.format("host add name %s ipaddr %s\n", name, address));
                    createObject.put(name, AddressTypeEnum.HOST.getCode());
                }
            }else{
                if (IpUtils.isIPRange(address)) {
                    String startIp = IpUtils.getStartIpFromRange(address);
                    String endIp = IpUtils.getEndIpFromRange(address);
                    sb.append(String.format("range add name %s ip1 %s ip2 %s\n", name, startIp, endIp));
                    createObject.put(name, AddressTypeEnum.RANG.getCode());
                } else if (IpUtils.isIPSegment(address)) {
                    String ip = IpUtils.getIpFromIpSegment(address);
                    String maskBit = IpUtils.getMaskBitFromIpSegment(address);
                    sb.append(String.format("subnet add name %s ipaddr %s mask %s\n", name, ip, IPUtil.getMaskByMaskBit(maskBit)));
                    createObject.put(name, AddressTypeEnum.SUB.getCode());
                } else {
                    sb.append(String.format("host add name %s ipaddr %s\n", name, address));
                    createObject.put(name, AddressTypeEnum.HOST.getCode());
                }
            }

            if (StringUtils.isBlank(dto.getFirstIpNameJoin())) {
                dto.setFirstIpNameJoin(name);
            }

            joinStr.append(name + " ");
            index ++;
        }

        String join = joinStr.toString().trim();
        dto.setName(join);
        dto.setJoin(join);
        dto.setCommandLine(sb.toString());
        dto.setAddressTypeMap(createObject);
        return dto;
    }


    public PolicyObjectDTO generateAddressObjectV2(String ipAddress, String ticket, String existsAddressName,String ipSystem,Integer ipType) {
        PolicyObjectDTO dto = new PolicyObjectDTO();
        dto.setObjectFlag(true);
        if (StringUtils.isNotBlank(existsAddressName)) {
            dto.setJoin(existsAddressName);
            dto.setFirstIpNameJoin(existsAddressName);
            return dto;
        }

        if (AliStringUtils.isEmpty(ipAddress) && AliStringUtils.isEmpty(existsAddressName)) {
            dto.setJoin("");
            return dto;
        }
        StringBuilder sb = new StringBuilder();

        //连接字符串，如果是多个对象，则用空格区分
        StringBuilder joinStr = new StringBuilder();

        String[] arr = ipAddress.split(",");
        int index = 1;
        // 统计地址对象
        Map<String,String> createObject = new HashMap<>();

        boolean  postSrcAddressIsAllHost = true;
        String allAddress = "";
        for (String address : arr) {
            if (ipType != null && ipType == 1) {
                if (!IpUtils.isIPv6(address)) {
                    postSrcAddressIsAllHost = false;
                } else {
                    allAddress = allAddress + String.format("%s ", address);
                }

            } else {
                if (!IpUtils.isIP(address)) {
                    postSrcAddressIsAllHost = false;
                } else {
                    allAddress = allAddress + String.format("%s ", address);
                }
            }
        }

        // 该逻辑主要是当转换后的目的地址都填单ip的时候，会合并成一个，建一个地址对象
        if(postSrcAddressIsAllHost){
            StringBuilder serialNum = new StringBuilder();
            String name = getObjectName(ticket, ipSystem, arr, index, serialNum);
            allAddress = allAddress.trim();
            allAddress = String.format("\'%s\'", allAddress);
            sb.append(String.format("host add name %s ipaddr %s\n", name, allAddress));
            createObject.put(name, AddressTypeEnum.HOST.getCode());

            dto.setName(name);
            dto.setJoin(name);
            dto.setCommandLine(sb.toString());
            dto.setAddressTypeMap(createObject);
            return dto;
        }



        for (String address : arr) {
            StringBuilder serialNum = new StringBuilder();
            String name = getObjectName(ticket, ipSystem, arr, index, serialNum);
//            ipv6
            if(ipType!=null&&ipType==1){
                if (IpUtils.isIPv6Range(address)) {
                    String startIp = IpUtils.getRangeStartIPv6(address);
                    String endIp = IpUtils.getRangeEndIPv6(address);
                    sb.append(String.format("range add name %s ip1 %s ip2 %s\n", name, startIp, endIp));
                    createObject.put(name, AddressTypeEnum.RANG.getCode());
                } else if (IpUtils.isIPv6Subnet(address)) {
                    String ip = IpUtils.getIpFromIpSegment(address);
                    sb.append(String.format("subnet add name %s ipaddr %s \n", name, address));
                    createObject.put(name, AddressTypeEnum.SUB.getCode());
                } else {
                    sb.append(String.format("host add name %s ipaddr %s\n", name, address));
                    createObject.put(name, AddressTypeEnum.HOST.getCode());
                }
            }else{
                if (IpUtils.isIPRange(address)) {
                    String startIp = IpUtils.getStartIpFromRange(address);
                    String endIp = IpUtils.getEndIpFromRange(address);
                    sb.append(String.format("range add name %s ip1 %s ip2 %s\n", name, startIp, endIp));
                    createObject.put(name, AddressTypeEnum.RANG.getCode());
                } else if (IpUtils.isIPSegment(address)) {
                    String ip = IpUtils.getIpFromIpSegment(address);
                    String maskBit = IpUtils.getMaskBitFromIpSegment(address);
                    sb.append(String.format("subnet add name %s ipaddr %s mask %s\n", name, ip, IPUtil.getMaskByMaskBit(maskBit)));
                    createObject.put(name, AddressTypeEnum.SUB.getCode());
                } else {
                    sb.append(String.format("host add name %s ipaddr %s\n", name, address));
                    createObject.put(name, AddressTypeEnum.HOST.getCode());
                }
            }

            if (StringUtils.isBlank(dto.getFirstIpNameJoin())) {
                dto.setFirstIpNameJoin(name);
            }

            joinStr.append(name + " ");
            index ++;
        }

        // 对于转换后的源地址,如果地址有多个需要建多个对象然后再建一个地址组
        String join = joinStr.toString().trim();
        String[] joins = join.split(" ");
        String allAddressNames = "";
        if (joins.length > 1) {
            for (String itemJoin : joins) {
                allAddressNames = allAddressNames + String.format("%s ", itemJoin);
            }
            allAddressNames = allAddressNames.trim();
            allAddressNames = String.format("\'%s\'",allAddressNames);
            String addressGroupName = String.format("%s_Group_%s", ticket, IdGen.getRandomNumberString());
            sb.append(String.format("group_address add name %s member %s", addressGroupName, allAddressNames));
            sb.append(StringUtils.LF);
            join = addressGroupName;
        }

        dto.setName(join);
        dto.setJoin(join);
        dto.setCommandLine(sb.toString());
        dto.setAddressTypeMap(createObject);
        return dto;
    }

    /**
     * 转换后的目的地址构建方法
     * @param ipAddress
     * @param ticket
     * @param existsAddressName
     * @param ipSystem
     * @param ipType
     * @return
     */
    public PolicyObjectDTO generatePostDstAddressObject(String ipAddress, String ticket, String existsAddressName,String ipSystem,Integer ipType) {
        PolicyObjectDTO dto = new PolicyObjectDTO();
        dto.setObjectFlag(true);
        if (StringUtils.isNotBlank(existsAddressName)) {
            dto.setJoin(existsAddressName);
            dto.setFirstIpNameJoin(existsAddressName);
            return dto;
        }

        if (AliStringUtils.isEmpty(ipAddress) && AliStringUtils.isEmpty(existsAddressName)) {
            dto.setJoin("");
            return dto;
        }
        StringBuilder sb = new StringBuilder();

        //连接字符串，如果是多个对象，则用空格区分
        StringBuilder joinStr = new StringBuilder();

        String[] arr = ipAddress.split(",");
        boolean  postDstAddressIsAllHost = true;
        String allAddress = "";
        for (String address : arr) {
            if (ipType != null && ipType == 1) {
                if (!IpUtils.isIPv6(address)) {
                    postDstAddressIsAllHost = false;
                } else {
                    allAddress = allAddress + String.format("%s ", address);
                }

            } else {
                if (!IpUtils.isIP(address)) {
                    postDstAddressIsAllHost = false;
                } else {
                    allAddress = allAddress + String.format("%s ", address);
                }
            }
        }

        int index = 1;
        Map<String,String> createObject = new HashMap<>();

        // 该逻辑主要是当转换后的目的地址都填单ip的时候，会合并成一个，建一个地址对象
        if(postDstAddressIsAllHost){
            StringBuilder serialNum = new StringBuilder();
            String name = getObjectName(ticket, ipSystem, arr, index, serialNum);
            allAddress = allAddress.trim();
            allAddress = String.format("\'%s\'", allAddress);
            sb.append(String.format("host add name %s ipaddr %s\n", name, allAddress));
            createObject.put(name, AddressTypeEnum.HOST.getCode());

            dto.setName(name);
            dto.setJoin(name);
            dto.setCommandLine(sb.toString());
            dto.setAddressTypeMap(createObject);
            return dto;
        }


        // 统计地址对象
        for (String address : arr) {
            StringBuilder serialNum = new StringBuilder();
            String name = getObjectName(ticket, ipSystem, arr, index, serialNum);
//            ipv6
            if(ipType!=null&&ipType==1){
                if (IpUtils.isIPv6Range(address)) {
                    String startIp = IpUtils.getRangeStartIPv6(address);
                    String endIp = IpUtils.getRangeEndIPv6(address);
                    sb.append(String.format("range add name %s ip1 %s ip2 %s\n", name, startIp, endIp));
                    createObject.put(name, AddressTypeEnum.RANG.getCode());
                } else if (IpUtils.isIPv6Subnet(address)) {
                    String ip = IpUtils.getIpFromIpSegment(address);
                    sb.append(String.format("subnet add name %s ipaddr %s \n", name, address));
                    createObject.put(name, AddressTypeEnum.SUB.getCode());
                } else {
                    sb.append(String.format("host add name %s ipaddr %s\n", name, address));
                    createObject.put(name, AddressTypeEnum.HOST.getCode());
                }
            }else{
                if (IpUtils.isIPRange(address)) {
                    String startIp = IpUtils.getStartIpFromRange(address);
                    String endIp = IpUtils.getEndIpFromRange(address);
                    sb.append(String.format("range add name %s ip1 %s ip2 %s\n", name, startIp, endIp));
                    createObject.put(name, AddressTypeEnum.RANG.getCode());
                } else if (IpUtils.isIPSegment(address)) {
                    String ip = IpUtils.getIpFromIpSegment(address);
                    String maskBit = IpUtils.getMaskBitFromIpSegment(address);
                    sb.append(String.format("subnet add name %s ipaddr %s mask %s\n", name, ip, IPUtil.getMaskByMaskBit(maskBit)));
                    createObject.put(name, AddressTypeEnum.SUB.getCode());
                } else {
                    sb.append(String.format("host add name %s ipaddr %s\n", name, address));
                    createObject.put(name, AddressTypeEnum.HOST.getCode());
                }
            }

            if (StringUtils.isBlank(dto.getFirstIpNameJoin())) {
                dto.setFirstIpNameJoin(name);
            }

            joinStr.append(name + " ");
            index ++;
        }

        String join = joinStr.toString().trim();
        dto.setName(join);
        dto.setJoin(join);
        dto.setCommandLine(sb.toString());
        dto.setAddressTypeMap(createObject);
        return dto;
    }

    /**
     * 获取对象名称
     * @param ticket
     * @param ipSystem
     * @param arr
     * @param index
     * @param serialNum
     * @return
     */
    private String getObjectName(String ticket, String ipSystem, String[] arr, int index, StringBuilder serialNum) {
        String name;
        if (StringUtils.isNotEmpty(ipSystem)) {
            name = ipSystem;
            if (arr.length > 1) {
                serialNum.append("_").append(index);
            }
            // 对象名称长度限制，一个中文2个字符
            name = strSub(name, getMaxObejctNameLength(), "GB2312");
            // 对象名称长度限制
            int len = 0;
            // 序列号长度
            int serialLength = serialNum.length();
            try {
                len = name.getBytes("GB2312").length;
            } catch (Exception e) {
                logger.error("字符串长度计算异常");
            }
            if (len > getMaxObejctNameLength() - (7 + serialLength)) {
                name = strSub(name, getMaxObejctNameLength() - (7 + serialLength), "GB2312");
            }
            name = String.format("%s_%s", name, DateUtils.getDate().replace("-", "").substring(2));
            name = name + serialNum;
        } else {
            name = String.format("%s_AO_%s", ticket, IdGen.getRandomNumberString());
        }
        return name;
    }


    public int getMaxObejctNameLength() {
        return MAX_OBJECT_NAME_LENGTH;
    }

    private PolicyObjectDTO generateServiceObject(List<ServiceDTO> serviceDTOList, String existsServiceName) {
        PolicyObjectDTO dto = new PolicyObjectDTO();
        dto.setObjectFlag(true);
        if (StringUtils.isNotBlank(existsServiceName)) {
            dto.setJoin(existsServiceName);
            return dto;
        }

        String name = getServiceName(serviceDTOList);

        StringBuilder sb = new StringBuilder();
        for (ServiceDTO service : serviceDTOList) {
            int protocolNum = Integer.valueOf(service.getProtocol());
            String protocolString = ProtocolUtils.getProtocolByValue(protocolNum).toLowerCase();
            if (protocolString.equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_ANY)) {
                dto.setJoin(PolicyConstants.POLICY_STR_VALUE_ANY);
                return dto;
            }

            if (protocolString.equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_ICMP)) {
                sb.append(String.format("service add name %s protocol %s", name, service.getProtocol()));
            } else {
                sb.append(String.format("service add name %s protocol %s port %s port2 %s", name, service.getProtocol(), service.getSrcPorts(), service.getDstPorts()));
            }
            sb.append("\n");
        }

        dto.setCommandLine(sb.toString());
        dto.setName(name);
        dto.setJoin(name);

        return dto;
    }

    public PolicyObjectDTO generateServiceObject(List<ServiceDTO> serviceDTOList, List<String> existsServiceNameList,Integer ipType) {
        PolicyObjectDTO dto = new PolicyObjectDTO();
        if (serviceDTOList.size() == 0 && existsServiceNameList.size() == 0) {
            dto.setJoin("");
            return dto;
        }

        StringBuilder nameSb = new StringBuilder();
        if (existsServiceNameList.size() > 0) {
            for (String name : existsServiceNameList) {
                nameSb.append(" ");
                nameSb.append(name);
                if(StringUtils.isBlank(dto.getFirstServiceJoin())){
                    dto.setFirstServiceJoin(name);
                }
            }
        }
        List<String> createServiceObjectName = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        for (ServiceDTO service : serviceDTOList) {
            int protocolNum = Integer.valueOf(service.getProtocol());
            String protocolString = ProtocolUtils.getProtocolByValue(protocolNum).toLowerCase();
            if (protocolString.equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_ANY)) {
                dto.setJoin("");
                return dto;
            }

            if(StringUtils.isNotEmpty(service.getDstPorts())){
                String[]  ports = service.getDstPorts().split(",");
                for(String port : ports){
                    String name = getServiceName(service,ipType,port);
                    nameSb.append(" ");
                    nameSb.append(name);
                    if(StringUtils.isBlank(dto.getFirstServiceJoin())){
                        dto.setFirstServiceJoin(name);
                    }
                    if(ipType!=null&&ipType==0) {
                        //ipv4
                        if (!protocolString.equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_ICMP)) {
                            if (!service.getDstPorts().equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_ANY)) {
                                if (PortUtils.isPortRange(port)) {
                                    String start = PortUtils.getStartPort(port);
                                    String end = PortUtils.getEndPort(port);
                                    sb.append(String.format("service add name %s protocol %s port %s port2 %s \n", name, service.getProtocol(), start, end));
                                } else {
                                    sb.append(String.format("service add name %s protocol %s port %s \n", name, service.getProtocol(), port));
                                }
                            }
                        }
                    }else{
                        if (protocolString.equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_ICMPV6)) {
                            sb.append(String.format("service add name ICMPv6 protocol 58 \n"));
                        } else {
                            if (!port.equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_ANY)) {
                                if (PortUtils.isPortRange(port)) {
                                    String start = PortUtils.getStartPort(port);
                                    String end = PortUtils.getEndPort(port);
                                    sb.append(String.format("service add name %s protocol %s port %s port2 %s \n", name, service.getProtocol(), start, end));
                                } else {
                                    sb.append(String.format("service add name %s protocol %s port %s \n", name, service.getProtocol(), port));
                                }
                            }
                        }
                    }
                    createServiceObjectName.add(name);
                }
            }
        }

        if (nameSb.length() > 0) {
            nameSb.deleteCharAt(0);
        }

        dto.setCommandLine(sb.toString());
        dto.setName(nameSb.toString());
        dto.setJoin(nameSb.toString());
        dto.setCreateServiceObjectName(createServiceObjectName);
        return dto;
    }

    public PolicyObjectDTO generateTimeObject(String startTime, String endTime, String ticket) {
        if (startTime == null) {
            return null;
        }
        PolicyObjectDTO object = new PolicyObjectDTO();
        String name = String.format("%s_TR_%s", ticket, IdGen.getRandomNumberString());
        StringBuilder sb = new StringBuilder();

        sb.append(String.format("schedule add name %s cyctype year sdate %s stime %s " +
                        " edate %s etime %s\n", name, getDate(startTime), getTime(startTime),
                getDate(endTime), getTime(endTime)));

        object.setName(name);
        object.setJoin(name);
        object.setCommandLine(sb.toString());
        return object;
    }

    private String getRandomString() {
        return IdGen.randomBase62(PolicyConstants.POLICY_INT_RAMDOM_ID_LENGTH);
    }

    private String formatTimeString(String timeString) {
        return TimeUtils.transformDateFormat(timeString, TimeUtils.EUROPEAN_TIME_FORMAT, TimeUtils.HUAWEI_TIME_FORMAT);
    }

    private String getDate(String timeString) {
        SimpleDateFormat sdf = new SimpleDateFormat(TimeUtils.EUROPEAN_TIME_FORMAT);
        Date date = new Date();
        try {
            date = sdf.parse(timeString);
        } catch (Exception e) {
            e.printStackTrace();
        }
        SimpleDateFormat dst = new SimpleDateFormat("yyyy-MM-dd");
        return dst.format(date);
    }

    private String getTime(String timeString) {
        SimpleDateFormat sdf = new SimpleDateFormat(TimeUtils.EUROPEAN_TIME_FORMAT);
        Date date = new Date();
        try {
            date = sdf.parse(timeString);
        } catch (Exception e) {
            e.printStackTrace();
        }
        SimpleDateFormat dst = new SimpleDateFormat("hh:mm:ss");
        return dst.format(date);
    }

    public String getServiceName(ServiceDTO service, Integer ipType,String port) {
        StringBuilder nameSb = new StringBuilder();
        nameSb.append(ProtocolUtils.getProtocolByString(service.getProtocol().toLowerCase()));
        int protocolNum = Integer.valueOf(service.getProtocol());
        String protocol=ProtocolUtils.getProtocolByValue(protocolNum);
//        ipv6
        if(ipType!=null&&ipType==1) {
            if (protocol.equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_ICMPV6)) {
                if (port.equals(PolicyConstants.POLICY_STR_VALUE_ANY)) {
                    //自定义服务大写
                    return nameSb.toString().toUpperCase().replace("V", "v");
                }
            }else{
                if (port.equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_ANY)) {
                    //自定义服务大写
                    return nameSb.toString().toUpperCase();
                } else {
                    nameSb.append("_");
                    if (PortUtils.isPortRange(port)) {
                        String start = PortUtils.getStartPort(port);
                        String end = PortUtils.getEndPort(port);
                        nameSb.append(start);
                        nameSb.append("_");
                        nameSb.append(end);
                    } else {
                        nameSb.append(port);
                    }
                }
            }
        }
//        ipv4
        else {
            if (protocol.equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_ICMP)) {
                if (port.equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_ANY)) {
                    //自定义服务大写
                    return nameSb.toString().toUpperCase();
                }
            }
            else {
                if (port.equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_ANY)) {
                    //自定义服务大写
                    return nameSb.toString().toUpperCase();
                } else {
                    nameSb.append("_");
                    if (PortUtils.isPortRange(port)) {
                        String start = PortUtils.getStartPort(port);
                        String end = PortUtils.getEndPort(port);
                        nameSb.append(start);
                        nameSb.append("_");
                        nameSb.append(end);
                    } else {
                        nameSb.append(port);
                    }
                }
            }
        }
        return nameSb.toString();
        }



    public static void main(String[] args) {
        CommandlineDTO dto = CommandlineDTO.getInstanceDemo();
        SecurityTopsec topsec = new SecurityTopsec();
        dto.setIpType(1);
        dto.setSrcIp("1.1.1.1/24");
        dto.setDstIp("2.1.1.22/32");

//        List<ServiceDTO> serviceDTOS = new ArrayList<>();
//        ServiceDTO serviceDTO = new ServiceDTO();
        List<String> serviceDTO3 = new ArrayList<>();
        serviceDTO3.add("freg");
//        serviceDTO.setProtocol("6");
//        serviceDTO.setDstPorts("42,45-47");
//        ServiceDTO serviceDTO2 = new ServiceDTO();
//        serviceDTO2.setProtocol("58");
//        serviceDTO2.setDstPorts("any");
//        serviceDTOS.add(serviceDTO);
//        serviceDTOS.add(serviceDTO2);
//        dto.setServiceList(serviceDTOS);
//        dto.setRestServiceList(serviceDTOS);
        dto.setExistServiceNameList(serviceDTO3);
        dto.setIdleTimeout(7834);
        dto.setStartTime("2008-1-3 10:33:13");
        dto.setEndTime("2008-1-3 10:33:16");

        String commandLine = topsec.composite(dto);
        System.out.println("commandline:\n" + commandLine);

    }
}
