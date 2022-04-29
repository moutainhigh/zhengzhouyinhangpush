package com.abtnetworks.totems.common.commandline.acl;

import com.abtnetworks.totems.command.line.dto.IpAddressSubnetIntDTO;
import com.abtnetworks.totems.command.line.enums.StatusTypeEnum;
import com.abtnetworks.totems.common.commandline.PolicyGenerator;
import com.abtnetworks.totems.common.commandline.SecurityPolicyGenerator;
import com.abtnetworks.totems.common.constants.PolicyConstants;
import com.abtnetworks.totems.common.dto.*;
import com.abtnetworks.totems.common.dto.commandline.CommandlineDTO;
import com.abtnetworks.totems.common.dto.commandline.PolicyObjectDTO;
import com.abtnetworks.totems.common.dto.commandline.ServiceDTO;
import com.abtnetworks.totems.common.enums.ActionEnum;
import com.abtnetworks.totems.common.utils.*;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Log4j2
public class AclJuniper extends SecurityPolicyGenerator implements PolicyGenerator {
    @Override
    public String generate(CmdDTO cmdDTO) {
        log.info("Juniper_ACL cmdDTO is " + JSONObject.toJSONString(cmdDTO, true));
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

        TaskDTO taskDTO = cmdDTO.getTask();
        dto.setName(taskDTO.getTheme());
        dto.setBusinessName(taskDTO.getTheme());
        // juniper acl 默认不创建对象
        dto.setCreateObjFlag(false);
        dto.setOutBound(settingDTO.isOutBound());

        ExistObjectDTO existObjectDTO = cmdDTO.getExistObject();
        dto.setSrcAddressName(existObjectDTO.getSrcAddressObjectName());
        dto.setDstAddressName(existObjectDTO.getDstAddressObjectName());
        dto.setServiceName(existObjectDTO.getServiceObjectName());
        dto.setExistServiceNameList(existObjectDTO.getExistServiceNameList());
        dto.setRestServiceList(existObjectDTO.getRestServiceList());

        dto.setSpecialExistObject(cmdDTO.getSpecialExistObject());
        log.info("Juniper ACL dto is" + JSONObject.toJSONString(dto, true));

        String commandLine = composite(dto);

        GeneratedObjectDTO generatedDto = cmdDTO.getGeneratedObject();

        generatedDto.setPolicyName(commandLine);
        generatedDto.setRollbackCommandLine(dto.getGeneratedDto().getRollbackCommandLine());
        return commandLine;
    }

    @Override
    public String generatePreCommandline(CommandlineDTO dto) {
        return "configure\n";
    }

    @Override
    public String generateCommandline(CommandlineDTO dto) {
        if (dto.getMergeDTO() == null || dto.isMustCreateFlag()) {
            return createCommandLine(dto);
        } else {
            return editCommandLine(dto);
        }
    }

    private String editCommandLine(CommandlineDTO dto) {
        return null;
    }

    public static CommandlineDTO getInstanceDemo() {
        CommandlineDTO dto = new CommandlineDTO();
        dto.setSrcIp("12.1.1.2-12.1.1.5");
        dto.setDstIp("13.1.1.2-13.1.1.5");
        List<ServiceDTO> list = new ArrayList<>();
        ServiceDTO serviceDTO = new ServiceDTO();
        /*
            ANY(0,"any"),
            ICMP(1, "icmp"),
            TCP(6, "tcp"),
            UDP(17, "udp"),
            ICMP6(58, "icmp6"),
            PROTOCOL(200, "protocol")
         */
        serviceDTO.setProtocol("6");
        serviceDTO.setDstPorts("56,99");
        list.add(serviceDTO);
        dto.setServiceList(list);
        dto.setSrcZone("any");
        dto.setDstZone("any");
        dto.setName("juniper_acl");
        dto.setRuleListName("re-pr");
        dto.setAction(ActionEnum.PERMIT.getKey());
//        dto.setSrcAddressName("源地址对象");
//        dto.setDstAddressName("目的地址对象");
        dto.setAddressType(false);
        return dto;
    }

    public static void main(String[] args) {
        AclJuniper juniper = new AclJuniper();
        String commandLine = juniper.createCommandLine(getInstanceDemo());
        System.out.println(commandLine);
    }

    public String createCommandLine(CommandlineDTO dto) {
        log.info("创建JUNIPER ACL命令行.....");
        StringBuilder sb = new StringBuilder();
        StringBuilder rollSb = new StringBuilder();
        String ticket = dto.getName();
        String srcIp = dto.getSrcIp();
        String dstIp = dto.getDstIp();
        String srcZone = StringUtils.isNotBlank(dto.getSrcZone()) ? dto.getSrcZone() : "";
        String dstZone = StringUtils.isNotBlank(dto.getDstZone()) ? dto.getDstZone() : "";
        List<String> existServiceNameList = dto.getExistServiceNameList();
        List<ServiceDTO> restServiceList = dto.getRestServiceList();
        boolean addressType = dto.getAddressType();
        String ruleListName = dto.getRuleListName();

        String ticketId = ticket + "_" + IdGen.getRandomNumberString(4);
        String ruleListNameId = ruleListName + "_" + IdGen.getRandomNumberString(4);
        // 不选接口的时候，新建策略集
        if (StringUtils.isBlank(dto.getSrcItf()) && StringUtils.isBlank(dto.getDstItf())) {
            ruleListName = "";
            ruleListNameId = "";
        }

        PolicyObjectDTO srcAddress = generateSrcAddressObject(srcIp, srcZone, ruleListName, ruleListNameId, ticket, ticketId, dto.getSrcAddressName(), dto.getSrcIpSystem(), addressType);
        PolicyObjectDTO dstAddress = generateDstAddressObject(dstIp, dstZone, ruleListName, ruleListNameId, ticket, ticketId, dto.getDstAddressName(), dto.getDstIpSystem(), addressType);
        PolicyObjectDTO service = generateServiceObject(dto.getServiceList(), ruleListName, ruleListNameId, ticket, ticketId, existServiceNameList, restServiceList);
        // 地址和服务对象不能共用公共方法，原因是有多个参数需要拼接
        recordObjectName(dto, srcAddress, dstAddress, service);

        if (srcAddress.isObjectFlag() && StringUtils.isNotBlank(srcAddress.getCommandLine())) {
            sb.append(String.format("%s\n", srcAddress.getCommandLine()));
            rollSb.append(srcAddress.getJoinRollbackParam());
        }
        if (dstAddress.isObjectFlag() && StringUtils.isNotBlank(dstAddress.getCommandLine())) {
            sb.append(String.format("%s\n", dstAddress.getCommandLine()));
            rollSb.append(dstAddress.getJoinRollbackParam());
        }

        if (StringUtils.isBlank(service.getCommandLine()) && StringUtils.isNotBlank(service.getJoin())) {
            if (StringUtils.isNotBlank(ruleListName)) {
                sb.append(String.format("set firewall family inet filter %s term %s from protocol %s \n", ruleListName, ruleListNameId, service.getJoin()));
                rollSb.append(String.format("delete firewall family inet filter %s term %s from protocol %s \n", ruleListName, ruleListNameId, service.getJoin()));
            } else {
                sb.append(String.format("set firewall family inet filter %s term %s from protocol %s \n", ticket, ticketId, service.getJoin()));
                rollSb.append(String.format("delete firewall family inet filter %s term %s from protocol %s \n", ticket, ticketId, service.getJoin()));
            }
        }
        if (service.isObjectFlag() && StringUtils.isNotBlank(service.getCommandLine())) {
            if (dto.getIdleTimeout() != null) {
                String serviceCommandLine = service.getCommandLine();
                if (service.getCommandLine().substring(service.getCommandLine().length() - 2, service.getCommandLine().length()).contains("\n")) {
                    serviceCommandLine = service.getCommandLine().substring(0, service.getCommandLine().length() - 2);
                }
                sb.append(String.format("%s inactivity-timeout %d \n", serviceCommandLine, dto.getIdleTimeout()));
            } else {
                log.info("服务对象命令行");
                sb.append(String.format("%s \n", service.getCommandLine()));
                rollSb.append(service.getJoinRollbackParam());
            }
        }
        if (dto.getAction().equalsIgnoreCase(ActionEnum.PERMIT.getKey())) {
            // set firewall family inet filter test3 term 10 then accept
            if (StringUtils.isNotBlank(ruleListName)) {
                sb.append(String.format("set firewall family inet filter %s term %s then %s \n", ruleListName, ruleListNameId, "accept"));
                rollSb.append(String.format("delete firewall family inet filter %s term %s then %s \n", ruleListName, ruleListNameId, "accept"));
            } else {
                sb.append(String.format("set firewall family inet filter %s term %s then %s \n", ticket, ticketId, "accept"));
                rollSb.append(String.format("delete firewall family inet filter %s term %s then %s \n", ticket, ticketId, "accept"));

            }
        } else {
            if (StringUtils.isNotBlank(ruleListName)) {
                sb.append(String.format("set firewall family inet filter %s term %s then %s \n", ruleListName, ruleListNameId, "discard"));
                rollSb.append(String.format("delete firewall family inet filter %s term %s then %s \n", ruleListName, ruleListNameId, "discard"));
            } else {
                sb.append(String.format("set firewall family inet filter %s term %s then %s \n", ticket, ticketId, "discard"));
                rollSb.append(String.format("delete firewall family inet filter %s term %s then %s \n", ticket, ticketId, "discard"));
            }
        }
        if (StringUtils.isNotBlank(rollSb.toString())) {

            GeneratedObjectDTO generatedDto = dto.getGeneratedDto();
            GeneratedObjectDTO aclGeneratedDto = new GeneratedObjectDTO();
            if (ObjectUtils.isNotEmpty(generatedDto)) {
                BeanUtils.copyProperties(generatedDto, aclGeneratedDto);
            }
            aclGeneratedDto.setRollbackCommandLine(rollSb.toString());
            dto.setGeneratedDto(aclGeneratedDto);
        }
        log.info("生成的Juniper Acl命令行为：{}\n", sb.toString());
        return sb.toString();
    }

    private void recordObjectName(CommandlineDTO dto, PolicyObjectDTO srcAddress, PolicyObjectDTO dstAddress, PolicyObjectDTO service) {
        List<String> addressGroupNames = new ArrayList<>();
        List<String> addressNames = new ArrayList<>();
        List<String> serviceGroupNames = new ArrayList<>();
        List<String> serviceNames = new ArrayList<>();
        if (null != srcAddress && srcAddress.isGroup() && srcAddress.isObjectFlag() && StringUtils.isNotBlank(srcAddress.getJoinRollbackParam())) {
            addressGroupNames.add(srcAddress.getJoinRollbackParam());
            dto.setAddressObjectGroupNameList(addressGroupNames);
        } else if (null != srcAddress && srcAddress.isObjectFlag() && StringUtils.isNotBlank(srcAddress.getJoinRollbackParam())) {
            addressNames.add(srcAddress.getJoinRollbackParam());
            dto.setAddressObjectNameList(addressNames);
        }

        if (null != dstAddress && dstAddress.isGroup() && dstAddress.isObjectFlag() && StringUtils.isNotBlank(dstAddress.getJoinRollbackParam())) {
            addressGroupNames.add(dstAddress.getJoinRollbackParam());
            dto.setAddressObjectGroupNameList(addressGroupNames);
        } else if (null != dstAddress && dstAddress.isObjectFlag() && StringUtils.isNotBlank(dstAddress.getJoinRollbackParam())) {
            addressNames.add(dstAddress.getJoinRollbackParam());
            dto.setAddressObjectNameList(addressNames);
        }

        if (null != service && service.isGroup() && service.isObjectFlag() && StringUtils.isNotBlank(service.getName())) {
            serviceGroupNames.add(service.getName());
            dto.setServiceObjectGroupNameList(serviceGroupNames);
        } else if (null != service && service.isObjectFlag() && StringUtils.isNotBlank(service.getName())) {
            serviceNames.add(service.getName());
            dto.setServiceObjectNameList(serviceNames);
        }
    }


    private PolicyObjectDTO generateServiceObject(List<ServiceDTO> serviceDTOList, String ruleListName, String ruleListNameId, String ticket, String ticketId, List<String> existServiceNameList, List<ServiceDTO> restServiceList) {
        PolicyObjectDTO dto = new PolicyObjectDTO();
        if ((serviceDTOList == null || serviceDTOList.isEmpty()) && CollectionUtils.isEmpty(existServiceNameList)) {
            return dto;
        }
        StringBuilder sb = new StringBuilder();
        StringBuilder rollBackSb = new StringBuilder();
        //对象名称集合
        List<String> serviceNameList = new ArrayList<>();

        List<ServiceDTO> serviceDTOLists = new ArrayList<>();
        for (ServiceDTO dtoList : serviceDTOList) {
            serviceDTOLists.add(dtoList);
        }
        if (CollectionUtils.isNotEmpty(existServiceNameList)) {
            for (String s : existServiceNameList) {
                serviceNameList.add(s);
            }
            serviceDTOList.clear();
            for (ServiceDTO serviceDTO : restServiceList) {
                serviceDTOList.add(serviceDTO);
            }
        }

        for (ServiceDTO service : serviceDTOList) {
            int protocolNum = Integer.valueOf(service.getProtocol());
            String protocolString = ProtocolUtils.getProtocolByValue(protocolNum).toLowerCase();
            if (protocolString.equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_ANY)) {
                dto.setCommandLine("");
                dto.setJoin("");
                return dto;
            }

            //只有1个服务，且端口是any ，或icmp type是空
            if (serviceDTOList.size() == 1) {
                if (protocolString.equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_ICMP)) {
                    //icmp协议，icmpType和icmpCode都为空
                    if (StringUtils.isBlank(service.getType()) && StringUtils.isBlank(service.getCode())) {
                        dto.setJoin("icmp");
                        dto.setName(dto.getJoin());
                        return dto;
                    }
                } else if (protocolString.equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_ICMPV6)) {
                    //icmpv6协议，icmpType和icmpCode都为空
                    if (StringUtils.isBlank(service.getType()) && StringUtils.isBlank(service.getCode())) {
                        dto.setJoin("icmp6");
                        dto.setName(dto.getJoin());
                        return dto;
                    }
                }
            }
            //多个服务建对象
            if (protocolString.equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_ICMP)) {
                String objName = PolicyConstants.POLICY_STR_VALUE_ICMP.toLowerCase();
                if (StringUtils.isEmpty(ruleListName)) {
                    sb.append(String.format("set firewall family inet filter %s term %s from protocol %s %n", ticket, ticketId, objName));
                    rollBackSb.append(String.format("delete firewall family inet filter %s term %s from protocol %s %n", ticket, ticketId, objName));
                } else {
                    sb.append(String.format("set firewall family inet filter %s term %s from protocol %s %n", ruleListName, ruleListNameId, objName));
                    rollBackSb.append(String.format("delete firewall family inet filter %s term %s from protocol %s %n", ruleListName, ruleListNameId, objName));
                }
                serviceNameList.add(objName);
            }
            if (protocolString.equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_ICMPV6)) {
                String objName = "";
                //只有ICMPV6，则不用建对象，直接添加到组
                if (StringUtils.isBlank(service.getType()) && StringUtils.isBlank(service.getCode())) {
                    objName = "icmp6";
                } else {
                    objName = PolicyConstants.POLICY_STR_VALUE_ICMP.toLowerCase();
                    if (StringUtils.isNotBlank(ruleListName)) {
                        sb.append(String.format("set firewall family inet filter %s term %s from protocol %s %s", ruleListName, ruleListNameId, objName, StringUtils.LF));
                        sb.append(String.format("set firewall family inet filter %s term %s from destination-port %s", ruleListName, ruleListNameId, StringUtils.LF));
                        rollBackSb.append(String.format("delete firewall family inet filter %s term %s from protocol %s %s", ruleListName, ruleListNameId, objName, StringUtils.LF));
                        rollBackSb.append(String.format("delete firewall family inet filter %s term %s from destination-port %s", ruleListName, ruleListNameId, StringUtils.LF));

                    } else {
                        sb.append(String.format("set firewall family inet filter %s term %s from protocol %s %s", ticket, ticketId, objName, StringUtils.LF));
                        sb.append(String.format("set firewall family inet filter %s term %s from destination-port 58 %s", ticket, ticketId, StringUtils.LF));
                        rollBackSb.append(String.format("delete firewall family inet filter %s term %s from protocol %s %s", ticket, ticketId, objName, StringUtils.LF));
                        rollBackSb.append(String.format("delete firewall family inet filter %s term %s from destination-port 58 %s", ticket, ticketId, StringUtils.LF));
                    }
                }
                serviceNameList.add(objName);
            }
            if (protocolString.equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_TCP) ||
                    protocolString.equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_UDP)) {
                //tcp、udp协议， 但是端口是any，则直接添加到组即可，不用建对象
                if (service.getDstPorts().equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_ANY)) {
                    sb.append(String.format("set firewall family inet filter %s term %s from protocol %s %s", ruleListName, ruleListNameId, protocolString, StringUtils.LF));
                    rollBackSb.append(String.format("delete firewall family inet filter %s term %s from protocol %s %s", ruleListName, ruleListNameId, protocolString, StringUtils.LF));
                    continue;
                }

                String[] dstPorts = service.getDstPorts().split(",");
                if (StringUtils.isNotBlank(ruleListName)) {
                    sb.append(String.format("set firewall family inet filter %s term %s from protocol %s %s", ruleListName, ruleListNameId, protocolString, StringUtils.LF));
                    rollBackSb.append(String.format("delete firewall family inet filter %s term %s from protocol %s %s", ruleListName, ruleListNameId, protocolString, StringUtils.LF));

                } else {
                    sb.append(String.format("set firewall family inet filter %s term %s from protocol %s %s", ticket, ticketId, protocolString, StringUtils.LF));
                    rollBackSb.append(String.format("delete firewall family inet filter %s term %s from protocol %s %s", ticket, ticketId, protocolString, StringUtils.LF));
                }
                for (String dstPortStr : dstPorts) {

                    String objName = getServiceNameByOne(protocolString, dstPortStr);
                    if (StringUtils.isNotBlank(ruleListName)) {
                        if (!dstPortStr.equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_ANY)) {
                            String dstPortString = PortUtils.getPortString(dstPortStr, PortUtils.DASH_FORMAT);
                            sb.append(String.format("set firewall family inet filter %s term %s from destination-port %s %s", ruleListName, ruleListNameId, dstPortString, StringUtils.LF));
                            rollBackSb.append(String.format("delete firewall family inet filter %s term %s from destination-port %s %s", ruleListName, ruleListNameId, dstPortString, StringUtils.LF));
                        }
                    } else {
                        if (!dstPortStr.equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_ANY)) {
                            String dstPortString = PortUtils.getPortString(dstPortStr, PortUtils.DASH_FORMAT);
                            sb.append(String.format("set firewall family inet filter %s term %s from destination-port %s %s", ticket, ticketId, dstPortString, StringUtils.LF));
                            rollBackSb.append(String.format("delete firewall family inet filter %s term %s from destination-port %s %s", ticket, ticketId, dstPortString, StringUtils.LF));
                        }
                    }
                    serviceNameList.add(objName);
                }
            }
        }
        dto.setCommandLine(sb.toString());
        dto.setJoinRollbackParam(rollBackSb.toString());
        dto.setObjectFlag(true);
        return dto;
    }

    private PolicyObjectDTO generateSrcAddressObject(String ipAddress, String zone, String ruleListName, String ruleListNameId, String ticket, String ticketId, String existsAddressName, String srcIpSystem, boolean addressType) {
        PolicyObjectDTO dto = new PolicyObjectDTO();
        try {
            dto.setObjectFlag(true);

            if (AliStringUtils.isEmpty(ipAddress)) {
                dto.setJoin("any");
                return dto;
            }

            if (StringUtils.isNotBlank(existsAddressName)) {
                String prefix = zone + "_zone_";
                if (existsAddressName.startsWith(prefix)) {
                    String name = existsAddressName.replaceFirst(prefix, "");
                    dto.setJoin(name);
                    return dto;
                } else {
                    dto.setJoin(existsAddressName);
                    return dto;
                }
            }

            StringBuilder sb = new StringBuilder();
            StringBuilder rollBackSb = new StringBuilder();
            String[] arr = ipAddress.split(",");
            String groupName = String.format("%s_AG_%s", ticket, IdGen.getRandomNumberString());
            String name = "";
            int index = 0;
            int indexIpv6 = 0;
            String joinRollbackIp = "";
            if (addressType) {
                for (String address : arr) {
                    joinRollbackIp = address;
                    if (IPUtil.isIPRange(address) || IPUtil.isIPv6Range(address)) {
                        //判断是范围
                        if (IPUtil.isIPv6Range(address)) {
                            List<String> toSubnetList = IP6Utils.convertRangeToSubnet(address);
                            for (String subIpv6 : toSubnetList) {
                                // set firewall family inet filter test2 term 20 from source-address 10.12.13.1/32
                                if (StringUtils.isNotBlank(ruleListName)) {
                                    sb.append(String.format("set firewall family inet filter %s term %s from source-address %s \n", ruleListName, ruleListNameId, subIpv6));
                                    rollBackSb.append(String.format("delete firewall family inet filter %s term %s from source-address %s \n", ruleListName, ruleListNameId, subIpv6));
                                } else {
                                    sb.append(String.format("set firewall family inet filter %s term %s from source-address %s \n", ticket, ticketId, subIpv6));
                                    rollBackSb.append(String.format("delete firewall family inet filter %s term %s from source-address %s \n", ticket, ticketId, subIpv6));
                                }
                                indexIpv6++;
                            }
                        } else {
                            String convertRangeToSegment = IpUtils.convertIpRangeToSegment(address);
                            String[] segmentList = convertRangeToSegment.split(",");
                            for (String rangeToSegment : segmentList) {
                                if (StringUtils.isNotBlank(ruleListName)) {
                                    sb.append(String.format("set firewall family inet filter %s term %s from source-address %s \n", ruleListName, ruleListNameId, rangeToSegment));
                                    rollBackSb.append(String.format("delete firewall family inet filter %s term %s from source-address %s \n", ruleListName, ruleListNameId, rangeToSegment));
                                } else {
                                    sb.append(String.format("set firewall family inet filter %s term %s from source-address %s \n", ticket, ticketId, rangeToSegment));
                                    rollBackSb.append(String.format("delete firewall family inet filter %s term %s from source-address %s \n", ticket, ticketId, rangeToSegment));
                                }
                            }

                        }
                    } else if (IPUtil.isIP(address) || IPUtil.isIPv6(address)) {
                        //判断是单IP
                        if (IPUtil.isIPv6(address)) {
                            address = address + "/128";
                            if (StringUtils.isNotBlank(ruleListName)) {
                                String addressObjectString = String.format("set firewall family inet filter %s term %s from source-address %s \n", ruleListName, ruleListNameId, address);
                                sb.append(addressObjectString);
                                rollBackSb.append(String.format("delete firewall family inet filter %s term %s from source-address %s \n", ruleListName, ruleListNameId, address));
                            } else {
                                String addressObjectString = String.format("set firewall family inet filter %s term %s from source-address %s \n", ticket, ticketId, address);
                                sb.append(addressObjectString);
                                rollBackSb.append(String.format("delete firewall family inet filter %s term %s from source-address %s \n", ticket, ticketId, address));
                            }
                            // 单ip新建对象的时候拼子网掩码128,回滚的时候应该也带上
                            joinRollbackIp = address;
                        } else {
                            address = address + "/32";
                            if (StringUtils.isNotBlank(ruleListName)) {
                                String addressObjectString = String.format("set firewall family inet filter %s term %s from source-address %s \n", ruleListName, ruleListNameId, address);
                                sb.append(addressObjectString);
                                String rollAddressObjectString = String.format("delete firewall family inet filter %s term %s from source-address %s \n", ruleListName, ruleListNameId, address);
                                rollBackSb.append(rollAddressObjectString);
                            } else {
                                String addressObjectString = String.format("set firewall family inet filter %s term %s from source-address %s \n", ticket, ticketId, address);
                                sb.append(addressObjectString);
                                String rollAddressObjectString = String.format("delete firewall family inet filter %s term %s from source-address %s \n", ticket, ticketId, address);
                                rollBackSb.append(rollAddressObjectString);
                            }
                        }
                    } else if (IPUtil.isIPSegment(address) || IPUtil.isIPv6Segment(address)) {
                        String[] array = StringUtils.split(address, "/");
                        if (IPUtil.isIPSegment(address)) {
                            IpAddressSubnetIntDTO ipAddressSubnetIntDTO = new IpAddressSubnetIntDTO();
                            ipAddressSubnetIntDTO.setIp(array[0]);
                            ipAddressSubnetIntDTO.setMask(Integer.parseInt(array[1]));
                            String ip6SrcCommand = generateSrcSubnetIntIpV6CommandLine(StatusTypeEnum.ADD, new IpAddressSubnetIntDTO[]{ipAddressSubnetIntDTO}, new String[]{ticket, ticketId, ruleListName, ruleListNameId});
                            if (StringUtils.isNotBlank(ip6SrcCommand)) {
                                String[] split = ip6SrcCommand.split("@@@");
                                sb.append(split[0]);
                                rollBackSb.append(split[1]);
                            }
                        } else {
                            // set firewall family inet filter test3 term 10 from source-address
                            if (StringUtils.isNotBlank(ruleListName)) {
                                String addressObjectString = String.format("set firewall family inet filter %s term %s from source-address %s\n", ruleListName, ruleListNameId, address);
                                sb.append(addressObjectString);
                                String rollAddressObjectString = String.format("delete firewall family inet filter %s term %s from source-address %s\n", ruleListName, ruleListNameId, address);
                                rollBackSb.append(rollAddressObjectString);
                            } else {
                                String addressObjectString = String.format("set firewall family inet filter %s term %s from source-address %s\n", ticket, ticketId, address);
                                sb.append(addressObjectString);
                                String rollAddressObjectString = String.format("delete firewall family inet filter %s term %s from source-address %s\n", ticket, ticketId, address);
                                rollBackSb.append(rollAddressObjectString);
                            }
                        }
                    }
                    index++;
                }
            } else {
                // 全局对象
                for (String address : arr) {
                    joinRollbackIp = address;
                    if (IPUtil.isIPRange(address) || IPUtil.isIPv6Range(address)) {
                        //判断是范围
                        if (IPUtil.isIPv6Range(address)) {
                            List<String> toSubnetList = IP6Utils.convertRangeToSubnet(address);
                            for (String subIpv6 : toSubnetList) {
                                // set firewall family inet filter test2 term 20 from source-address 10.12.13.1/32
                                if (StringUtils.isNotBlank(ruleListName)) {
                                    sb.append(String.format("set firewall family inet filter %s term %s from source-address %s \n", ruleListName, ruleListNameId, subIpv6));
                                    rollBackSb.append(String.format("delete firewall family inet filter %s term %s from source-address %s \n", ruleListName, ruleListNameId, subIpv6));
                                } else {
                                    sb.append(String.format("set firewall family inet filter %s term %s from source-address %s \n", ticket, ticketId, subIpv6));
                                    rollBackSb.append(String.format("delete firewall family inet filter %s term %s from source-address %s \n", ticket, ticketId, subIpv6));
                                }
                                indexIpv6++;
                            }
                        } else {
                            String convertRangeToSegment = IpUtils.convertIpRangeToSegment(address);
                            String[] segmentList = convertRangeToSegment.split(",");
                            for (String rangeToSegment : segmentList) {
                                if (StringUtils.isNotBlank(ruleListName)) {
                                    sb.append(String.format("set firewall family inet filter %s term %s from source-address %s \n", ruleListName, ruleListNameId, rangeToSegment));
                                    rollBackSb.append(String.format("delete firewall family inet filter %s term %s from source-address %s \n", ruleListName, ruleListNameId, rangeToSegment));
                                } else {
                                    sb.append(String.format("set firewall family inet filter %s term %s from source-address %s \n", ticket, ticketId, rangeToSegment));
                                    rollBackSb.append(String.format("delete firewall family inet filter %s term %s from source-address %s \n", ticket, ticketId, rangeToSegment));
                                }
                            }

                        }
                    } else if (IPUtil.isIP(address) || IPUtil.isIPv6(address)) {
                        //判断是单IP
                        if (IPUtil.isIPv6(address)) {
                            address = address + "/128";
                            if (StringUtils.isNotBlank(ruleListName)) {
                                String addressObjectString = String.format("set firewall family inet filter %s term %s from source-address %s \n", ruleListName, ruleListNameId, address);
                                sb.append(addressObjectString);
                                String rollAddressObjectString = String.format("delete firewall family inet filter %s term %s from source-address %s \n", ruleListName, ruleListNameId, address);
                                rollBackSb.append(rollAddressObjectString);
                            } else {
                                String addressObjectString = String.format("set firewall family inet filter %s term %s from source-address %s \n", ticket, ticketId, address);
                                sb.append(addressObjectString);
                                String rollAddressObjectString = String.format("delete firewall family inet filter %s term %s from source-address %s \n", ticket, ticketId, address);
                                rollBackSb.append(rollAddressObjectString);
                            }
                            // 单ip新建对象的时候拼子网掩码128,回滚的时候应该也带上
                            joinRollbackIp = address;
                        } else {
                            address = address + "/32";
                            if (StringUtils.isNotBlank(ruleListName)) {
                                String addressObjectString = String.format("set firewall family inet filter %s term %s from source-address %s \n", ruleListName, ruleListNameId, address);
                                sb.append(addressObjectString);
                                String rollAddressObjectString = String.format("delete firewall family inet filter %s term %s from source-address %s \n", ruleListName, ruleListNameId, address);
                                rollBackSb.append(rollAddressObjectString);
                            } else {
                                String addressObjectString = String.format("set firewall family inet filter %s term %s from source-address %s \n", ticket, ticketId, address);
                                sb.append(addressObjectString);
                                String rollAddressObjectString = String.format("delete firewall family inet filter %s term %s from source-address %s \n", ticket, ticketId, address);
                                rollBackSb.append(rollAddressObjectString);
                            }
                        }
                    } else if (IPUtil.isIPSegment(address) || IPUtil.isIPv6Segment(address)) {
                        String[] array = StringUtils.split(address, "/");
                        if (IPUtil.isIPSegment(address)) {
                            IpAddressSubnetIntDTO ipAddressSubnetIntDTO = new IpAddressSubnetIntDTO();
                            ipAddressSubnetIntDTO.setIp(array[0]);
                            ipAddressSubnetIntDTO.setMask(Integer.parseInt(array[1]));
                            String ip6SrcCommand = generateSrcSubnetIntIpV6CommandLine(StatusTypeEnum.ADD, new IpAddressSubnetIntDTO[]{ipAddressSubnetIntDTO}, new String[]{ticket, ticketId, ruleListName, ruleListNameId});
                            if (StringUtils.isNotBlank(ip6SrcCommand)) {
                                String[] split = ip6SrcCommand.split("@@@");
                                sb.append(split[0]);
                                rollBackSb.append(split[1]);
                            }
                        } else {
                            // set firewall family inet filter test3 term 10 from source-address
                            if (StringUtils.isNotBlank(ruleListName)) {
                                String addressObjectString = String.format("set firewall family inet filter %s term %s from source-address %s\n", ruleListName, ruleListNameId, address);
                                sb.append(addressObjectString);
                                String rollAddressObjectString = String.format("delete firewall family inet filter %s term %s from source-address %s\n", ruleListName, ruleListNameId, address);
                                rollBackSb.append(rollAddressObjectString);
                            } else {
                                String addressObjectString = String.format("set firewall family inet filter %s term %s from source-address %s\n", ticket, ticketId, address);
                                sb.append(addressObjectString);
                                String rollAddressObjectString = String.format("set firewall family inet filter %s term %s from source-address %s\n", ticket, ticketId, address);
                                rollBackSb.append(rollAddressObjectString);
                            }
                        }
                    }
                    index++;
                }
            }
            boolean IPv6Range = indexIpv6 == 0 || indexIpv6 == 1;
            dto.setCommandLine(sb.toString());
            dto.setGroup(index == 1 && IPv6Range ? false : true);
            if (StringUtils.isNotBlank(ruleListName)) {
                dto.setJoinRollbackParam(String.format("%s,%s,%s", ruleListName, ruleListNameId, joinRollbackIp));
            } else {
                dto.setJoinRollbackParam(String.format("%s,%s,%s", ticket, ticketId, joinRollbackIp));
            }

            dto.setName(index == 1 && IPv6Range ? name : groupName);
            dto.setJoin(dto.getName());
            dto.setJoinRollbackParam(rollBackSb.toString());
        } catch (Exception e) {
            log.info("", e);
        }
        return dto;
    }

    private PolicyObjectDTO generateDstAddressObject(String ipAddress, String zone, String ruleListName, String ruleListNameId, String ticket, String ticketId, String existsAddressName, String ipSystem, boolean addressType) {
        PolicyObjectDTO dto = new PolicyObjectDTO();
        try {
            dto.setObjectFlag(true);

            if (AliStringUtils.isEmpty(ipAddress)) {
                dto.setJoin("any");
                return dto;
            }

            if (AliStringUtils.isEmpty(zone)) {
                zone = "any";
            }
            if (StringUtils.isNotBlank(existsAddressName)) {
                String prefix = zone + "_zone_";
                if (existsAddressName.startsWith(prefix)) {
                    String name = existsAddressName.replaceFirst(prefix, "");
                    dto.setJoin(name);
                    return dto;
                } else {
                    dto.setJoin(existsAddressName);
                    return dto;
                }
            }

            StringBuilder sb = new StringBuilder();
            StringBuilder rollbackSb = new StringBuilder();
            String[] arr = ipAddress.split(",");
            String groupName = String.format("%s_AG_%s", ticket, IdGen.getRandomNumberString());
            String name = "";
            int index = 0;
            int indexIpv6 = 0;
            String joinRollbackIp = "";
            if (addressType) {
                for (String address : arr) {
                    joinRollbackIp = address;
                    if (IPUtil.isIPRange(address) || IPUtil.isIPv6Range(address)) {
                        //判断是范围
                        if (IPUtil.isIPv6Range(address)) {
                            List<String> toSubnetList = IP6Utils.convertRangeToSubnet(address);
                            for (String subIpv6 : toSubnetList) {
                                // set firewall family inet filter test2 term 20 from source-address 10.12.13.1/32
                                if (StringUtils.isNotBlank(ruleListName)) {

                                    sb.append(String.format("set firewall family inet filter %s term %s from destination-address %s \n", ruleListName, ruleListNameId, subIpv6));
                                    rollbackSb.append(String.format("delete firewall family inet filter %s term %s from destination-address %s \n", ruleListName, ruleListNameId, subIpv6));
                                } else {
                                    sb.append(String.format("set firewall family inet filter %s term %s from destination-address %s \n", ticket, ticketId, subIpv6));
                                    rollbackSb.append(String.format("delete firewall family inet filter %s term %s from destination-address %s \n", ticket, ticketId, subIpv6));
                                }
                                indexIpv6++;
                            }
                        } else {
                            String convertRangeToSegment = IpUtils.convertIpRangeToSegment(address);
                            String[] segmentList = convertRangeToSegment.split(",");
                            for (String rangeToSegment : segmentList) {
                                if (StringUtils.isNotBlank(ruleListName)) {
                                    sb.append(String.format("set firewall family inet filter %s term %s from destination-address %s \n", ruleListName, ruleListNameId, rangeToSegment));
                                    rollbackSb.append(String.format("delete firewall family inet filter %s term %s from destination-address %s \n", ruleListName, ruleListNameId, rangeToSegment));
                                } else {
                                    sb.append(String.format("set firewall family inet filter %s term %s from destination-address %s \n", ticket, ticketId, rangeToSegment));
                                    rollbackSb.append(String.format("delete firewall family inet filter %s term %s from destination-address %s \n", ticket, ticketId, rangeToSegment));
                                }
                            }
                        }
                    } else if (IPUtil.isIP(address) || IPUtil.isIPv6(address)) {
                        //判断是单IP
                        if (IPUtil.isIPv6(address)) {
                            address = address + "/128";
                            if (StringUtils.isNotBlank(ruleListName)) {
                                sb.append(String.format("set firewall family inet filter %s term %s from destination-address %s \n", ruleListName, ruleListNameId, address));
                                rollbackSb.append(String.format("delete firewall family inet filter %s term %s from destination-address %s \n", ruleListName, ruleListNameId, address));
                            } else {
                                sb.append(String.format("set firewall family inet filter %s term %s from destination-address %s \n", ticket, ticketId, address));
                                rollbackSb.append(String.format("delete firewall family inet filter %s term %s from destination-address %s \n", ticket, ticketId, address));
                            }
                            // 单ip新建对象的时候拼子网掩码128,回滚的时候应该也带上
                            joinRollbackIp = address;
                        } else {
                            address = address + "/32";
                            if (StringUtils.isNotBlank(ruleListName)) {
                                sb.append(String.format("set firewall family inet filter %s term %s from destination-address %s \n", ruleListName, ruleListNameId, address));
                                rollbackSb.append(String.format("delete firewall family inet filter %s term %s from destination-address %s \n", ruleListName, ruleListNameId, address));
                            } else {
                                sb.append(String.format("set firewall family inet filter %s term %s from destination-address %s \n", ticket, ticketId, address));
                                rollbackSb.append(String.format("delete firewall family inet filter %s term %s from destination-address %s \n", ticket, ticketId, address));
                            }
                        }
                    } else if (IPUtil.isIPSegment(address) || IPUtil.isIPv6Segment(address)) {
                        String[] array = StringUtils.split(address, "/");
                        if (IPUtil.isIPSegment(address)) {
                            IpAddressSubnetIntDTO ipAddressSubnetIntDTO = new IpAddressSubnetIntDTO();
                            ipAddressSubnetIntDTO.setIp(array[0]);
                            ipAddressSubnetIntDTO.setMask(Integer.parseInt(array[1]));
                            String ip6Command = generateDstSubnetIntIpV6CommandLine(StatusTypeEnum.ADD, new IpAddressSubnetIntDTO[]{ipAddressSubnetIntDTO}, new String[]{ticket, ticketId, ruleListName, ruleListNameId});
                            if (StringUtils.isNotBlank(ip6Command)) {
                                String[] split = ip6Command.split("@@@");
                                sb.append(split[0]);
                                rollbackSb.append(split[1]);
                            }
                        } else {
                            // set firewall family inet filter test3 term 10 from source-address
                            if (StringUtils.isNotBlank(ruleListName)) {
                                sb.append(String.format("set firewall family inet filter %s term %s from destination-address %s\n", ruleListName, ruleListNameId, address));
                                rollbackSb.append(String.format("delete firewall family inet filter %s term %s from destination-address %s\n", ruleListName, ruleListNameId, address));
                            } else {
                                sb.append(String.format("set firewall family inet filter %s term %s from destination-address %s\n", ticket, ticketId, address));
                                rollbackSb.append(String.format("delete firewall family inet filter %s term %s from destination-address %s\n", ticket, ticketId, address));
                            }
                        }
                    }
                    index++;
                }
            } else {
                // 全局对象
                for (String address : arr) {
                    joinRollbackIp = address;
                    if (IPUtil.isIPRange(address) || IPUtil.isIPv6Range(address)) {
                        //判断是范围
                        if (IPUtil.isIPv6Range(address)) {
                            List<String> toSubnetList = IP6Utils.convertRangeToSubnet(address);
                            for (String subIpv6 : toSubnetList) {
                                // set firewall family inet filter test2 term 20 from source-address 10.12.13.1/32
                                if (StringUtils.isNotBlank(ruleListName)) {
                                    sb.append(String.format("set firewall family inet filter %s term %s from destination-address %s/120 \n", ruleListName, ruleListNameId, subIpv6));
                                    rollbackSb.append(String.format("delete firewall family inet filter %s term %s from destination-address %s/120 \n", ruleListName, ruleListNameId, subIpv6));
                                } else {
                                    sb.append(String.format("set firewall family inet filter %s term %s from destination-address %s/120 \n", ticket, ticketId, subIpv6));
                                    rollbackSb.append(String.format("delete firewall family inet filter %s term %s from destination-address %s/120 \n", ticket, ticketId, subIpv6));
                                }
                                indexIpv6++;
                            }
                        } else {
                            String convertRangeToSegment = IpUtils.convertIpRangeToSegment(address);
                            String[] segmentList = convertRangeToSegment.split(",");
                            for (String rangeToSegment : segmentList) {
                                if (StringUtils.isNotBlank(ruleListName)) {
                                    sb.append(String.format("set firewall family inet filter %s term %s from destination-address %s \n", ruleListName, ruleListNameId, rangeToSegment));
                                    rollbackSb.append(String.format("delete firewall family inet filter %s term %s from destination-address %s \n", ruleListName, ruleListNameId, rangeToSegment));
                                } else {
                                    sb.append(String.format("set firewall family inet filter %s term %s from destination-address %s \n", ticket, ticketId, rangeToSegment));
                                    rollbackSb.append(String.format("delete firewall family inet filter %s term %s from destination-address %s \n", ticket, ticketId, rangeToSegment));
                                }
                            }

                        }
                    } else if (IPUtil.isIP(address) || IPUtil.isIPv6(address)) {
                        //判断是单IP
                        if (IPUtil.isIPv6(address)) {
                            address = address + "/128";
                            if (StringUtils.isNotBlank(ruleListName)) {
                                sb.append(String.format("set firewall family inet filter %s term %s from destination-address %s \n", ruleListName, ruleListNameId, address));
                                rollbackSb.append(String.format("delete firewall family inet filter %s term %s from destination-address %s \n", ruleListName, ruleListNameId, address));
                            } else {
                                sb.append(String.format("set firewall family inet filter %s term %s from destination-address %s \n", ticket, ticketId, address));
                                rollbackSb.append(String.format("delete firewall family inet filter %s term %s from destination-address %s \n", ticket, ticketId, address));
                            }
                            // 单ip新建对象的时候拼子网掩码128,回滚的时候应该也带上
                            joinRollbackIp = address;
                        } else {
                            address = address + "/32";
                            if (StringUtils.isNotBlank(ruleListName)) {
                                sb.append(String.format("set firewall family inet filter %s term %s from destination-address %s \n", ruleListName, ruleListNameId, address));
                                rollbackSb.append(String.format("delete firewall family inet filter %s term %s from destination-address %s \n", ruleListName, ruleListNameId, address));
                            } else {
                                sb.append(String.format("set firewall family inet filter %s term %s from destination-address %s \n", ticket, ticketId, address));
                                rollbackSb.append(String.format("delete firewall family inet filter %s term %s from destination-address %s \n", ticket, ticketId, address));
                            }
                        }
                    } else if (IPUtil.isIPSegment(address) || IPUtil.isIPv6Segment(address)) {
                        String[] array = StringUtils.split(address, "/");
                        if (IPUtil.isIPSegment(address)) {
                            IpAddressSubnetIntDTO ipAddressSubnetIntDTO = new IpAddressSubnetIntDTO();
                            ipAddressSubnetIntDTO.setIp(array[0]);
                            ipAddressSubnetIntDTO.setMask(Integer.parseInt(array[1]));
                            String ip6Command = generateDstSubnetIntIpV6CommandLine(StatusTypeEnum.ADD, new IpAddressSubnetIntDTO[]{ipAddressSubnetIntDTO}, new String[]{ticket, ticketId, ruleListName, ruleListNameId});
                            if (StringUtils.isNotBlank(ip6Command)) {
                                String[] split = ip6Command.split("@@@");
                                sb.append(split[0]);
                                rollbackSb.append(split[1]);
                            }
                        } else {
                            // set firewall family inet filter test3 term 10 from source-address
                            if (StringUtils.isNotBlank(ruleListName)) {
                                sb.append(String.format("set firewall family inet filter %s term %s from destination-address %s\n", ruleListName, ruleListNameId, address));
                                rollbackSb.append(String.format("delete firewall family inet filter %s term %s from destination-address %s\n", ruleListName, ruleListNameId, address));
                            } else {
                                sb.append(String.format("set firewall family inet filter %s term %s from destination-address %s\n", ticket, ticketId, address));
                                rollbackSb.append(String.format("delete firewall family inet filter %s term %s from destination-address %s\n", ticket, ticketId, address));
                            }
                        }
                    }
                    index++;
                }
            }
            boolean IPv6Range = indexIpv6 == 0 || indexIpv6 == 1;
            dto.setCommandLine(sb.toString());
            dto.setGroup(index == 1 && IPv6Range ? false : true);
            if (dto.isGroup()) {
                dto.setJoinRollbackParam(String.format("%s,%s,%s", zone, groupName, name));
            } else {
                dto.setJoinRollbackParam(String.format("%s,%s,%s", zone, name, joinRollbackIp));
            }
            dto.setName(index == 1 && IPv6Range ? name : groupName);
            dto.setJoin(dto.getName());
            dto.setJoinRollbackParam(rollbackSb.toString());
        } catch (Exception e) {
            log.info("", e);
        }
        return dto;
    }

    public String generateSrcSubnetIntIpV6CommandLine(StatusTypeEnum statusTypeEnum, IpAddressSubnetIntDTO[] subnetIpArray, String[] args) {
        //默认有地址对象名
        String ticked = args[0];
        String tickedId = args[1];
        String ruleListName = args[2];
        String ruleListNameId = args[3];

        if (statusTypeEnum.equals(StatusTypeEnum.MODIFY) || statusTypeEnum.equals(StatusTypeEnum.DELETE)) {

        } else {
            StringBuffer subnetIpv6Cl = new StringBuffer();
            StringBuffer rollbackSubnetIpv6Cl = new StringBuffer();
            if (ArrayUtils.isEmpty(subnetIpArray)) {
                return StringUtils.EMPTY;
            }
            for (int i = 0; i < subnetIpArray.length; i++) {
                IpAddressSubnetIntDTO ipAddressSubnetIntDTO = subnetIpArray[i];
                if (StringUtils.isNotBlank(ruleListName)) {
                    subnetIpv6Cl.append(String.format("set firewall family inet filter %s term %s from source-address %s/%s\n", ruleListName, ruleListNameId, ipAddressSubnetIntDTO.getIp(), ipAddressSubnetIntDTO.getMask()));
                    rollbackSubnetIpv6Cl.append(String.format("delete firewall family inet filter %s term %s from source-address %s/%s\n", ruleListName, ruleListNameId, ipAddressSubnetIntDTO.getIp(), ipAddressSubnetIntDTO.getMask()));

                } else {
                    subnetIpv6Cl.append(String.format("set firewall family inet filter %s term %s from source-address %s/%s\n", ticked, tickedId, ipAddressSubnetIntDTO.getIp(), ipAddressSubnetIntDTO.getMask()));
                    rollbackSubnetIpv6Cl.append(String.format("delete firewall family inet filter %s term %s from source-address %s/%s\n", ticked, tickedId, ipAddressSubnetIntDTO.getIp(), ipAddressSubnetIntDTO.getMask()));
                }
            }
            String stringAppend = subnetIpv6Cl.toString() + "@@@" + rollbackSubnetIpv6Cl.toString();
            return stringAppend;
        }
        return StringUtils.EMPTY;
    }

    public String generateDstSubnetIntIpV6CommandLine(StatusTypeEnum statusTypeEnum, IpAddressSubnetIntDTO[] subnetIpArray, String[] args) {
        //默认有地址对象名
        String ticked = args[0];
        String tickedId = args[1];
        String ruleListName = args[2];
        String ruleListNameId = args[3];

        if (statusTypeEnum.equals(StatusTypeEnum.MODIFY) || statusTypeEnum.equals(StatusTypeEnum.DELETE)) {

        } else {
            StringBuffer subnetIpv6Cl = new StringBuffer();
            StringBuffer rollbackSubnetIpv6Cl = new StringBuffer();
            if (ArrayUtils.isEmpty(subnetIpArray)) {
                return StringUtils.EMPTY;
            }
            for (int i = 0; i < subnetIpArray.length; i++) {
                IpAddressSubnetIntDTO ipAddressSubnetIntDTO = subnetIpArray[i];
                if (StringUtils.isNotBlank(ruleListName)) {
                    subnetIpv6Cl.append(String.format("set firewall family inet filter %s term %s from destination-address %s/%s\n", ruleListName, ruleListNameId, ipAddressSubnetIntDTO.getIp(), ipAddressSubnetIntDTO.getMask()));
                    rollbackSubnetIpv6Cl.append(String.format("delete firewall family inet filter %s term %s from destination-address %s/%s\n", ruleListName, ruleListNameId, ipAddressSubnetIntDTO.getIp(), ipAddressSubnetIntDTO.getMask()));

                } else {
                    subnetIpv6Cl.append(String.format("set firewall family inet filter %s term %s from destination-address %s/%s\n", ticked, tickedId, ipAddressSubnetIntDTO.getIp(), ipAddressSubnetIntDTO.getMask()));
                    rollbackSubnetIpv6Cl.append(String.format("delete firewall family inet filter %s term %s from destination-address %s/%s\n", ticked, tickedId, ipAddressSubnetIntDTO.getIp(), ipAddressSubnetIntDTO.getMask()));
                }
            }
            String stringAppend = subnetIpv6Cl.toString() + "@@@" + rollbackSubnetIpv6Cl.toString();
            return stringAppend;
        }
        return StringUtils.EMPTY;
    }

    @Override
    public String generatePostCommandline(CommandlineDTO dto) {
        return "commit";
    }
}
