package com.abtnetworks.totems.common.commandline.security;

import com.abtnetworks.totems.common.annotation.CustomCli;
import com.abtnetworks.totems.common.commandline.PolicyGenerator;
import com.abtnetworks.totems.common.commandline.SecurityPolicyGenerator;
import com.abtnetworks.totems.common.constants.PolicyConstants;
import com.abtnetworks.totems.common.dto.*;
import com.abtnetworks.totems.common.dto.commandline.CommandlineDTO;
import com.abtnetworks.totems.common.dto.commandline.PolicyMergeDTO;
import com.abtnetworks.totems.common.dto.commandline.PolicyObjectDTO;
import com.abtnetworks.totems.common.dto.commandline.ServiceDTO;
import com.abtnetworks.totems.common.enums.*;
import com.abtnetworks.totems.common.utils.*;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * @author lps
 */
@Service
@CustomCli(value = DeviceModelNumberEnum.USG6000, type = PolicyEnum.SECURITY)
public class SecurityUsg6000ForFoShan extends SecurityPolicyGenerator implements PolicyGenerator {

    private static Logger logger = Logger.getLogger(SecurityUsg6000ForFoShan.class);

    private final String SOURCE_ADDRESS = "source-address";

    private static final Set<Integer> allowType = new HashSet<>();

    private final String DESTINATION_ADDRESS = "destination-address";

    private static Integer index = 0;

    private final int MAX_NAME_LENGTH = 63;

    private final int V500_MAX_NAME_LENGTH = 63;

    private final int HOUR_SECOND = 60 * 60;

    private static void init() {
        allowType.add(3);
        allowType.add(4);
        allowType.add(5);
        allowType.add(8);
        allowType.add(11);
        allowType.add(12);
        allowType.add(13);
        allowType.add(15);
    }
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
        dto.setSwapRuleNameId(settingDTO.getSwapNameId());
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

        dto.setIdleTimeout(policyDTO.getIdleTimeout());
        // ip类型默认为ipv4
        if(ObjectUtils.isEmpty(dto.getIpType())){
            dto.setIpType(IpTypeEnum.IPV4.getCode());
        }

        GeneratedObjectDTO generatedDto = cmdDTO.getGeneratedObject();
        generatedDto.setPolicyName(dto.getName());
        String command = composite(dto);
        //将生成的对象名字，带回
        generatedDto.setSrcObjectName(dto.getReturnSrcAddrObjectName());
        return command;
    }

    @Override
    public String generatePreCommandline(CommandlineDTO dto) {
        StringBuilder sb = new StringBuilder();
        sb.append("system-view\n");
        if (dto.isVsys()) {
            sb.append("switch vsys " + dto.getVsysName() + "\n");
            sb.append("system-view\n");
        }
        return sb.toString();
    }

    @Override
    public String generateCommandline(CommandlineDTO dto) {
        if (dto.getMergeDTO() == null || dto.isMustCreateFlag()) {
            return createCommandLine(dto);
        } else {
            return editCommandLine(dto);
        }
    }

    private String createCommandLine(CommandlineDTO dto) {
        boolean createObjFlag = dto.isCreateObjFlag();
        PolicyObjectDTO srcAddressObject = generateAddressObject(dto.getSrcIp(), dto.getName(), SOURCE_ADDRESS, createObjFlag, dto.getSrcAddressName(), dto.getSrcIpSystem(), dto.getIpType());

        PolicyObjectDTO dstAddressObject = generateAddressObject(dto.getDstIp(), dto.getName(), DESTINATION_ADDRESS, createObjFlag, dto.getDstAddressName(), dto.getDstIpSystem(), dto.getIpType());

        PolicyObjectDTO serviceObject = generateServiceObject(dto.getServiceList(), createObjFlag, dto.getServiceName(), dto.getIpType());

        PolicyObjectDTO timeObject = generateTimeObject(dto.getStartTime(), dto.getEndTime(), dto.getName());

        StringBuilder sb = new StringBuilder();
        //定义对象
        if (srcAddressObject.isObjectFlag() && StringUtils.isNotBlank(srcAddressObject.getCommandLine())) {
            sb.append(String.format("%s\n", srcAddressObject.getCommandLine()));
            dto.setReturnSrcAddrObjectName(srcAddressObject.getName());
        }

        if (dstAddressObject.isObjectFlag() && StringUtils.isNotBlank(dstAddressObject.getCommandLine())) {
            sb.append(String.format("%s\n", dstAddressObject.getCommandLine()));
        }

        if (serviceObject.isObjectFlag() && StringUtils.isNotBlank(serviceObject.getCommandLine())) {
            sb.append(String.format("%s\n", serviceObject.getCommandLine()));
        }
        if (timeObject != null) {
            sb.append(String.format("%s\n", timeObject.getCommandLine()));
        }

        sb.append("security-policy\n");
        String name = dto.getBusinessName();
        sb.append(String.format("rule name %s\n", name));

        if (!AliStringUtils.isEmpty(dto.getDescription())) {
            sb.append(String.format("description %s\n", dto.getDescription()));
        }

        if (StringUtils.isNotBlank(dto.getSrcZone())) {
            sb.append(String.format("source-zone %s\n", dto.getSrcZone()));
        }
        if (StringUtils.isNotBlank(dto.getDstZone())) {
            sb.append(String.format("destination-zone %s\n", dto.getDstZone()));
        }

        //衔接地址对象名称 或 直接显示内容
        if (StringUtils.isNotBlank(srcAddressObject.getJoin())) {
            sb.append(srcAddressObject.getJoin());
        } else if (StringUtils.isNotBlank(srcAddressObject.getCommandLine())) {
            sb.append(srcAddressObject.getCommandLine());
        }

        if (StringUtils.isNotBlank(dstAddressObject.getJoin())) {
            sb.append(dstAddressObject.getJoin());
        } else if (StringUtils.isNotBlank(dstAddressObject.getCommandLine())) {
            sb.append(dstAddressObject.getCommandLine());
        }

        //衔接服务对象名称 或 直接显示服务对象内容
        if (StringUtils.isNotBlank(serviceObject.getJoin())) {
            sb.append(serviceObject.getJoin());
        } else if (StringUtils.isNotBlank(serviceObject.getCommandLine())) {
            sb.append(serviceObject.getCommandLine());
        }

        if (dto.getIdleTimeout() != null) {
            sb.append("long-link enable\n");
            sb.append(String.format("long-link aging-time %d\n", dto.getIdleTimeout() / HOUR_SECOND));
        }

        if (timeObject != null) {
            sb.append(timeObject.getJoin());
        }


        sb.append(String.format("action %s\n", dto.getAction().toLowerCase()));
        //rule之后添加quit
        sb.append("quit\n");

        //移动位置， 默认在最后 modify by zy 20200618 转向下发那边直接移动，不要命令行中拼接

        String swapRuleNameId = StringUtils.isNotBlank(dto.getSwapRuleNameId()) ? dto.getSwapRuleNameId() : "";
        int moveSeatCode = dto.getMoveSeatEnum().getCode();
        if (moveSeatCode == MoveSeatEnum.FIRST.getCode()) {
            if ( DeviceModelNumberEnum.USG6000.getKey().equalsIgnoreCase(dto.getModelNumber().getKey())) {
                sb.append(String.format("rule move %s top\n", name));
            }
        } else if (moveSeatCode == MoveSeatEnum.BEFORE.getCode() || moveSeatCode == MoveSeatEnum.AFTER.getCode()) {
            if(StringUtils.isNotEmpty(swapRuleNameId)){
                sb.append(String.format("rule move %s %s %s\n", name, dto.getMoveSeatEnum().getKey(), swapRuleNameId));
            }else{
                sb.append("\n");
            }

        }


        sb.append("return\n");

        return sb.toString();
    }

    private String editCommandLine(CommandlineDTO dto) {
        PolicyMergeDTO mergeDTO = dto.getMergeDTO();
        if (mergeDTO == null || StringUtils.isBlank(mergeDTO.getRuleName()) || StringUtils.isBlank(mergeDTO.getMergeField())) {
            logger.info("进行修改策略命令时，合并信息ruleName、mergeField 有为空的");
            return createCommandLine(dto);
        }

        String ruleName = mergeDTO.getRuleName();
        String mergeField = mergeDTO.getMergeField();

        //正式开始编辑
        StringBuilder sb = new StringBuilder();

        boolean createObjFlag = dto.isCreateObjFlag();
        PolicyObjectDTO srcAddressObject = generateAddressObject(dto.getSrcIp(), dto.getName(), SOURCE_ADDRESS, createObjFlag, dto.getSrcAddressName(), dto.getSrcIpSystem(), dto.getIpType());

        PolicyObjectDTO dstAddressObject = generateAddressObject(dto.getDstIp(), dto.getName(), DESTINATION_ADDRESS, createObjFlag, dto.getDstAddressName(), dto.getDstIpSystem(), dto.getIpType());

        PolicyObjectDTO serviceObject = generateServiceObject(dto.getServiceList(), createObjFlag, dto.getServiceName(), dto.getIpType());


        if (mergeField.equals(PolicyConstants.SRC) && srcAddressObject.isObjectFlag() && StringUtils.isNotBlank(srcAddressObject.getCommandLine())) {
            sb.append(srcAddressObject.getCommandLine());
        }
        if (mergeField.equals(PolicyConstants.DST) && dstAddressObject.isObjectFlag() && StringUtils.isNotBlank(dstAddressObject.getCommandLine())) {
            sb.append(dstAddressObject.getCommandLine());
        }
        if (mergeField.equals(PolicyConstants.SERVICE) && serviceObject.isObjectFlag() && StringUtils.isNotBlank(serviceObject.getCommandLine())) {
            sb.append(serviceObject.getCommandLine());
        }

        sb.append("security-policy\n");
        sb.append(String.format("rule name %s\n", ruleName));
        if (mergeField.equals(PolicyConstants.SRC)) {
            if (StringUtils.isNotBlank(srcAddressObject.getJoin())) {
                sb.append(srcAddressObject.getJoin());
            } else {
                sb.append(srcAddressObject.getCommandLine());
            }
        } else if (mergeField.equals(PolicyConstants.DST)) {
            if (StringUtils.isNotBlank(dstAddressObject.getJoin())) {
                sb.append(dstAddressObject.getJoin());
            } else {
                sb.append(dstAddressObject.getCommandLine());
            }
        } else if (mergeField.equals(PolicyConstants.SERVICE)) {
            if (StringUtils.isNotBlank(serviceObject.getJoin())) {
                sb.append(serviceObject.getJoin());
            } else {
                sb.append(serviceObject.getCommandLine());
            }
        }
        sb.append("quit\n");
        sb.append("return\n");

        return sb.toString();
    }

    @Override
    public String generatePostCommandline(CommandlineDTO dto) {
        return "";
    }

    public PolicyObjectDTO generateAddressObject(String ipAddress, String ticket, String ipPrefix, boolean createObjFlag, String existsAddressName, String ipSystem,
                                                 Integer ipType) {
        PolicyObjectDTO dto = new PolicyObjectDTO();

        if (AliStringUtils.isEmpty(ipAddress)) {
            dto.setJoin("");
            dto.setCommandLine("");
            return dto;
        }

        if (StringUtils.isNotBlank(existsAddressName)) {
            dto.setObjectFlag(true);
            existsAddressName = containsQuotes(existsAddressName);
            dto.setJoin(ipPrefix + " address-set " + existsAddressName + " \n");
            return dto;
        }


        dto.setObjectFlag(createObjFlag);
        String[] arr = ipAddress.split(",");
        // 是创建对象
        if (createObjFlag) {
            StringBuilder sb = new StringBuilder();
            sb.append("ip address-set ");
            String objName = "";
            if (arr.length == 1) {
                if(ipType.intValue() == IpTypeEnum.IPV4.getCode()){
                    if (IpUtils.isIPRange(ipAddress)) {
                        String startIp = IpUtils.getStartIpFromRange(ipAddress);
                        String endIp = IpUtils.getEndIpFromRange(ipAddress);
                        objName = String.format("%s-%s\n", startIp, endIp);
                    } else if (IpUtils.isIPSegment(ipAddress)) {
                        String ip = IpUtils.getIpFromIpSegment(ipAddress);
                        String maskBit = IpUtils.getMaskBitFromIpSegment(ipAddress);
                        objName = String.format("%s_%s\n", ip,maskBit);
                    } else {
                        objName = String.format("%s\n", ipAddress);
                    }
                } else {
                    // IPV6
                    if(ipAddress.contains("-")){
                        // 范围
                        String startIp = IpUtils.getRangeStartIPv6(ipAddress);
                        String endIp = IpUtils.getRangeEndIPv6(ipAddress);
                        objName = String.format("%s-%s\n", startIp, endIp);
                    } else if (ipAddress.contains("/")) {
                        // 子网
                        String ip = IpUtils.getIpSegmentStartIPv6(ipAddress);
                        String maskBit = IpUtils.getIpSegmentMaskIPv6(ipAddress);
                        objName = String.format("%s_%s\n", ip,maskBit);
                    } else {
                        objName = String.format("%s\n", ipAddress);
                    }
                }
                objName = containsQuotes(objName);
                dto.setName(objName);
                sb.append(objName);
            } else {
                int num = 0;
                List<String> list = new ArrayList<>(Arrays.asList(arr));
                if (ObjectUtils.isNotEmpty(list)) {
                    num += list.hashCode();

                }
                objName = String.format("address %s", "add_"+Math.abs(num));
                dto.setName(objName);
                sb.append(objName);
                sb.append(" type object\n");
            }
            int index = 0;
            for (String address : arr) {
                String addressCmd ;
                if(ipType.intValue() == IpTypeEnum.IPV4.getCode()){
                    if (IpUtils.isIPRange(address)) {
                        String startIp = IpUtils.getStartIpFromRange(address);
                        String endIp = IpUtils.getEndIpFromRange(address);
                        addressCmd = String.format("address " + index + " range %s %s\n", startIp, endIp);
                    } else if (IpUtils.isIPSegment(address)) {
                        String ip = IpUtils.getIpFromIpSegment(address);
                        String maskBit = IpUtils.getMaskBitFromIpSegment(address);
                        addressCmd = String.format("address " + index + " %s/%s\n", ip, maskBit);
                    } else {
                        addressCmd = String.format("address " + index + " %s \n", address);
                    }
                } else {
                    // IPV6
                    if(address.contains("-")){
                        // 范围
                        String startIp = IpUtils.getRangeStartIPv6(address);
                        String endIp = IpUtils.getRangeEndIPv6(address);
                        addressCmd = String.format("address " + index + " range %s %s\n", startIp, endIp);
                    } else if (address.contains("/")) {
                        // 子网
                        String ip = IpUtils.getIpSegmentStartIPv6(address);
                        String maskBit = IpUtils.getIpSegmentMaskIPv6(address);
                        addressCmd = String.format("address " + index + " %s/%s\n", ip, maskBit);
                    } else {
                        addressCmd = String.format("address " + index + " %s \n", address);
                    }
                }
                index++;
                sb.append(addressCmd);
            }
            sb.append("quit\n");
            dto.setCommandLine(sb.toString());
            dto.setJoin(ipPrefix + " address-set " + dto.getName() + " \n");
        } else {
            //直接显示内容
            StringBuilder sb = new StringBuilder();
            for (String address : arr) {
                String addressCmd = "";
                if(ipType.intValue() == IpTypeEnum.IPV4.getCode()){
                    if (IpUtils.isIPRange(address)) {
                        String startIp = IpUtils.getStartIpFromRange(address);
                        String endIp = IpUtils.getEndIpFromRange(address);
                        addressCmd = ipPrefix + String.format(" range %s %s\n", startIp, endIp);
                    } else if (IpUtils.isIPSegment(address)) {
                        if (!address.equals(PolicyConstants.IPV4_ANY)) {
                            String ip = IpUtils.getIpFromIpSegment(address);
                            String maskBit = IpUtils.getMaskBitFromIpSegment(address);
                            addressCmd = ipPrefix + String.format(" %s %s\n", ip, maskBit);
                        }
                    } else if (IpUtils.isIP(address)) {
                        addressCmd = ipPrefix + String.format(" %s 32\n", address);
                    } else if (address.contains(":")) {
                        if (address.contains("/")) {
                            String[] addrArray = address.split("/");
                            if (StringUtils.isNotEmpty(addrArray[0])) {
                                addressCmd = ipPrefix + String.format(" %s %s\n", addrArray[0].toUpperCase(), addrArray[1]);
                            }
                        } else if (address.contains("-")) {
                            String[] addrArray = address.split("-");
                            if (StringUtils.isNoneEmpty(addrArray[0], addrArray[1])) {
                                addressCmd = ipPrefix + String.format(" range %s %s\n", addrArray[0].toUpperCase(), addrArray[1].toUpperCase());
                            }
                        } else {
                            addressCmd = ipPrefix + String.format(" %s 128\n", address.toUpperCase());
                        }
                    }
                } else {
                    // ipv6
                    if (address.contains("/")) {
                        String[] addrArray = address.split("/");
                        if (StringUtils.isNotEmpty(addrArray[0])) {
                            addressCmd = ipPrefix + String.format(" %s %s\n", addrArray[0].toUpperCase(), addrArray[1]);
                        }
                    } else if (address.contains("-")) {
                        String[] addrArray = address.split("-");
                        if (StringUtils.isNoneEmpty(addrArray[0], addrArray[1])) {
                            addressCmd = ipPrefix + String.format(" range %s %s\n", addrArray[0].toUpperCase(), addrArray[1].toUpperCase());
                        }
                    } else {
                        addressCmd = ipPrefix + String.format(" %s 128\n", address.toUpperCase());
                    }
                }
                sb.append(addressCmd);
            }
            dto.setCommandLine(sb.toString());
        }
        return dto;
    }

    public PolicyObjectDTO generateServiceObject(List<ServiceDTO> serviceDTOList, boolean createObjFlag, String existsServiceName,Integer ipType) {
        PolicyObjectDTO dto = new PolicyObjectDTO();
        if ((serviceDTOList == null || serviceDTOList.isEmpty()) && StringUtils.isBlank(existsServiceName)) {
            return dto;
        }

        if (StringUtils.isNotBlank(existsServiceName)) {
            dto.setObjectFlag(true);
            dto.setJoin(String.format("service %s \n", existsServiceName));
            return dto;
        }

        if (serviceDTOList.size() == 1 && serviceDTOList.get(0).getDstPorts().equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_ANY)) {
            createObjFlag = false;
        }

        dto.setObjectFlag(createObjFlag);

        StringBuilder sb = new StringBuilder();

        List<String> serviceNameList = new ArrayList<>();
        index = 0;
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
                sb.append(String.format("ip service-set %s type object",name));
                sb.append("\n");
                dto.setName(name);

                //当协议为any，则策略中不填service即可
                if (protocolString.equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_ANY)) {
                    return dto;
                }

                if (protocolString.equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_ICMP)) {
                    Integer type = StringUtils.isNotBlank(service.getType()) ? Integer.valueOf(service.getType()) : null;
                    Integer code = StringUtils.isNotBlank(service.getCode()) ? Integer.valueOf(service.getCode()) : 0;
                    sb.append(createIcmpProtocol(type, code, createObjFlag, index, ipType));
                } else {
                    String[] srcPorts = service.getSrcPorts().split(",");
                    String[] dstPorts = service.getDstPorts().split(",");
                    sb.append(createOtherProtocol(protocolString, srcPorts, dstPort, createObjFlag, index));
                }
            }
        }
        if (createObjFlag) {
            sb.append("quit\n");
            StringBuilder joinSb = new StringBuilder();
            for (String objName : serviceNameList) {
                joinSb.append(String.format("service %s\n", objName));
            }
            dto.setJoin(joinSb.toString());
        }
        dto.setCommandLine(sb.toString());
        return dto;
    }

    /**
     * 创建协议对象| 内容
     *
     * @param protocolString 协议：TCP、UDP、ANY
     * @param srcPorts       源端口数组
     * @param dstPort       目的端口数组
     * @return
     * @description 创建对象时，端口为any，也需要显示具体的数字0-65535，不创建对象时，可省略any的端口信息
     */
    private String createOtherProtocol(String protocolString, String[] srcPorts, String dstPort, boolean createObjFlag, Integer num) {
        StringBuffer sb = new StringBuffer();
        for (String srcPort : srcPorts) {
            sb.append("service");
            if (createObjFlag) {
                //创建对象时，有需要序号
                sb.append(" " + num);
            }
            sb.append(String.format(" protocol %s ", protocolString.toUpperCase()));

            if (srcPort.equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_ANY)) {
//                    if(createObjFlag){
//                        sb.append("source-port 0 to 65535 ");
//                    }
            } else if (PortUtils.isPortRange(srcPort)) {
                String startPort = PortUtils.getStartPort(srcPort);
                String endPort = PortUtils.getEndPort(srcPort);
                sb.append(String.format("source-port %s to %s ", startPort, endPort));
            } else {
                sb.append(String.format("source-port %s ", srcPort));
            }

            if (dstPort.equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_ANY)) {
//                    if(createObjFlag) {
//                        sb.append("destination-port 0 to 65535 ");
//                    }
            } else if (PortUtils.isPortRange(dstPort)) {
                String startPort = PortUtils.getStartPort(dstPort);
                String endPort = PortUtils.getEndPort(dstPort);
                sb.append(String.format("destination-port %s to %s ", startPort, endPort));
            } else {
                sb.append(String.format("destination-port %s  ", dstPort));
            }

            sb.append("\n");

        }
        index = num;
        return sb.toString();
    }

    /**
     * 创建imcp协议
     **/
    private String createIcmpProtocol(Integer icmpType, Integer icmpCode, boolean createObjFlag, Integer num,Integer ipType) {
        StringBuffer sb = new StringBuffer();
        sb.append("service");
        if (createObjFlag) {
            //创建对象时，有需要序号
            sb.append(" " + num);
            num++;
            index = num;
        }
        if(ipType.intValue() == IpTypeEnum.IPV4.getCode()){
            sb.append(" icmp ");
        } else {
            sb.append(" icmpv6 ");
        }
        if (icmpType != null) {
            sb.append(String.format("icmp-type %d %d", icmpType, icmpCode));
        }
        sb.append("\n");
        return sb.toString();
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

        String objName = String.format("\"to%s\"",
                TimeUtils.transformDateFormat(endTimeString, TimeUtils.EUROPEAN_TIME_FORMAT, TimeUtils.COMMON_TIME_DAY_FORMAT));
        PolicyObjectDTO dto = new PolicyObjectDTO();
        dto.setName(objName);
        dto.setCommandLine(String.format("time-range %s \nabsolute-range %s to %s \nquit\n", objName, startTime, endTime));
        dto.setJoin(String.format("time-range %s\n", objName));
        return dto;
    }


    private String formatTimeString(String timeString) {
        return TimeUtils.transformDateFormat(timeString, TimeUtils.EUROPEAN_TIME_FORMAT, TimeUtils.HUAWEI_TIME_FORMAT);
    }

    @Override
    public int getMaxNameLength() {
        return MAX_NAME_LENGTH;
    }

    public static void main(String[] args) {
        SecurityUsg6000ForFoShan usg6000 = new SecurityUsg6000ForFoShan();
        CommandlineDTO dto = CommandlineDTO.getInstanceDemo();
        List<String> restSrc = new ArrayList<>();
        restSrc.add("1.1.1.1");
        dto.setRestSrcAddressList(restSrc);
        List<String> restDst = new ArrayList<>();
        restDst.add("2.2.2.2/24");
        dto.setRestDstAddressList(restDst);
        List<ServiceDTO> serviceList = new ArrayList<>();
        ServiceDTO serviceDTO = new ServiceDTO();
        serviceDTO.setProtocol("6");
        serviceDTO.setDstPorts("61-60");
        serviceList.add(serviceDTO);
        /*ServiceDTO serviceDTO2 = new ServiceDTO();
        serviceDTO2.setProtocol("17");
        serviceDTO2.setDstPorts("768");
        serviceDTO2.setSrcPorts("");
        serviceList.add(serviceDTO2);*/
        dto.setRestServiceList(serviceList);
        String commandLine = usg6000.composite(dto);
        System.out.println("commandline:\n" + commandLine);
    }
}
