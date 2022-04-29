package com.abtnetworks.totems.common.commandline.security;

import com.abtnetworks.totems.common.commandline.PolicyGenerator;
import com.abtnetworks.totems.common.commandline.SecurityPolicyGenerator;
import com.abtnetworks.totems.common.constants.PolicyConstants;
import com.abtnetworks.totems.common.dto.*;
import com.abtnetworks.totems.common.dto.commandline.CommandlineDTO;
import com.abtnetworks.totems.common.dto.commandline.PolicyMergeDTO;
import com.abtnetworks.totems.common.dto.commandline.PolicyObjectDTO;
import com.abtnetworks.totems.common.dto.commandline.ServiceDTO;
import com.abtnetworks.totems.common.enums.ActionEnum;
import com.abtnetworks.totems.common.enums.IpTypeEnum;
import com.abtnetworks.totems.common.enums.MoveSeatEnum;
import com.abtnetworks.totems.common.enums.UrlTypeEnum;
import com.abtnetworks.totems.common.utils.*;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 山石5.5设备 安全策略命令行
 * @Author: lifei
 * @Date: 2021/7/14 9:41
 */
@Service
public class SecurityHillStoneV5 extends SecurityPolicyGenerator implements PolicyGenerator {

    private static Logger logger = Logger.getLogger(SecurityHillStoneV5.class);

    private static Set<String> allowType = new HashSet<>();

    private final int MAX_NAME_LENGTH = 95;

    private final int DAY_SECOND = 24 * 60 * 60;

    public SecurityHillStoneV5() {
        init();
    }

    private static void init() {
        allowType.add("3");
        allowType.add("4");
        allowType.add("5");
        allowType.add("8");
        allowType.add("11");
        allowType.add("12");
        allowType.add("13");
        allowType.add("15");
    }

    @Override
    public String generate(CmdDTO cmdDTO) {

        logger.info("cmdDTO is " + cmdDTO);
        CommandlineDTO dto = new CommandlineDTO();
        PolicyDTO policyDTO = cmdDTO.getPolicy();
        BeanUtils.copyProperties(policyDTO, dto);
        DeviceDTO deviceDTO = cmdDTO.getDevice();
        BeanUtils.copyProperties(deviceDTO, dto);
        SettingDTO settingDTO = cmdDTO.getSetting();
        BeanUtils.copyProperties(settingDTO, dto);
        if(policyDTO.getAction().equals(ActionEnum.PERMIT)) {
            dto.setAction(PolicyConstants.POLICY_STR_PERMISSION_PERMIT);
        } else {
            dto.setAction(PolicyConstants.POLICY_STR_PERMISSION_DENY);
        }

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

        dto.setExistSrcAddressList(existObjectDTO.getExistSrcAddressList());
        dto.setRestSrcAddressList(existObjectDTO.getRestSrcAddressList());
        dto.setExistDstAddressList(existObjectDTO.getExistDstAddressList());
        dto.setRestDstAddressList(existObjectDTO.getRestDstAddressList());
        dto.setSwapRuleNameId(settingDTO.getSwapNameId());
        dto.setIdleTimeout(policyDTO.getIdleTimeout());
        // ip类型默认为ipv4
        if(ObjectUtils.isEmpty(dto.getIpType())){
            dto.setIpType(IpTypeEnum.IPV4.getCode());
        }

        GeneratedObjectDTO generatedDto = cmdDTO.getGeneratedObject();
        generatedDto.setPolicyName(dto.getName());
        String commandLine = composite(dto);
        generatedDto.setAddressObjectNameList(dto.getAddressObjectNameList());
        generatedDto.setAddressObjectGroupNameList(dto.getAddressObjectGroupNameList());
        generatedDto.setServiceObjectNameList(dto.getServiceObjectNameList());
        generatedDto.setServiceObjectGroupNameList(dto.getServiceObjectGroupNameList());
        generatedDto.setTimeObjectNameList(dto.getTimeObjectNameList());
        return  commandLine;
    }

    @Override
    public String generatePreCommandline(CommandlineDTO dto) {
        StringBuilder sb = new StringBuilder();
        if (dto.isVsys()) {
            sb.append("enter-vsys " + dto.getVsysName() + "\n");
        }
        return sb.toString();
    }

    @Override
    public String generateCommandline(CommandlineDTO dto) {


        if(dto.getMergeDTO() == null || dto.isMustCreateFlag()) {
            return createCommandLine(dto);
        }else{
            return editCommandLine(dto);
        }
    }

    public String editCommandLine(CommandlineDTO dto) {
        PolicyMergeDTO mergeDTO = dto.getMergeDTO();
        if (mergeDTO == null || StringUtils.isBlank(mergeDTO.getRuleId()) || StringUtils.isBlank(mergeDTO.getMergeField())) {
            logger.info("进行修改策略命令时，合并信息ruleID、mergeField 有为空的");
            return createCommandLine(dto);
        }
        String ruleId = mergeDTO.getRuleId();
        String mergeField = mergeDTO.getMergeField();

        //正式开始编辑
        StringBuilder sb = new StringBuilder();
        sb.append("configure\n");

        boolean createObjFlag = dto.isCreateObjFlag();
        String ticket = dto.getName();
        PolicyObjectDTO srcAddressObject = generateAddressObject(dto.getSrcIp(), ticket, "src", createObjFlag, dto.getSrcAddressName(), dto.getSrcIpSystem(), dto.getIpType(),dto.getUrlType());
        PolicyObjectDTO dstAddressObject = generateAddressObject(dto.getDstIp(), ticket, "dst", createObjFlag, dto.getDstAddressName(), dto.getDstIpSystem(), dto.getIpType(),dto.getUrlType());
        PolicyObjectDTO serviceObject = generateServiceObject(dto.getRestServiceList(), dto.getExistServiceNameList(), dto.getIdleTimeout(),0);

        // 记录创建对象名称
        recordCreateAddrAndServiceObjectName(dto,srcAddressObject,dstAddressObject,null,null);
        recordCreateServiceObjectNames(dto,serviceObject);


        if(mergeField.equals(PolicyConstants.SRC) && srcAddressObject.isObjectFlag() && StringUtils.isNotBlank(srcAddressObject.getCommandLine())) {
            sb.append(srcAddressObject.getCommandLine());
        }
        if(mergeField.equals(PolicyConstants.DST) && dstAddressObject.isObjectFlag() && StringUtils.isNotBlank(dstAddressObject.getCommandLine())) {
            sb.append(dstAddressObject.getCommandLine());
        }
        if(mergeField.equals(PolicyConstants.SERVICE) && serviceObject.isObjectFlag() && StringUtils.isNotBlank(serviceObject.getCommandLine())) {
            sb.append(serviceObject.getCommandLine());
        }

        sb.append(String.format("rule id %s\n", ruleId));
        if(mergeField.equals(PolicyConstants.SRC)) {
            if(srcAddressObject.isObjectFlag()) {
                sb.append(srcAddressObject.getJoin());
            }else{
                sb.append(srcAddressObject.getCommandLine());
            }
        }else if(mergeField.equals(PolicyConstants.DST)) {
            if(dstAddressObject.isObjectFlag()) {
                sb.append(dstAddressObject.getJoin());
            }else{
                sb.append(dstAddressObject.getCommandLine());
            }
        }else if(mergeField.equals(PolicyConstants.SERVICE)) {
            if (serviceObject.isObjectFlag() && StringUtils.isNotBlank(serviceObject.getJoin())) {
                sb.append(serviceObject.getJoin());
            } else {
                sb.append(serviceObject.getCommandLine());
            }
        }

        sb.append("exit\n");
        sb.append("end\n");
        return sb.toString();
    }

    public String createCommandLine(CommandlineDTO dto) {
        String[] split = dto.getSrcIp().split(",");
        boolean isSrcSingle = false;
        if(split.length==1){
            isSrcSingle = true;
        }

        String[] split2 = dto.getDstIp().split(",");
        boolean isDstSingle = false;
        if(split2.length==1){
            isDstSingle = true;
        }


        StringBuilder sb = new StringBuilder();
        String ticket = dto.getName();
        String srcIp = dto.getSrcIp();
        String dstIp = dto.getDstIp();
        String startTime = dto.getStartTime();
        String endTime = dto.getEndTime();
        boolean createObjFlag = dto.isCreateObjFlag();
        String  existSrcName = dto.getSrcAddressName();
        PolicyObjectDTO srcAddress;
        if(StringUtils.isNotEmpty(existSrcName)){
            srcAddress = generateAddressObject(srcIp,ticket,"src",createObjFlag,existSrcName,dto.getSrcIpSystem(), dto.getIpType(),dto.getUrlType());
        }else {
            srcAddress = generateAddressObject(dto.getExistSrcAddressList(), dto.getRestSrcAddressList(), ticket, "src", createObjFlag, dto.getSrcIpSystem(), dto.getIpType(),dto.getUrlType());
        }
        String existDstName = dto.getDstAddressName();
        PolicyObjectDTO dstAddress;
        if(StringUtils.isNotEmpty(existDstName)){
            dstAddress = generateAddressObject(dstIp,ticket,"dst",createObjFlag,existDstName,dto.getDstIpSystem(), dto.getIpType(),dto.getUrlType());
        }else{
            dstAddress  = generateAddressObject(dto.getExistDstAddressList(), dto.getRestDstAddressList(), ticket, "dst", createObjFlag, dto.getDstIpSystem(), dto.getIpType(),dto.getUrlType());
        }
        String existServiceName = dto.getServiceName();
        PolicyObjectDTO serviceObject;
        if(StringUtils.isNotEmpty(existServiceName)){
            serviceObject = generateServiceObject(dto.getServiceList(), dto.getIdleTimeout(), existServiceName);
        }else{
            serviceObject = generateServiceObject(dto.getRestServiceList(), dto.getExistServiceNameList(), dto.getIdleTimeout(), dto.getIpType());
        }

        PolicyObjectDTO time = generateTimeObject(startTime, endTime, ticket);

        sb.append("configure\n");

        //定义对象
        if(!isSrcSingle){
            if(srcAddress.isObjectFlag() && StringUtils.isNotBlank(srcAddress.getCommandLine())) {
                sb.append(String.format("%s\n", srcAddress.getCommandLine()));
            }
        }

        if(!isDstSingle){
            if(dstAddress.isObjectFlag() && StringUtils.isNotBlank(dstAddress.getCommandLine())) {
                sb.append(String.format("%s\n", dstAddress.getCommandLine()));
            }
        }
        if(!isSrcSingle && !isDstSingle){
            recordCreateAddrAndServiceObjectName(dto,srcAddress,dstAddress,null,null);
        }else if(!isSrcSingle && isDstSingle){
            recordCreateAddrAndServiceObjectName(dto,srcAddress,null,null,null);
        }else if(isSrcSingle && !isDstSingle){
            recordCreateAddrAndServiceObjectName(dto,null,dstAddress,null,null);
        }


        //山石比较特殊，即使指定了不创建对象，但有具体的源、目的端口时，也会创建对象的
        if(serviceObject != null && serviceObject.isObjectFlag() && StringUtils.isNotBlank(serviceObject.getCommandLine())) {
            sb.append(String.format("%s\n", serviceObject.getCommandLine()));
            recordCreateServiceObjectNames(dto,serviceObject);
        }
        if(time != null) {
            sb.append(String.format("%s\n", time.getCommandLine()));
            recordCreateTimeObjectName(dto,time);
        }

        sb.append("rule ");
        String swapRuleNameId = StringUtils.isNotBlank(dto.getSwapRuleNameId()) ? dto.getSwapRuleNameId() : "";
        int moveSeatCode = dto.getMoveSeatEnum().getCode();
        if (moveSeatCode == MoveSeatEnum.FIRST.getCode()) {
            sb.append("top \n");
        } else if (moveSeatCode == MoveSeatEnum.BEFORE.getCode() || moveSeatCode == MoveSeatEnum.AFTER.getCode()) {
            if(StringUtils.isNotEmpty(swapRuleNameId)){
                sb.append(String.format("%s %s\n",dto.getMoveSeatEnum().getKey(), swapRuleNameId));
            }else{
                sb.append("\n");
            }

        } else {
            sb.append("\n");
        }

        sb.append(String.format("name %s\n",dto.getName()));

//        sb.append(String.format("name %s\n", dto.getBusinessName()));
        if(!AliStringUtils.isEmpty(dto.getDescription())){
            sb.append(String.format("description %s\n", dto.getDescription()));
        }

//        if(StringUtils.isNotBlank(dto.getSrcZone())) {
//            sb.append(String.format("src-zone %s\n", dto.getSrcZone()));
//        }
//        if(StringUtils.isNotBlank(dto.getDstZone())) {
//            sb.append(String.format("dst-zone %s\n", dto.getDstZone()));
//        }

        // if (srcAddress.isObjectFlag() && StringUtils.isNotBlank(srcAddress.getJoin())) {
        if(isSrcSingle&&StringUtils.isNotEmpty(dto.getSrcIp())) {
            String fullStr = "";
            if (IpTypeEnum.IPV4.getCode() == dto.getIpType().intValue()) {
                if (IpUtils.isIPSegment(dto.getSrcIp())) {
                    fullStr = String.format("src-ip %s\n", dto.getSrcIp());
                } else if (IpUtils.isIPRange(dto.getSrcIp())) {
                    String startIp = IpUtils.getStartIpFromRange(dto.getSrcIp());
                    String endIp = IpUtils.getEndIpFromRange(dto.getSrcIp());
                    fullStr = String.format("src-range %s %s\n", startIp, endIp);
                } else {
                    fullStr = String.format("src-ip %s/32\n", dto.getSrcIp());
                }
            }else if(dto.getIpType().intValue() == IpTypeEnum.IPV6.getCode()){
                // ipv6
                if (dto.getSrcIp().contains("/")) {
                    fullStr = String.format("src-ip %s\n", dto.getSrcIp());
                } else if(dto.getSrcIp().contains("-")){
                    // 范围
                    String startIp = IpUtils.getRangeStartIPv6(dto.getSrcIp());
                    String endIp = IpUtils.getRangeEndIPv6(dto.getSrcIp());
                    fullStr = String.format("src-range %s %s\n", startIp, endIp);
                } else {
                    fullStr = String.format("src-ip %s/128\n", dto.getSrcIp());
                }
            }else {
                // 目的地址是URL类型
                // ipv4
                String address = dto.getSrcIp();
                if (IpUtils.isIPSegment(address)) {
                    fullStr = String.format("src-ip %s\n", address);
                } else if (IpUtils.isIPRange(address)) {
                    String startIp = IpUtils.getStartIpFromRange(address);
                    String endIp = IpUtils.getEndIpFromRange(address);
                    fullStr = String.format("src-range %s %s\n", startIp, endIp);
                } else if (IpUtils.isIPv6(address)) {
                    // ipv6
                    if (address.contains("/")) {
                        fullStr = String.format("src-ip %s\n", address);
                    } else if (address.contains("-")) {
                        // 范围
                        String startIp = IpUtils.getRangeStartIPv6(address);
                        String endIp = IpUtils.getRangeEndIPv6(address);
                        fullStr = String.format("src-range %s %s\n", startIp, endIp);
                    } else {
                        fullStr = String.format("src-ip %s/128\n", address);
                    }
                } else if (IpUtils.isIP(address)) {
                    fullStr = String.format("src-ip %s/32\n", address);
                } else {
                    // 域名
                    fullStr = String.format("src-host %s\n", address);
                }
            }
            sb.append(fullStr);

        }
        else if(StringUtils.isEmpty(dto.getSrcIp())){
            if(dto.getIpType().intValue() == IpTypeEnum.IPV6.getCode()){
                String ipv6name="\"IPv6-any\"";
                sb.append(String.format("src-addr %s\n", ipv6name));
            }
            else
            {
                sb.append("src-addr any\n");
            }

        }
        else{
            if (StringUtils.isNotBlank(srcAddress.getJoin())) {
                sb.append(srcAddress.getJoin());
            } else {
                sb.append(srcAddress.getName());
            }
        }

        // if (dstAddress.isObjectFlag() && StringUtils.isNotBlank(dstAddress.getJoin())) {
        if(isDstSingle&&StringUtils.isNotEmpty(dto.getDstIp())){
            String fullStr = "";
            if(IpTypeEnum.IPV4.getCode() == dto.getIpType().intValue()){
                if (IpUtils.isIPSegment(dto.getDstIp())) {
                    fullStr = String.format("dst-ip %s\n", dto.getDstIp());
                } else if (IpUtils.isIPRange(dto.getDstIp())) {
                    String startIp = IpUtils.getStartIpFromRange(dto.getDstIp());
                    String endIp = IpUtils.getEndIpFromRange(dto.getDstIp());
                    fullStr = String.format("dst-range %s %s\n", startIp, endIp);
                } else {
                    fullStr = String.format("dst-ip %s/32\n", dto.getDstIp());
                }
            }else if (IpTypeEnum.IPV6.getCode() == dto.getIpType().intValue()){
                // ipv6
                if (dto.getDstIp().contains("/")) {
                    fullStr = String.format("dst-ip %s\n", dto.getDstIp());
                } else if(dto.getDstIp().contains("-")){
                    // 范围
                    String startIp = IpUtils.getRangeStartIPv6(dto.getDstIp());
                    String endIp = IpUtils.getRangeEndIPv6(dto.getDstIp());
                    fullStr = String.format("dst-range %s %s\n", startIp, endIp);
                } else {
                    fullStr = String.format("dst-ip %s/128\n", dto.getDstIp());
                }
            }else {
                // 域名
                fullStr = String.format("host %s\n", dto.getDstIp());
            }
            sb.append(fullStr);
        }else if(StringUtils.isEmpty(dto.getDstIp()))
        {
            if(dto.getIpType().intValue() == IpTypeEnum.IPV6.getCode()){
                String ipv6name="\"IPv6-any\"";
                sb.append(String.format("dst-addr %s\n", ipv6name));
            }
            else {
                sb.append(String.format("dst-addr any\n"));
            }
        }else
            {
            if (StringUtils.isNotBlank(dstAddress.getJoin())) {
                sb.append(dstAddress.getJoin());
            } else {
                sb.append(dstAddress.getName());
            }
        }


        if (serviceObject != null && serviceObject.isObjectFlag() && StringUtils.isNotBlank(serviceObject.getJoin())) {
            sb.append(serviceObject.getJoin());
        } else if (serviceObject != null && StringUtils.isNotBlank(serviceObject.getCommandLine())) {
            sb.append(serviceObject.getName());
        }

        if(time != null) {
            sb.append(String.format("schedule %s\n", time.getName()));
        }
        sb.append(String.format("action %s\n", dto.getAction().toLowerCase()));
        sb.append("exit\n");
        sb.append("end\n");
        return sb.toString();
    }

    @Override
    public String generatePostCommandline(CommandlineDTO dto) {
        return "";
    }

    public PolicyObjectDTO generateAddressObject(List<String> existAddressList, List<String>restAddressList, String ticket,
                                                 String ipPrefix, boolean createObjFlag,String ipSystem, Integer ipType,Integer urlType) {
        PolicyObjectDTO policyObjectDTO = new PolicyObjectDTO();
        policyObjectDTO.setName("any");
        if(ipType.intValue() == IpTypeEnum.IPV4.getCode()){
            policyObjectDTO.setJoin(ipPrefix + "-addr any\n");
        } else if(ipType.intValue() == IpTypeEnum.IPV6.getCode()){
            policyObjectDTO.setJoin(ipPrefix + "-addr IPv6-any\n");
        }else if (ipType.intValue() == IpTypeEnum.URL.getCode()){
            if(urlType.intValue() == UrlTypeEnum.IPV4.getCode()){
                policyObjectDTO.setJoin(ipPrefix + "-addr any\n");
            } else if(urlType.intValue() == UrlTypeEnum.IPV6.getCode()){
                policyObjectDTO.setJoin(ipPrefix + "-addr IPv6-any\n");
            }
        }
        policyObjectDTO.setObjectFlag(true);

        if(restAddressList.size() > 0) {
            StringBuilder sb = new StringBuilder();
            for(String addr: restAddressList) {
                sb.append(",");
                sb.append(addr);
            }
            if(sb.length() >0 ) {
                sb.deleteCharAt(0);
            }
            policyObjectDTO = generateAddressObject(sb.toString(), ticket, ipPrefix, createObjFlag,"", ipSystem,ipType,urlType);
        }

        if(existAddressList.size() > 0) {
            for(String existName: existAddressList) {
                policyObjectDTO.setJoin((policyObjectDTO.getJoin().contains("any")?"":policyObjectDTO.getJoin()) + ipPrefix + "-addr " + existName + "\n");
            }
        }

        logger.info("policyObjectDTO is " + JSONObject.toJSONString(policyObjectDTO));
        return policyObjectDTO;
    }

    public PolicyObjectDTO generateAddressObject(String ipAddress, String ticket, String ipPrefix, boolean createObjFlag, String existsAddressName, String ipSystem, Integer ipType,Integer urlType) {
        PolicyObjectDTO dto = new PolicyObjectDTO();

        if(AliStringUtils.isEmpty(ipAddress) || ipAddress.equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_ANY)) {
            if(ipType.intValue() == IpTypeEnum.IPV4.getCode()){
                dto.setJoin(ipPrefix + "-addr any\n");
                dto.setName("any");
                dto.setObjectFlag(true);
                return dto;
            } else if(ipType.intValue() == IpTypeEnum.IPV6.getCode()){
                dto.setJoin(ipPrefix + "-addr IPv6-any\n");
                dto.setName("any");
                dto.setObjectFlag(true);
                return dto;
            }else if(ipType.intValue() == IpTypeEnum.URL.getCode()){
                if (urlType.intValue() == UrlTypeEnum.IPV4.getCode()){
                    dto.setJoin(ipPrefix + "-addr any\n");
                    dto.setName("any");
                    dto.setObjectFlag(true);
                    return dto;
                }else {
                    dto.setJoin(ipPrefix + "-addr IPv6-any\n");
                    dto.setName("any");
                    dto.setObjectFlag(true);
                    return dto;
                }
            }
        }

        if (StringUtils.isNotBlank(existsAddressName)) {
            dto.setObjectFlag(true);
            if (existsAddressName.indexOf("\"") < 0) {
                existsAddressName = "\"" + existsAddressName + "\"";
            }
            dto.setJoin(ipPrefix + "-addr " + existsAddressName + "\n");
            dto.setName(existsAddressName);
            return dto;
        }
        List<String>  createObjectNames = new ArrayList<>();
        dto.setObjectFlag(createObjFlag);

        String[] arr = ipAddress.split(",");
        StringBuilder sb = new StringBuilder();
        String name = null;

        if(StringUtils.isNotBlank(ipSystem)){
            name = dealIpSystemName(ipSystem);
        } else {
            name = String.format("%s_AO_%s", ticket, IdGen.getRandomNumberString());
        }
        name = String.format("\"%s\"", name);

        createObjectNames.add(name);
        if (ipType.intValue() == IpTypeEnum.URL.getCode()){
            if (urlType.intValue() == UrlTypeEnum.IPV6.getCode()){
                sb.append(String.format("address %s %s\n", name, "ipv6"));
            }else {
                sb.append(String.format("address %s\n", name));
            }
        }else if (ipType.intValue() == IpTypeEnum.IPV6.getCode()) {
            sb.append(String.format("address %s %s\n", name, "ipv6"));
        } else {
            sb.append(String.format("address %s\n", name));
        }
        for (String address : arr) {
            // 是创建对象
            formatFullAddress(address, sb, ipPrefix, createObjFlag, dto, name, arr.length, ipType);
        }
        if (createObjFlag) {
            dto.setName(name);
            sb.append("exit\n");
        } else {
            dto.setName(sb.toString());
        }
        dto.setJoin(ipPrefix + "-addr " + name + "\n");
        dto.setCommandLine(sb.toString());
        dto.setCreateObjectName(createObjectNames);
        return dto;
    }

    public PolicyObjectDTO generateAddressObjectForNat(String ipAddress, String ticket, String ipPrefix, boolean createObjFlag, String existsAddressName, String ipSystem,Integer ipType) {
        PolicyObjectDTO dto = new PolicyObjectDTO();
        List<String> createAddressObjectNames = new ArrayList<>();
        if(AliStringUtils.isEmpty(ipAddress) || ipAddress.equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_ANY)) {
            if(ipType.intValue() == IpTypeEnum.IPV4.getCode()){
                dto.setJoin(ipPrefix + "-addr any\n");
                dto.setName("any");
                dto.setObjectFlag(true);
                return dto;
            } else {
                dto.setJoin(ipPrefix + "-addr IPv6-any\n");
                dto.setName("any");
                dto.setObjectFlag(true);
                return dto;
            }
        }


        if (StringUtils.isNotBlank(existsAddressName)) {
            dto.setObjectFlag(true);
            if (existsAddressName.indexOf("\"") < 0) {
                existsAddressName = "\"" + existsAddressName + "\"";
            }
            dto.setJoin(ipPrefix + "-addr " + existsAddressName + "\n");
            dto.setName(existsAddressName);
            return dto;
        }

        dto.setObjectFlag(createObjFlag);

        String[] arr = ipAddress.split(",");
        StringBuilder sb = new StringBuilder();

        String name = null;
        if(StringUtils.isNotBlank(ipSystem)){
            name = dealIpSystemName(ipSystem);
        } else {
            name = String.format("%s_AO_%s",ticket, IdGen.getRandomNumberString());
        }
        if (ipType.intValue() == IpTypeEnum.IPV6.getCode()) {
            sb.append(String.format("address %s %s\n", name, "ipv6"));
        } else {
            sb.append(String.format("address %s\n", name));
        }
        if(arr.length>1){

            for (String address : arr) {
                String fullStr = "";
                if(ipType.intValue() == IpTypeEnum.IPV4.getCode()){
                    if (IpUtils.isIPSegment(address)) {
                        fullStr = String.format("ip %s\n", address);
                    } else if (IpUtils.isIPRange(address)) {
                        String startIp = IpUtils.getStartIpFromRange(address);
                        String endIp = IpUtils.getEndIpFromRange(address);
                        fullStr = String.format("range %s %s\n", startIp, endIp);
                    } else {
                        fullStr = String.format("ip %s/32\n", address);
                    }
                } else {
                    // ipv6
                    if (address.contains("/")) {
                        fullStr = String.format("ip %s\n", address);
                    } else if(address.contains("-")){
                        // 范围
                        String startIp = IpUtils.getRangeStartIPv6(address);
                        String endIp = IpUtils.getRangeEndIPv6(address);
                        fullStr = String.format("range %s %s\n", startIp, endIp);
                    } else {
                        fullStr = String.format("ip %s/128\n", address);
                    }
                }
                sb.append(fullStr);
            }

            sb.append("exit\n");
            dto.setName(name);

            dto.setCommandLine(sb.toString());
        }else{
            if(arr.length == 1){
                String address = arr[0];
                String fullStr = "";
                if(ipType.intValue() == IpTypeEnum.IPV4.getCode()){
                    if (IpUtils.isIPSegment(address)) {
                        fullStr = String.format("ip %s\n", address);
                    } else if (IpUtils.isIPRange(address)) {
                        String startIp = IpUtils.getStartIpFromRange(address);
                        String endIp = IpUtils.getEndIpFromRange(address);
                        fullStr = String.format("range %s %s\n", startIp, endIp);
                    } else {
                        fullStr = String.format("ip %s/32\n", address);
                    }
                } else {
                    // ipv6
                    if (address.contains("/")) {
                        fullStr = String.format("ip %s\n", address);
                    } else if(address.contains("-")){
                        // 范围
                        String startIp = IpUtils.getRangeStartIPv6(address);
                        String endIp = IpUtils.getRangeEndIPv6(address);
                        fullStr = String.format("range %s %s\n", startIp, endIp);
                    } else {
                        fullStr = String.format("ip %s/128\n", address);
                    }
                }
                sb.append(fullStr);
                sb.append("exit\n");

                dto.setCommandLine(sb.toString());
                dto.setName(name);
            }

        }
        createAddressObjectNames.add(name);
        dto.setCreateObjectName(createAddressObjectNames);
        return dto;
    }
    private void formatFullAddress(String address, StringBuilder sb, String ipPrefix, boolean createObjFlag, PolicyObjectDTO dto, String name,int length, Integer ipType) {
        String fullStr = "";
        if(IpTypeEnum.IPV4.getCode()== ipType.intValue()){
            if (IpUtils.isIPSegment(address)) {
                fullStr = String.format("ip %s\n", address);
            } else if (IpUtils.isIPRange(address)) {
                String startIp = IpUtils.getStartIpFromRange(address);
                String endIp = IpUtils.getEndIpFromRange(address);
                fullStr = String.format("range %s %s\n", startIp, endIp);
            } else {
                fullStr = String.format("ip %s/32\n", address);
            }
        } else if(IpTypeEnum.IPV6.getCode() == ipType.intValue()){
            // ipv6
            if (address.contains("/")) {
                fullStr = String.format("ip %s\n", address);
            } else if(address.contains("-")){
                // 范围
                String startIp = IpUtils.getRangeStartIPv6(address);
                String endIp = IpUtils.getRangeEndIPv6(address);
                fullStr = String.format("range %s %s\n", startIp, endIp);
            } else {
                fullStr = String.format("ip %s/128\n", address);
            }
        }else {
            // 域名
            fullStr = String.format("host %s\n", address);
        }

        if (createObjFlag) {
            sb.append(fullStr);
        } else {
            sb.append(ipPrefix + "-" + fullStr);
        }
    }


    public PolicyObjectDTO generateServiceObject(List<ServiceDTO> serviceDTOList, Integer idleTimeout, String existsServiceName) {
        PolicyObjectDTO dto = new PolicyObjectDTO();
        List<String> createServiceObjectName = new ArrayList<>();
        List<String> createServiceGroupObjectName = new ArrayList<>();
        if (StringUtils.isNotBlank(existsServiceName)) {
            dto.setObjectFlag(true);
            dto.setJoin("service " + existsServiceName +"\n");
            return dto;
        }

        StringBuilder sb = new StringBuilder();

        boolean groupFlag = false;
        //对象名称集合, 不一定会建组，建组条件：有2组及以上协议，其中有一个协议，不带端口
        List<String> serviceNameList = new ArrayList<>();

        //直接写内容，当端口是any时，可以直接写内容，但有具体端口时，就必须创建对象
        if (serviceDTOList != null && serviceDTOList.size() == 1) {
            //无端口时，有返回值，  有端口就需要建对象，是没有返回值的
            String command = getServiceNameByNoPort(serviceDTOList.get(0), 0);
            if (StringUtils.isNotBlank(command)) {
                dto.setObjectFlag(false);
                dto.setCommandLine(String.format("service %s\n", command));
                return dto;
            }
        }

        //多个服务，必须建对象或组
        boolean createObjFlag = true;
        dto.setObjectFlag(createObjFlag);


        StringBuilder objNameSb = new StringBuilder();

        //多个，建对象
        for(ServiceDTO service : serviceDTOList){
            int protocolNum = Integer.valueOf(service.getProtocol());
            String protocolString = ProtocolUtils.getProtocolByValue(protocolNum).toLowerCase();

            if(protocolString.equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_ANY)) {
                dto.setCommandLine("service Any \n");
                return dto;
            }


            String command = getServiceNameByNoPort(service, 0);
            if(StringUtils.isNotBlank(command)) {
                groupFlag = true;
                serviceNameList.add(command);
                continue;
            }
            String serviceName = getServiceName(service);

            objNameSb.append(serviceName+"_");

            //定义对象有多种情况
            if (protocolString.equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_TCP)
                    || protocolString.equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_UDP)){

                String[] srcPorts = service.getSrcPorts().split(",");
                String[] dstPorts = service.getDstPorts().split(",");
                //源、目的都有具体的值，或源不为空，目的为空
                /*if(srcPorts != null && srcPorts.length >0) {
                    for(String srcPort: srcPorts) {
                        for(String dstPort: dstPorts) {
                            sb.append(String.format("%s dst-port %s src-port %s\n", protocolString, PortUtils.getPortString(dstPort, PortUtils.BLANK_FORMAT), PortUtils.getPortString(srcPort, PortUtils.BLANK_FORMAT)));
                        }
                    }
                }*/
                //当协议为tcp/udp协议，源端口为any，目的端口为具体值,源端口不显示
                for (String dstPort : dstPorts) {
                    sb.append(String.format("%s dst-port %s\n", protocolString, PortUtils.getPortString(dstPort, PortUtils.BLANK_FORMAT)));
                }
            }else if (protocolString.equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_ICMP)) {
                //icmpType为空的话，默认为icmp type 3，
                if (StringUtils.isBlank(service.getType()) || !allowType.contains(service.getType())) {
                    sb.append("icmp type 3\n");
                } else if (StringUtils.isNotBlank(service.getType()) && allowType.contains(service.getType())) {
                    //icmpType不为空的话，若icmpType为3,4,5,8,11,12,13,15，则正常生成icmp type 和 code信息， 否则设定为icmp type 3
                    //有code增加code，没有code则为空字符串
                    sb.append(String.format("icmp type %s %s\n", service.getType(), service.getCode() == null ? "" : String.format("code %d", Integer.valueOf(service.getCode()))));
                }
            }
        }

        //有对象
        if(createObjFlag && sb.toString().length() > 0) {
            String objName = objNameSb.toString();
            if (objName.substring(objName.length() - 1).equals("_")) {
                objName = objName.substring(0, objName.length() - 1);
            }

            //service name限制长度
            if(objName.length() > getMaxNameLength()) {
                String shortName = objName.substring(0, getMaxNameLength()-5);
                objName = String.format("%s_etcs", shortName.substring(0, shortName.lastIndexOf("_")));
            }
            dto.setName(objName);
            serviceNameList.add(objName);
            String tmp = sb.toString();
            StringBuilder tmpSb = new StringBuilder();
            tmpSb.append(String.format("service %s\n", objName));
            tmpSb.append(tmp);
            tmpSb.append("exit\n");
            sb.setLength(0);
            sb.append(tmpSb);
        }


        //要建组
        if(groupFlag){
            String groupName = getServiceName(serviceDTOList);
            sb.append(String.format("servgroup %s\n", groupName));
            for(String objName : serviceNameList){
                sb.append(String.format("service %s\n", objName));
            }
            sb.append("exit\n");
            dto.setJoin("service " + groupName +"\n");
            dto.setName(groupName);
            createServiceGroupObjectName.add(groupName);
        }else{
            createServiceObjectName.add(dto.getName());
            dto.setJoin("service " + dto.getName() +"\n");
        }
        dto.setCreateServiceObjectName(createServiceObjectName);
        dto.setCreateServiceGroupObjectNames(createServiceGroupObjectName);
        dto.setCommandLine(sb.toString());
        return dto;
    }

    @Override
    public String getServiceName(ServiceDTO dto) {
        StringBuilder sb = new StringBuilder();
        String protocolString = ProtocolUtils.getProtocolByValue(Integer.valueOf(dto.getProtocol())).toLowerCase();
        sb.append(protocolString.toLowerCase());
        if (StringUtils.isBlank(dto.getDstPorts())) {
            return sb.toString();
        } else if (protocolString.equalsIgnoreCase("icmp")) {
            return sb.toString();
        } else if (!dto.getDstPorts().equalsIgnoreCase("any") && !dto.getDstPorts().equals("0-65535")) {
            String[] dstPorts = dto.getDstPorts().split(",");
            String[] var5 = dstPorts;
            int var6 = dstPorts.length;

            for(int var7 = 0; var7 < var6; ++var7) {
                String dstPort = var5[var7];
                if (PortUtils.isPortRange(dstPort)) {
                    String startPort = PortUtils.getStartPort(dstPort);
                    String endPort = PortUtils.getEndPort(dstPort);
                    sb.append(String.format("_%s_%s", startPort, endPort));
                } else {
                    sb.append(String.format("_%s", dstPort));
                }
            }

            return sb.toString().toLowerCase();
        } else {
            return sb.toString();
        }
    }

    //协议没有端口时，衔接
    private String getServiceNameByNoPort(ServiceDTO service, Integer ipType) {
        String command = "";
        int protocolNum = Integer.valueOf(service.getProtocol());
        String protocolString = ProtocolUtils.getProtocolByValue(protocolNum).toLowerCase();
        if (protocolString.equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_ANY)) {
            command = " any ";
        } else if (protocolString.equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_TCP) ||
                protocolString.equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_UDP)) {
            if (StringUtils.isBlank(service.getDstPorts()) || service.getDstPorts().equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_ANY)) {
                command =  protocolString + "-any ";
            }
        } else if (protocolString.equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_ICMP)) {
            if (ipType.intValue() == IpTypeEnum.IPV6.getCode()) {
                // ipv6的 icmp
                command = protocolString + "v6 ";
            } else {
                command = protocolString + " ";
            }
        }else if (protocolString.equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_ICMPV6)) {
            command = protocolString + " ";
        }
        return command;
    }

    private PolicyObjectDTO generateTimeObject(String startTime, String endTime, String ticket) {
        if(startTime == null) {
            return null;
        }
        PolicyObjectDTO dto = new PolicyObjectDTO();
        StringBuilder sb = new StringBuilder();
        String name = String.format("\"to%s\"",
                TimeUtils.transformDateFormat(endTime, TimeUtils.EUROPEAN_TIME_FORMAT, TimeUtils.COMMON_TIME_DAY_FORMAT));
        sb.append(String.format("schedule %s\n", name));
        sb.append(String.format("absolute start %s end %s\n", formatTimeString(startTime), formatTimeString(endTime)));
        sb.append("exit\n");

        dto.setName(name);
        dto.setCommandLine(sb.toString());
        return dto;
    }

    private String formatTimeString(String timeString) {
        return TimeUtils.transformDateFormat(timeString, TimeUtils.EUROPEAN_TIME_FORMAT, TimeUtils.HILLSTONE_TIME_FORMAT);
    }

    @Override
    public int getMaxNameLength() {
        return MAX_NAME_LENGTH;
    }

    @Override
    public String getServiceName(List<ServiceDTO> serviceDTOList){
        StringBuilder nameSb = new StringBuilder();
        int number = 0;
        for (ServiceDTO dto : serviceDTOList) {
            if (number != 0) {
                nameSb.append("_");
            }
            nameSb.append(getServiceName(dto));
            number++;
        }

        String name = nameSb.toString();
        if(name.length() > getMaxNameLength()) {
            String shortName = name.substring(0, getMaxNameLength()-6);
            name = String.format("%s_etcsg", shortName.substring(0, shortName.lastIndexOf("_")));
        }
        return name;
    }

    public PolicyObjectDTO generateServiceObject(List<ServiceDTO> serviceDTOList, List<String> existServiceNameList, Integer idleTimeout, Integer ipType) {
        logger.info("idleTimeout is " + idleTimeout);
        PolicyObjectDTO dto = new PolicyObjectDTO();

        StringBuilder sb = new StringBuilder();

        boolean groupFlag = false;
        //对象名称集合, 不一定会建组，建组条件：有2组及以上协议，其中有一个协议，不带端口
        List<String> serviceNameList = new ArrayList<>();

        //直接写内容，当端口是any时，可以直接写内容，但有具体端口时，就必须创建对象
        if (serviceDTOList != null && serviceDTOList.size() == 1 && existServiceNameList.size() == 0) {
            //无端口时，有返回值，  有端口就需要建对象，是没有返回值的
            String command = getServiceNameByNoPort(serviceDTOList.get(0), ipType);
            if (StringUtils.isNotBlank(command)) {
                dto.setObjectFlag(true);
                dto.setJoin(String.format("service %s\n", command));
                return dto;
            }
        }

        //多个服务，必须建对象或组
        boolean createObjFlag = true;
        groupFlag = true;
        dto.setObjectFlag(createObjFlag);

        if (existServiceNameList.size() > 0) {
            for (String existServiceName : existServiceNameList) {
                if (existServiceName.indexOf("\"") < 0) {
                    existServiceName = "\"" + existServiceName + "\"";
                }
                serviceNameList.add(existServiceName);
            }
        }

        List<String> createServiceObjectNames = new ArrayList<>();

        //多个，建对象
        for(ServiceDTO service : serviceDTOList){
            int protocolNum = Integer.valueOf(service.getProtocol());
            String protocolString = ProtocolUtils.getProtocolByValue(protocolNum).toLowerCase();

            if(protocolString.equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_ANY)) {
                dto.setCommandLine("service Any \n");
                return dto;
            }


            if(StringUtils.isNotEmpty(service.getDstPorts())||service.getDstPorts().equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_ANY)) {
                String command = getServiceNameByNoPort(service, ipType);
                if (StringUtils.isNotBlank(command)) {
                    groupFlag = true;
                    serviceNameList.add(command);
                    continue;
                }
            }




            if (protocolString.equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_TCP)
                    || protocolString.equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_UDP)){


                String[] dstPorts = service.getDstPorts().split(",");
                //源、目的都有具体的值，或源不为空，目的为空
                /*if(srcPorts != null && srcPorts.length >0) {
                    for(String srcPort: srcPorts) {
                        for(String dstPort: dstPorts) {
                            sb.append(String.format("%s dst-port %s src-port %s\n", protocolString, PortUtils.getPortString(dstPort, PortUtils.BLANK_FORMAT), PortUtils.getPortString(srcPort, PortUtils.BLANK_FORMAT)));
                        }
                    }
                }*/
                //当协议为tcp/udp协议，源端口为any，目的端口为具体值,源端口不显示
                String names=null;
                for (String dstPort : dstPorts) {
                    if(idleTimeout != null) {
                        names = String.format("\"L-%s-%s\"", protocolString.substring(0, 3).toUpperCase(), dstPort);
                    }
                    if(PortUtils.isPortRange(dstPort)) {
                        String start = PortUtils.getStartPort(dstPort);
                        String end = PortUtils.getEndPort(dstPort);
                        names =  String.format("\"%s-%s-%s\"", protocolString.substring(0, 3).toUpperCase(), start, end);
                        if(idleTimeout != null) {
                            names =  String.format("\"L-%s-%s-%s\"", protocolString.substring(0, 3).toUpperCase(), start, end);
                        }
                    }
                    else if(StringUtils.isEmpty(dstPort)){
                        names = String.format("\"%s-%s\"", protocolString.substring(0, 3).toLowerCase(), dstPort);
                        if(idleTimeout != null) {
                            names =  String.format("\"L-%s-%s\"", protocolString.substring(0, 3).toLowerCase(), dstPort);
                        }
                    }
                    else {
                        if(idleTimeout != null) {
                            names = String.format("\"L-%s-%s\"", protocolString.substring(0, 3).toUpperCase(), dstPort);
                        }
                        else{
                            names=String.format("\"%s-%s\"", protocolString.substring(0, 3).toUpperCase(), dstPort);
                        }

                    }
                    serviceNameList.add(names);
                    sb.append("service " + names);
                    sb.append("\n");
                    dto.setName(names);
                    createServiceObjectNames.add(names);
                        sb.append(String.format("%s dst-port %s", protocolString, PortUtils.getPortString(dstPort, PortUtils.BLANK_FORMAT)));
                        if (idleTimeout != null) {
                            int day = idleTimeout / DAY_SECOND;
                            if ((idleTimeout % DAY_SECOND) > 0) {
                                day = day + 1;
                            }
                            sb.append(String.format(" timeout-day %d", 10));
                        }
                        sb.append("\n");
                        sb.append("exit\n");
                    }
            }else if (protocolString.equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_ICMP)) {
                //icmpType为空的话，默认为icmp type 3，
                if (StringUtils.isBlank(service.getType()) || !allowType.contains(service.getType())) {
                    sb.append("icmp type 3\n");
                } else if (StringUtils.isNotBlank(service.getType()) && allowType.contains(service.getType())) {
                    //icmpType不为空的话，若icmpType为3,4,5,8,11,12,13,15，则正常生成icmp type 和 code信息， 否则设定为icmp type 3
                    //有code增加code，没有code则为空字符串
                    sb.append(String.format("icmp type %s %s\n", service.getType(), service.getCode() == null ? "" : String.format("code %d", Integer.valueOf(service.getCode()))));
                }
                sb.append("exit\n");
            }
        }
        System.out.println(JSONObject.toJSONString(serviceNameList));
        //要建组, 用多个服务替代
        if(groupFlag){
            StringBuilder joinSb = new StringBuilder();
            for(String objName :serviceNameList) {
                joinSb.append(String.format("service %s\n", objName));
            }
            dto.setJoin(joinSb.toString());
        }else{
            dto.setJoin("service " + dto.getName() +"\n");
        }

        dto.setCreateServiceObjectName(createServiceObjectNames);
        dto.setCommandLine(sb.toString());
        return dto;
    }

    /**
     * 处理系统名称 作为地址对象名称去创建对象
     *
     * @param ipSystem
     */
    private String dealIpSystemName(String ipSystem) {
        String setName = ipSystem;
        // 对象名称长度限制，一个中文2个字符
        setName = strSub(setName, getMaxObejctNameLength(), "GB2312");
        // 对象名称长度限制
        int len = 0;
        try {
            len = setName.getBytes("GB2312").length;
        } catch (Exception e) {
            logger.error("字符串长度计算异常");
        }
        if (len > getMaxObejctNameLength() - 7) {
            setName = strSub(setName, getMaxObejctNameLength() - 7, "GB2312");
        }
        return String.format("%s_%s", setName, DateUtils.getDate().replace("-", "").substring(2));
    }

    /**
     * 判断传进来的字符串，是否
     * 大于指定的字节，如果大于递归调用
     * 直到小于指定字节数 ，一定要指定字符编码，因为各个系统字符编码都不一样，字节数也不一样
     * @param s
     *            原始字符串
     * @param num
     *            传进来指定字节数
     * @return String 截取后的字符串

     * @throws
     */
    protected static String strSub(String s, int num, String charsetName){
        int len = 0;
        try{
            len = s.getBytes(charsetName).length;
        }catch (Exception e) {
            logger.error("字符串长度计算异常");
        }

        if (len > num) {
            s = s.substring(0, s.length() - 1);
            s = strSub(s, num, charsetName);
        }
        return s;
    }

    public int getMaxObejctNameLength() {
        return MAX_NAME_LENGTH;
    }


    public static void main(String[] args) {
        CommandlineDTO dto = CommandlineDTO.getInstanceDemo();
        List<String> restSrc = new ArrayList<>();
        restSrc.add("2.5.3.2,2.5.3.3,2.5.3.5-2.5.3.8");

        dto.setRestSrcAddressList(restSrc);
        List<String> restDst = new ArrayList<>();
        restDst.add("2.2.3.1");

        dto.setSrcIp("12.3.1.3,2.4.5.2");
        dto.setDstIp("4.2.3.2,3.4.2.4");
        dto.setIpType(1);
        dto.setRestDstAddressList(restDst);
        List<ServiceDTO> serviceDTOS = new ArrayList<>();
        ServiceDTO serviceDTO = new ServiceDTO();
        ServiceDTO serviceDTO2 = new ServiceDTO();
        serviceDTO.setProtocol("6");
        serviceDTO.setDstPorts("any");
        serviceDTO2.setProtocol("1");
        serviceDTO2.setDstPorts("33");
        serviceDTOS.add(serviceDTO);
        dto.setServiceName("hahha");
        dto.setServiceList(serviceDTOS);
        dto.setRestServiceList(serviceDTOS);
        dto.setIdleTimeout(99999);
        dto.setVsys(true);
        dto.setVsysName("zy");
        SecurityHillStoneV5 hillStoneR5 = new SecurityHillStoneV5();
        String commandLine = hillStoneR5.composite(dto);
        System.out.println("commandline:\n" + commandLine);
    }
}
