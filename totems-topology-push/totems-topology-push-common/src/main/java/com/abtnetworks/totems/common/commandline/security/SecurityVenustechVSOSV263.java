package com.abtnetworks.totems.common.commandline.security;

import com.abtnetworks.totems.common.commandline.PolicyGenerator;
import com.abtnetworks.totems.common.commandline.SecurityPolicyGenerator;
import com.abtnetworks.totems.common.constants.CommonConstants;
import com.abtnetworks.totems.common.constants.PolicyConstants;
import com.abtnetworks.totems.common.dto.CmdDTO;
import com.abtnetworks.totems.common.dto.DeviceDTO;
import com.abtnetworks.totems.common.dto.ExistObjectDTO;
import com.abtnetworks.totems.common.dto.PolicyDTO;
import com.abtnetworks.totems.common.dto.SettingDTO;
import com.abtnetworks.totems.common.dto.TaskDTO;
import com.abtnetworks.totems.common.dto.commandline.CommandlineDTO;
import com.abtnetworks.totems.common.dto.commandline.PolicyObjectDTO;
import com.abtnetworks.totems.common.dto.commandline.PolicyRecommendSecurityPolicyDTO;
import com.abtnetworks.totems.common.dto.commandline.ServiceDTO;
import com.abtnetworks.totems.common.enums.ActionEnum;
import com.abtnetworks.totems.common.enums.MoveSeatEnum;
import com.abtnetworks.totems.common.utils.IdGen;
import com.abtnetworks.totems.common.utils.IpUtils;
import com.abtnetworks.totems.common.utils.PortUtils;
import com.abtnetworks.totems.common.utils.ProtocolUtils;
import com.abtnetworks.totems.common.utils.TimeUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author zc
 * @date 2020/01/09
 */
@Slf4j
@Service
public class SecurityVenustechVSOSV263 extends SecurityPolicyGenerator implements PolicyGenerator {

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

        dto.setCurrentId(settingDTO.getPolicyId());
        dto.setMoveSeatEnum(MoveSeatEnum.BEFORE);
        dto.setSwapRuleNameId(settingDTO.getSwapNameId());
        return composite(dto);
    }

    @Override
    public String generatePreCommandline(CommandlineDTO dto) {
        return "enable\nconfigure terminal\n";
    }

    @Override
    public String generateCommandline(CommandlineDTO dto) {
        return createCommandLine(dto);
    }

    private String createCommandLine(CommandlineDTO dto) {
        StringBuilder sb = new StringBuilder();

        List<PolicyObjectDTO> srcAddressList = generateAddressObject(dto.getSrcIp(), dto.getBusinessName(), dto.getSrcAddressName());
        List<PolicyObjectDTO> dstAddressList = generateAddressObject(dto.getDstIp(), dto.getBusinessName(), dto.getDstAddressName());
        List<PolicyObjectDTO> serviceList = generateServiceObject(dto.getServiceList(), dto.getServiceName());
        List<PolicyObjectDTO> timeList = generateTimeObject(dto.getStartTime(), dto.getEndTime(), dto.getBusinessName());

        srcAddressList.forEach(policyObject -> sb.append(StringUtils.defaultString(policyObject.getCommandLine())));
        dstAddressList.forEach(policyObject -> sb.append(StringUtils.defaultString(policyObject.getCommandLine())));
        serviceList.forEach(policyObject -> sb.append(StringUtils.defaultString(policyObject.getCommandLine())));
        timeList.forEach(policyObject -> sb.append(StringUtils.defaultString(policyObject.getCommandLine())));

        String srcZone = "any";
        String dstZone = "any";
        String srcIpObject = "any";
        String dstIpObject = "any";
        String serviceObject = "any";
        String timeObject = "always";
        if (StringUtils.isNotEmpty(dto.getSrcZone())) {
            srcZone = dto.getSrcZone();
        }
        if (StringUtils.isNotEmpty(dto.getDstZone())) {
            dstZone = dto.getDstZone();
        }
        if (srcAddressList.size() == 1) {
            srcIpObject = srcAddressList.get(0).getName();
        }
        if (dstAddressList.size() == 1) {
            dstIpObject = dstAddressList.get(0).getName();
        }
        if (serviceList.size() == 1) {
            serviceObject = serviceList.get(0).getName();
        }
        if (timeList.size() == 1) {
            timeObject = timeList.get(0).getName();
        }

        String action = dto.getAction();
        if (StringUtils.isEmpty(action)) {
            action = "permit";
        }

        String currentId = dto.getCurrentId();
        sb.append(String.format("firewall policy %s\nsrc-zone %s\ndst-zone %s\nsrc-addr %s\ndst-addr %s\nservice %s\ntimerange %s\naction %s\n",
                currentId, srcZone, dstZone, srcIpObject, dstIpObject, serviceObject, timeObject, action.toLowerCase()));
        if (StringUtils.isNotEmpty(dto.getDescription())) {
            sb.append(String.format("description %s\n", dto.getDescription()));
        }
        sb.append("enable\n");

        if(StringUtils.isNotEmpty(dto.getSwapRuleNameId())) {
            sb.append(String.format("firewall policy move %s before %s\n", currentId, dto.getSwapRuleNameId()));
        }
        sb.append("end\n");
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
            dto.setName(existsAddressName);
            return Collections.singletonList(dto);
        }

        if (StringUtils.isEmpty(ipAddress)) {
            return Collections.emptyList();
        }

        log.debug("转化为<ip类型:ip列表>的map");
        Map<String, List<Pair<String, String>>> typeIpListMap = Arrays.stream(ipAddress.split(","))
                .map(address -> {
                    String type;
                    if(IpUtils.isIPRange(address)) {
                        type = "ipv4Range";
                    } else if (IpUtils.isIPSegment(address)) {
                        type = "ipv4Subnet";
                    } else if (IpUtils.isIP(address)) {
                        type = "ipv4Host";
                    } else {
                        throw new IllegalArgumentException("未知的地址类型");
                    }
                    return Pair.of(type, address);
                })
                .collect(Collectors.groupingBy(pair -> (String) pair.getKey()));

        log.debug("生成地址对象列表和对象对应的命令行列表");
        PolicyObjectDTO dto = new PolicyObjectDTO();
        StringBuilder stringBuilder = new StringBuilder();
        String setName = String.format("%s_AO_%s", ticket, IdGen.getRandomNumberString());
        stringBuilder.append(String.format("address %s\n", setName));
        typeIpListMap.forEach((type, list) -> {
            if ("ipv4Host".equals(type)) {
                list.forEach(pair -> stringBuilder.append(String.format("host-address %s\n", pair.getValue())));
            } else if ("ipv4Range".equals(type)) {
                list.forEach(pair -> {
                    String address = (String) pair.getValue();
                    String startIp = IpUtils.getStartIpFromRange(address);
                    String endIp = IpUtils.getEndIpFromRange(address);
                    stringBuilder.append(String.format("range-address %s %s\n", startIp, endIp));
                });
            } else if ("ipv4Subnet".equals(type)) {
                list.forEach(pair -> stringBuilder.append(String.format("net-address %s\n", pair.getValue())));
            }
        });
        stringBuilder.append("exit\n");
        dto.setName(setName);
        dto.setCommandLine(stringBuilder.toString());
        return Collections.singletonList(dto);
    }

    /**
     * 生成服务对象
     * @param serviceList
     * @param existsServiceName
     * @return
     */
    private List<PolicyObjectDTO> generateServiceObject(List<ServiceDTO> serviceList, String existsServiceName) {
        PolicyObjectDTO dto = new PolicyObjectDTO();
        if (StringUtils.isNotBlank(existsServiceName)) {
            dto.setName(existsServiceName);
            return Collections.singletonList(dto);
        }

        boolean existAny = serviceList.stream()
                .anyMatch(serviceDTO -> StringUtils.equalsAnyIgnoreCase(getServiceName(serviceDTO),
                        PolicyConstants.POLICY_STR_VALUE_ANY,
                        PolicyConstants.POLICY_STR_VALUE_ICMP,
                        PolicyConstants.POLICY_STR_VALUE_TCP,
                        PolicyConstants.POLICY_STR_VALUE_UDP));

        StringBuilder stringBuilder = new StringBuilder();
        List<String> serviceObjectNames = new ArrayList<>();

        if (!existAny) {
            stringBuilder.append(String.format("service %s\n", getServiceName(serviceList)));
        }
        for (ServiceDTO serviceDTO : serviceList) {
            String setName = getServiceName(serviceDTO);
            if (StringUtils.equalsAnyIgnoreCase(setName, PolicyConstants.POLICY_STR_VALUE_ANY, PolicyConstants.POLICY_STR_VALUE_ICMP,
                    PolicyConstants.POLICY_STR_VALUE_TCP, PolicyConstants.POLICY_STR_VALUE_UDP)) {
                serviceObjectNames.add(setName);
            } else {
                if (existAny) {
                    stringBuilder.append(String.format("service %s\n", setName));
                }
                int protocolNum = Integer.valueOf(serviceDTO.getProtocol());
                String protocolString = ProtocolUtils.getProtocolByValue(protocolNum);
                switch (protocolString) {
                    case PolicyConstants.POLICY_STR_VALUE_ANY:
                        log.error("不支持协议为any的服务对象生成");
                        break;
                    case PolicyConstants.POLICY_STR_VALUE_ICMP:
                        if (StringUtils.isNoneEmpty(serviceDTO.getType(), serviceDTO.getCode())) {
                            stringBuilder.append(String.format("icmp %s %s\n", serviceDTO.getType(), serviceDTO.getCode()));
                        } else if (StringUtils.isNotEmpty(serviceDTO.getType())){
                            stringBuilder.append(String.format("icmp %s\n", serviceDTO.getType()));
                        }
                        break;
                    case PolicyConstants.POLICY_STR_VALUE_TCP:
                    case PolicyConstants.POLICY_STR_VALUE_UDP:
                        Arrays.stream(serviceDTO.getDstPorts().split(","))
                                .forEach(dstPort -> {
                                    if (PortUtils.isPortRange(dstPort)) {
                                        String startPort = PortUtils.getStartPort(dstPort);
                                        String endPort = PortUtils.getEndPort(dstPort);
                                        stringBuilder.append(String.format("%s dest %s %s\n", protocolString.toLowerCase(), startPort, endPort));
                                    } else {
                                        stringBuilder.append(String.format("%s dest %s\n", protocolString.toLowerCase(), dstPort));
                                    }
                                });
                        break;
                    default:
                        log.error("未知的协议类型[{}]",protocolString);
                        break;
                    }
                if (existAny) {
                    stringBuilder.append("exit\n");
                    serviceObjectNames.add(setName);
                }
            }
        }
        if (!existAny) {
            stringBuilder.append("exit\n");
        }

        if (serviceList.size() == 1 && existAny) {
            log.debug("一个协议，且为标准协议");
            dto.setName(serviceObjectNames.get(0));
            return Collections.singletonList(dto);
        } else if (serviceList.size() == 1) {
            log.debug("一个协议，不为标准协议");
            dto.setName(getServiceName(serviceList));
        } else if (existAny) {
            log.debug("多个协议，存在标准协议");
            String groupName = getServiceName(serviceList);
            stringBuilder.append(String.format("service-group %s\n",groupName));
            serviceObjectNames.forEach(name -> stringBuilder.append(String.format("service-object %s\n", name)));
            stringBuilder.append("exit\n");
            dto.setName(groupName);
        } else {
            log.debug("多个协议，不存在标准协议");
            dto.setName(getServiceName(serviceList));
        }
        dto.setCommandLine(stringBuilder.toString());
        return Collections.singletonList(dto);
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
            String command = String.format("schedule onetime %s\nabsolute %s %s\n", setName,
                    TimeUtils.transformDateFormat(startTimeString, TimeUtils.EUROPEAN_TIME_FORMAT, TimeUtils.VenustechVSOS_TIME_FORMAT),
                    TimeUtils.transformDateFormat(endTimeString, TimeUtils.EUROPEAN_TIME_FORMAT, TimeUtils.VenustechVSOS_TIME_FORMAT));
            command += "exit\n";
            policyObjectDTO.setName(setName);
            policyObjectDTO.setCommandLine(command);
            return Collections.singletonList(policyObjectDTO);
        } else {
            return Collections.emptyList();
        }
    }


    public String createMergeCommandLine(CommandlineDTO dto) {
        String mergeField = dto.getMergeDTO().getMergeField();
        List<String> restSrcAddressList = null;
        List<String> restDstAddressList =null;
        List<ServiceDTO> restServiceList =null;
        List<String> existSrcAddressList = new ArrayList<>();
        List<String> existDstAddressList = new ArrayList<>();
        List<String> existServiceNameList = new ArrayList<>();
        if(CollectionUtils.isNotEmpty( dto.getRestSrcAddressList())){
            restSrcAddressList = dto.getRestSrcAddressList();
        }
        if(CollectionUtils.isNotEmpty( dto.getRestDstAddressList())){
            restDstAddressList = dto.getRestDstAddressList();
        }
        if(CollectionUtils.isNotEmpty( dto.getRestServiceList())){
            restServiceList = dto.getRestServiceList();
        }
        if(CollectionUtils.isNotEmpty( dto.getExistSrcAddressList())){
            existSrcAddressList = dto.getExistSrcAddressList();
        }
        if(CollectionUtils.isNotEmpty( dto.getExistDstAddressList())){
            existDstAddressList = dto.getExistDstAddressList();
        }
        if(CollectionUtils.isNotEmpty( dto.getExistServiceNameList())){
            existServiceNameList = dto.getExistServiceNameList();
        }
        String name = dto.getName();
        log.info("开始编辑...");
        StringBuilder sb = new StringBuilder();
        PolicyObjectDTO srcAddressDTO = new PolicyObjectDTO();
        if (CollectionUtils.isNotEmpty(restSrcAddressList)) {
            srcAddressDTO = generateMerginAddrssObject(name, dto.getBusinessName(), existSrcAddressList, restSrcAddressList);
            sb.append(srcAddressDTO.getCommandLine());
        }
        PolicyObjectDTO dstAddressDTO = new PolicyObjectDTO();
        if (CollectionUtils.isNotEmpty(restDstAddressList)) {
            dstAddressDTO = generateMerginAddrssObject(name, dto.getBusinessName(), existDstAddressList, restDstAddressList);
            sb.append(dstAddressDTO.getCommandLine());
        }
        StringBuilder existServiceName = new StringBuilder();
        PolicyObjectDTO service = null;
        for (String serviceName : existServiceNameList) {
            existServiceName.append(serviceName);
        }
        service = generateMerginServiceObject(restServiceList, existServiceName.toString());
        if (StringUtils.isNotBlank(service.getCommandLine())) {
            sb.append(String.format("%s", service.getCommandLine()));
        }
        if (service.isObjectFlag()) {
            sb.append(String.format("%s", service.getJoin()));
        }
        PolicyRecommendSecurityPolicyDTO securityPolicy = dto.getSecurityPolicy();

        String mergePreCommandline = "";
        if (StringUtils.isNotEmpty(securityPolicy.getId())) {
            mergePreCommandline = String.format("firewall policy %s %s", securityPolicy.getRuleId(), CommonConstants.LINE_BREAK);
        }
        sb.append(mergePreCommandline);
        if (mergeField.equalsIgnoreCase(PolicyConstants.SRC) && StringUtils.isNotBlank(srcAddressDTO.getCommandLine())) {
            sb.append(String.format("address %s", srcAddressDTO.getName()));
            sb.append(CommonConstants.LINE_BREAK);
            sb.append("exit\n");
        }

        if (mergeField.equalsIgnoreCase(PolicyConstants.DST) && StringUtils.isNotBlank(dstAddressDTO.getCommandLine())) {
            sb.append(String.format("address %s", dstAddressDTO.getName()));
            sb.append(CommonConstants.LINE_BREAK);
            sb.append("exit\n");
        }
        if (mergeField.equalsIgnoreCase(PolicyConstants.SERVICE) && StringUtils.isNotBlank(service.getCommandLine())) {
            sb.append(String.format("service %s %s", service.getName(), CommonConstants.LINE_BREAK));
            sb.append("exit\n");
        }

//        if (mergeField.equalsIgnoreCase(PolicyConstants.SERVICE) && ObjectUtils.isNotEmpty(service)) {
//            if (StringUtils.isNotBlank(service.getCommandLine())) {
//                sb.append(service.getCommandLine());
//            }
//            sb.append("\n");
//        }
        // 记录创建对象的名称

        sb.append("end");
        return sb.toString();
    }

    private PolicyObjectDTO generateMerginServiceObject(List<ServiceDTO> serviceList, String existsServiceName) {
        PolicyObjectDTO dto = new PolicyObjectDTO();
        if (CollectionUtils.isEmpty(serviceList) && StringUtils.isBlank(existsServiceName)) {
            return dto;
        }
        if (StringUtils.isNotEmpty(existsServiceName) && CollectionUtils.isEmpty(serviceList)) {
            dto.setObjectFlag(true);
            dto.setJoin("service " + existsServiceName + "\nexit\n");
            return dto;
        }
        boolean existAny = serviceList.stream()
                .anyMatch(serviceDTO -> StringUtils.equalsAnyIgnoreCase(getServiceName(serviceDTO),
                        PolicyConstants.POLICY_STR_VALUE_ANY,
                        PolicyConstants.POLICY_STR_VALUE_ICMP,
                        PolicyConstants.POLICY_STR_VALUE_TCP,
                        PolicyConstants.POLICY_STR_VALUE_UDP));

        StringBuilder stringBuilder = new StringBuilder();
        List<String> serviceObjectNames = new ArrayList<>();

        if (!existAny) {
            stringBuilder.append(String.format("service %s\n", getServiceName(serviceList)));
        }
        for (ServiceDTO serviceDTO : serviceList) {
            String setName = getServiceName(serviceDTO);
            if (StringUtils.equalsAnyIgnoreCase(setName, PolicyConstants.POLICY_STR_VALUE_ANY, PolicyConstants.POLICY_STR_VALUE_ICMP,
                    PolicyConstants.POLICY_STR_VALUE_TCP, PolicyConstants.POLICY_STR_VALUE_UDP)) {
                serviceObjectNames.add(setName);
            } else {
                if (existAny) {
                    stringBuilder.append(String.format("service %s\n", setName));
                }
                int protocolNum = Integer.valueOf(serviceDTO.getProtocol());
                String protocolString = ProtocolUtils.getProtocolByValue(protocolNum);
                switch (protocolString) {
                    case PolicyConstants.POLICY_STR_VALUE_ANY:
                        log.error("不支持协议为any的服务对象生成");
                        break;
                    case PolicyConstants.POLICY_STR_VALUE_ICMP:
                        if (StringUtils.isNoneEmpty(serviceDTO.getType(), serviceDTO.getCode())) {
                            stringBuilder.append(String.format("icmp %s %s\n", serviceDTO.getType(), serviceDTO.getCode()));
                        } else if (StringUtils.isNotEmpty(serviceDTO.getType())){
                            stringBuilder.append(String.format("icmp %s\n", serviceDTO.getType()));
                        }
                        break;
                    case PolicyConstants.POLICY_STR_VALUE_TCP:
                    case PolicyConstants.POLICY_STR_VALUE_UDP:
                        Arrays.stream(serviceDTO.getDstPorts().split(","))
                                .forEach(dstPort -> {
                                    if (PortUtils.isPortRange(dstPort)) {
                                        String startPort = PortUtils.getStartPort(dstPort);
                                        String endPort = PortUtils.getEndPort(dstPort);
                                        stringBuilder.append(String.format("%s dest %s %s source 1 65535\n", protocolString.toLowerCase(), startPort, endPort));
                                    } else {
                                        stringBuilder.append(String.format("%s dest %s source 1 65535\n", protocolString.toLowerCase(), dstPort));
                                    }
                                });
                        break;
                    default:
                        log.error("未知的协议类型[{}]",protocolString);
                        break;
                }
                if (existAny) {
                    stringBuilder.append("exit\n");
                    serviceObjectNames.add(setName);
                }
            }
        }
        if (!existAny) {
            stringBuilder.append("exit\n");
        }

        if (serviceList.size() == 1 && existAny) {
            log.debug("一个协议，且为标准协议");
            dto.setName(serviceObjectNames.get(0));
            return dto;
        } else if (serviceList.size() == 1) {
            log.debug("一个协议，不为标准协议");
            dto.setName(getServiceName(serviceList));
        } else if (existAny) {
            log.debug("多个协议，存在标准协议");
            String groupName = getServiceName(serviceList);
            stringBuilder.append(String.format("service-group %s\n",groupName));
            serviceObjectNames.forEach(name -> stringBuilder.append(String.format("service-object %s\n", name)));
            stringBuilder.append("exit\n");
            dto.setName(groupName);
        } else {
            log.debug("多个协议，不存在标准协议");
            dto.setName(getServiceName(serviceList));
        }
        dto.setCommandLine(stringBuilder.toString());
        return dto;
    }

    private PolicyObjectDTO generateMerginAddrssObject(String name, String ticket,
                                                       List<String> existSrcAddressList, List<String> restSrcAddressList) {
        PolicyObjectDTO dto = new PolicyObjectDTO();
        if (CollectionUtils.isNotEmpty(existSrcAddressList)) {
            for (String existSrcAddress : existSrcAddressList) {
                dto.setName(existSrcAddress);
            }
            return dto;
        }

        if (StringUtils.isEmpty(name)) {
            return dto;
        }
        StringBuilder sb = new StringBuilder();
        String setName = String.format("%s_AO_%s", ticket, IdGen.getRandomNumberString());
        sb.append(String.format("address "));
        sb.append(String.format("%s ", setName));
        sb.append("\n");
        for (String addressStr : restSrcAddressList) {
            if (addressStr.contains(",")) {
                String[] ipAddressArr = addressStr.split(",");
                for (String address : ipAddressArr) {
                    if (IpUtils.isIPRange(address)) {
                        String startIp = IpUtils.getStartIpFromRange(address);
                        String endIp = IpUtils.getEndIpFromRange(address);
                        sb.append(String.format("range-address %s %s\n", startIp, endIp));
                    } else if (IpUtils.isIPSegment(address)) {
                        sb.append("net-address ").append(address).append(" \n");
                    } else if (IpUtils.isIP(address)) {
                        sb.append("host-address ").append(address).append(" \n");
                    }
                }
            } else {
                if (IpUtils.isIPRange(addressStr)) {
                    sb.append("range-address ").append(addressStr).append(" \n");
                } else if (IpUtils.isIPSegment(addressStr)) {
                    sb.append("net-address ").append(addressStr).append(" \n");
                } else if (IpUtils.isIP(addressStr)) {
                    sb.append("host-address ").append(addressStr).append("\n");
                }
            }
        }


        sb.append("exit\n");
        dto.setName(setName);
        dto.setCommandLine(sb.toString());
        return dto;
    }

    public static void main(String[] args) {
        SecurityVenustechVSOSV263 securityVenustechVSOS = new SecurityVenustechVSOSV263();
        CommandlineDTO dto = CommandlineDTO.getInstanceDemo();
        dto.setSrcIp("1.2.3.3");
        dto.setDstIp("15.3.2.5");
        String commandLine = securityVenustechVSOS.composite(dto);
        System.out.println("commandline:\n" + commandLine);
    }

}
