package com.abtnetworks.totems.security;

import com.abtnetworks.totems.common.annotation.CustomCli;
import com.abtnetworks.totems.common.commandline.PolicyGenerator;
import com.abtnetworks.totems.common.commandline.SecurityPolicyGenerator;
import com.abtnetworks.totems.common.constants.CommonConstants;
import com.abtnetworks.totems.common.constants.PolicyConstants;
import com.abtnetworks.totems.common.dto.*;
import com.abtnetworks.totems.common.dto.commandline.CommandlineDTO;
import com.abtnetworks.totems.common.dto.commandline.PolicyObjectDTO;
import com.abtnetworks.totems.common.dto.commandline.ServiceDTO;
import com.abtnetworks.totems.common.enums.ActionEnum;
import com.abtnetworks.totems.common.enums.DeviceModelNumberEnum;
import com.abtnetworks.totems.common.enums.PolicyEnum;
import com.abtnetworks.totems.common.utils.*;
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
 * @desc    山东农信定制命令行
 * @author liuchanghao
 * @date 2020-11-06 09:52
 */
@Service
@CustomCli(value = DeviceModelNumberEnum.H3CV7_OP, type = PolicyEnum.SECURITY)
public class H3cSecPathV7OPForSDNX extends SecurityPolicyGenerator implements PolicyGenerator {

    private static Logger logger = Logger.getLogger(H3cSecPathV7OPForSDNX.class);

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
        return composite(dto);
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

        PolicyObjectDTO serviceObject = generateServiceObject(dto.getRestServiceList(), dto.getExistServiceNameList(), isCreateObject);

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
        sb.append(String.format("object-policy ip %s\n", CommonConstants.OBJECT_POLICY));
        String action = dto.getAction();
        if ("deny".equalsIgnoreCase(action)) {
            sb.append("rule drop ");
        } else {
            sb.append("rule pass ");
        }
        sb.append("logging enable ");
        sb.append("counting enable ");
        if (StringUtils.isNotEmpty(srcAddressObject.getJoin())) {
            sb.append(srcAddressObject.getJoin().replace("\n", " "));
        }
        if (StringUtils.isNotEmpty(dstAddressObject.getJoin())) {
            sb.append(dstAddressObject.getJoin().replace("\n", " "));
        }

        if (serviceObject != null && !AliStringUtils.isEmpty(serviceObject.getJoin())) {
            sb.append(serviceObject.getJoin().replace("\n", " "));
        }
        if(ObjectUtils.isNotEmpty(dto.getIdleTimeout())){
            sb.append("session persistent aging-time 24\n");
        }
        if (timeObject != null) {
            sb.append(String.format("time-range %s ", timeObject.getJoin()));
        }
        sb.append("\nquit\n");
        String command = sb.toString();
        return command;
    }

    @Override
    public String generatePostCommandline(CommandlineDTO dto) {
        return "";
    }

    /**
     * 获取地址对象
     *
     * @param ipAddress ip地址
     * @return 地址对象
     */
    private PolicyObjectDTO generateAddressObject(String ipAddress, String ticket, String existsAddressName, String prefix, boolean isCreateObject, String ipSystem) {
        PolicyObjectDTO dto = new PolicyObjectDTO();
        if (StringUtils.isNotBlank(existsAddressName)) {
            existsAddressName = containsQuotes(existsAddressName);
            dto.setObjectFlag(true);
            dto.setJoin(prefix + " " + existsAddressName + "\n");
            return dto;
        }

        //若为IPv6地址，则必须创建对象
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
                //ipv6时
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

    /**
     * 获取服务集对象文本
     *
     * @param serviceDTOList 服务列表
     * @return 服务集对象
     */
    private PolicyObjectDTO generateServiceObject(List<ServiceDTO> serviceDTOList, List<String> existServiceNameList, boolean isCreateObject) {
        PolicyObjectDTO dto = new PolicyObjectDTO();
        dto.setObjectFlag(true);
        if (CollectionUtils.isEmpty(serviceDTOList)) {
            dto.setJoin("");
            return dto;
        }
        String join = "";
        String command = "";
        if (isCreateObject) {
            StringBuilder sb = new StringBuilder();
            for (ServiceDTO serviceDTO : serviceDTOList) {
                StringBuilder nameSb = new StringBuilder();
                nameSb.append(getServiceName(serviceDTO));

                String setName = nameSb.toString();

                if (PolicyConstants.POLICY_STR_VALUE_ANY.equalsIgnoreCase(ProtocolUtils.getProtocolByString(serviceDTOList.get(0).getProtocol()))) {
                    dto.setJoin("");
                    return dto;
                }
                sb.append(String.format("object-group service %s\n", setName));

                int index = 0;
                int protocolNum = Integer.valueOf(serviceDTO.getProtocol());
                String protocolString = ProtocolUtils.getProtocolByValue(protocolNum).toLowerCase();
                if (protocolString.equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_ANY)) {
                    dto = new PolicyObjectDTO();
                    dto.setJoin(join);
                    dto.setCommandLine(command);
                    return dto;
                }

                if (protocolString.equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_ICMP)) {
                    sb.append(String.format("%d service icmp ", index));
                    if (StringUtils.isNotBlank(serviceDTO.getType())) {
                        sb.append(String.format("%d ", Integer.valueOf(serviceDTO.getType())));
                    }
                    if (StringUtils.isNotBlank(serviceDTO.getCode())) {
                        sb.append(String.format("%d ", Integer.valueOf(serviceDTO.getCode())));
                    }
                    sb.append("\n");
                    index++;
                } else if ((protocolString.equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_TCP) ||
                        protocolString.equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_UDP))) {
                    String[] srcPorts = serviceDTO.getSrcPorts().split(",");
                    String[] dstPorts = serviceDTO.getDstPorts().split(",");
                    //是TCP/UPD协议
                    //源为any，目的端口有值，则仅显示目的端口
                    if (serviceDTO.getSrcPorts().equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_ANY) && !serviceDTO.getDstPorts().equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_ANY)) {
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

                    } else if (!serviceDTO.getSrcPorts().equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_ANY) && serviceDTO.getDstPorts().equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_ANY)) {
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
                sb.append("quit\n");
                existServiceNameList.add(setName);
            }
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

        StringBuilder joinSb = new StringBuilder();
        for(String objName :existServiceNameList) {
            joinSb.append(String.format("service %s\n", objName));
        }
        dto.setJoin(joinSb.toString());
        dto.setCommandLine(command);
        return dto;
    }

    public int getMaxObejctNameLength() {
        return MAX_OBJECT_NAME_LENGTH;
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
            logger.error("时间转化异常", e);
        }

        return dateStr;
    }

    /**
     * 生成时间区间对象
     *
     * @param startTimeString 开始时间字符串
     * @param endTimeString   结束时间字符串
     * @return 时间区间对象
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
        H3cSecPathV7OPForSDNX h3cv7 = new H3cSecPathV7OPForSDNX();
        String commandLine = h3cv7.composite(dto);
        System.out.println("commandline:\n" + commandLine);
    }

}
