package com.abtnetworks.totems.common.commandline.security;

import com.abtnetworks.totems.common.commandline.PolicyGenerator;
import com.abtnetworks.totems.common.commandline.SecurityPolicyGenerator;
import com.abtnetworks.totems.common.constants.PolicyConstants;
import com.abtnetworks.totems.common.enums.ActionEnum;
import com.abtnetworks.totems.common.enums.MoveSeatEnum;
import com.abtnetworks.totems.common.dto.*;
import com.abtnetworks.totems.common.dto.commandline.CommandlineDTO;
import com.abtnetworks.totems.common.dto.commandline.PolicyObjectDTO;
import com.abtnetworks.totems.common.dto.commandline.ServiceDTO;
import com.abtnetworks.totems.common.utils.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * @author zc
 * @date 2019/12/27
 */
@Slf4j
@Service
public class SecurityH3cSecPathV5 extends SecurityPolicyGenerator implements PolicyGenerator {

    /**
     * 此ip不能出现在华三v5设备中
     */
    private static final String specialIp = "0.0.0.0";

    @Override
    public String generate(CmdDTO cmdDTO) {
        CommandlineDTO dto = new CommandlineDTO();
        PolicyDTO policyDTO = cmdDTO.getPolicy();
        BeanUtils.copyProperties(policyDTO, dto);
        DeviceDTO deviceDTO = cmdDTO.getDevice();
        BeanUtils.copyProperties(deviceDTO, dto);
        SettingDTO settingDTO = cmdDTO.getSetting();
        BeanUtils.copyProperties(settingDTO, dto);
        dto.setSwapRuleNameId(settingDTO.getSwapNameId());
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
        return composite(dto);
    }

    @Override
    public String generatePreCommandline(CommandlineDTO dto) {
        return "system-view\n";
    }

    @Override
    public String generateCommandline(CommandlineDTO dto) {
        String srcZone = dto.getSrcZone();
        String dstZone = dto.getDstZone();
        if (StringUtils.isEmpty(srcZone)) {
            srcZone = "Any";
        } else {
            if(srcZone.contains("_")){
                srcZone = StringUtils.substringAfterLast(srcZone, "_");
            }else if(srcZone.contains("-")){
                srcZone = StringUtils.substringAfterLast(srcZone, "-");
            }

        }
        if (StringUtils.isEmpty(dstZone)) {
            dstZone = "Any";
        } else {
            if(dstZone.contains("_")) {
                dstZone = StringUtils.substringAfterLast(dstZone, "_");
            }else if(dstZone.contains("-")){
                dstZone = StringUtils.substringAfterLast(dstZone, "-");
            }
        }

//        if (!"Any".equals(srcZone) && "Any".equals(dstZone)) {
//            throw new IllegalArgumentException("域不支持源不为Any,目的为Any的配置");
//        }

        StringBuilder sb = new StringBuilder();

        List<PolicyObjectDTO> srcAddressList = generateAddressObject(dto.getSrcIp(), dto.getName(), dto.getSrcAddressName());
        List<PolicyObjectDTO> dstAddressList = generateAddressObject(dto.getDstIp(), dto.getName(), dto.getDstAddressName());
        List<PolicyObjectDTO> serviceList = generateServiceObject(dto.getServiceList(), dto.getServiceName());
        List<PolicyObjectDTO> timeList = generateTimeObject(dto.getStartTime(), dto.getEndTime(), dto.getName());

        srcAddressList.forEach(policyObject -> sb.append(StringUtils.defaultString(policyObject.getCommandLine())));
        dstAddressList.forEach(policyObject -> sb.append(StringUtils.defaultString(policyObject.getCommandLine())));
        serviceList.forEach(policyObject -> sb.append(StringUtils.defaultString(policyObject.getCommandLine())));
        timeList.forEach(policyObject -> sb.append(StringUtils.defaultString(policyObject.getCommandLine())));

        sb.append(String.format("interzone source %s destination %s\n", srcZone, dstZone));

        String action = dto.getAction();
        if ("deny".equalsIgnoreCase(action)) {
            sb.append("rule deny");
        } else {
            sb.append("rule permit");
        }
        if (timeList.size() == 1) {
            sb.append(" time-range ").append(timeList.get(0).getName());
        }
        sb.append("\n");

        if (StringUtils.isNotEmpty(dto.getDescription())) {
            sb.append(String.format("comment %s\n", dto.getDescription()));
        }

        if (srcAddressList.size() != 0) {
            srcAddressList.forEach(policyObject -> sb.append(String.format("source-ip %s\n", policyObject.getName())));
        } else {
            sb.append("source-ip any_address\n");
        }
        if (dstAddressList.size() != 0) {
            dstAddressList.forEach(policyObject -> sb.append(String.format("destination-ip %s\n", policyObject.getName())));
        } else {
            sb.append("destination-ip any_address\n");
        }
        if (serviceList.size() != 0) {
            serviceList.forEach(policyObject -> sb.append(String.format("service %s\n", policyObject.getName())));
        } else {
            sb.append("service any_service\n");
        }

        sb.append("rule enable\n");

//        String swapRuleNameId = StringUtils.isNotBlank(dto.getSwapRuleNameId()) ? dto.getSwapRuleNameId() : "";
//        int moveSeatCode = dto.getMoveSeatEnum().getCode();
//        if (moveSeatCode == MoveSeatEnum.BEFORE.getCode() || moveSeatCode == MoveSeatEnum.FIRST.getCode()) {
//            if(!AliStringUtils.isEmpty(swapRuleNameId)) {
//                sb.append(String.format("move rule before %s\n", swapRuleNameId));
//            }
//        }
        sb.append(String.format("Display interzone-policy source %s destination %s | include rule\n", srcZone, dstZone));
        sb.append("quit\n");
        sb.append("return\n");
        return sb.toString();
    }

    @Override
    public String generatePostCommandline(CommandlineDTO dto) {
        return "\n";
    }

    /**
     * 生成地址对象
     * @param ipAddress ip地址
     * @return 地址对象
     */
    private List<PolicyObjectDTO> generateAddressObject(String ipAddress, String ticket, String existsAddressName) {
        if (StringUtils.isNotBlank(existsAddressName)) {
            PolicyObjectDTO dto = new PolicyObjectDTO();
            existsAddressName = containsQuotes(existsAddressName);
            if(existsAddressName.contains("Root-")){
                existsAddressName = existsAddressName.replace("Root-","");
            }
            dto.setName(existsAddressName);
            return Collections.singletonList(dto);
        }

        if (StringUtils.isEmpty(ipAddress)) {
            return Collections.emptyList();
        }

        List<String> ipv4Host = new ArrayList<>();
        List<Pair<String, List<String>>> pairList = new ArrayList<>();
        Arrays.stream(ipAddress.split(","))
                .forEach(address -> {
                    if(IpUtils.isIPRange(address)) {
                        pairList.add(Pair.of("ipv4Range",Collections.singletonList(address)));
                    } else if (IpUtils.isIPSegment(address)) {
                        pairList.add(Pair.of("ipv4Subnet",Collections.singletonList(address)));
                    } else if (IpUtils.isIP(address)) {
                        ipv4Host.add(address);
                    } else {
                        throw new IllegalArgumentException("不支持的ip格式" + address);
                    }
                });
        if (ipv4Host.size() != 0) {
            pairList.add(Pair.of("ipv4Host", ipv4Host));
        }
        log.debug("生成地址对象列表和对象对应的命令行列表");
        List<PolicyObjectDTO> policyObjectDTOS = new ArrayList<>();
        pairList.forEach(pair -> {
            String type = pair.getKey();
            List<String> list = pair.getValue();
            PolicyObjectDTO dto = new PolicyObjectDTO();
            StringBuilder stringBuilder = new StringBuilder();
            String name = String.format("%s_AO_%s", ticket, IdGen.getRandomNumberString());
            String setName = containsQuotes(name);
            if ("ipv4Host".equals(type)) {
                stringBuilder.append(String.format("object network host %s\n", setName));
                list.forEach(ip -> {
                    if (!ip.equals(specialIp)) {
                        stringBuilder.append(String.format("host address %s\n", ip));
                    }
                });
            } else if ("ipv4Range".equals(type)) {
                stringBuilder.append(String.format("object network range %s\n", setName));
                list.forEach(address -> {
                    String startIp = IpUtils.getStartIpFromRange(address);
                    if (specialIp.equals(startIp)) {
                        startIp = "0.0.0.1";
                    }
                    String endIp = IpUtils.getEndIpFromRange(address);
                    stringBuilder.append(String.format("range %s %s\n", startIp, endIp));
                });
            } else if ("ipv4Subnet".equals(type)) {
                list.forEach(address -> {
                    String startIp = IpUtils.getStartIp(address);
                    if (specialIp.equals(startIp)) {
                        startIp = "0.0.0.1";
                        String endIp = IpUtils.getEndIp(address);
                        stringBuilder.append(String.format("object network range %s\n", setName));
                        stringBuilder.append(String.format("range %s %s\n", startIp, endIp));
                    } else {
                        stringBuilder.append(String.format("object network subnet %s\n", setName));
                        int maskBit = Integer.parseInt(IpUtils.getMaskBitFromIpSegment(address));
                        String wildcardMask;
                        if (maskBit == 32) {
                            wildcardMask = "0.0.0.0";
                        } else {
                            wildcardMask = IPUtil.longToIP(-1 >>> maskBit);
                        }
                        stringBuilder.append(String.format("subnet %s %s\n", startIp, wildcardMask));
                    }
                });
            }
            stringBuilder.append("quit\n");
            dto.setName(setName);
            dto.setCommandLine(stringBuilder.toString());
            policyObjectDTOS.add(dto);
        });
        return policyObjectDTOS;
    }

    /**
     * 生成服务对象
     * @param serviceList
     * @param existsServiceName
     * @return
     */
    private List<PolicyObjectDTO> generateServiceObject(List<ServiceDTO> serviceList, String existsServiceName) {
        if (StringUtils.isNotBlank(existsServiceName)) {
            PolicyObjectDTO dto = new PolicyObjectDTO();
            if(existsServiceName.contains("Root-")){
                //KSH-5239
                existsServiceName = existsServiceName.replace("Root-","");
            }
            dto.setName(existsServiceName);
            return Collections.singletonList(dto);
        }

        List<Pair<String, String>> pairList = new ArrayList<>();
        serviceList.forEach(service -> {
            int protocolNum = Integer.valueOf(service.getProtocol());
            String protocolString = ProtocolUtils.getProtocolByValue(protocolNum);
            switch (protocolString) {
                case PolicyConstants.POLICY_STR_VALUE_ANY:
                    log.error("不支持协议为any的服务对象生成");
                    break;
                case PolicyConstants.POLICY_STR_VALUE_ICMP:
                    String name1 = getServiceName(service);
                    String cmd1;
                    if (StringUtils.isNoneEmpty(service.getType(), service.getCode())) {
                        cmd1 = String.format("service icmp %s %s\n", service.getType(), service.getCode());
                    } else {
                        cmd1 = "service icmp 0 0\n";
                    }
                    pairList.add(Pair.of(name1, cmd1));
                    break;
                case PolicyConstants.POLICY_STR_VALUE_TCP:
                case PolicyConstants.POLICY_STR_VALUE_UDP:
                    Arrays.stream(service.getDstPorts().split(","))
                            .forEach(dstPort -> {
                                ServiceDTO serviceDTO = new ServiceDTO();
                                serviceDTO.setProtocol(String.valueOf(protocolNum));
                                serviceDTO.setDstPorts(dstPort);
                                String name2 = getServiceName(serviceDTO);
                                String cmd2;
                                if (StringUtils.equalsAnyIgnoreCase(name2, PolicyConstants.POLICY_STR_VALUE_TCP, PolicyConstants.POLICY_STR_VALUE_UDP)) {
                                    cmd2 = String.format("service %s\n", name2);
                                } else if (PortUtils.isPortRange(dstPort)) {
                                    String startPort = PortUtils.getStartPort(dstPort);
                                    String endPort = PortUtils.getEndPort(dstPort);
                                    cmd2 = String.format("service %s destination-port %s %s \n", protocolString.toLowerCase(), startPort, endPort);
                                } else {
                                    cmd2 = String.format("service %s destination-port %s \n", protocolString.toLowerCase(), dstPort);
                                }
                                pairList.add(Pair.of(name2, cmd2));
                            });
                    break;
                default:
                    log.error("未知的协议类型[{}]",protocolString);
                    break;
            }
        });
        List<PolicyObjectDTO> policyObjectDTOList = new ArrayList<>();
        pairList.forEach(pair -> {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append(String.format("object service %s\n", pair.getLeft()));
            stringBuilder.append(pair.getRight());
            stringBuilder.append("quit\n");
            PolicyObjectDTO dto = new PolicyObjectDTO();
            dto.setName(pair.getLeft());
            dto.setCommandLine(stringBuilder.toString());
            policyObjectDTOList.add(dto);
        });
        return policyObjectDTOList;
    }

    /**
     * 生成时间对象
     * @param startTimeString
     * @param endTimeString
     * @param ticket
     * @return
     */
    private List<PolicyObjectDTO> generateTimeObject(String startTimeString, String endTimeString, String ticket) {
        if (StringUtils.isNoneEmpty(startTimeString, endTimeString, ticket)) {
            PolicyObjectDTO policyObjectDTO = new PolicyObjectDTO();
            String setName = String.format("%s_TR_%s", ticket, IdGen.getRandomNumberString());
            String command = String.format("time-range %s from %s to %s \n", setName,
                    SecurityH3cSecPathV7.formatTimeString(startTimeString), SecurityH3cSecPathV7.formatTimeString(endTimeString));
            policyObjectDTO.setName(setName);
            policyObjectDTO.setCommandLine(command);
            return Collections.singletonList(policyObjectDTO);
        } else {
            return Collections.emptyList();
        }
    }
    public static void main(String[] args) {
        CommandlineDTO dto = CommandlineDTO.getInstanceDemo();
        List<ServiceDTO> srcport=new ArrayList<>();
        ServiceDTO serviceDTO=new ServiceDTO();
        serviceDTO.setDstPorts("any");
        serviceDTO.setProtocol("58");
        srcport.add(serviceDTO);
        dto.setServiceList(srcport);
//
        dto.setSrcIp("1.1.12.2");
        dto.setDstIp("15.3.2.5");
//        dto.setIpType(1);
        SecurityH3cSecPathV5 h3cv5 = new SecurityH3cSecPathV5();
        String commandLine = h3cv5.composite(dto);
        System.out.println("commandline:\n" + commandLine);
    }

}
