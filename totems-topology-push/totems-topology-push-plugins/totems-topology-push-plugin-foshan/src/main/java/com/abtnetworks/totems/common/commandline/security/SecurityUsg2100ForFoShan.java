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
import com.abtnetworks.totems.common.enums.ActionEnum;
import com.abtnetworks.totems.common.enums.DeviceModelNumberEnum;
import com.abtnetworks.totems.common.enums.IpTypeEnum;
import com.abtnetworks.totems.common.enums.PolicyEnum;
import com.abtnetworks.totems.common.utils.*;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author lps
 */
@Service
@CustomCli(value = DeviceModelNumberEnum.USG2000, type = PolicyEnum.SECURITY)
public class SecurityUsg2100ForFoShan extends SecurityPolicyGenerator implements PolicyGenerator {
    private static Logger logger = Logger.getLogger(SecurityUsg2100ForFoShan.class);

    private final int HOUR_SECOND = 60 * 60;

    @Override
    public String generate(CmdDTO cmdDTO) {
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

        GeneratedObjectDTO generatedDto = cmdDTO.getGeneratedObject();
        generatedDto.setPolicyName(dto.getName());
        return composite(dto);
    }

    @Override
    public String generatePreCommandline(CommandlineDTO dto) {
        return "system-view\n";
    }

    @Override
    public String generateCommandline(CommandlineDTO dto) {
        if(dto.getMergeDTO() == null || dto.isMustCreateFlag()) {
            return createCommandLine(dto);
        }else{
            return editCommandLine(dto);
        }
    }

    public String createCommandLine(CommandlineDTO dto) {
        boolean createObjFlag = dto.isCreateObjFlag();
        PolicyObjectDTO srcAddressObject = generateAddressObject(dto.getSrcIp(), dto.getName(), createObjFlag, dto.getSrcAddressName(), dto.getIpType());
        PolicyObjectDTO dstAddressObject = generateAddressObject(dto.getDstIp(), dto.getName(), createObjFlag, dto.getDstAddressName(), dto.getIpType());
        PolicyObjectDTO serviceObject = generateServiceObject(dto.getServiceList(), dto.getName(), createObjFlag, dto.getServiceName());
        PolicyObjectDTO timeObject = generateTimeObject(dto.getStartTime(), dto.getEndTime(), dto.getName());

        StringBuilder sb = new StringBuilder();

        if(srcAddressObject.isObjectFlag() && StringUtils.isNotBlank(srcAddressObject.getCommandLine())) {
            sb.append(String.format("%s\n", srcAddressObject.getCommandLine()));
        }

        if(dstAddressObject.isObjectFlag() && StringUtils.isNotBlank(dstAddressObject.getCommandLine())) {
            sb.append(String.format("%s\n", dstAddressObject.getCommandLine()));
        }

        if(serviceObject.isObjectFlag() && StringUtils.isNotBlank(serviceObject.getCommandLine())) {
            sb.append(String.format("%s\n", serviceObject.getCommandLine()));
        }
        if(timeObject != null) {
            sb.append(timeObject.getCommandLine());
            sb.append("\n");
        }
        boolean isOneEmpty = (StringUtils.isEmpty(dto.getSrcZone()) && StringUtils.isNotEmpty(dto.getDstZone())) || (StringUtils.isEmpty(dto.getDstZone()) && StringUtils.isNotEmpty(dto.getSrcZone())) ;
        if(isOneEmpty){
            String zone;
            if(StringUtils.isNotEmpty(dto.getSrcZone())){
                zone = dto.getSrcZone();
            }else{
                zone = dto.getDstZone();
            }
            sb.append(String.format("policy zone %s\n", zone));
        }
        else  if(StringUtils.isNotEmpty(dto.getSrcZone()) && dto.getSrcZone().equals(dto.getDstZone())) {
            sb.append(String.format("policy zone %s\n", dto.getSrcZone()));
        } else {
            sb.append(String.format("policy interzone %s %s %s\n", dto.getSrcZone(), dto.getDstZone(), getDirection(dto.getSrcZonePriority(), dto.getDstZonePriority())));
        }
        sb.append(String.format("policy \n"));
        if(!AliStringUtils.isEmpty(dto.getDescription())){
            sb.append(String.format("description %s\n", dto.getDescription()));
        }
        sb.append("policy logging\n");
        if (dto.getIdleTimeout() != null) {
            sb.append("long-link enable\n");
            sb.append(String.format("long-link aging-time %d\n", dto.getIdleTimeout() / HOUR_SECOND));
        }
        sb.append(String.format("action %s\n", dto.getAction()));
        if(StringUtils.isNotBlank(serviceObject.getJoin())) {
            sb.append(String.format("policy service %s\n", serviceObject.getJoin()));
        }

        if(StringUtils.isNotBlank(srcAddressObject.getJoin())) {
            sb.append(String.format("policy source %s", srcAddressObject.getJoin()));
        }else{
            for(String command : srcAddressObject.getCommandLineList()) {
                sb.append(String.format("policy source %s", command));
            }

        }
        if(StringUtils.isNotBlank(dstAddressObject.getJoin())) {
            sb.append(String.format("policy destination %s", dstAddressObject.getJoin()));
        }else{
            for(String command : dstAddressObject.getCommandLineList()) {
                sb.append(String.format("policy destination %s", command));
            }
        }

        if(timeObject != null) {
            sb.append(String.format("policy time-range %s\n", timeObject.getJoin()));
        }

//        sb.append(String.format("policy move %d top\n", huaweiId));
//        sb.append("return\n");
//
//        huaweiId = huaweiId + 1;
        return sb.toString();
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
        boolean createObjFlag = dto.isCreateObjFlag();
        PolicyObjectDTO srcAddressObject = generateAddressObject(dto.getSrcIp(), dto.getName(), createObjFlag, dto.getSrcAddressName(), dto.getIpType());
        PolicyObjectDTO dstAddressObject = generateAddressObject(dto.getDstIp(), dto.getName(), createObjFlag, dto.getDstAddressName(), dto.getIpType());
        PolicyObjectDTO serviceObject = generateServiceObject(dto.getServiceList(), dto.getName(), createObjFlag, dto.getServiceName());
        if(mergeField.equals(PolicyConstants.SRC) && srcAddressObject.isObjectFlag()) {
            sb.append(srcAddressObject.getCommandLine());
        }
        if(mergeField.equals(PolicyConstants.DST) && dstAddressObject.isObjectFlag()) {
            sb.append(dstAddressObject.getCommandLine());
        }
        if(mergeField.equals(PolicyConstants.SERVICE) && serviceObject != null && serviceObject.isObjectFlag()) {
            sb.append(serviceObject.getCommandLine());
        }

        sb.append(String.format("policy interzone %s %s inbound\n", dto.getSrcZone(), dto.getDstZone()));
        sb.append(String.format("policy %s\n", ruleId));
        if(mergeField.equals(PolicyConstants.SRC)) {
            if(srcAddressObject.isObjectFlag()) {
                sb.append(String.format("policy source %s", srcAddressObject.getJoin()));
            }else{
                for(String command : srcAddressObject.getCommandLineList()) {
                    sb.append(String.format("policy source %s", command));
                }
            }
        }else if(mergeField.equals(PolicyConstants.DST)) {
            if(dstAddressObject.isObjectFlag()) {
                sb.append(String.format("policy destination %s", dstAddressObject.getJoin()));
            }else{
                for(String command : dstAddressObject.getCommandLineList()) {
                    sb.append(String.format("policy destination %s", command));
                }
            }
        }else if(mergeField.equals(PolicyConstants.SERVICE)) {
            sb.append(String.format("policy service service-set %s\n", serviceObject.getJoin()));
        }

        return sb.toString();
    }

    @Override
    public String generatePostCommandline(CommandlineDTO dto) {
        return "";
    }


    /**
     * 获取地址对象
     * @param ipAddress ip地址
     * @return 地址对象
     */
    private PolicyObjectDTO generateAddressObject(String ipAddress, String ticket, boolean createObjFlag, String existsAddressName,Integer ipType) {
        PolicyObjectDTO dto = new PolicyObjectDTO();
        if(StringUtils.isNotBlank(existsAddressName)){
            dto.setObjectFlag(true);
            existsAddressName = containsQuotes(existsAddressName);
            dto.setJoin(String.format("address-set %s\n", existsAddressName));
            return dto;
        }

        dto.setObjectFlag(createObjFlag);

        String addressCmd = "";

        String[] arr = ipAddress.split(",");
        StringBuilder sb = new StringBuilder();
        //只有1个地址，且创建对象
        if(!createObjFlag) {
            List<String> commandLine = new ArrayList<>();
            for (String address : arr) {
                if(IpUtils.isIPRange(address)) {
                    String startIp = IpUtils.getStartIpFromRange(address);
                    String endIp = IpUtils.getEndIpFromRange(address);
                    addressCmd = String.format("range %s %s\n", startIp, endIp);
                } else if (IpUtils.isIPSegment(address)) {
                    String ip = IpUtils.getIpFromIpSegment(address);
                    String maskBit = IpUtils.getMaskBitFromIpSegment(address);
                    addressCmd = String.format("%s mask %s\n", ip, maskBit);
                } else {
                    addressCmd = String.format("%s 0\n", address);
                }
                commandLine.add(addressCmd);
            }
            dto.setCommandLineList(commandLine);
            return dto;
        }
        String objName = "";
        if (arr.length == 1) {
            if(ipType.intValue() == IpTypeEnum.IPV4.getCode()){
                if (IpUtils.isIPRange(ipAddress)) {
                    String startIp = IpUtils.getStartIpFromRange(ipAddress);
                    String endIp = IpUtils.getEndIpFromRange(ipAddress);
                    objName = String.format("address %s-%s\n", startIp, endIp);
                } else if (IpUtils.isIPSegment(ipAddress)) {
                    String ip = IpUtils.getIpFromIpSegment(ipAddress);
                    String maskBit = IpUtils.getMaskBitFromIpSegment(ipAddress);
                    objName = String.format("address %s_%s\n", ip,maskBit);
                } else {
                    objName = String.format("address %s\n", ipAddress);
                }
            } else {
                // IPV6
                if(ipAddress.contains("-")){
                    // 范围
                    String startIp = IpUtils.getRangeStartIPv6(ipAddress);
                    String endIp = IpUtils.getRangeEndIPv6(ipAddress);
                    objName = String.format("%s-%s\n", startIp, endIp);
                } else if (ipAddress.contains("/")) {
                    // 子网
                    String ip = IpUtils.getIpSegmentStartIPv6(ipAddress);
                    String maskBit = IpUtils.getIpSegmentMaskIPv6(ipAddress);
                    objName = String.format("%s_%s\n", ip,maskBit);
                } else {
                    objName = String.format("%s\n", ipAddress);
                }
            }
            dto.setName(objName);
            sb.append(objName);
        } else {
            int num = 0;
            List<String> list = new ArrayList<>(Arrays.asList(arr));
            if (ObjectUtils.isNotEmpty(list)) {
                num += list.hashCode();

            }
            objName = String.format("address %s", "add_"+Math.abs(num));
            dto.setName(objName);
            sb.append(objName);
            sb.append(" type object\n");
        }


        int index = 0;
        for (String address : arr) {
            if (IpUtils.isIPRange(address)) {
                String startIp = IpUtils.getStartIpFromRange(address);
                String endIp = IpUtils.getEndIpFromRange(address);
                addressCmd = String.format("address %s range %s %s\n", index, startIp, endIp);
            } else if (IpUtils.isIPSegment(address)) {
                String ip = IpUtils.getIpFromIpSegment(address);
                String maskBit = IpUtils.getMaskBitFromIpSegment(address);
                addressCmd = String.format("address %s %s mask %s\n", index, ip, maskBit);
            } else {
                addressCmd = String.format("address %s %s\n", index, address);
            }
            index++;
            sb.append(addressCmd);
        }
        sb.append("quit\n");
        dto.setCommandLine(sb.toString());
        dto.setJoin("address-set " + dto.getName() + " \n");
        return dto;
    }

    /**
     * 获取服务集对象文本
     * @param serviceDTOList 服务列表
     * @return 服务集对象
     */
    private PolicyObjectDTO generateServiceObject(List<ServiceDTO> serviceDTOList, String ticket, boolean createObjFlag, String existsServiceName) {
        PolicyObjectDTO dto = new PolicyObjectDTO();
        if (StringUtils.isNotBlank(existsServiceName)) {
            dto.setObjectFlag(true);
            dto.setJoin(String.format("service-set %s", existsServiceName));
            return dto;
        }
        StringBuilder sb = new StringBuilder();


        List<String> serviceNameList = new ArrayList<>();

        int index = 0;
        for(ServiceDTO service : serviceDTOList){
            int protocolNum = Integer.parseInt(service.getProtocol());
            String protocolString = ProtocolUtils.getProtocolByValue(protocolNum).toLowerCase();

            if(protocolString.equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_ANY)) {
                return dto;
            }
            //不创建对象：如果是tcp/udp/icmp协议（不带端口）， 就直接在策略中写，其他的都需要定义对象
            if (serviceDTOList.size() == 1) {
                if ((protocolString.equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_ICMP) ||
                        protocolString.equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_TCP) ||
                        protocolString.equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_UDP))
                        && service.getDstPorts().equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_ANY)) {
                    dto.setJoin(protocolString);
                    dto.setObjectFlag(false);
                    return dto;
                }
            }

            String[] split = service.getDstPorts().split(",");
            for (String dstPort : split) {
                String name = String.format("\"%s%s\"", protocolString, dstPort);

                if (PortUtils.isPortRange(dstPort)) {
                    String start = PortUtils.getStartPort(dstPort);
                    String end = PortUtils.getEndPort(dstPort);
                    name = String.format("\"%s%s-%s\"", protocolString, start, end);

                }
                serviceNameList.add(name);
                sb.append(String.format("ip service-set %s type object",name));
                sb.append("\n");
                dto.setName(name);

                //建对象
                if(protocolString.equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_ICMP)) {
                    //icmp协议没有端口
                    sb.append(String.format("service %s protocol %s \n", index, protocolString));
                }else{
                    String[] srcPorts = service.getSrcPorts().split(",");
                    String[] dstPorts = service.getDstPorts().split(",");

                    for(String srcPort: srcPorts) {
                        sb.append(String.format("service %s protocol %s ", index, protocolString));
                        if (srcPort.equals(PolicyConstants.POLICY_STR_VALUE_ANY)) {
                            sb.append("source-port 0 to 65535 ");
                        } else if (PortUtils.isPortRange(srcPort)) {
                            String startPort = PortUtils.getStartPort(srcPort);
                            String endPort = PortUtils.getEndPort(srcPort);
                            sb.append(String.format("source-port %s to %s ", startPort, endPort));
                        } else {
                            sb.append(String.format("source-port %s ", srcPort));
                        }

                        if (dstPort.equals(PolicyConstants.POLICY_STR_VALUE_ANY)) {
                            sb.append("destination-port 0 to 65535 ");
                        } else if (PortUtils.isPortRange(dstPort)) {
                            String startPort = PortUtils.getStartPort(dstPort);
                            String endPort = PortUtils.getEndPort(dstPort);
                            sb.append(String.format("destination-port %s to %s ", startPort, endPort));
                        } else {
                            sb.append(String.format("destination-port %s ", dstPort));
                        }
                        sb.append("\n");
                        index ++;
                    }
                }
            }




        }

        sb.append("quit\n");
        StringBuilder joinSb = new StringBuilder();
        for (String objName : serviceNameList) {
            joinSb.append(String.format("service %s\n", objName));
        }
        dto.setJoin(joinSb.toString());
        dto.setCommandLine(sb.toString());
        dto.setObjectFlag(true);
        return dto;
    }

    /**
     * 生成时间区间对象
     * @param startTimeString 开始时间字符串
     * @param endTimeString 结束时间字符串
     * @return 时间区间对象
     */
    private PolicyObjectDTO generateTimeObject(String startTimeString, String endTimeString, String ticket) {
        if (AliStringUtils.isEmpty(startTimeString) || AliStringUtils.isEmpty(endTimeString)) {
            return null;
        }

        String startTime = formatTimeString(startTimeString);
        String endTime = formatTimeString(endTimeString);

        String setName = String.format("\"to%s\"",
                TimeUtils.transformDateFormat(endTimeString, TimeUtils.EUROPEAN_TIME_FORMAT, TimeUtils.COMMON_TIME_DAY_FORMAT));

        PolicyObjectDTO dto = new PolicyObjectDTO();
        dto.setName(setName);
        dto.setJoin(setName);
        dto.setCommandLine(String.format("time-range %s from %s to %s \n", setName, startTime, endTime));
        return dto;
    }


    private String getRandomString() {
        return IdGen.randomBase62(PolicyConstants.POLICY_INT_RAMDOM_ID_LENGTH);
    }

    private String formatTimeString(String timeString) {
        return TimeUtils.transformDateFormat(timeString, TimeUtils.EUROPEAN_TIME_FORMAT, TimeUtils.HUAWEI_USG2000_FORMAT);
    }

    /**
     * 获取流量方向 低->高 inbound，高->低 outbound
     * @param srcPriority 源域优先级
     * @param dstPriority 目的域优先级
     * @return {inbound|outbound}
     */
    private String getDirection(int srcPriority, int dstPriority) {
        if (srcPriority > dstPriority) {
            return "inbound";
        }
        return "outbound";
    }

    public static void main(String[] args) {
        CommandlineDTO dto = CommandlineDTO.getInstanceDemo();

        SecurityUsg2100ForFoShan usg2100 = new SecurityUsg2100ForFoShan();
        String commandLine = usg2100.composite(dto);
        System.out.println("commandline:\n" + commandLine);


    }
}
