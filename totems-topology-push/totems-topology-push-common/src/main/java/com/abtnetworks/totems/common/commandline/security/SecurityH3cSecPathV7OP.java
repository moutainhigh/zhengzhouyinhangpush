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
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * @author luwei
 * @date 2019-03-21
 */
@Slf4j
@Service
public class SecurityH3cSecPathV7OP extends SecurityPolicyGenerator implements PolicyGenerator {

    private static Logger logger = Logger.getLogger(SecurityH3cSecPathV7OP.class);

    public final int MAX_OBJECT_NAME_LENGTH = 31;

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
        generatedDto.setRollbackShowCmd(dto.getGeneratedDto().getRollbackShowCmd());
        return commandLine;
    }

    @Override
    public String generatePreCommandline(CommandlineDTO dto) {
        StringBuilder sb = new StringBuilder();
        sb.append("system-view\n");
        sb.append("\n");
        if (dto.isVsys()) {
            sb.append("switchto context " + dto.getVsysName() + "\n");
            sb.append("\n");
            sb.append("system-view\n");
            sb.append("\n");
        }
        return sb.toString();
    }

    @Override
    public String generateCommandline(CommandlineDTO dto) {

        boolean isCreateObject = dto.isCreateObjFlag();

        PolicyObjectDTO srcAddressObject = generateAddressObject(dto.getSrcIp(), dto.getName(), dto.getSrcAddressName(), "source-ip", isCreateObject, dto.getSrcIpSystem());

        PolicyObjectDTO dstAddressObject = generateAddressObject(dto.getDstIp(), dto.getName(), dto.getDstAddressName(), "destination-ip", isCreateObject, dto.getDstIpSystem());

        PolicyObjectDTO serviceObject = generateServiceObject(dto.getServiceList(), dto.getServiceName(), isCreateObject);

        PolicyObjectDTO timeObject = generateTimeObject(dto.getStartTime(), dto.getEndTime(), dto.getName());

        StringBuilder sb = new StringBuilder();

        if (srcAddressObject.isObjectFlag() && StringUtils.isNotBlank(srcAddressObject.getCommandLine())) {
            sb.append(srcAddressObject.getCommandLine());
            sb.append("\n");
        }
        if (dstAddressObject.isObjectFlag() && StringUtils.isNotBlank(dstAddressObject.getCommandLine())) {
            sb.append(dstAddressObject.getCommandLine());
            sb.append("\n");
        }
        if (serviceObject.isObjectFlag() && StringUtils.isNotBlank(serviceObject.getCommandLine())) {
            sb.append(serviceObject.getCommandLine());
            sb.append("\n");
        }
        if (timeObject != null) {
            sb.append(timeObject.getCommandLine());
            sb.append("\n");
        }
        String srcZone = StringUtils.isEmpty(dto.getSrcZone()) ? PolicyConstants.POLICY_STR_VALUE_ANY : dto.getSrcZone();
        String dstZone = StringUtils.isEmpty(dto.getDstZone()) ? PolicyConstants.POLICY_STR_VALUE_ANY : dto.getDstZone();
        sb.append(String.format("object-policy ip %s\n", CommonConstants.OBJECT_POLICY));
        String action = dto.getAction();
        if ("deny".equalsIgnoreCase(action)) {
            sb.append("rule drop ");
        } else {
            sb.append("rule pass ");
        }

        StringBuffer  showPolicyidCommand= new StringBuffer();
        showPolicyidCommand.append("policy show ");
        if (StringUtils.isNotEmpty(srcAddressObject.getJoin())) {
            sb.append(srcAddressObject.getJoin().replace("\n", " "));
            showPolicyidCommand.append(srcAddressObject.getJoin().replace("\n", " "));
        }
        if (StringUtils.isNotEmpty(dstAddressObject.getJoin())) {
            sb.append(dstAddressObject.getJoin().replace("\n", " "));
            showPolicyidCommand.append(dstAddressObject.getJoin().replace("\n", " "));
        }

        if (serviceObject != null && !AliStringUtils.isEmpty(serviceObject.getJoin())) {
            sb.append(serviceObject.getJoin().replace("\n", " "));
            showPolicyidCommand.append(serviceObject.getJoin().replace("\n", " "));
        }
        if (timeObject != null) {
            sb.append(String.format("time-range %s ", timeObject.getJoin()));
            showPolicyidCommand.append(String.format("time-range %s ", timeObject.getJoin()));
        }
        GeneratedObjectDTO generatedDto = new GeneratedObjectDTO();
        generatedDto.setRollbackShowCmd(showPolicyidCommand.toString());
        dto.setGeneratedDto(generatedDto);
//        if(!AliStringUtils.isEmpty(dto.getDescription())) {
//            sb.append("description " + dto.getDescription() + " ");
//        }
//        sb.append("\nquit\n");
//        sb.append(String.format("zone-pair security source %s destination %s\n",srcZone,dstZone));
//        sb.append(String.format("object-policy apply ip %s\n",dto.getName()));

//        String action = dto.getAction();
//        if ("deny".equalsIgnoreCase(action)) {
//            sb.append("action drop \n");
//        } else {
//            sb.append("action pass \n");
//        }
//        if(StringUtils.isNotBlank(dto.getSrcZone())) {
//            sb.append(String.format("source-zone %s\n", srcZone));
//        }
//        if(StringUtils.isNotBlank(dto.getDstZone())) {
//            sb.append(String.format("destination-zone %s\n", dstZone));
//        }


//        String swapRuleNameId = StringUtils.isNotBlank(dto.getSwapRuleNameId()) ? dto.getSwapRuleNameId() : "";
//        int moveSeatCode = dto.getMoveSeatEnum().getCode();
//        if (moveSeatCode == MoveSeatEnum.BEFORE.getCode() || moveSeatCode == MoveSeatEnum.FIRST.getCode()) {
//            if (!AliStringUtils.isEmpty(swapRuleNameId)) {
//                sb.append(String.format("move rule before %s\n", swapRuleNameId));
//            }
//        }
//
        Integer idleTimeout = dto.getIdleTimeout();
        if(ObjectUtils.isNotEmpty(idleTimeout)){
            idleTimeout /= 3600;
            sb.append("session persistent aging-time ").append(idleTimeout).append(StringUtils.LF);
        }
        sb.append("\nquit\n");
//        sb.append("return\n");
        String command = sb.toString();
        return command;
    }

    @Override
    public String generatePostCommandline(CommandlineDTO dto) {
        return "";
    }

    /**
     * ??????????????????
     *
     * @param ipAddress ip??????
     * @return ????????????
     */
    private PolicyObjectDTO generateAddressObject(String ipAddress, String ticket, String existsAddressName, String prefix, boolean isCreateObject, String ipSystem) {
        PolicyObjectDTO dto = new PolicyObjectDTO();
        if (StringUtils.isNotBlank(existsAddressName)) {
            dto.setObjectFlag(true);
            existsAddressName = containsQuotes(existsAddressName);
            dto.setJoin(prefix + " " + existsAddressName + "\n");
            return dto;
        }

        //??????IPv6??????????????????????????????
        if (IpUtils.isIPv6(ipAddress)) {
            isCreateObject = true;
        }

        String join = "";
        String command = "";

        if (AliStringUtils.isEmpty(ipAddress)) {
            dto.setJoin(join);
            dto.setCommandLine(command);
            return dto;
        }

        if (isCreateObject) {
            String name;
            if(StringUtils.isNotEmpty(ipSystem)){
                name = ipSystem;
                // ???????????????????????????????????????2?????????
                name = strSub(name, getMaxObejctNameLength(),"GB2312");
                // ????????????????????????
                int len = 0;
                try{
                    len = name.getBytes("GB2312").length;
                }catch (Exception e) {
                    logger.error("???????????????????????????");
                }
                if(len > getMaxObejctNameLength() -7 ) {
                    name = strSub(name, getMaxObejctNameLength() -7, "GB2312");
                }
                name = String.format("%s_%s", name, DateUtils.getDate().replace("-","").substring(2));
            } else {
                name = String.format("%s_AO_%s", ticket, IdGen.getRandomNumberString());
            }
            name = containsQuotes(name);
            StringBuilder sb = new StringBuilder();
            sb.append("object-group ip address ");
            sb.append(name + "\n");
            join = prefix + " " + name + "\n";

            String addressCmd = "";
            String[] arr = ipAddress.split(",");
            int index = 0;
            boolean isIpv6 = false;
            for (String address : arr) {
                if (IpUtils.isIPRange(address)) {
                    String startIp = IpUtils.getStartIpFromRange(address);
                    String endIp = IpUtils.getEndIpFromRange(address);
                    addressCmd = String.format("%s network range %s %s\n", index, startIp, endIp);
                } else if (IpUtils.isIPSegment(address)) {
                    String ip = IpUtils.getIpFromIpSegment(address);
                    String maskBit = IpUtils.getMaskBitFromIpSegment(address);
                    addressCmd = String.format("%s network subnet %s %s\n", index, ip, maskBit);
                } else if (IpUtils.isIP(address)) {
                    addressCmd = String.format("%s network host address %s\n", index, address);
                } else if (address.contains(":")) {
                    isIpv6 = true;
                    //ipv6
                    if (address.contains("/")) {
                        String[] addrArray = address.split("/");
                        if (StringUtils.isNotEmpty(addrArray[0])) {
                            addressCmd = String.format("%s network subnet %s %s\n", index, addrArray[0].toLowerCase(), addrArray[1]);
                        }
                    } else if (address.contains("-")) {
                        String[] addrArray = address.split("-");
                        if (StringUtils.isNoneEmpty(addrArray[0], addrArray[1])) {
                            addressCmd = String.format("%s network range %s %s\n", index, addrArray[0].toLowerCase(), addrArray[1].toLowerCase());
                        }
                    } else {
                        addressCmd = String.format("%s network host address %s\n", index, address.toLowerCase());
                    }
                }
                index++;
                sb.append(addressCmd);
            }

            sb.append("quit\n");

            command = sb.toString();
            if (isIpv6) {
                //ipv6???
                command = command.replace("object-group ip", "object-group ipv6");
            }
        } else {
            String addressCmd = "";
            String[] arr = ipAddress.split(",");
            StringBuilder sb = new StringBuilder();
            for (String address : arr) {
                if (IpUtils.isIPRange(address)) {
                    String startIp = IpUtils.getStartIpFromRange(address);
                    String endIp = IpUtils.getEndIpFromRange(address);
                    addressCmd = String.format("%s-range %s %s\n", prefix, startIp, endIp);
                } else if (IpUtils.isIPSegment(address)) {
                    String ip = IpUtils.getIpFromIpSegment(address);
                    String maskBit = IpUtils.getMaskBitFromIpSegment(address);
                    addressCmd = String.format("%s-subnet %s %s\n", prefix, ip, maskBit);
                } else if (IpUtils.isIP(address)) {
                    addressCmd = String.format("%s-host %s\n", prefix, address);
                }
                sb.append(addressCmd);
            }
            join = sb.toString();
        }

        dto.setJoin(join);
        dto.setCommandLine(command);
        dto.setObjectFlag(true);
        return dto;
    }

    public int getMaxObejctNameLength() {
        return MAX_OBJECT_NAME_LENGTH;
    }

    /**
     * ???????????????????????????
     *
     * @param serviceDTOList ????????????
     * @return ???????????????
     */
    private PolicyObjectDTO generateServiceObject(List<ServiceDTO> serviceDTOList, String existsServiceName, boolean isCreateObject) {
        PolicyObjectDTO dto = new PolicyObjectDTO();
        dto.setObjectFlag(true);
        if (StringUtils.isNotBlank(existsServiceName)) {
            dto.setJoin("service" + " " + existsServiceName + "\n");
            return dto;
        }
        if (CollectionUtils.isEmpty(serviceDTOList)) {
            dto.setJoin("");
            return dto;
        }
        String join = "";
        String command = "";
        if (isCreateObject) {
            StringBuilder sb = new StringBuilder();
            String setName = getServiceName(serviceDTOList);
            if (PolicyConstants.POLICY_STR_VALUE_ANY.equalsIgnoreCase(ProtocolUtils.getProtocolByString(serviceDTOList.get(0).getProtocol()))) {
                dto.setJoin("");
                return dto;
            }
            sb.append(String.format("object-group service %s\n", setName));

            int index = 0;
            for (ServiceDTO service : serviceDTOList) {
                int protocolNum = Integer.valueOf(service.getProtocol());
                String protocolString = ProtocolUtils.getProtocolByValue(protocolNum).toLowerCase();
                if (protocolString.equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_ANY)) {
                    dto = new PolicyObjectDTO();
                    dto.setJoin(join);
                    dto.setCommandLine(command);
                    return dto;
                }

                if (protocolString.equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_ICMP)) {
                    sb.append(String.format("%d service icmp ", index));
                    if (StringUtils.isNotBlank(service.getType())) {
                        sb.append(String.format("%d ", Integer.valueOf(service.getType())));
                    }
                    if (StringUtils.isNotBlank(service.getCode())) {
                        sb.append(String.format("%d ", Integer.valueOf(service.getCode())));
                    }
                    sb.append("\n");
                    index++;
                } else if ((protocolString.equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_TCP) ||
                        protocolString.equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_UDP))) {
                    String[] srcPorts = service.getSrcPorts().split(",");
                    String[] dstPorts = service.getDstPorts().split(",");
                    //???TCP/UPD??????
                    //??????any????????????????????????????????????????????????
                    if (service.getSrcPorts().equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_ANY) && !service.getDstPorts().equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_ANY)) {
                        for (String dstPort : dstPorts) {
                            sb.append(String.format(index + " service %s ", protocolString));
                            if (PortUtils.isPortRange(dstPort)) {
                                String startPort = PortUtils.getStartPort(dstPort);
                                String endPort = PortUtils.getEndPort(dstPort);
                                sb.append(String.format("destination range %s %s \n", startPort, endPort));
                            } else {
                                sb.append(String.format("destination eq %s  \n", dstPort));
                            }
                            index++;
                        }

                    } else if (!service.getSrcPorts().equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_ANY) && service.getDstPorts().equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_ANY)) {
                        //??????????????????????????????any????????????????????????
                        for (String srcPort : srcPorts) {
                            sb.append(String.format(index + " service %s ", protocolString));
                            if (PortUtils.isPortRange(srcPort)) {
                                String startPort = PortUtils.getStartPort(srcPort);
                                String endPort = PortUtils.getEndPort(srcPort);
                                sb.append(String.format("source range %s %s \n", startPort, endPort));
                            } else {
                                sb.append(String.format("source eq %s \n", srcPort));
                            }
                            index++;
                        }
                    } else {
                        //???????????????????????????????????????????????????any
                        for (String srcPort : srcPorts) {
                            for (String dstPort : dstPorts) {
                                sb.append(String.format(index + " service %s ", protocolString));

                                if (srcPort.equals(PolicyConstants.POLICY_STR_VALUE_ANY)) {
                                    sb.append(" ");
                                } else if (PortUtils.isPortRange(srcPort)) {
                                    String startPort = PortUtils.getStartPort(srcPort);
                                    String endPort = PortUtils.getEndPort(srcPort);
                                    sb.append(String.format("source range %s %s ", startPort, endPort));
                                } else {
                                    sb.append(String.format("source eq %s ", srcPort));
                                }

                                if (dstPort.equals(PolicyConstants.POLICY_STR_VALUE_ANY)) {
                                    sb.append(" ");
                                } else if (PortUtils.isPortRange(dstPort)) {
                                    String startPort = PortUtils.getStartPort(dstPort);
                                    String endPort = PortUtils.getEndPort(dstPort);
                                    sb.append(String.format("destination range %s %s", startPort, endPort));
                                } else {
                                    sb.append(String.format("destination eq %s", dstPort));
                                }

                                sb.append("\n");
                                index++;
                            }
                        }
                    }
                }
            }

            sb.append("quit\n");
            join = String.format("service %s\n", setName);
            command = sb.toString();
        } else {
            StringBuilder sb = new StringBuilder();
            for (ServiceDTO service : serviceDTOList) {
                int protocolNum = Integer.valueOf(service.getProtocol());
                String protocolString = ProtocolUtils.getProtocolByValue(protocolNum).toLowerCase();
                if (protocolString.equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_ANY)) {
                    dto = new PolicyObjectDTO();
                    dto.setJoin(join);
                    dto.setCommandLine(command);
                    return dto;
                }

                if (protocolString.equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_ICMP)) {
                    sb.append("service-port icmp ");
                    if (StringUtils.isNotBlank(service.getType())) {
                        sb.append(String.format("%d ", Integer.valueOf(service.getType())));
                    }
                    if (StringUtils.isNotBlank(service.getCode())) {
                        sb.append(String.format("%d ", Integer.valueOf(service.getCode())));
                    }
                    sb.append("\n");
                } else if ((protocolString.equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_TCP) ||
                        protocolString.equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_UDP))) {
                    String[] srcPorts = service.getSrcPorts().split(",");
                    String[] dstPorts = service.getDstPorts().split(",");
                    //???TCP/UPD??????
                    //??????any????????????????????????????????????????????????
                    if (service.getSrcPorts().equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_ANY) && !service.getDstPorts().equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_ANY)) {
                        for (String dstPort : dstPorts) {
                            sb.append(String.format("service-port %s ", protocolString));
                            if (PortUtils.isPortRange(dstPort)) {
                                String startPort = PortUtils.getStartPort(dstPort);
                                String endPort = PortUtils.getEndPort(dstPort);
                                sb.append(String.format("destination range %s %s \n", startPort, endPort));
                            } else {
                                sb.append(String.format("destination eq %s  \n", dstPort));
                            }
                        }

                    } else if (!service.getSrcPorts().equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_ANY) && service.getDstPorts().equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_ANY)) {
                        //??????????????????????????????any????????????????????????
                        for (String srcPort : srcPorts) {
                            sb.append(String.format("service-port %s ", protocolString));
                            if (PortUtils.isPortRange(srcPort)) {
                                String startPort = PortUtils.getStartPort(srcPort);
                                String endPort = PortUtils.getEndPort(srcPort);
                                sb.append(String.format("source range %s %s \n", startPort, endPort));
                            } else {
                                sb.append(String.format("source eq %s \n", srcPort));
                            }
                        }
                    } else {
                        //???????????????????????????????????????????????????any
                        for (String srcPort : srcPorts) {
                            for (String dstPort : dstPorts) {
                                sb.append(String.format("service-port %s ", protocolString));

                                if (srcPort.equals(PolicyConstants.POLICY_STR_VALUE_ANY)) {
                                    sb.append(" ");
                                } else if (PortUtils.isPortRange(srcPort)) {
                                    String startPort = PortUtils.getStartPort(srcPort);
                                    String endPort = PortUtils.getEndPort(srcPort);
                                    sb.append(String.format("source range %s %s ", startPort, endPort));
                                } else {
                                    sb.append(String.format("source eq %s ", srcPort));
                                }

                                if (dstPort.equals(PolicyConstants.POLICY_STR_VALUE_ANY)) {
                                    sb.append(" ");
                                } else if (PortUtils.isPortRange(dstPort)) {
                                    String startPort = PortUtils.getStartPort(dstPort);
                                    String endPort = PortUtils.getEndPort(dstPort);
                                    sb.append(String.format("destination range %s %s", startPort, endPort));
                                } else {
                                    sb.append(String.format("destination eq %s", dstPort));
                                }

                                sb.append("\n");
                            }
                        }
                    }
                }
            }
            join = sb.toString();
        }

        dto.setJoin(join);
        dto.setCommandLine(command);
        return dto;
    }

    static String formatTimeString(String timeString) {
        if (StringUtils.isBlank(timeString)) {
            return null;
        }

        String dateStr = "";
        SimpleDateFormat sdf = new SimpleDateFormat(TimeUtils.EUROPEAN_TIME_FORMAT);
        SimpleDateFormat sdf2 = new SimpleDateFormat(TimeUtils.H3C_V7_FORMAT);
        try {
            Date date = sdf.parse(timeString);
            dateStr = sdf2.format(date);
        } catch (Exception e) {
            logger.error("??????????????????", e);
        }

        return dateStr;
    }

    /**
     * ????????????????????????
     *
     * @param startTimeString ?????????????????????
     * @param endTimeString   ?????????????????????
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

    public static void main(String[] args) {
        CommandlineDTO dto = CommandlineDTO.getInstanceDemo();
        SecurityH3cSecPathV7OP h3cv7 = new SecurityH3cSecPathV7OP();
        String commandLine = h3cv7.composite(dto);
        System.out.println("commandline:\n" + commandLine);
    }

}
