package com.abtnetworks.totems.common.commandline.security;

import com.abtnetworks.totems.common.annotation.CustomCli;
import com.abtnetworks.totems.common.commandline.PolicyGenerator;
import com.abtnetworks.totems.common.commandline.SecurityPolicyGenerator;
import com.abtnetworks.totems.common.constants.PolicyConstants;
import com.abtnetworks.totems.common.dto.*;
import com.abtnetworks.totems.common.dto.commandline.CommandlineDTO;
import com.abtnetworks.totems.common.dto.commandline.PolicyObjectDTO;
import com.abtnetworks.totems.common.dto.commandline.ServiceDTO;
import com.abtnetworks.totems.common.enums.ActionEnum;
import com.abtnetworks.totems.common.enums.DeviceModelNumberEnum;
import com.abtnetworks.totems.common.enums.MoveSeatEnum;
import com.abtnetworks.totems.common.enums.PolicyEnum;
import com.abtnetworks.totems.common.utils.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
/**
 * @Author: wxx
 * @Date: 2021/5/17 09:20
 * @Desc:迪普R003
 */
@Slf4j
@Service
@CustomCli(value = DeviceModelNumberEnum.DPTECHR003, type = PolicyEnum.SECURITY)
public class SecurityDpTechR003ForGuangZhou extends SecurityPolicyGenerator implements PolicyGenerator {

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
        return "language-mode chinese\nconf-mode\n";
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


        PolicyObjectDTO srcAddress = createAddressObject(srcIp, ticket, src_prefix, dto.getSrcAddressName(),dto.getSrcIpSystem());
        PolicyObjectDTO dstAddress = createAddressObject(dstIp, ticket, dst_prefix, dto.getDstAddressName(),dto.getSrcIpSystem());
        PolicyObjectDTO service = createServiceObject(dto.getRestServiceList(), dto.getExistServiceNameList());
        PolicyObjectDTO time = createTimeObject(startTime, endTime, ticket);

        sb.append(String.format("%s", srcAddress.getCommandLine()));
        sb.append(String.format("%s", dstAddress.getCommandLine()));
        sb.append(String.format("%s", service.getCommandLine()));
        sb.append(String.format("%s", time.getCommandLine()));

        String name = dto.getBusinessName();
        sb.append(String.format("pf-policy %s\n", name));
        sb.append(srcZoneJoin);
        sb.append(dstZoneJoin);
        sb.append(srcAddress.getJoin());
        sb.append(dstAddress.getJoin());
        sb.append(service.getJoin());
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


        //针对广州农信添加日志
        sb.append("language-mode chinese\n");
        sb.append(String.format("security-policy %s logging policy\n",dto.getName()));
        sb.append(String.format("security-policy %s logging session\n",dto.getName()));

        sb.append("end\n");

        return sb.toString();
    }

    @Override
    public String generatePostCommandline(CommandlineDTO dto) {
        return "";
    }

    PolicyObjectDTO createAddressObject(String ipAddresses, String theme, String prefix, String existAddressName, String ipSystem) {
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
        String name="";
        for(String ipAddress : ipAddressList) {
            //String name = String.format("%s_AO_%s", theme, IdGen.getRandomNumberString());

            if(StringUtils.isNotEmpty(ipSystem)){
                name = String.format("%s_%s", ipSystem, ipAddress);
            }else{
                name = "ip_" + ipAddress;
            }


            if(isIPv6(ipAddress)) {

            } else {
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
        policyObjectDTO.setJoin(joinSb.toString());
        policyObjectDTO.setCommandLine(cmdSb.toString());

        return policyObjectDTO;
    }

    PolicyObjectDTO createServiceObject(List<ServiceDTO> serviceList, List<String> existsServiceNameList){
        PolicyObjectDTO dto = new PolicyObjectDTO();
        if(serviceList.size() == 0 && existsServiceNameList.size() == 0) {
            dto.setJoin("");
            dto.setCommandLine("");
            return dto;
        }

        StringBuilder nameSb = new StringBuilder("");
        if(existsServiceNameList.size() >0 ) {
            for(String name: existsServiceNameList) {
                nameSb.append(String.format("usr-service %s\n", name));
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

            String name = getServiceName(service);
            nameSb.append(String.format("usr-service %s\n", name));
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
                            dstPortList.add(formatPort(port, dst_prefix));
                        }
                    }
                    for(String dstPort: dstPortList) {
                       sb.append(String.format("usr-service %s %s%s%s", name, ProtocolUtils.getProtocolByString(service.getProtocol()).toLowerCase(), srcPort, dstPort));
                    }
                }
            } else {
                sb.append(String.format("usr-service %s icmp type 0 code 0", name));
            }
            sb.append("\n");
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

       // String name = String.format("%s_TO_%S", theme, IdGen.getRandomNumberString());

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        Date startDate = new Date();
        Date endDate = new Date();
        try {
            startDate = sdf.parse(startTime);
            endDate = sdf.parse(endTime);
        } catch (Exception e) {
            e.printStackTrace();
        }
        SimpleDateFormat dst = new SimpleDateFormat("yyyyMMddHHmm");
        String finstartTime=dst.format(startDate).substring(2,12);
        String finendTime=dst.format(endDate).substring(2,12);

        String name=finstartTime+"_"+finendTime;

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

    public static void main(String[] args) {
        CommandlineDTO dto = CommandlineDTO.getInstanceDemo();
        SecurityDpTechR003ForGuangZhou juniperSrx = new SecurityDpTechR003ForGuangZhou();
        List<ServiceDTO> srcport=new ArrayList<>();
        ServiceDTO serviceDTO = new ServiceDTO();
        serviceDTO.setProtocol("6");
        serviceDTO.setSrcPorts("22");
        serviceDTO.setDstPorts("70-99");

        srcport.add(serviceDTO);
        dto.setSrcIp("127.0.0.1-9.9.9.10");
        dto.setDstIp("1.1.1.1/20");
        dto.setSrcIpSystem("");
        dto.setDstIpSystem("");
        dto.setStartTime("2020-06-15 12:00:00");
        dto.setEndTime("2020-06-17 12:00:00");
        dto.setServiceList(srcport);

//
      /*  dto.setSrcIp("1111:a1a1::1111-1111:a1a1::1112");
        dto.setDstIp("1111:a1a1::1121/4008");*/
        dto.setIpType(0);

        String commandLine = juniperSrx.composite(dto);
        System.out.println("commandline:\n" + commandLine);

    }
}
