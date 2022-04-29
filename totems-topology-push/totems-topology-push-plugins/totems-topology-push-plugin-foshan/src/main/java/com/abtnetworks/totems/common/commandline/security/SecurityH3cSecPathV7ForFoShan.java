package com.abtnetworks.totems.common.commandline.security;

import com.abtnetworks.totems.common.annotation.CustomCli;
import com.abtnetworks.totems.common.commandline.PolicyGenerator;
import com.abtnetworks.totems.common.commandline.SecurityPolicyGenerator;
import com.abtnetworks.totems.common.constants.PolicyConstants;
import com.abtnetworks.totems.common.enums.*;
import com.abtnetworks.totems.common.dto.*;
import com.abtnetworks.totems.common.dto.commandline.CommandlineDTO;
import com.abtnetworks.totems.common.dto.commandline.PolicyObjectDTO;
import com.abtnetworks.totems.common.dto.commandline.ServiceDTO;
import com.abtnetworks.totems.common.utils.*;
import com.twelvemonkeys.imageio.metadata.tiff.IFD;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * @author lps
 */
@Service
@CustomCli(value = DeviceModelNumberEnum.H3CV7, type = PolicyEnum.SECURITY)
public class SecurityH3cSecPathV7ForFoShan extends SecurityPolicyGenerator implements PolicyGenerator {

    private static Logger logger = Logger.getLogger(SecurityH3cSecPathV7.class);

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
        PolicyObjectDTO srcAddressObject = generateAddressObject(dto.getSrcIp(), dto.getSrcAddressName(), "source-ip", isCreateObject, dto.getIpType());

        PolicyObjectDTO dstAddressObject = generateAddressObject(dto.getDstIp(), dto.getDstAddressName(), "destination-ip", isCreateObject, dto.getIpType());

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
        if (dto.getIdleTimeout() != null) {
            sb.append(String.format("session persistent aging-time %s\n",dto.getIdleTimeout()));
        }

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
    private PolicyObjectDTO generateAddressObject(String ipAddress, String existsAddressName, String prefix, boolean isCreateObject, Integer ipType) {
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
            String name;
            StringBuilder sb = new StringBuilder();
            String addressCmd = "";
            String[] arr = ipAddress.split(",");
            if (arr.length == 1) {
                if(ipType.intValue() == IpTypeEnum.IPV4.getCode()){
                    if (IpUtils.isIPRange(ipAddress)) {
                        String startIp = IpUtils.getStartIpFromRange(ipAddress);
                        String endIp = IpUtils.getEndIpFromRange(ipAddress);
                        name = String.format("%s-%s", startIp, endIp);
                    } else if (IpUtils.isIPSegment(ipAddress)) {
                        String ip = IpUtils.getIpFromIpSegment(ipAddress);
                        String maskBit = IpUtils.getMaskBitFromIpSegment(ipAddress);
                        name = String.format("%s_%s", ip,maskBit);
                    } else {
                        name = String.format("%s", ipAddress);
                    }
                } else {
                    // IPV6
                    if(ipAddress.contains("-")){
                        // 范围
                        String startIp = IpUtils.getRangeStartIPv6(ipAddress);
                        String endIp = IpUtils.getRangeEndIPv6(ipAddress);
                        name = String.format("%s-%s", startIp, endIp);
                    } else if (ipAddress.contains("/")) {
                        // 子网
                        String ip = IpUtils.getIpSegmentStartIPv6(ipAddress);
                        String maskBit = IpUtils.getIpSegmentMaskIPv6(ipAddress);
                        name = String.format("%s_%s", ip,maskBit);
                    } else {
                        name = String.format("%s", ipAddress);
                    }
                }
                dto.setName(name);
                sb.append(String.format("address %s\n", name));
            } else {
                int num = 0;
                List<String> list = new ArrayList<>(Arrays.asList(arr));
                if (ObjectUtils.isNotEmpty(list)) {
                    num += list.hashCode();
                }
                name = String.format("address %s", "add_"+Math.abs(num));
                dto.setName(name);
                sb.append(name);
                sb.append(" type object\n");
            }
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
                    addressCmd = String.format("%s network subnet %s/%s\n", index, ip, maskBit);
                } else if (IpUtils.isIP(address)){
                    addressCmd = String.format("%s network ip address %s\n", index, address);
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
            join = String.format("service %s\n", name);
            command = sb.toString();

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
                    addressCmd = String.format("%s-subnet %s/%s\n", prefix, ip, maskBit);
                } else if (IpUtils.isIP(address)){
                    addressCmd = String.format("%s-ip %s\n", prefix, address);
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
            List<String> serviceNameList = new ArrayList<>();
            int index = 0;
            for (ServiceDTO service : serviceDTOList) {
                int protocolNum = Integer.parseInt(service.getProtocol());
                String protocolString = ProtocolUtils.getProtocolByValue(protocolNum).toLowerCase();

                if (protocolString.equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_ANY)) {
                    dto.setCommandLine("service Any \n");
                    return dto;
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
                    sb.append(String.format("service %s",name));
                    sb.append("\n");
                    dto.setName(name);
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

                    } else if ((protocolString.equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_TCP) ||
                            protocolString.equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_UDP))) {
                        String[] srcPorts = service.getSrcPorts().split(",");
                        String[] dstPorts = service.getDstPorts().split(",");
                        //是TCP/UPD协议
                        //源为any，目的端口有值，则仅显示目的端口
                        if (service.getSrcPorts().equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_ANY) && !service.getDstPorts().equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_ANY)) {
                            sb.append(String.format(index + " service %s ", protocolString));
                            if (PortUtils.isPortRange(dstPort)) {
                                String startPort = PortUtils.getStartPort(dstPort);
                                String endPort = PortUtils.getEndPort(dstPort);
                                sb.append(String.format("destination range %s %s \n", startPort, endPort));
                            } else {
                                sb.append(String.format("destination eq %s  \n", dstPort));
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

                            }
                        } else {
                            //源和目的端口都有具体的值、或者都为any
                            for (String srcPort : srcPorts) {
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
                            }
                        }
                    }
                }

            }

            sb.append("quit\n");
            StringBuilder joinSb = new StringBuilder();
            for (String objName : serviceNameList) {
                joinSb.append(String.format("service %s\n", objName));
            }
            join = joinSb.toString();
            command = sb.toString();
        } else {
            StringBuilder sb = new StringBuilder();
            for (ServiceDTO service : serviceDTOList) {
                int protocolNum = Integer.parseInt(service.getProtocol());
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


    @Override
    public String getServiceName(ServiceDTO dto) {
        StringBuilder sb = new StringBuilder();
        String protocolString = ProtocolUtils.getProtocolByValue(Integer.valueOf(dto.getProtocol()));
        sb.append(protocolString.toLowerCase());
        if (StringUtils.isBlank(dto.getDstPorts())) {
            return sb.toString();
        } else if (protocolString.equalsIgnoreCase("ICMP")) {
            return sb.toString();
        } else if (!dto.getDstPorts().equalsIgnoreCase("any") && !dto.getDstPorts().equals("0-65535")) {
            String[] dstPorts = dto.getDstPorts().split(",");
            String[] var5 = dstPorts;
            int var6 = dstPorts.length;

            for (int var7 = 0; var7 < var6; ++var7) {
                String dstPort = var5[var7];
                if (PortUtils.isPortRange(dstPort)) {
                    String startPort = PortUtils.getStartPort(dstPort);
                    String endPort = PortUtils.getEndPort(dstPort);
                    sb.append(String.format("_%s-%s", startPort, endPort));
                } else {
                    sb.append(String.format("_%s", dstPort));
                }
            }

            return sb.toString().toLowerCase();
        } else {
            return sb.toString();
        }

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

        String setName = String.format("\"to%s\"",
                TimeUtils.transformDateFormat(endTimeString, TimeUtils.EUROPEAN_TIME_FORMAT, TimeUtils.COMMON_TIME_DAY_FORMAT));

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
        serviceDTO.setDstPorts("any");
        serviceDTO.setProtocol("58");
        srcport.add(serviceDTO);
        dto.setServiceList(srcport);
//
//        dto.setSrcIp("1111:a1a1::1111,1111:a1a1::1111-1111:a1a1::1112,1111:a1a1::1111/128");
//        dto.setDstIp("1111:a1a1::1121");
        dto.setIpType(1);
        SecurityH3cSecPathV7 h3cv7 = new SecurityH3cSecPathV7();
        String commandLine = h3cv7.composite(dto);
        System.out.println("commandline:\n" + commandLine);
    }

}
