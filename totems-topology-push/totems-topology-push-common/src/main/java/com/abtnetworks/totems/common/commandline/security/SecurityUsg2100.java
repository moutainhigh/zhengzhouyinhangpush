package com.abtnetworks.totems.common.commandline.security;

import com.abtnetworks.totems.common.commandline.PolicyGenerator;
import com.abtnetworks.totems.common.commandline.SecurityPolicyGenerator;
import com.abtnetworks.totems.common.constants.PolicyConstants;
import com.abtnetworks.totems.common.enums.ActionEnum;
import com.abtnetworks.totems.common.dto.*;
import com.abtnetworks.totems.common.dto.commandline.CommandlineDTO;
import com.abtnetworks.totems.common.dto.commandline.PolicyMergeDTO;
import com.abtnetworks.totems.common.dto.commandline.PolicyObjectDTO;
import com.abtnetworks.totems.common.dto.commandline.ServiceDTO;
import com.abtnetworks.totems.common.utils.*;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * @Author: wenjiachang
 * @Date: 2019/1/11 14:02
 */
@Service
public class SecurityUsg2100 extends SecurityPolicyGenerator implements PolicyGenerator {
    private static Logger logger = Logger.getLogger(SecurityUsg2100.class);

    // private static int huaweiId = 40000;

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
        String commandLine = composite(dto);
        generatedDto.setAddressObjectNameList(dto.getAddressObjectNameList());
        generatedDto.setServiceObjectNameList(dto.getServiceObjectNameList());
        generatedDto.setTimeObjectNameList(dto.getTimeObjectNameList());
        return commandLine;
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
        PolicyObjectDTO srcAddressObject = generateAddressObject(dto.getSrcIp(), dto.getName(), createObjFlag, dto.getSrcAddressName());
        PolicyObjectDTO dstAddressObject = generateAddressObject(dto.getDstIp(), dto.getName(), createObjFlag, dto.getDstAddressName());
        PolicyObjectDTO serviceObject = generateServiceObject(dto.getServiceList(), dto.getName(), createObjFlag, dto.getServiceName());
        PolicyObjectDTO timeObject = generateTimeObject(dto.getStartTime(), dto.getEndTime(), dto.getName());

        // ???????????????????????????
        recordCreateAddrAndServiceObjectName(dto, srcAddressObject, dstAddressObject, null,null);
        recordCreateServiceObjectNames(dto,serviceObject);
        recordCreateTimeObjectName(dto,timeObject);

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
            logger.info("??????????????????????????????????????????ruleID???mergeField ????????????");
            return createCommandLine(dto);
        }
        String ruleId = mergeDTO.getRuleId();
        String mergeField = mergeDTO.getMergeField();

        //??????????????????
        StringBuilder sb = new StringBuilder();
        boolean createObjFlag = dto.isCreateObjFlag();
        PolicyObjectDTO srcAddressObject = generateAddressObject(dto.getSrcIp(), dto.getName(), createObjFlag, dto.getSrcAddressName());
        PolicyObjectDTO dstAddressObject = generateAddressObject(dto.getDstIp(), dto.getName(), createObjFlag, dto.getDstAddressName());
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

        // ???????????????????????????
        recordCreateAddrAndServiceObjectName(dto, srcAddressObject, dstAddressObject, null,null);
        recordCreateTimeObjectName(dto,serviceObject);


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
     * ??????????????????
     * @param ipAddress ip??????
     * @return ????????????
     */
    private PolicyObjectDTO generateAddressObject(String ipAddress, String ticket, boolean createObjFlag, String existsAddressName) {
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
        //??????1???????????????????????????
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

        List<String>  createObjectNames = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        sb.append("ip address-set ");
        String setName = String.format("%s_AO_%s", ticket, IdGen.getRandomNumberString());
        setName = containsQuotes(setName);
        sb.append(setName);
        sb.append(" type object\n");
        createObjectNames.add(setName);

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
                addressCmd = String.format("address %s %s 0\n", index, address);
            }
            index++;
            sb.append(addressCmd);
        }
        sb.append("quit\n");

        dto.setName(setName);
        dto.setJoin(String.format("address-set %s\n", setName));
        dto.setCommandLine(sb.toString());
        dto.setCreateObjectName(createObjectNames);
        return dto;
    }

    /**
     * ???????????????????????????
     * @param serviceDTOList ????????????
     * @return ???????????????
     */
    private PolicyObjectDTO generateServiceObject(List<ServiceDTO> serviceDTOList, String ticket, boolean createObjFlag, String existsServiceName) {
        PolicyObjectDTO dto = new PolicyObjectDTO();
        if (StringUtils.isNotBlank(existsServiceName)) {
            dto.setObjectFlag(true);
            existsServiceName = containsQuotes(existsServiceName);

            dto.setJoin(String.format("service-set %s", existsServiceName));
            return dto;
        }
        StringBuilder sb = new StringBuilder();

        List<String>  createServiceObjectNames = new ArrayList<>();
        String setName = getServiceName(serviceDTOList);
        sb.append(String.format("ip service-set %s type object\n", setName));
        createServiceObjectNames.add(setName);
        int index = 0;
        for(ServiceDTO service : serviceDTOList){
            int protocolNum = Integer.valueOf(service.getProtocol());

            String protocolString = ProtocolUtils.getProtocolByValue(protocolNum).toLowerCase();
            if(protocolString.equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_ANY)) {
                return dto;
            }

            //???????????????????????????tcp/udp/icmp??????????????????????????? ?????????????????????????????????????????????????????????
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

            //?????????
            if(protocolString.equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_ICMP)) {
                //icmp??????????????????
                sb.append(String.format("service %s protocol %s \n", index, protocolString));
            }else{
                String[] srcPorts = service.getSrcPorts().split(",");
                String[] dstPorts = service.getDstPorts().split(",");

                for(String srcPort: srcPorts) {
                    for(String dstPort: dstPorts) {
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

        dto.setName(setName);
        dto.setJoin(String.format("service-set %s", setName));
        dto.setCommandLine(sb.toString());
        dto.setObjectFlag(true);
        dto.setCreateServiceObjectName(createServiceObjectNames);
        return dto;
    }

    /**
     * ????????????????????????
     * @param startTimeString ?????????????????????
     * @param endTimeString ?????????????????????
     * @return ??????????????????
     */
    private PolicyObjectDTO generateTimeObject(String startTimeString, String endTimeString, String ticket) {
        if (AliStringUtils.isEmpty(startTimeString) || AliStringUtils.isEmpty(endTimeString)) {
            return null;
        }

        String startTime = formatTimeString(startTimeString);
        String endTime = formatTimeString(endTimeString);

        String setName = String.format("%s_TR_%s", ticket, IdGen.getRandomNumberString());

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
     * ?????????????????? ???->??? inbound??????->??? outbound
     * @param srcPriority ???????????????
     * @param dstPriority ??????????????????
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

        SecurityUsg2100 usg2100 = new SecurityUsg2100();
        String commandLine = usg2100.composite(dto);
        System.out.println("commandline:\n" + commandLine);


    }
}
