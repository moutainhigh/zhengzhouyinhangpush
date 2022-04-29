package com.abtnetworks.totems.common.commandline.security;

import com.abtnetworks.totems.common.annotation.CustomCli;
import com.abtnetworks.totems.common.commandline.PolicyGenerator;
import com.abtnetworks.totems.common.commandline.SecurityPolicyGenerator;
import com.abtnetworks.totems.common.constants.PolicyConstants;
import com.abtnetworks.totems.common.dto.*;
import com.abtnetworks.totems.common.dto.commandline.CommandlineDTO;
import com.abtnetworks.totems.common.dto.commandline.PolicyMergeDTO;
import com.abtnetworks.totems.common.dto.commandline.PolicyObjectDTO;
import com.abtnetworks.totems.common.dto.commandline.ServiceDTO;
import com.abtnetworks.totems.common.enums.*;
import com.abtnetworks.totems.common.utils.*;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * @Author: kevin
 * @Date: 2021/9/7 22:52
 */
@Service
@CustomCli(value = DeviceModelNumberEnum.HILLSTONE, type = PolicyEnum.SECURITY)
public class SecurityHillStoneR5ForHunanDL extends SecurityPolicyGenerator implements PolicyGenerator {

    private static Logger logger = Logger.getLogger(SecurityHillStoneR5ForHunanDL.class);

    private static Set<Integer> allowType = new HashSet<>();

    private final int MAX_NAME_LENGTH = 95;

    private static String serviceKey = "HILLSTONE_Service";

    private static String ipKey = "HILLSTONE_IP";

    private final int DAY_SECOND = 24 * 60 * 60;

    public SecurityHillStoneR5ForHunanDL() {
        init();
    }

    private static void init() {
        allowType.add(3);
        allowType.add(4);
        allowType.add(5);
        allowType.add(8);
        allowType.add(11);
        allowType.add(12);
        allowType.add(13);
        allowType.add(15);
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
        if (policyDTO.getAction().equals(ActionEnum.PERMIT)) {
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
        if (ObjectUtils.isEmpty(dto.getIpType())) {
            dto.setIpType(IpTypeEnum.IPV4.getCode());
        }

        GeneratedObjectDTO generatedDto = cmdDTO.getGeneratedObject();
        generatedDto.setPolicyName(dto.getName());
        generatedDto.setVsys(dto.isVsys());
        generatedDto.setVsysName(dto.getVsysName());
        generatedDto.setHasVsys(dto.isHasVsys());
        String commandLine = composite(dto);
        generatedDto.setAddressObjectNameList(dto.getAddressObjectNameList());
        generatedDto.setServiceObjectNameList(dto.getServiceObjectNameList());
        generatedDto.setTimeObjectNameList(dto.getTimeObjectNameList());
        return commandLine;
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

        if (dto.getMergeDTO() == null || dto.isMustCreateFlag()) {
            return createCommandLine(dto);
        } else {
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

        List<String> createObjectName = new ArrayList<>();
        List<String> createServiceObjectName = new ArrayList<>();

        boolean createObjFlag = dto.isCreateObjFlag();
        String ticket = dto.getName();
        PolicyObjectDTO srcAddressObject = generateAddressObject(dto.getSrcIp(), ticket, "src", createObjFlag, dto.getSrcAddressName(), dto.getSrcIpSystem(), dto.getIpType(),createObjectName);
        PolicyObjectDTO dstAddressObject = generateAddressObject(dto.getDstIp(), ticket, "dst", createObjFlag, dto.getDstAddressName(), dto.getDstIpSystem(), dto.getIpType(),createObjectName);
        PolicyObjectDTO serviceObject = generateServiceObject(dto.getRestServiceList(), dto.getExistServiceNameList(), dto.getIdleTimeout(), 0,createServiceObjectName);
        dto.setAddressObjectNameList(createObjectName);
        dto.setServiceObjectNameList(createServiceObjectName);

        if (mergeField.equals(PolicyConstants.SRC) && srcAddressObject.isObjectFlag() && StringUtils.isNotBlank(srcAddressObject.getCommandLine())) {
            sb.append(srcAddressObject.getCommandLine());
        }
        if (mergeField.equals(PolicyConstants.DST) && dstAddressObject.isObjectFlag() && StringUtils.isNotBlank(dstAddressObject.getCommandLine())) {
            sb.append(dstAddressObject.getCommandLine());
        }
        if (mergeField.equals(PolicyConstants.SERVICE) && serviceObject.isObjectFlag() && StringUtils.isNotBlank(serviceObject.getCommandLine())) {
            sb.append(serviceObject.getCommandLine());
        }

        sb.append(String.format("rule id %s\n", ruleId));
        sb.append("log session-start\n");
        sb.append("log session-end\n");
        if (mergeField.equals(PolicyConstants.SRC)) {
            if (srcAddressObject.isObjectFlag()) {
                sb.append(srcAddressObject.getJoin());
            } else {
                sb.append(srcAddressObject.getCommandLine());
            }
        } else if (mergeField.equals(PolicyConstants.DST)) {
            if (dstAddressObject.isObjectFlag()) {
                sb.append(dstAddressObject.getJoin());
            } else {
                sb.append(dstAddressObject.getCommandLine());
            }
        } else if (mergeField.equals(PolicyConstants.SERVICE)) {
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
        logger.info("进入插件生成山石命令行");
        StringBuilder sb = new StringBuilder();
        String ticket = dto.getName();
        String srcIp = dto.getSrcIp();
        String dstIp = dto.getDstIp();
        String startTime = dto.getStartTime();
        String endTime = dto.getEndTime();
        boolean createObjFlag = dto.isCreateObjFlag();
        String existSrcName = dto.getSrcAddressName();


        List<String> createObjectName = new ArrayList<>();
        PolicyObjectDTO srcAddress;
        if (StringUtils.isNotEmpty(existSrcName)) {
            srcAddress = generateAddressObject(srcIp, ticket, "src", createObjFlag, existSrcName, dto.getSrcIpSystem(), dto.getIpType(),null);
        } else {
            srcAddress = generateAddressObject(dto.getExistSrcAddressList(), dto.getRestSrcAddressList(), ticket, "src", createObjFlag, dto.getSrcIpSystem(), dto.getIpType(),createObjectName);
        }
        String existDstName = dto.getDstAddressName();

        Boolean isDomain = isDomainForIp(dstIp);
        Integer dstIpType = dto.getIpType();
        if (isDomain) {
            dstIpType = 2;
        }

        PolicyObjectDTO dstAddress;
        if (StringUtils.isNotEmpty(existDstName) && ObjectUtils.isNotEmpty(dto.getRestDstAddressList())) {
            dstAddress = generateAddressObject(dstIp, ticket, "dst", createObjFlag, existDstName, dto.getDstIpSystem(), dstIpType,null);
        } else {
            dstAddress = generateAddressObject(dto.getExistDstAddressList(), dto.getRestDstAddressList(), ticket, "dst", createObjFlag, dto.getDstIpSystem(), dstIpType,createObjectName);
        }
        String existServiceName = dto.getServiceName();
        PolicyObjectDTO serviceObject;
        PolicyObjectDTO serviceTimeoutObject = new PolicyObjectDTO();

        List<String> createServiceObjectName = new ArrayList<>();
        if (StringUtils.isNotEmpty(existServiceName)) {

            serviceObject = generateServiceObject(dto.getServiceList(), dto.getIdleTimeout(), existServiceName);
            if (null != dto.getIdleTimeout()) {
                serviceTimeoutObject = generateServiceTimeoutObject(dto.getServiceList(), dto.getIdleTimeout(), dto.getIpType());
            }
        } else {
            serviceObject = generateServiceObject(dto.getRestServiceList(), dto.getExistServiceNameList(), dto.getIdleTimeout(), dto.getIpType(),createServiceObjectName);

            if (null != dto.getRestServiceList() && dto.getRestServiceList().size() == 1 && dto.getExistServiceNameList().size() == 0 && null != dto.getIdleTimeout()) {
                    String command = getServiceNameByNoPort(dto.getRestServiceList().get(0), dto.getIpType());
                    if (StringUtils.isBlank(command)) {
                        //当服务端口不为any时，前面已经创建了长连接对象，所以不需要额外考虑
                    }else {
                        //当服务端口为any时，建服务不会考虑长连接对象，此时需要额外逻辑处理长连接服务对象
                        serviceTimeoutObject = generateServiceTimeoutObject(dto.getRestServiceList(), dto.getIdleTimeout(), dto.getIpType());
                    }
                 }
        }
        List<String> createTimeObjectNames = new ArrayList<>();

        PolicyObjectDTO time = generateTimeObject(startTime, endTime, ticket);
        if (null != time) {
            createTimeObjectNames.add(time.getName());
        }
        dto.setAddressObjectNameList(createObjectName);
        dto.setServiceObjectNameList(createServiceObjectName);
        dto.setTimeObjectNameList(createTimeObjectNames);
        sb.append("configure\n");

        //定义对象
        if (srcAddress.isObjectFlag() && StringUtils.isNotBlank(srcAddress.getCommandLine())) {
            sb.append(String.format("%s\n", srcAddress.getCommandLine()));
        }
        if (dstAddress.isObjectFlag() && StringUtils.isNotBlank(dstAddress.getCommandLine())) {
            sb.append(String.format("%s\n", dstAddress.getCommandLine()));
        }

        //山石比较特殊，即使指定了不创建对象，但有具体的源、目的端口时，也会创建对象的
        if (serviceObject != null && serviceObject.isObjectFlag() && StringUtils.isNotBlank(serviceObject.getCommandLine())) {
            sb.append(String.format("%s\n", serviceObject.getCommandLine()));
        }

        if (null != serviceTimeoutObject && serviceTimeoutObject.isObjectFlag() && StringUtils.isNotBlank(serviceTimeoutObject.getCommandLine())) {
            sb.append(String.format("%s\n", serviceTimeoutObject.getCommandLine()));
        }
        if (time != null) {
            sb.append(String.format("%s\n", time.getCommandLine()));
        }

        sb.append("rule ");
        String swapRuleNameId = StringUtils.isNotBlank(dto.getSwapRuleNameId()) ? dto.getSwapRuleNameId() : "";
        int moveSeatCode = dto.getMoveSeatEnum().getCode();
        if (moveSeatCode == MoveSeatEnum.FIRST.getCode()) {
            sb.append("top \n");
        } else if (moveSeatCode == MoveSeatEnum.BEFORE.getCode() || moveSeatCode == MoveSeatEnum.AFTER.getCode()) {
            if (StringUtils.isNotEmpty(swapRuleNameId)) {
                sb.append(String.format("%s %s\n", dto.getMoveSeatEnum().getKey(), swapRuleNameId));
            } else {
                sb.append("\n");
            }

        } else {
            sb.append("\n");
        }

        sb.append(String.format("name %s\n", dto.getBusinessName()));
        sb.append("log session-start\n");
        sb.append("log session-end\n");
        if (!AliStringUtils.isEmpty(dto.getDescription())) {
            sb.append(String.format("description %s\n", dto.getDescription()));
        }

        /*if (StringUtils.isNotBlank(dto.getSrcZone())) {
            sb.append(String.format("src-zone %s\n", dto.getSrcZone()));
        }
        if (StringUtils.isNotBlank(dto.getDstZone())) {
            sb.append(String.format("dst-zone %s\n", dto.getDstZone()));
        }*/

        // if (srcAddress.isObjectFlag() && StringUtils.isNotBlank(srcAddress.getJoin())) {
        if (StringUtils.isNotBlank(srcAddress.getJoin())) {
            sb.append(srcAddress.getJoin());
        } else {
            sb.append(srcAddress.getName());
        }

        // if (dstAddress.isObjectFlag() && StringUtils.isNotBlank(dstAddress.getJoin())) {
        if (StringUtils.isNotBlank(dstAddress.getJoin())) {
            sb.append(dstAddress.getJoin());
        } else {
            sb.append(dstAddress.getName());
        }

        if (serviceObject != null && serviceObject.isObjectFlag() && StringUtils.isNotBlank(serviceObject.getJoin())) {
            sb.append(serviceObject.getJoin());
        } else if (serviceObject != null && StringUtils.isNotBlank(serviceObject.getCommandLine())) {
            sb.append(serviceObject.getName());
        }

        if (serviceTimeoutObject != null && serviceTimeoutObject.isObjectFlag() && StringUtils.isNotBlank(serviceTimeoutObject.getJoin())) {
            sb.append(serviceTimeoutObject.getJoin());
        }

        if (time != null) {
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

    public PolicyObjectDTO generateAddressObject(List<String> existAddressList, List<String> restAddressList, String ticket,
                                                 String ipPrefix, boolean createObjFlag, String ipSystem, Integer ipType,List<String> createObjectName) {
        PolicyObjectDTO policyObjectDTO = new PolicyObjectDTO();
        policyObjectDTO.setName("any");
        if (ipType.intValue() == IpTypeEnum.IPV4.getCode()) {
            policyObjectDTO.setJoin(ipPrefix + "-addr any\n");
        } else {
            policyObjectDTO.setJoin(ipPrefix + "-addr IPv6-any\n");
        }
        policyObjectDTO.setObjectFlag(true);

        if (restAddressList.size() > 0) {
            StringBuilder sb = new StringBuilder();
            for (String addr : restAddressList) {
                sb.append(",");
                sb.append(addr);
            }
            if (sb.length() > 0) {
                sb.deleteCharAt(0);
            }
            policyObjectDTO = generateAddressObject(sb.toString(), ticket, ipPrefix, createObjFlag, "", ipSystem, ipType,createObjectName);
        }

        if (existAddressList.size() > 0) {
            for (String existName : existAddressList) {
                policyObjectDTO.setJoin((policyObjectDTO.getJoin().contains("any") ? "" : policyObjectDTO.getJoin()) + ipPrefix + "-addr " + existName + "\n");
            }
        }

        logger.info("policyObjectDTO is " + JSONObject.toJSONString(policyObjectDTO));
        return policyObjectDTO;
    }

    public PolicyObjectDTO generateAddressObject(String ipAddress, String ticket, String ipPrefix, boolean createObjFlag, String existsAddressName, String ipSystem, Integer ipType,List<String> createObjectName) {
        PolicyObjectDTO dto = new PolicyObjectDTO();

        if (AliStringUtils.isEmpty(ipAddress) || ipAddress.equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_ANY)) {
            if (ipType.intValue() == IpTypeEnum.IPV4.getCode()) {
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
        int index = 1;
        for (String address : arr) {
            // 是创建对象
            if (createObjFlag) {
                formatFullAddress(address, sb, ipPrefix, createObjFlag, dto, ticket, ipSystem, arr.length, ipType, index);
                sb.append("exit\n");
                dto.setCommandLine(sb.toString());
                createObjectName.add(dto.getName());
            } else {
                //直接显示内容
                formatFullAddress(address, sb, ipPrefix, createObjFlag, dto, ticket, ipSystem, arr.length, ipType, index);
                dto.setCommandLine(sb.toString());
                dto.setName(sb.toString());
            }
            index++;
        }
        return dto;
    }

    public PolicyObjectDTO generateAddressObjectForNat(String ipAddress, String ticket, String ipPrefix, boolean createObjFlag, String existsAddressName, String ipSystem) {
        PolicyObjectDTO dto = new PolicyObjectDTO();

        if (AliStringUtils.isEmpty(ipAddress) || ipAddress.equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_ANY)) {
            dto.setJoin(ipPrefix + "-addr any\n");
            dto.setName("any");
            dto.setObjectFlag(true);
            return dto;
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

        String name;
        if (StringUtils.isNotEmpty(ipSystem)) {
            name = ipSystem;
        } else {
            name = String.format("%s_AO_%s", ticket, IdGen.getRandomNumberString());
        }

        sb.append(String.format("address %s\n", name));
        if (arr.length > 1) {

            for (String address : arr) {
                String fullStr = "";
                if (IpUtils.isIPSegment(address)) {
                    fullStr = String.format("ip %s\n", address);
                } else if (IpUtils.isIPRange(address)) {
                    String startIp = IpUtils.getStartIpFromRange(address);
                    String endIp = IpUtils.getEndIpFromRange(address);
                    fullStr = String.format("range %s %s\n", startIp, endIp);
                } else {
                    fullStr = String.format("ip %s/32\n", address);
                }
                sb.append(fullStr);
            }

            sb.append("exit\n");
            dto.setName(name);
            dto.setCommandLine(sb.toString());
        } else {
            if (arr.length == 1) {
                //直接显示内容
                formatFullAddress(arr[0], sb, ipPrefix, false, dto, ticket, ipSystem, arr.length, 0, 0);
                dto.setCommandLine(sb.toString());
                dto.setName(sb.toString());
            }

        }
        return dto;
    }

    private void formatFullAddress(String address, StringBuilder sb, String ipPrefix, boolean createObjFlag, PolicyObjectDTO dto, String ticket, String ipSystem, int length,
                                   Integer ipType, int index) {
        String name;
        StringBuilder serialNum = new StringBuilder();
        name = getIPNameKevin(ipType,address,true,ipSystem,ipSystem,length,index);

        if (StringUtils.isNotEmpty(ipSystem)) {
            // 对象名称长度限制，一个中文2个字符
            name = strSub(name, getMaxNameLength(), "GB2312");
            // 对象名称长度限制
            int len = 0;
            try {
                len = name.getBytes("GB2312").length;
            } catch (Exception e) {
                logger.error("字符串长度计算异常");
            }
            // 序列号长度
            int serialLengh = serialNum.length();
            if (len > getMaxNameLength() - (7 + serialLengh)) {
                name = strSub(name, getMaxNameLength() - (7 + serialLengh), "GB2312");
            }
            name = String.format("%s_%s", name, DateUtils.getDate().replace("-", "").substring(2));
            if (USE_RAW_NAME) {
                name = ipSystem;
            }
            name = name + serialNum.toString();
        }

        String fullStr = "";
        if (ipType.intValue() == IpTypeEnum.IPV4.getCode()) {
            if (IpUtils.isIPSegment(address)) {
                fullStr = String.format("ip %s\n", address);
            } else if (IpUtils.isIPRange(address)) {
                String startIp = IpUtils.getStartIpFromRange(address);
                String endIp = IpUtils.getEndIpFromRange(address);
                fullStr = String.format("range %s %s\n", startIp, endIp);
            } else {
                fullStr = String.format("ip %s/32\n", address);
            }
            name = String.format("\"%s\"", name);
            dto.setName(name);
            sb.append(String.format("address %s\n", name));
            if (dto.getJoin() != null) {
                dto.setJoin(dto.getJoin() + ipPrefix + "-addr " + name + "\n");
            } else {
                dto.setJoin(ipPrefix + "-addr " + dto.getName() + "\n");
            }
        } else if (ipType.intValue() == IpTypeEnum.IPV6.getCode()) {
            // ipv6
            if (address.contains("/")) {
                fullStr = String.format("ip %s\n", address);
            } else if (address.contains("-")) {
                // 范围
                String startIp = IpUtils.getRangeStartIPv6(address);
                String endIp = IpUtils.getRangeEndIPv6(address);
                fullStr = String.format("range %s %s\n", startIp, endIp);
            } else {
                fullStr = String.format("ip %s/128\n", address);
            }
            name = String.format("\"%s\"", name);
            dto.setName(name);
            sb.append(String.format("address %s  ipv6\n", name));
            if (dto.getJoin() != null) {
                dto.setJoin(dto.getJoin() + ipPrefix + "-addr " + name + "\n");
            } else {
                dto.setJoin(ipPrefix + "-addr " + dto.getName() + "\n");
            }
        } else {
            // 目的地址是URL类型
            // ipv4
            if (IpUtils.isIPSegment(address)) {
                fullStr = String.format("ip %s\n", address);
            } else if (IpUtils.isIPRange(address)) {
                String startIp = IpUtils.getStartIpFromRange(address);
                String endIp = IpUtils.getEndIpFromRange(address);
                fullStr = String.format("range %s %s\n", startIp, endIp);
            } else if (IpUtils.isIPv6(address)) {
                // ipv6
                if (address.contains("/")) {
                    fullStr = String.format("ip %s\n", address);
                } else if (address.contains("-")) {
                    // 范围
                    String startIp = IpUtils.getRangeStartIPv6(address);
                    String endIp = IpUtils.getRangeEndIPv6(address);
                    fullStr = String.format("range %s %s\n", startIp, endIp);
                } else {
                    fullStr = String.format("ip %s/128\n", address);
                }
            } else if (IpUtils.isIP(address)) {
                fullStr = String.format("ip %s/32\n", address);
            } else {
                // 域名
                fullStr = String.format("host %s\n", address);
            }
            name = String.format("\"%s\"", address);
            dto.setName(name);
            sb.append(String.format("address %s\n", name));
            if (dto.getJoin() != null) {
                dto.setJoin(dto.getJoin() + ipPrefix + "-addr " + name + "\n");
            } else {
                dto.setJoin(ipPrefix + "-addr " + dto.getName() + "\n");
            }
        }

        if (createObjFlag) {
            sb.append(fullStr);
        } else {
            sb.append(ipPrefix + "-" + fullStr);
        }
    }


    public PolicyObjectDTO generateServiceObject(List<ServiceDTO> serviceDTOList, Integer idleTimeout, String existsServiceName) {
        PolicyObjectDTO dto = new PolicyObjectDTO();
        if (StringUtils.isNotBlank(existsServiceName)) {
            dto.setObjectFlag(true);
            dto.setJoin("service " + existsServiceName + "\n");
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

        dto.setObjectFlag(true);


        StringBuilder objNameSb = new StringBuilder();

        //多个，建对象
        for (ServiceDTO service : serviceDTOList) {
            int protocolNum = Integer.valueOf(service.getProtocol());
            String protocolString = ProtocolUtils.getProtocolByValue(protocolNum).toLowerCase();

            if (protocolString.equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_ANY)) {
                dto.setCommandLine("service Any \n");
                return dto;
            }


            String command = getServiceNameByNoPort(service, 0);
            if (StringUtils.isNotBlank(command)) {
                groupFlag = true;
                serviceNameList.add(command);
                continue;
            }

            objNameSb.append(getServiceName(service) + "_");

            //定义对象有多种情况
            if (protocolString.equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_TCP)
                    || protocolString.equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_UDP)) {

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
                    if (idleTimeout != null) {
                        int day = idleTimeout / DAY_SECOND;
                        if ((idleTimeout % DAY_SECOND) > 0) {
                            day = day + 1;
                        }
                        sb.append(String.format(" timeout-day %d", day));
                    }
                    sb.append("\n");
                }
            } else if (protocolString.equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_ICMP)) {
                //icmpType为空的话，默认为icmp type 3，
                if (StringUtils.isBlank(service.getType()) || !allowType.contains(service.getType())) {
                    sb.append("icmp type 3\n");
                } else if (StringUtils.isNotBlank(service.getType()) && allowType.contains(service.getType())) {
                    //icmpType不为空的话，若icmpType为3,4,5,8,11,12,13,15，则正常生成icmp type 和 code信息， 否则设定为icmp type 3
                    //有code增加code，没有code则为空字符串
                    sb.append(String.format("icmp type %d %s\n", service.getType(), service.getCode() == null ? "" : String.format("code %d", Integer.valueOf(service.getCode()))));
                }
            }
        }

        //有对象
        if (sb.toString().length() > 0) {
            String objName = objNameSb.toString();
            if (objName.substring(objName.length() - 1).equals("_")) {
                objName = objName.substring(0, objName.length() - 1);
            }

            //service name限制长度
            if (objName.length() > getMaxNameLength()) {
                String shortName = objName.substring(0, getMaxNameLength() - 5);
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
        if (groupFlag) {
            String groupName = getServiceName(serviceDTOList);
            sb.append(String.format("servgroup %s\n", groupName));
            for (String objName : serviceNameList) {
                sb.append(String.format("service %s\n", objName));
            }
            sb.append("exit\n");
            dto.setJoin("service " + groupName + "\n");
            dto.setName(groupName);
        } else {
            dto.setJoin("service " + dto.getName() + "\n");
        }

        dto.setCommandLine(sb.toString());
        return dto;
    }

    //协议没有端口时，衔接
    public String getServiceNameByNoPort(ServiceDTO service, Integer ipType) {
        String command = "";
        int protocolNum = Integer.valueOf(service.getProtocol());
        String protocolString = ProtocolUtils.getProtocolByValue(protocolNum).toLowerCase();
        if (protocolString.equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_ANY)) {
            command = " any ";
        } else if (protocolString.equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_TCP) ||
                protocolString.equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_UDP)) {
            if (StringUtils.isBlank(service.getDstPorts()) || service.getDstPorts().equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_ANY)) {
                command = protocolString + "-any ";
            }
        } else if (protocolString.equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_ICMP)) {
            if (ipType.intValue() == IpTypeEnum.IPV6.getCode()) {
                // ipv6的 icmp
                command = protocolString + "v6 ";
            } else {
                command = protocolString + " ";
            }
        } else if (protocolString.equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_ICMPV6)) {
            command = protocolString + " ";
        }
        return command;
    }

    public PolicyObjectDTO generateTimeObject(String startTime, String endTime, String ticket) {
        if (startTime == null) {
            return null;
        }
        PolicyObjectDTO dto = new PolicyObjectDTO();
        StringBuilder sb = new StringBuilder();
        String name = String.format("\"to%s\"",
                TimeUtils.transformDateFormat(endTime, TimeUtils.EUROPEAN_TIME_FORMAT, TimeUtils.COMMON_TIME_DAY_FORMAT));
        if (USE_RAW_NAME) {
            name = ticket;
        }
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
    public String getServiceName(List<ServiceDTO> serviceDTOList) {
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
        if (name.length() > getMaxNameLength()) {
            String shortName = name.substring(0, getMaxNameLength() - 6);
            name = String.format("%s_etcsg", shortName.substring(0, shortName.lastIndexOf("_")));
        }
        return name;
    }

    public PolicyObjectDTO generateServiceObject(List<ServiceDTO> serviceDTOList, List<String> existServiceNameList, Integer idleTimeout, Integer ipType,List<String> createServiceObjectName) {
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

        //多个，建对象
        for (ServiceDTO service : serviceDTOList) {
            int protocolNum = Integer.valueOf(service.getProtocol());
            String protocolString = ProtocolUtils.getProtocolByValue(protocolNum).toLowerCase();

            if (protocolString.equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_ANY)) {
                dto.setCommandLine("service Any \n");
                return dto;
            }

            if (protocolString.equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_ICMP)) {
//                dto.setCommandLine(String.format("service %s \n",PolicyConstants.POLICY_STR_VALUE_ICMP));
                serviceNameList.add(PolicyConstants.POLICY_STR_VALUE_ICMP);
                continue;
            }

            if (protocolString.equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_ICMPV6)) {
                serviceNameList.add(PolicyConstants.POLICY_STR_VALUE_ICMPV6);
                continue;
            }

            if (service.getDstPorts().equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_ANY)) {
                String command = getServiceNameByNoPort(service, ipType);
                if (StringUtils.isNotBlank(command)) {
                    groupFlag = true;
                    serviceNameList.add(command);
                    //continue;
                }
                service.setDstPorts(PolicyConstants.PORT_ANY);
            }

            String name = getServiceNameKevin(serviceKey, protocolString,service.getDstPorts(),idleTimeout);
            serviceNameList.add(name);
            sb.append("service " + name);
            sb.append("\n");
            dto.setName(name);

            //定义对象有多种情况
            if (protocolString.equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_TCP)
                    || protocolString.equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_UDP)) {

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
                    sb.append(String.format("%s dst-port %s", protocolString, PortUtils.getPortString(dstPort, PortUtils.BLANK_FORMAT)));
                    if (idleTimeout != null) {
                        int day = idleTimeout / DAY_SECOND;
                        if ((idleTimeout % DAY_SECOND) > 0) {
                            day = day + 1;
                        }
                        sb.append(String.format(" timeout-day %d", day));
                    }
                    sb.append("\n");
                }
            } else if (protocolString.equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_ICMP)) {
                //icmpType为空的话，默认为icmp type 3，
                if (StringUtils.isBlank(service.getType()) || !allowType.contains(service.getType())) {
                    sb.append("icmp type 3\n");
                } else if (StringUtils.isNotBlank(service.getType()) && allowType.contains(service.getType())) {
                    //icmpType不为空的话，若icmpType为3,4,5,8,11,12,13,15，则正常生成icmp type 和 code信息， 否则设定为icmp type 3
                    //有code增加code，没有code则为空字符串
                    sb.append(String.format("icmp type %d %s\n", service.getType(), service.getCode() == null ? "" : String.format("code %d", Integer.valueOf(service.getCode()))));
                }
            }
            sb.append("exit\n");
            createServiceObjectName.add(name);
        }

        System.out.println(JSONObject.toJSONString(serviceNameList));
        //要建组, 用多个服务替代
        if (groupFlag) {
            StringBuilder joinSb = new StringBuilder();
            for (String objName : serviceNameList) {
                joinSb.append(String.format("service %s\n", objName));
            }
            dto.setJoin(joinSb.toString());
        } else {
            dto.setJoin("service " + dto.getName() + "\n");
        }


        dto.setCommandLine(sb.toString());
        return dto;
    }

    public PolicyObjectDTO generateServiceTimeoutObject(List<ServiceDTO> serviceDTOList, Integer idleTimeout, Integer ipType) {
        logger.info("idleTimeout is " + idleTimeout);
        PolicyObjectDTO dto = new PolicyObjectDTO();

        StringBuilder sb = new StringBuilder();

        //对象名称集合, 不一定会建组，建组条件：有2组及以上协议，其中有一个协议，不带端口
        List<String> serviceNameList = new ArrayList<>();

        //多个服务，必须建对象或组
        dto.setObjectFlag(true);

        //多个，建对象
        for (ServiceDTO service : serviceDTOList) {
            int protocolNum = Integer.valueOf(service.getProtocol());
            String protocolString = ProtocolUtils.getProtocolByValue(protocolNum).toLowerCase();

            if (service.getDstPorts().equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_ANY)) {
                service.setDstPorts(PolicyConstants.PORT_ANY);
            }

            String name = String.format("\"%s%s\"", protocolString.substring(0, 1).toUpperCase(), service.getDstPorts());
            if (idleTimeout != null) {
                name = String.format("\"%s%sL\"", protocolString.substring(0, 1).toUpperCase(), service.getDstPorts());
            }
            if (PortUtils.isPortRange(service.getDstPorts())) {
                String start = PortUtils.getStartPort(service.getDstPorts());
                String end = PortUtils.getEndPort(service.getDstPorts());
                name = String.format("\"%s%s-%s\"", protocolString.substring(0, 1).toUpperCase(), start, end);
                if (idleTimeout != null) {
                    name = String.format("\"%s%s-%sL\"", protocolString.substring(0, 1).toUpperCase(), start, end);
                }
            }
            if (protocolString.equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_ICMP) ){
                name = String.format("\"%sL\"", protocolString.toUpperCase());
            }
            if (protocolString.equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_ICMPV6)){
                name = String.format("\"%sL\"", PolicyConstants.POLICY_STR_VALUE_ICMP.toUpperCase());
            }
            serviceNameList.add(name);
            sb.append("service " + name);
            sb.append("\n");
            dto.setName(name);

            //定义对象有多种情况
            if (protocolString.equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_TCP)
                    || protocolString.equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_UDP)) {

                String[] dstPorts = service.getDstPorts().split(",");
                //当协议为tcp/udp协议，源端口为any，目的端口为具体值,源端口不显示
                for (String dstPort : dstPorts) {
                    sb.append(String.format("%s dst-port %s", protocolString, PortUtils.getPortString(dstPort, PortUtils.BLANK_FORMAT)));
                    if (idleTimeout != null) {
                        int day = idleTimeout / DAY_SECOND;
                        if ((idleTimeout % DAY_SECOND) > 0) {
                            day = day + 1;
                        }
                        sb.append(String.format(" timeout-day %d", day));
                    }
                    sb.append("\n");
                }
            }
            if (protocolString.equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_ICMP)){
                sb.append(String.format("%s type 8",protocolString));
                if (idleTimeout != null) {
                    int day = idleTimeout / DAY_SECOND;
                    if ((idleTimeout % DAY_SECOND) > 0) {
                        day = day + 1;
                    }
                    sb.append(String.format(" timeout-day %d", day));
                }
                sb.append("\n");
            }
            if (protocolString.equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_ICMPV6)){
                sb.append(String.format("%s type 1",protocolString));
                if (idleTimeout != null) {
                    int day = idleTimeout / DAY_SECOND;
                    if ((idleTimeout % DAY_SECOND) > 0) {
                        day = day + 1;
                    }
                    sb.append(String.format(" timeout-day %d", day));
                }
                sb.append("\n");
            }

            sb.append("exit\n");
        }

        //要建组, 用多个服务替代
        StringBuilder joinSb = new StringBuilder();
        for (String objName : serviceNameList) {
            joinSb.append(String.format("service %s\n", objName));
        }
        dto.setJoin(joinSb.toString());

        dto.setCommandLine(sb.toString());
        return dto;
    }

    /**
     * 私有方法区
     */

    /**
     * 判断目的IP是否是域名
     * @param dstIp
     * @return
     */
    private Boolean isDomainForIp(String dstIp) {
        for (String ip : dstIp.split(",")) {
            if (!(IpUtils.isIPSegment(ip) || IpUtils.isIPRange(ip) || IpUtils.isIPv6(ip) || IpUtils.isIP(ip))) {
                return true;
            }
        }
        return false;
    }

    public static void main(String[] args) {
        CommandlineDTO dto = CommandlineDTO.getInstanceDemo();
        SecurityHillStoneR5ForHunanDL hillStoneR5 = new SecurityHillStoneR5ForHunanDL();
        String commandLine = hillStoneR5.composite(dto);
        System.out.println("commandline:\n" + commandLine);
    }

    public String createCommandLine(CommandlineDTO dto, Integer mergeProperty) {
        StringBuilder sb = new StringBuilder();
        String ticket = dto.getName();
        String srcIp = dto.getSrcIp();
        String dstIp = dto.getDstIp();
        boolean createObjFlag = dto.isCreateObjFlag();
        String existSrcName = dto.getSrcAddressName();

        PolicyObjectDTO srcAddress;


        List<String> createObjectName = new ArrayList<>();
        if (StringUtils.isNotEmpty(existSrcName)) {
            srcAddress = generateAddressObject(srcIp, ticket, "src", createObjFlag, existSrcName, dto.getSrcIpSystem(), dto.getIpType(),null);
        } else {
            srcAddress = generateAddressObject(dto.getExistSrcAddressList(), dto.getRestSrcAddressList(), ticket, "src", createObjFlag, dto.getSrcIpSystem(), dto.getIpType(),createObjectName);
        }
        String existDstName = dto.getDstAddressName();
        Boolean isDomain = false;
        if(!AliStringUtils.isEmpty(dstIp)){
            isDomain = isDomainForIp(dstIp);
        }
        Integer dstIpType = dto.getIpType();
        if (Boolean.TRUE.equals(isDomain)) {
            dstIpType = 2;
        }

        PolicyObjectDTO dstAddress;
        if (StringUtils.isNotEmpty(existDstName) && ObjectUtils.isNotEmpty(dto.getRestDstAddressList())) {
            dstAddress = generateAddressObject(dstIp, ticket, "dst", createObjFlag, existDstName, dto.getDstIpSystem(), dstIpType,null);
        } else {
            dstAddress = generateAddressObject(dto.getExistDstAddressList(), dto.getRestDstAddressList(), ticket, "dst", createObjFlag, dto.getDstIpSystem(), dstIpType,createObjectName);
        }
        String existServiceName = dto.getServiceName();

        List<String> createServiceObjectName = new ArrayList<>();

        PolicyObjectDTO serviceObject;
        if (StringUtils.isNotEmpty(existServiceName)) {

            serviceObject = generateServiceObject(dto.getServiceList(), dto.getIdleTimeout(), existServiceName);
        } else {
            serviceObject = generateServiceObject(dto.getRestServiceList(), dto.getExistServiceNameList(), dto.getIdleTimeout(), dto.getIpType(),createServiceObjectName);
        }
        dto.setAddressObjectNameList(createObjectName);
        dto.setServiceObjectNameList(createServiceObjectName);

        sb.append("configure\n");

        //定义对象
        if (srcAddress.isObjectFlag() && StringUtils.isNotBlank(srcAddress.getCommandLine())) {
            sb.append(String.format("%s\n", srcAddress.getCommandLine()));
        }
        if (dstAddress.isObjectFlag() && StringUtils.isNotBlank(dstAddress.getCommandLine())) {
            sb.append(String.format("%s\n", dstAddress.getCommandLine()));
        }

        if (serviceObject != null && serviceObject.isObjectFlag() && StringUtils.isNotBlank(serviceObject.getCommandLine())) {
            sb.append(String.format("%s\n", serviceObject.getCommandLine()));
        }

        if(dto.getSecurityPolicy()!=null && dto.getSecurityPolicy().getRuleId()!=null){
            sb.append(String.format("rule id %s\n", dto.getSecurityPolicy().getRuleId()));
        }else{
            sb.append(String.format("rule name %s\n", dto.getBusinessName()));
        }
        sb.append("log session-start\n");
        sb.append("log session-end\n");
        if (mergeProperty == 0) {
            if (StringUtils.isNotBlank(srcAddress.getJoin())) {
                sb.append(srcAddress.getJoin());
            } else {
                sb.append(srcAddress.getName());
            }
        }
        if (mergeProperty == 1) {
            if (StringUtils.isNotBlank(dstAddress.getJoin())) {
                sb.append(dstAddress.getJoin());
            } else {
                sb.append(dstAddress.getName());
            }
        }
        if (mergeProperty == 2) {
            if (serviceObject != null && serviceObject.isObjectFlag() && StringUtils.isNotBlank(serviceObject.getJoin())) {
                sb.append(serviceObject.getJoin());
            } else if (serviceObject != null && StringUtils.isNotBlank(serviceObject.getCommandLine())) {
                sb.append(serviceObject.getName());
            }
        }
        sb.append("exit\n");
        sb.append("end\n");

        return sb.toString();
    }

    public static String getServiceNameKevin(String key, String protocolString,String dstPorts,Integer idleTimeout) {
        logger.info("进入utils获取ip名称");
        String name;
        String servicePrefix = "tcp_,udp_,icmp,-";
        String[] serviceArr = servicePrefix.split(",");
        String prefix;
        String line = "-";
        if (serviceArr.length == 4) {
            line = serviceArr[3];
        }
        if (protocolString.equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_TCP)) {
            prefix = serviceArr[0];
        } else if (protocolString.equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_UDP)) {
            prefix = serviceArr[1];
        } else {
            prefix = serviceArr[2];
        }
        name = String.format("\"%s%s\"", prefix, dstPorts);
        if (idleTimeout != null) {
            name = String.format("\"%s%sL\"", prefix, dstPorts);
        }
        if (PortUtils.isPortRange(dstPorts)) {
            String start = PortUtils.getStartPort(dstPorts);
            String end = PortUtils.getEndPort(dstPorts);
            name = String.format("\"%s%s%s%s\"", prefix, start, line, end);
            if (idleTimeout != null) {
                name = String.format("\"%s%s%s%sL\"", prefix, start,line,  end);
            }
        }
        return name;
    }

    public static String getIPNameKevin(Integer ipType, String address, boolean isSrcIp, String srcIpSystem, String dstIpSystem, int length, int index){
        logger.info("进入utils获取service名称,ipType:" + ipType);
        String ipPrefix = ",,,3";
        String[] arr = ipPrefix.split(",");
        String isIP = arr[0];//单个ip前缀
        String iPRange = arr[1];//ip范围前缀
        String iPSegment = arr[2];//ip子网前缀
        String random = "";//使用自定义名称时，是否使用随机数
        if (arr.length == 4) {
            random = arr[3];
        }
        if (isSrcIp && StringUtils.isNotEmpty(srcIpSystem)) {
            if ("1".equals(random)) {
                return srcIpSystem + "_" + IdGen.getRandomNumberString();
            } else if ("2".equals(random)) {
                return srcIpSystem + "_" + address;
            } else {
                if (length == 1) {
                    return srcIpSystem;
                } else {
                    return srcIpSystem + "_" + index;
                }
            }
        } else if (!isSrcIp && StringUtils.isNotEmpty(dstIpSystem)) {
            if ("1".equals(random)) {
                return dstIpSystem + "_" + IdGen.getRandomNumberString();
            } else if ("2".equals(random)) {
                return dstIpSystem + "_" + address;
            } else {
                if (length == 1) {
                    return dstIpSystem;
                } else {
                    return dstIpSystem + "_" + index;
                }
            }
        }
        if (ipType.intValue() == IpTypeEnum.IPV4.getCode()) {
            if (IpUtils.isIPSegment(address)) {
                return iPSegment + address;
            } else if (IpUtils.isIPRange(address)) {
                String startIp = IpUtils.getStartIpFromIpAddress(address);
                String endIp = IpUtils.getEndIpFromIpAddress(address);
                String[] startArr = startIp.split("\\.");
                String[] endArr = endIp.split("\\.");
                if (startArr[0].equals(endArr[0])
                        && startArr[1].equals(endArr[1])
                        && startArr[2].equals(endArr[2])) {
                    return iPRange + startIp + "-" + endArr[3];
                }else {
                    return iPRange + address;
                }
            } else {
                return isIP + address;
            }
        } else if (ipType.intValue() == IpTypeEnum.IPV6.getCode()) {
            // ipv6
            if (address.contains("/")) {
                return iPSegment + address;
            } else if (address.contains("-")) {
                return iPRange + address;
            } else {
                return isIP + address;
            }
        } else {
            // 目的地址是URL类型
            return address;
        }
    }

}
