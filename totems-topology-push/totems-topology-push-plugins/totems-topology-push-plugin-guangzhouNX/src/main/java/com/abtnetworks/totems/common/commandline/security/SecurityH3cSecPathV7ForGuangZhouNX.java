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
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @Author: wxx
 * @Date: 2021/5/17 09:20
 * @Desc:华三v7
 */
@Slf4j
@Service
@CustomCli(value = DeviceModelNumberEnum.H3CV7, type = PolicyEnum.SECURITY)
public class SecurityH3cSecPathV7ForGuangZhouNX extends SecurityPolicyGenerator implements PolicyGenerator {

    private static Logger logger = Logger.getLogger(SecurityH3cSecPathV7ForGuangZhouNX.class);

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
        StringBuilder sb = new StringBuilder();
        if (dto.isVsys()) {
            sb.append("system-view\n");
            sb.append("switchto context " + dto.getVsysName() + "\n");
        }
        sb.append("system-view\n");
        return sb.toString();
    }

    @Override
    public String generateCommandline(CommandlineDTO dto) {
        boolean isIPv6 = false;
        boolean isCreateObject = dto.isCreateObjFlag();
        PolicyObjectDTO srcAddressObject = generateAddressObject(dto.getSrcIp(), dto.getName(), dto.getSrcAddressName(), "source-ip", isCreateObject, dto.getSrcIpSystem());

        PolicyObjectDTO dstAddressObject = generateAddressObject(dto.getDstIp(), dto.getName(), dto.getDstAddressName(), "destination-ip", isCreateObject, dto.getDstIpSystem());

        PolicyObjectDTO serviceObject = generateServiceObject(dto.getServiceList(), dto.getServiceName(),isCreateObject,dto.getIpType());

        PolicyObjectDTO timeObject = generateTimeObject(dto.getStartTime(), dto.getEndTime(), dto.getName());

        //IPv6地址必须创建对象
        if(dto.getIpType()!=null&&dto.getIpType()==1) {
            isCreateObject = true;
            isIPv6 = true;
        }
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

        sb.append("security-policy ");
        if(isIPv6) {
            sb.append("ipv6\n");
        } else {
            sb.append("ip\n");
        }

        sb.append(String.format("rule name %s\n", dto.getName()));
        if(!AliStringUtils.isEmpty(dto.getDescription())) {
            sb.append("description " + dto.getDescription() + "\n");
        }
        String action = dto.getAction();
        if ("deny".equalsIgnoreCase(action)) {
            sb.append("action drop \n");
        } else {
            sb.append("action pass \n");
        }
        if(StringUtils.isNotBlank(dto.getSrcZone())) {
            sb.append(String.format("source-zone %s\n", dto.getSrcZone()));
        }
        if(StringUtils.isNotBlank(dto.getDstZone())) {
            sb.append(String.format("destination-zone %s\n", dto.getDstZone()));
        }

        sb.append(srcAddressObject.getJoin());
        sb.append(dstAddressObject.getJoin());
        if(serviceObject != null && !AliStringUtils.isEmpty(serviceObject.getJoin())) {
            sb.append(serviceObject.getJoin());
        }
        if(timeObject != null) {
            sb.append(String.format("time-range %s\n", timeObject.getJoin()));
        }

        String swapRuleNameId = StringUtils.isNotBlank(dto.getSwapRuleNameId()) ? dto.getSwapRuleNameId() : "";
        int moveSeatCode = dto.getMoveSeatEnum().getCode();
        if (moveSeatCode == MoveSeatEnum.BEFORE.getCode() || moveSeatCode == MoveSeatEnum.FIRST.getCode()) {
            if(!AliStringUtils.isEmpty(swapRuleNameId)) {
                sb.append(String.format("move rule before %s\n", swapRuleNameId));
            }
        }

        //针对广州农信新增最终日志
        sb.append("logging enable\n");
        sb.append("counting enable\n");


        sb.append("quit\n");
        sb.append("return\n");
        String command = sb.toString();
        return command;
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
    private PolicyObjectDTO generateAddressObject(String ipAddress, String ticket, String existsAddressName, String prefix, boolean isCreateObject, String ipSystem) {
        PolicyObjectDTO dto = new PolicyObjectDTO();
        if (StringUtils.isNotBlank(existsAddressName)) {
            dto.setObjectFlag(true);
            existsAddressName = containsQuotes(existsAddressName);
            dto.setJoin(prefix + " " + existsAddressName + "\n");
            return dto;
        }

        //若为IPv6地址，则必须创建对象
        if(IpUtils.isIPv6(ipAddress)) {
            isCreateObject = true;
        }

        String join = "";
        String command = "";

        if(AliStringUtils.isEmpty(ipAddress)) {
            dto.setJoin(join);
            dto.setCommandLine(command);
            return dto;
        }

        if(isCreateObject) {
            String name="";
            if(StringUtils.isNotEmpty(ipSystem)){
                name = ipSystem;
                // 对象名称长度限制，一个中文2个字符
                name = strSub(name, getMaxObejctNameLength(),"GB2312");
                // 对象名称长度限制
                int len = 0;
                try{
                    len = name.getBytes("GB2312").length;
                }catch (Exception e) {
                    logger.error("字符串长度计算异常");
                }
                if(len > getMaxObejctNameLength() -7 ) {
                    name = strSub(name, getMaxObejctNameLength() -7, "GB2312");
                }
                name = String.format("%s_%s", name, DateUtils.getDate().replace("-","").substring(2));
                name = String.format("%s_%s", ipSystem, ipAddress);
            } else {
                if (ipAddress != null && IpUtils.isIPSegment(ipAddress)) {
                    String mask = IpUtils.getMaskBitFromIpSegment(ipAddress);
                    if (StringUtils.isNotBlank(mask) && mask.equalsIgnoreCase("32")) {
                        name = "ip_" + ipAddress;
                    }else  {
                        String arr2[] = ipAddress.split("/");
                        name = "net_" + arr2[0] + "/" + arr2[1];
                    }
                }else if(IpUtils.isIPv6Subnet(ipAddress)){
                    name = "net_" + ipAddress;
                }else {
                    name = "ip_" + ipAddress;
                }
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
            for(String address : arr) {
                if(IpUtils.isIPRange(address)) {
                    String startIp = IpUtils.getStartIpFromRange(address);
                    String endIp = IpUtils.getEndIpFromRange(address);
                    addressCmd = String.format("%s network range %s %s\n", index, startIp, endIp);
                } else if (IpUtils.isIPSegment(address)) {
                    String ip = IpUtils.getIpFromIpSegment(address);
                    String maskBit = IpUtils.getMaskBitFromIpSegment(address);
                    addressCmd = String.format("%s network subnet %s %s\n", index, ip, maskBit);
                } else if (IpUtils.isIP(address)){
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
                        addressCmd = String.format("%s network host address %s\n", index,  address.toLowerCase());
                    }
                }
                index++;
                sb.append(addressCmd);
            }

            sb.append("quit\n");

            command = sb.toString();
            /*if (isIpv6) {
                //ipv6时
                command = command.replace("object-group ip", "object-group ipv6");
            }*/
        } else {
            String addressCmd = "";
            String[] arr = ipAddress.split(",");
            StringBuilder sb = new StringBuilder();
            for(String address : arr) {
                if(IpUtils.isIPRange(address)) {
                    String startIp = IpUtils.getStartIpFromRange(address);
                    String endIp = IpUtils.getEndIpFromRange(address);
                    addressCmd = String.format( "%s-range %s %s\n", prefix, startIp, endIp);
                } else if (IpUtils.isIPSegment(address)) {
                    String ip = IpUtils.getIpFromIpSegment(address);
                    String maskBit = IpUtils.getMaskBitFromIpSegment(address);
                    addressCmd = String.format("%s-subnet %s %s\n", prefix, ip, maskBit);
                } else if (IpUtils.isIP(address)){
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
     * 获取服务集对象文本
     * @param serviceDTOList 服务列表
     * @return 服务集对象
     */
    private PolicyObjectDTO generateServiceObject(List<ServiceDTO> serviceDTOList, String existsServiceName, boolean isCreateObject,Integer iptype ) {
        PolicyObjectDTO dto = new PolicyObjectDTO();
        dto.setObjectFlag(true);
        if (StringUtils.isNotBlank(existsServiceName)) {
            dto.setJoin("service" + " " + existsServiceName + "\n");
            return dto;
        }
        if(CollectionUtils.isEmpty(serviceDTOList)){
            dto.setJoin("service any\n");
            return dto;
        }

        String join = "";
        String command = "";
        if(isCreateObject) {
            StringBuilder sb = new StringBuilder();
            String setName = getServiceName(serviceDTOList);
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

                if (protocolString.equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_ICMP)||protocolString.equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_ICMPV6)) {
                    if(iptype==1){
                        sb.append(String.format("%d service icmpv6 ", index));
                    }else {
                        sb.append(String.format("%d service icmp ", index));
                    }
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
                    //是TCP/UPD协议
                    //源为any，目的端口有值，则仅显示目的端口
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
                        //源端口有值，目的端口any，则仅显示源端口
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
                        //源和目的端口都有具体的值、或者都为any
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
                    //是TCP/UPD协议
                    //源为any，目的端口有值，则仅显示目的端口
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
                        //源端口有值，目的端口any，则仅显示源端口
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
                        //源和目的端口都有具体的值、或者都为any
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


    /**获取服务名称***/
    public String getServiceName(List<ServiceDTO> serviceDTOList){
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
        if(name.length() > getMaxNameLength()) {
            String shortName = name.substring(0, getMaxNameLength()-4);
            name = String.format("%s_etc", shortName.substring(0, shortName.lastIndexOf("_")));
        }
        return name;
    }

    public String getServiceName(ServiceDTO dto) {
        StringBuilder sb = new StringBuilder();
        String protocolString = ProtocolUtils.getProtocolByValue(Integer.valueOf(dto.getProtocol()));
        sb.append(protocolString.toLowerCase());
        if (StringUtils.isBlank(dto.getDstPorts())) {
            return sb.toString();
        }
        if(protocolString.equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_ICMP)){
            return sb.toString();
        }
        if(dto.getDstPorts().equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_ANY) || dto.getDstPorts().equals(PolicyConstants.PORT_ANY)){
            return sb.toString();
        }
        String[] dstPorts = dto.getDstPorts().split(",");
        for (String dstPort : dstPorts) {
            if (PortUtils.isPortRange(dstPort)) {
                String startPort = PortUtils.getStartPort(dstPort);
                String endPort = PortUtils.getEndPort(dstPort);
                sb.append(String.format("_%s-%s", startPort, endPort));
            } else {
                sb.append(String.format("_%s", dstPort));
            }
        }
        return sb.toString().toLowerCase();
    }

    static String formatTimeString(String timeString) {
        if(StringUtils.isBlank(timeString)) {
            return null;
        }

        String dateStr = "";
        SimpleDateFormat sdf = new SimpleDateFormat(TimeUtils.EUROPEAN_TIME_FORMAT);
        SimpleDateFormat sdf2 = new SimpleDateFormat(TimeUtils.H3C_V7_FORMAT);
        try{
            Date date = sdf.parse(timeString);
            dateStr = sdf2.format(date);
        }catch (Exception e) {
            logger.error("时间转化异常", e);
        }

        return dateStr;
    }

    /**
     * 生成时间区间对象
     * @param startTimeString 开始时间字符串
     * @param endTimeString 结束时间字符串
     * @return 时间区间对象
     */
    private PolicyObjectDTO generateTimeObject(String startTimeString, String endTimeString, String ticket) {
        if(AliStringUtils.isEmpty(startTimeString) || AliStringUtils.isEmpty(endTimeString)) {
            return null;
        }

        String startTime = formatTimeString(startTimeString);
        String endTime = formatTimeString(endTimeString);

        //String setName = String.format("%s_TR_%s", ticket, IdGen.getRandomNumberString());
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        Date startDate = new Date();
        Date endDate = new Date();
        try {
            startDate = sdf.parse(startTimeString);
            endDate = sdf.parse(endTimeString);
        } catch (Exception e) {
            e.printStackTrace();
        }
        SimpleDateFormat dst = new SimpleDateFormat("yyyyMMddHHmm");
        String finstartTime=dst.format(startDate).substring(2,12);
        String finendTime=dst.format(endDate).substring(2,12);

        String setName=finstartTime+"_"+finendTime;


        PolicyObjectDTO dto = new PolicyObjectDTO();
        dto.setName(setName);
        dto.setJoin(setName);
        dto.setCommandLine(String.format("time-range %s from %s to %s \n", setName, startTime, endTime));
        return dto;
    }

    public static void main(String[] args) {
        CommandlineDTO dto = CommandlineDTO.getInstanceDemo();
        List<ServiceDTO> srcport=new ArrayList<>();
        ServiceDTO serviceDTO=new ServiceDTO();
        serviceDTO.setProtocol("6");
        serviceDTO.setSrcPorts("44");
        serviceDTO.setDstPorts("22,10-80");

        srcport.add(serviceDTO);
        dto.setSrcIp("1.1.1.1/10,5.5.5.5");
        dto.setDstIp("2.2.2.2-4.4.4.4");
        dto.setSrcIpSystem("");
        dto.setDstIpSystem("");
        dto.setStartTime("2020-06-15 12:00:00");
        dto.setEndTime("2020-06-17 12:00:00");
        dto.setServiceList(srcport);

//
       /* dto.setSrcIp("1111:a1a1::1121-a1a1:1121::4008");
        dto.setDstIp("1111:a1a1::1121/4008,a1a1:1121::4008");*/
        dto.setIpType(0);
        SecurityH3cSecPathV7ForGuangZhouNX h3cv7 = new SecurityH3cSecPathV7ForGuangZhouNX();
        String commandLine = h3cv7.composite(dto);
        System.out.println("commandline:\n" + commandLine);
    }

}
