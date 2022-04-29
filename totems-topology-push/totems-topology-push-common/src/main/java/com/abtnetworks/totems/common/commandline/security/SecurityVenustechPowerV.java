package com.abtnetworks.totems.common.commandline.security;

import com.abtnetworks.totems.common.commandline.PolicyGenerator;
import com.abtnetworks.totems.common.commandline.SecurityPolicyGenerator;
import com.abtnetworks.totems.common.constants.CommonConstants;
import com.abtnetworks.totems.common.constants.PolicyConstants;
import com.abtnetworks.totems.common.dto.*;
import com.abtnetworks.totems.common.dto.commandline.CommandlineDTO;
import com.abtnetworks.totems.common.dto.commandline.PolicyObjectDTO;
import com.abtnetworks.totems.common.dto.commandline.ServiceDTO;
import com.abtnetworks.totems.common.enums.ActionEnum;
import com.abtnetworks.totems.common.enums.MoveSeatEnum;
import com.abtnetworks.totems.common.utils.*;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Administrator
 * @Title:
 * @Description: 网御安全策略命令行生成
 * @date 2020/7/2
 */
@Slf4j
@Service
public class SecurityVenustechPowerV extends SecurityPolicyGenerator implements PolicyGenerator {

    private static Logger logger = LoggerFactory.getLogger(SecurityCiscoASA.class);

    private final String IN_KEY = "in";

    private final int MAX_NAME_LENGTH = 65;

    @Override
    public String generate(CmdDTO cmdDTO) {
        log.info("cmdDTO is " + JSONObject.toJSONString(cmdDTO, true));
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
        dto.setCreateObjFlag(settingDTO.isCreateObject());
        dto.setSwapRuleNameId(settingDTO.getSwapNameId());

        TaskDTO taskDTO = cmdDTO.getTask();
        dto.setName(taskDTO.getTheme());
        dto.setBusinessName(taskDTO.getTheme());
        dto.setCiscoInterfaceCreate(settingDTO.isCreateCiscoItfRuleList());
        dto.setCiscoInterfacePolicyName(settingDTO.getCiscoItfRuleListName());
        dto.setCreateObjFlag(settingDTO.isCreateObject());

        ExistObjectDTO existObjectDTO = cmdDTO.getExistObject();
        dto.setSrcAddressName(existObjectDTO.getSrcAddressObjectName());
        dto.setDstAddressName(existObjectDTO.getDstAddressObjectName());
        dto.setServiceName(existObjectDTO.getServiceObjectName());
        dto.setExistSrcAddressList(existObjectDTO.getExistSrcAddressList());
        dto.setExistDstAddressList(existObjectDTO.getExistDstAddressList());
        dto.setExistServiceNameList(existObjectDTO.getExistServiceNameList());
        dto.setRestServiceList(existObjectDTO.getRestServiceList());

        log.info("dto is" + JSONObject.toJSONString(dto, true));
        String commandLine = composite(dto);
        GeneratedObjectDTO generatedDto = cmdDTO.getGeneratedObject();
        generatedDto.setPolicyName(dto.getName());
        return commandLine;
    }

    @Override
    public String generatePreCommandline(CommandlineDTO dto) {

        return "";
    }

    @Override
    public String generateCommandline(CommandlineDTO dto) {

        boolean createObjFlag = dto.isCreateObjFlag();
        String ticket = dto.getName();
        //创建服务、地址对象

        PolicyObjectDTO srcAddress = generateAddressObject(dto.getSrcIp(), ticket, createObjFlag, dto.getSrcAddressName(),null);
        PolicyObjectDTO dstAddress = generateAddressObject(dto.getDstIp(), ticket, createObjFlag, dto.getDstAddressName(),null);

        PolicyObjectDTO serviceObject = generateServiceObject(dto.getRestServiceList(), createObjFlag, dto.getExistServiceNameList());
        return commonLine(srcAddress, dstAddress, serviceObject, dto);
    }


    public String commonLine(PolicyObjectDTO srcAddress, PolicyObjectDTO dstAddress, PolicyObjectDTO serviceObject,
                             CommandlineDTO dto) {

        String name = dto.getBusinessName();

        String ticket = dto.getName();

        String startTime = dto.getStartTime();
        String endTime = dto.getEndTime();

        String action = dto.getAction();

        PolicyObjectDTO time = generateTimeObject(startTime, endTime, ticket);

        StringBuilder sb = new StringBuilder();

        //定义对象
        if (srcAddress.isObjectFlag() && StringUtils.isNotBlank(srcAddress.getCommandLine())) {
            sb.append(String.format("%s\n", srcAddress.getCommandLine()));
        }
        if (dstAddress.isObjectFlag() && StringUtils.isNotBlank(dstAddress.getCommandLine())) {
            sb.append(String.format("%s\n", dstAddress.getCommandLine()));
        }
        if (serviceObject.isObjectFlag() && StringUtils.isNotBlank(serviceObject.getCommandLine())) {
            sb.append(String.format("%s\n", serviceObject.getCommandLine()));
        }
        if (time != null) {
            sb.append(String.format("%s\n", time.getCommandLine()));
        }


        sb.append(CommonConstants.LINE_BREAK);
        String policyName = String.format("%s_policy", name);
        String actionCmd;
        if (CommonConstants.DENY.equalsIgnoreCase(action)) {
            actionCmd = String.format("rule add type %s name \"%s\" ", action, policyName);
        } else {
            actionCmd = String.format("rule add type %s name \"%s\" ", action, policyName);
        }
        sb.append(actionCmd);
        int moveSeatCode = dto.getMoveSeatEnum().getCode();
        if (moveSeatCode == MoveSeatEnum.FIRST.getCode()) {
            sb.append(" id 1 ");
        } else if (moveSeatCode == MoveSeatEnum.BEFORE.getCode()) {
            // todo 不支持
        } else if (moveSeatCode == MoveSeatEnum.AFTER.getCode()) {
            // todo 不支持
        }

        String addressCmd = String.format(" sa %s da %s ",
                srcAddress.getJoin(), dstAddress.getJoin());
        sb.append(addressCmd);

        String srcZone;
        if (StringUtils.isNotEmpty(dto.getSrcZone())) {
            srcZone = String.format("iif %s ", dto.getSrcZone());

        } else {
            srcZone = "iif  any";
        }
        sb.append(srcZone);

        String dstZone;
        if (StringUtils.isNotEmpty(dto.getDstZone())) {
            dstZone = String.format(" oif %s ", dto.getDstZone());

        } else {
            dstZone = " oif any ";
        }
        sb.append(dstZone);

        String serviceNext = String.format(" service %s  ", serviceObject.getJoin());
        sb.append(serviceNext);

        String timeNext;
        if (time != null) {
            timeNext = String.format(" time %s ", time.getJoin());
            sb.append(timeNext);
        }


        String descNext;
        if (StringUtils.isNotEmpty(dto.getDescription())) {
            descNext = String.format(" comment %s ", dto.getDescription());
            sb.append(descNext);
        }
        sb.append(CommonConstants.LINE_BREAK);

        sb.append("newconfig save\n");
        return sb.toString();
    }

    @Override
    public String generatePostCommandline(CommandlineDTO dto) {
        return "";
    }

    public PolicyObjectDTO generateAddressObject(String ipAddress, String ticket, boolean createObjFlag, String existsAddressName,List<String> existSrcAddressList) {
        PolicyObjectDTO dto = new PolicyObjectDTO();
        dto.setObjectFlag(true);

        if (StringUtils.isNotBlank(existsAddressName)) {
            dto.setJoin(existsAddressName);
            dto.setName(existsAddressName);

            return dto;
        }
        if (AliStringUtils.isEmpty(ipAddress) && CollectionUtils.isEmpty(existSrcAddressList)) {
            dto.setJoin("any");
            dto.setCommandLine("");
            return dto;
        }
        StringBuilder sb = new StringBuilder();
        String join = "";
//        if (CollectionUtils.isNotEmpty(existSrcAddressList) && StringUtils.isEmpty(ipAddress)){
//            String groupName = String.format("%s_group_%s", ticket, IdGen.getRandomNumberString());
//            sb.append(String.format("addrgrp add name %s member \"%s\"\n", groupName,StringUtils.join(existSrcAddressList.toArray(),",")));
//            join = groupName;
//        }
        boolean isIpV6 = false;
        //若为IPv6地址，
        if (IpUtils.isIPv6(ipAddress)) {
            isIpV6 = true;
        }


        String[] arr = ipAddress.split(",");
        String setName = String.format("%s_AO_%s", ticket, IdGen.getRandomNumberString());

        join = setName;
        String addressCmd = "";
        List<String> list = new ArrayList<>();
        if (CollectionUtils.isEmpty(existSrcAddressList)){
//            sb.append("address add name ").append(join).append(CommonConstants.LINE_BREAK);
            for (String address : arr) {
                if (IpUtils.isIPRange(address)) {
                    String startIp = IpUtils.getStartIpFromRange(address);
                    String endIp = IpUtils.getEndIpFromRange(address);
                    addressCmd = String.format("%s:%s", startIp, endIp);
//                    sb.append(addressCmd);
                    list.add(addressCmd);

                } else if (IpUtils.isIPSegment(address)) {
                    String ipStart = IpUtils.getStartIp(address);
                    String maskBitFromIpSegment = IpUtils.getMaskBitFromIpSegment(address);
                    String ipEnd = IpUtils.getMaskByMaskBit(maskBitFromIpSegment);
                    addressCmd = String.format("%s/%s", ipStart, ipEnd);
//                    sb.append(addressCmd);
                    list.add(addressCmd);

                } else {
                    addressCmd = String.format("%s", address);
                    list.add(addressCmd);
                }
            }
            sb.append(String.format("address add name %s ip \"%s\"",join,StringUtils.join(list.toArray(),","))).append(StringUtils.LF);
        }else {
            if (isIpV6) {

            } else {
                // 是创建对象
                AtomicInteger count = new AtomicInteger(0);
                StringBuffer addressObject = new StringBuffer();
                for (String address : arr) {
                    join = setName;
                    int mark = count.get();
                    if (mark > 0) {
                        join = String.format("%s_%s ",join,mark);
                    }
                    addressObject.append(join).append(",");
                    count.addAndGet(1);
                    if (IpUtils.isIPRange(address)) {
                        String startIp = IpUtils.getStartIpFromRange(address);
                        String endIp = IpUtils.getEndIpFromRange(address);
                        addressCmd = String.format("address add name  %s ip %s:%s\n", join, startIp, endIp);
                        sb.append(addressCmd);

                    } else if (IpUtils.isIPSegment(address)) {
                        String ipStart = IpUtils.getStartIp(address);
                        String maskBitFromIpSegment = IpUtils.getMaskBitFromIpSegment(address);
                        String ipEnd = IpUtils.getMaskByMaskBit(maskBitFromIpSegment);
                        addressCmd = String.format("address add name  %s ip %s/%s\n", join, ipStart, ipEnd);
                        sb.append(addressCmd);

                    } else {
                        sb.append("address add name ").append(join)
                                .append(" ip ").append(address).append(CommonConstants.LINE_BREAK);
                    }
                }
                if (arr.length + existSrcAddressList.size()> 1) {
                    String groupName = String.format("%s_group_%s", ticket, IdGen.getRandomNumberString());
                    if (CollectionUtils.isNotEmpty(existSrcAddressList)){
                        addressObject.append(StringUtils.join(existSrcAddressList.toArray(),","));
                    }
                    String toString = addressObject.toString();
                    if (StringUtils.isNotEmpty(toString) && toString.substring(toString.length()-1,toString.length()).equals(",")){
                        toString = toString.substring(0,toString.length()-1);
                    }
                    sb.append(String.format("addrgrp add name %s member \"%s\"\n", groupName,toString));
                    join = groupName;
                }else {
                    if (existSrcAddressList.size() ==1){
                        join = existSrcAddressList.get(0);
                    }
                }

            }
        }

        sb.append("exit\n");
        dto.setCommandLine(sb.toString());
        dto.setJoin(join);
        return dto;
    }


    public PolicyObjectDTO generateServiceObject(List<ServiceDTO> serviceDTOList, boolean createObjFlag, List<String> existsServiceNames) {
        PolicyObjectDTO dto = new PolicyObjectDTO();
        dto.setObjectFlag(true);
        StringBuilder sb = new StringBuilder();
        String join = "";
        int protocolNum1 = Integer.valueOf(serviceDTOList.get(0).getProtocol());
        String protocolString1 = ProtocolUtils.getProtocolByValue(protocolNum1).toLowerCase();
        if (protocolString1.equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_ANY)) {
            if (CollectionUtils.isNotEmpty(existsServiceNames)) {
                if (existsServiceNames.size() > 1) {
                    StringBuffer stringBuffer = new StringBuffer();
                    StringBuffer name = new StringBuffer();
                    for (String existServiceName : existsServiceNames) {
                        name.append(existServiceName).append("_");
                        stringBuffer.append(existServiceName).append(" ");
                    }
                    name.deleteCharAt(name.lastIndexOf("_"));

                    String serviceName = String.format("%s_group_%s", name, IdGen.getRandomNumberString());
                    sb.append("servgrp add name ").append(serviceName).append(CommonConstants.LINE_BREAK)
                            .append(String.format("servgrp set name  %s addmbr %s\n", serviceName, stringBuffer));
                    join = String.format("%s", serviceName);
                    sb.append("exit\n");
                    dto.setCommandLine(sb.toString());
                } else {
                    join = String.format("%s", existsServiceNames.get(0));
                }
                dto.setJoin(join);
            } else {
                dto.setJoin("any");
            }

            return dto;
        }

        String name = getServiceName(serviceDTOList);
        dto.setName(name);
        AtomicInteger count = new AtomicInteger(0);
        StringBuffer serviceObject = new StringBuffer();
        StringBuffer nameBuffer = new StringBuffer();
        Map<String, String> mergeServiceDTOMap = new HashMap<>();
        for (ServiceDTO service : serviceDTOList) {
            String protocol = service.getProtocol();
            String port = service.getDstPorts();
            if (mergeServiceDTOMap.containsKey(protocol)) {
                String newPort = mergeServiceDTOMap.get(protocol) + "," + port;
                mergeServiceDTOMap.put(protocol, newPort);
            } else {
                mergeServiceDTOMap.put(protocol, port);
            }
        }
        for (String protocol : mergeServiceDTOMap.keySet()) {
            int protocolNum = Integer.valueOf(protocol);
            String protocolString = ProtocolUtils.getProtocolByValue(protocolNum).toLowerCase();
            ServiceDTO serviceDTO = new ServiceDTO();
            String port = mergeServiceDTOMap.get(protocol);
            serviceDTO.setProtocol(protocol);
            serviceDTO.setDstPorts(port);
            String setName = getServiceName(serviceDTO);


            //是TCP/UPD协议
            //源为any，目的端口有值，则仅显示目的端口
            String serviceName = String.format("service add name %s  ", setName);
            sb.append(serviceName);

            if (StringUtils.isEmpty(port)) {
                continue;
            }
            serviceObject.append(setName).append(" ");
            String[] dstPorts = port.split(",");
            if (!port.equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_ANY)) {
                count.addAndGet(1);
                nameBuffer.append(setName).append("_");
                for (String dstPort : dstPorts) {
                    if (!PortUtils.isPortRange(dstPort)) {
                        sb.append(String.format("protocol %s dp %s ", protocolString, dstPort));
                    } else {
                        sb.append(String.format("protocol %s dp %s ", protocolString, PortUtils.getPortString(dstPort, PortUtils.COLON_FORMAT)));
                    }
                }

            } else {
                //都为any
                sb.append(String.format("protocol %s ", protocolString));
            }
            sb.append(CommonConstants.LINE_BREAK);
        }

        int sizeTotal = existsServiceNames.size() + mergeServiceDTOMap.size();
        if (sizeTotal > 1) {
            String serviceName;
            if (mergeServiceDTOMap.size() >= 1 && existsServiceNames.size() >= 1) {
                for (String existServiceName : existsServiceNames) {
                    nameBuffer.append(existServiceName).append("_");
                    serviceObject.append(existServiceName).append(" ");
                }

                serviceName = nameBuffer.deleteCharAt(nameBuffer.lastIndexOf("_")).toString();
            } else {
                serviceName = name;
            }
            serviceName = String.format("%s_group_%s", serviceName, IdGen.getRandomNumberString());
            sb.append("servgrp add name ").append(serviceName).append(CommonConstants.LINE_BREAK)
                    .append(String.format("servgrp set name  %s addmbr %s\n", serviceName, serviceObject));
            join = String.format("%s", serviceName);

        } else {
            join = String.format("%s", name);
        }
        sb.append("exit\n");
        dto.setJoin(join);
        dto.setCommandLine(sb.toString());

        return dto;
    }


    /**
     * 生成时间对象
     *
     * @param startTime
     * @param endTime
     * @param ticket
     * @return
     */
    private PolicyObjectDTO generateTimeObject(String startTime, String endTime, String ticket) {
        PolicyObjectDTO dto = new PolicyObjectDTO();
        if (startTime == null) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        String name = String.format("%s_TR_%s", ticket, IdGen.getRandomNumberString());

        sb.append("time add name ").append(name).append(" type once start ").append(formatDateString(startTime, TimeUtils.LEAD_TIME_FORMAT))
                .append(" stop ").append(formatDateString(endTime, TimeUtils.LEAD_TIME_FORMAT)).append(CommonConstants.LINE_BREAK);
        sb.append("exit\n");
        dto.setName(name);
        dto.setCommandLine(sb.toString());
        dto.setJoin(name);
        return dto;
    }

    private String formatDateString(String timeString, String format) {
        return TimeUtils.transformDateFormat(timeString, TimeUtils.EUROPEAN_TIME_FORMAT, format);
    }

    @Override
    public int getMaxNameLength() {
        return MAX_NAME_LENGTH;
    }

    public static void main(String[] args) {
        SecurityVenustechPowerV securityLegendSec = new SecurityVenustechPowerV();
        CommandlineDTO dto = CommandlineDTO.getInstanceDemo();
        String commandLine = securityLegendSec.composite(dto);
        System.out.println("commandline:\n" + commandLine);


    }
}
