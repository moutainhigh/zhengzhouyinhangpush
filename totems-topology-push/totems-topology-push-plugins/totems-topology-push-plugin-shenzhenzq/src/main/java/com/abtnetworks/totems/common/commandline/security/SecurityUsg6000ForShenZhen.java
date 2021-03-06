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

import java.util.ArrayList;
import java.util.List;

/**
 * @author lps
 */
@Service
@CustomCli(value = DeviceModelNumberEnum.USG6000, type = PolicyEnum.SECURITY)
public class SecurityUsg6000ForShenZhen extends SecurityPolicyGenerator implements PolicyGenerator {

    private static Logger logger = Logger.getLogger(SecurityUsg6000ForShenZhen.class);

    private final String SOURCE_ADDRESS = "source-address";

    private final String DESTINATION_ADDRESS = "destination-address";

    private static Integer index = 0;

    private final int MAX_NAME_LENGTH = 63;

    private final int V500_MAX_NAME_LENGTH = 63;

    private final int HOUR_SECOND = 60 * 60;

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
        // ip???????????????ipv4
        if(ObjectUtils.isEmpty(dto.getIpType())){
            dto.setIpType(IpTypeEnum.IPV4.getCode());
        }

        GeneratedObjectDTO generatedDto = cmdDTO.getGeneratedObject();
        generatedDto.setPolicyName(dto.getName());
        String command = composite(dto);
        //?????????????????????????????????
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
        //????????????
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

        //???????????????????????? ??? ??????????????????
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

        //???????????????????????? ??? ??????????????????????????????
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
        //rule????????????quit
        sb.append("quit\n");

        //??????????????? ??????????????? modify by zy 20200618 ?????????????????????????????????????????????????????????

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
            logger.info("??????????????????????????????????????????ruleName???mergeField ????????????");
            return createCommandLine(dto);
        }

        String ruleName = mergeDTO.getRuleName();
        String mergeField = mergeDTO.getMergeField();

        //??????????????????
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

        sb.append("policy logging\n");
        sb.append("session logging\n");

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
        // ???????????????
        if (createObjFlag) {
            List<String> addressNameList = new ArrayList<>();
            StringBuilder sb = new StringBuilder();
            String objName = "";
            for (String address : arr) {
                String addressCmd ;
                sb.append("ip address-set ");
                if(ipType.intValue() == IpTypeEnum.IPV4.getCode()){
                    if (IpUtils.isIPRange(address)) {
                        String startIp = IpUtils.getStartIpFromRange(address);
                        String endIp = IpUtils.getEndIpFromRange(address);
                        addressCmd = String.format("address range %s %s\n", startIp, endIp);
                        objName = String.format("%s-%s/", startIp, endIp);
                    } else if (IpUtils.isIPSegment(address)) {
                        String ip = IpUtils.getIpFromIpSegment(address);
                        String maskBit = IpUtils.getMaskBitFromIpSegment(address);
                        addressCmd = String.format("address %s mask %s\n", ip, maskBit);
                        objName = String.format("%s/%s/", ip,maskBit);
                    } else {
                        addressCmd = String.format("address %s 0\n", address);
                        objName = String.format("%s/", address);
                    }
                } else {
                    // IPV6
                    if(address.contains("-")){
                        // ??????
                        String startIp = IpUtils.getRangeStartIPv6(address);
                        String endIp = IpUtils.getRangeEndIPv6(address);
                        addressCmd = String.format("address range %s %s\n", startIp, endIp);
                        objName = String.format("%s-%s/", startIp, endIp);
                    } else if (address.contains("/")) {
                        // ??????
                        String ip = IpUtils.getIpSegmentStartIPv6(address);
                        String maskBit = IpUtils.getIpSegmentMaskIPv6(address);
                        addressCmd = String.format("address %s %s\n", ip, maskBit);
                        objName = String.format("%s/%s/", ip,maskBit);
                    } else {
                        addressCmd = String.format("address %s 128\n", address);
                        objName = String.format("%s/", address);
                    }
                }
                addressNameList.add(objName);
                sb.append(String.format("%s\n",objName));
                sb.append(addressCmd);
            }
            sb.append("quit\n");
            dto.setCommandLine(sb.toString());
            StringBuilder joinSb = new StringBuilder();

            for (String addrName : addressNameList) {
                joinSb.append(String.format("%s address-set %s\n",ipPrefix,addrName));
            }
            dto.setJoin(joinSb+"");
        } else {
            //??????????????????
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
                String name = String.format("\"%s_%s\"", protocolString, dstPort);

                if (PortUtils.isPortRange(dstPort)) {
                    String start = PortUtils.getStartPort(dstPort);
                    String end = PortUtils.getEndPort(dstPort);
                    name = String.format("\"%s_%s-%s\"", protocolString, start, end);

                }
                serviceNameList.add(name);
                sb.append(String.format("ip service-set %s type object",name));
                sb.append("\n");
                dto.setName(name);

                //????????????any?????????????????????service??????
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
     * ??????????????????| ??????
     *
     * @param protocolString ?????????TCP???UDP???ANY
     * @param srcPorts       ???????????????
     * @param dstPort       ??????????????????
     * @return
     * @description ???????????????????????????any?????????????????????????????????0-65535?????????????????????????????????any???????????????
     */
    private String createOtherProtocol(String protocolString, String[] srcPorts, String dstPort, boolean createObjFlag, Integer num) {
        StringBuffer sb = new StringBuffer();
        for (String srcPort : srcPorts) {
            sb.append("service");
            if (createObjFlag) {
                //?????????????????????????????????
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
     * ??????imcp??????
     **/
    private String createIcmpProtocol(Integer icmpType, Integer icmpCode, boolean createObjFlag, Integer num,Integer ipType) {
        StringBuffer sb = new StringBuffer();
        sb.append("service");
        if (createObjFlag) {
            //?????????????????????????????????
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

        String objName = String.format("%s_TR_%s", ticket, IdGen.getRandomNumberString());

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
        SecurityUsg6000ForShenZhen usg6000 = new SecurityUsg6000ForShenZhen();
        CommandlineDTO dto = CommandlineDTO.getInstanceDemo();

        String commandLine = usg6000.composite(dto);
        System.out.println("commandline:\n" + commandLine);
    }
}
