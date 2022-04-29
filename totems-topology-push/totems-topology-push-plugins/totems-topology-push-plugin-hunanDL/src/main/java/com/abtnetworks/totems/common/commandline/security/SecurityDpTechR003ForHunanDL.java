package com.abtnetworks.totems.common.commandline.security;

import com.abtnetworks.totems.common.annotation.CustomCli;
import com.abtnetworks.totems.common.commandline.PolicyGenerator;
import com.abtnetworks.totems.common.commandline.SecurityPolicyGenerator;
import com.abtnetworks.totems.common.constants.PolicyConstants;
import com.abtnetworks.totems.common.dto.*;
import com.abtnetworks.totems.common.dto.commandline.CommandlineDTO;
import com.abtnetworks.totems.common.dto.commandline.PolicyObjectDTO;
import com.abtnetworks.totems.common.dto.commandline.ServiceDTO;
import com.abtnetworks.totems.common.enums.*;
import com.abtnetworks.totems.common.utils.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@CustomCli(value = DeviceModelNumberEnum.DPTECHR003, type = PolicyEnum.SECURITY)
public class SecurityDpTechR003ForHunanDL extends SecurityPolicyGenerator implements PolicyGenerator {

    private static Logger logger = Logger.getLogger(SecurityDpTechR003ForHunanDL.class);

    private static final String src_prefix = "src-";

    private static final String dst_prefix = "dst-";

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

        GeneratedObjectDTO generatedDto = cmdDTO.getGeneratedObject();
        generatedDto.setPolicyName(dto.getName());
        return composite(dto);
    }
    @Override
    public String generatePreCommandline(CommandlineDTO dto) {
        StringBuilder sb = new StringBuilder();
        if(Boolean.TRUE.equals(dto.isVsys()) && StringUtils.isNotEmpty(dto.getVsysName())){
            sb.append("switch-vsys ").append(dto.getVsysName()).append(StringUtils.LF);
        }
        sb.append("language-mode chinese\nconf-mode\n");
        return sb.toString();
    }

    @Override
    public String generateCommandline(CommandlineDTO dto) {
        StringBuilder sb = new StringBuilder();
        String ticket = dto.getName();
        String srcIp = dto.getSrcIp();
        String dstIp = dto.getDstIp();
        String startTime = dto.getStartTime();
        String endTime = dto.getEndTime();
        String srcZoneJoin = StringUtils.isNotBlank(dto.getSrcZone()) ? "src-zone " + dto.getSrcZone() + "\n" : "";
        String dstZoneJoin = StringUtils.isNotBlank(dto.getDstZone()) ? "dst-zone " + dto.getDstZone() + "\n" : "";


        PolicyObjectDTO srcAddress = createAddressObject(srcIp, ticket, src_prefix,dto.getSrcIpSystem(), dto.getSrcAddressName());
        PolicyObjectDTO dstAddress = createAddressObject(dstIp, ticket, dst_prefix,dto.getDstIpSystem(), dto.getDstAddressName());
        PolicyObjectDTO service = createServiceObject(dto.getRestServiceList(), dto.getExistServiceNameList(),dto.getIdleTimeout());
        PolicyObjectDTO time = createTimeObject(startTime, endTime, ticket);

        sb.append(String.format("%s", srcAddress.getCommandLine()));
        sb.append(String.format("%s", dstAddress.getCommandLine()));
        sb.append(String.format("%s", service.getCommandLine()));
        sb.append(String.format("%s", time.getCommandLine()));

        String name = dto.getBusinessName();
        sb.append(String.format("pf-policy %s\n", name));
        sb.append(srcZoneJoin);
        sb.append(dstZoneJoin);
        for (String join : srcAddress.getJoin().split(",")) {
            sb.append(join);
        }
        for (String join : dstAddress.getJoin().split(",")) {
            sb.append(join);
        }
        for (String join : service.getJoin().split(",")) {
            sb.append(join);
        }
        sb.append(String.format("action %s\n", dto.getAction().equalsIgnoreCase("PERMIT")?"pass":"drop"));
        sb.append(time.getJoin());

        String swapRuleNameId = StringUtils.isNotBlank(dto.getSwapRuleNameId()) ? dto.getSwapRuleNameId() : "";
        int moveSeatCode = dto.getMoveSeatEnum().getCode();
        if (moveSeatCode == MoveSeatEnum.BEFORE.getCode() || moveSeatCode == MoveSeatEnum.FIRST.getCode()) {
            if(!AliStringUtils.isEmpty(swapRuleNameId)) {
                sb.append(String.format("move before %s", swapRuleNameId));
            }
            sb.append("\n");
        } else if (moveSeatCode == MoveSeatEnum.AFTER.getCode()) {
            sb.append("\n");
            sb.append(String.format("move after %s\n", swapRuleNameId));
        }

        sb.append("end\n");

        return sb.toString();
    }

    @Override
    public String generatePostCommandline(CommandlineDTO dto) {
        return "";
    }

    PolicyObjectDTO createAddressObject(String ipAddresses, String theme, String prefix,String ipSystem, String existAddressName) {
        PolicyObjectDTO policyObjectDTO = new PolicyObjectDTO();
        policyObjectDTO.setJoin("");
        policyObjectDTO.setCommandLine("");
//        if(!AliStringUtils.isEmpty(existAddressName) ) {
//            policyObjectDTO.setJoin(String.format("%sip %s\n", prefix, existAddressName));
//            return policyObjectDTO;
//        }

        //地址为空则不创建命令行
        if(AliStringUtils.isEmpty(ipAddresses)) {
            return policyObjectDTO;
        }

        String[] ipAddressList = ipAddresses.split(",");
        StringBuilder cmdSb = new StringBuilder();
        StringBuilder joinSb = new StringBuilder();
        int index = 1;
        for(String ipAddress : ipAddressList) {
            String name = getIPNameKevin(1,ipAddress,true,ipSystem,"",ipAddress.length(),index);
            index++;
            if(isIPv6(ipAddress)) {

            } else {
                joinSb.append(",");
                if(IpUtils.isIPRange(ipAddress)) {
                    String startIp = IpUtils.getStartIpFromIpAddress(ipAddress);
                    String endIp = IpUtils.getEndIpFromIpAddress(ipAddress);
                    joinSb.append(String.format("%sip %s\n", prefix, name));
                    cmdSb.append(String.format("ip-obj-range %s\n", name));
                    cmdSb.append(String.format("ip-range start-ip %s end-ip %s\n", startIp, endIp));
                } else if (IpUtils.isIPSegment(ipAddress)) {
                    joinSb.append(String.format("%sip %s\n", prefix, name));
                    cmdSb.append(String.format("ip-obj-mask %s\n", name));
                    cmdSb.append(String.format("ip-mask %s\n", ipAddress));
                } else {
                    joinSb.append(String.format("%sip %s\n", prefix, name));
                    cmdSb.append(String.format("ip-obj-mask %s\n", name));
                    cmdSb.append(String.format("ip-mask %s/32\n", ipAddress));
                }
            }

            cmdSb.append("exit\n");
        }
        if (joinSb.length() > 0) {
            joinSb.deleteCharAt(0);
        }
        policyObjectDTO.setJoin(joinSb.toString());
        policyObjectDTO.setCommandLine(cmdSb.toString());

        return policyObjectDTO;
    }

    PolicyObjectDTO createServiceObject(List<ServiceDTO> serviceList, List<String> existsServiceNameList, Integer idleTimeout){
        PolicyObjectDTO dto = new PolicyObjectDTO();
        if(serviceList.size() == 0 && existsServiceNameList.size() == 0) {
            dto.setJoin("");
            dto.setCommandLine("");
            return dto;
        }
        StringBuilder nameSb = new StringBuilder("");
        if(existsServiceNameList.size() >0 ) {
            for(String name: existsServiceNameList) {
                nameSb.append(",").append(String.format("usr-service %s\n", name));
            }
        }

        StringBuilder sb = new StringBuilder("");
        for(ServiceDTO service : serviceList) {
            int protocolNum = Integer.valueOf(service.getProtocol());
            String protocolString = ProtocolUtils.getProtocolByValue(protocolNum).toLowerCase();
            if(protocolString.equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_ANY)) {
                dto.setJoin("");
                dto.setCommandLine("");
                return dto;
            }

            if(!protocolString.equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_ICMP)) {
                String srcPorts = service.getSrcPorts();
                List<String> srcPortList = new ArrayList<>();
                if(srcPorts == PolicyConstants.POLICY_STR_VALUE_ANY) {
                    String port = "0-65535";
                    srcPortList.add(formatPort(port, src_prefix));
                } else {
                    String[] srcPortArray = srcPorts.split(",");
                    for(String port: srcPortArray) {
                        srcPortList.add(formatPort(port, src_prefix));
                    }
                }
                for(String srcPort: srcPortList) {
                    String dstPorts = service.getDstPorts();
                    List<String> dstPortList = new ArrayList<>();
                    if(dstPorts == PolicyConstants.POLICY_STR_VALUE_ANY) {
                        String port = "0-65535";
                        dstPortList.add(formatPort(port, dst_prefix));
                    } else {
                        String[] dstPortArray = dstPorts.split(",");
                        for(String port: dstPortArray) {
                            String dstPort = formatPort(port, dst_prefix);
                            String name = getServiceNameKevin(ProtocolUtils.getProtocolByString(service.getProtocol()).toLowerCase(), port,idleTimeout);
                            nameSb.append(",").append(String.format("usr-service %s\n", name));
                            sb.append(String.format("usr-service %s %s%s%s", name, ProtocolUtils.getProtocolByString(service.getProtocol()).toLowerCase(), srcPort, dstPort));
                        }
                    }
                }
            } else {
                String name = getServiceNameKevin(ProtocolUtils.getProtocolByString(service.getProtocol()).toLowerCase(), "",idleTimeout);
                nameSb.append(",").append(String.format("usr-service %s\n", name));
                sb.append(String.format("usr-service %s icmp type 0 code 0", name));
            }
            sb.append("\n");
        }
        if (nameSb.length() > 0) {
            nameSb.deleteCharAt(0);
        }

        dto.setCommandLine(sb.toString());
        dto.setName(nameSb.toString());
        dto.setJoin(nameSb.toString());

        return dto;
    }

    public String getServiceName(ServiceDTO service){
        StringBuilder nameSb = new StringBuilder();
        nameSb.append(ProtocolUtils.getProtocolByString(service.getProtocol().toLowerCase()));
        if(service.getDstPorts().equals(PolicyConstants.POLICY_STR_VALUE_ANY)) {
            //自定义服务大写
            return nameSb.toString().toUpperCase();
        } else {
            if(service.getDstPorts().equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_ANY)) {
                //自定义服务大写
                return nameSb.toString().toUpperCase();
            } else {
                nameSb.append("_");
                if (PortUtils.isPortRange(service.getDstPorts())) {
                    String start = PortUtils.getStartPort(service.getDstPorts());
                    String end = PortUtils.getEndPort(service.getDstPorts());
                    nameSb.append(start);
                    nameSb.append("_");
                    nameSb.append(end);
                } else {
                    nameSb.append(service.getDstPorts());
                }
            }
            return nameSb.toString();
        }
    }

    String formatPort(String port, String prefix) {
        if (PortUtils.isPortRange(port)) {
            String start = PortUtils.getStartPort(port);
            String end = PortUtils.getEndPort(port);
            return String.format(" %sport %s %s", prefix, start, end);
        } else {
            return String.format(" %sport %s %s", prefix, port, port);
        }
    }

    PolicyObjectDTO createTimeObject(String startTime, String endTime, String theme) {
        PolicyObjectDTO policyObjectDTO = new PolicyObjectDTO();
        policyObjectDTO.setCommandLine("");
        policyObjectDTO.setJoin("");

        if(AliStringUtils.isEmpty(startTime)) {
            return policyObjectDTO;
        }

//        String name = String.format("%s_TO_%S", theme, IdGen.getRandomNumberString());
//        String name = String.format("to%S", formatTime(endTime));
        String name = String.format("\"to%s\"",
                TimeUtils.transformDateFormat(endTime, TimeUtils.EUROPEAN_TIME_FORMAT, TimeUtils.COMMON_TIME_DAY_FORMAT));
        policyObjectDTO.setJoin(String.format("time-range %s\n", name));

        StringBuilder sb = new StringBuilder();
        sb.append(String.format("time-range %s\n", name));
        sb.append(String.format("absolute start %s end %s\n", formatTime(startTime), formatTime(endTime)));
        sb.append("exit\n");
        policyObjectDTO.setCommandLine(sb.toString());
        return policyObjectDTO;
    }

    private boolean isIPv6(String ipAddress) {
        return false;
    }

    String formatTime(String time) {
        return TimeUtils.transformDateFormat(time, TimeUtils.EUROPEAN_TIME_FORMAT, TimeUtils.DPTECH_FIREWALL_FORMAT);
    }


    public static String getServiceNameKevin(String protocolString,String dstPorts,Integer idleTimeout) {
        logger.info("进入utils获取ip名称");
        String name;
        String servicePrefix = "tcp,udp,icmp,-";
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
        logger.info("进入utils获取service名称" );
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

        if (IpUtils.isIPRange(address)) {
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
        } else if (IpUtils.isIPSegment(address)) {
            return iPSegment + address;
        } else if (IpUtils.isIP(address)) {
            return isIP + address;
        } else if (address.contains(":")) {
            //ipv6
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
