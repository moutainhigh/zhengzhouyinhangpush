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
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * @Author: wenjiachang
 * @Date: 2019/1/4 18:12
 */
@Slf4j
@Service
@CustomCli(value = DeviceModelNumberEnum.DPTECHR004, type = PolicyEnum.SECURITY)
public class SecurityDpTechR004ForHunanDL extends SecurityPolicyGenerator implements PolicyGenerator {

    private static Logger logger = Logger.getLogger(SecurityDpTechR004ForHunanDL.class);

    public final int MAX_OBJECT_NAME_LENGTH = 44;

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
        if(dto.getMergeDTO() == null || dto.isMustCreateFlag()) {
            return createCommandLine(dto);
        }else{
            return editCommandLine(dto);
        }
    }


    public String createCommandLine(CommandlineDTO dto) {
        PolicyObjectDTO srcAddressObject = generateAddressObject(dto.getSrcIp(), dto.getName(), dto.getSrcAddressName(), dto.getSrcIpSystem());
        PolicyObjectDTO dstAddressObject = generateAddressObject(dto.getDstIp(), dto.getName(), dto.getDstAddressName(), dto.getDstIpSystem());
        List<PolicyObjectDTO> serviceObjectList = generateServiceObject(dto.getServiceList(), dto.getServiceName(),dto.getIdleTimeout());
        PolicyObjectDTO timeObject = generateTimeObject(dto.getStartTime(), dto.getEndTime(), dto.getName());

        StringBuilder sb = new StringBuilder();

        if(srcAddressObject.isObjectFlag() && StringUtils.isNotBlank(srcAddressObject.getCommandLine())) {
            sb.append(String.format("%s\n", srcAddressObject.getCommandLine()));
        }
        if(dstAddressObject.isObjectFlag() && StringUtils.isNotBlank(dstAddressObject.getCommandLine())) {
            sb.append(String.format("%s\n", dstAddressObject.getCommandLine()));
        }
        if(serviceObjectList != null && serviceObjectList.size() > 0) {
            for (PolicyObjectDTO serviceObject : serviceObjectList) {
                if(serviceObject.isObjectFlag() && StringUtils.isNotBlank(serviceObject.getCommandLine())) {
                    sb.append(serviceObject.getCommandLine());
                }
            }
            sb.append("\n");
        }
        if(timeObject != null) {
            sb.append(timeObject.getCommandLine());
            sb.append("\n");
        }

        String securityPolicy = String.format("security-policy %s ", dto.getName());

        securityPolicy += String.format("src-zone %s ", AliStringUtils.isEmpty(dto.getSrcZone())?"any":dto.getSrcZone());

            securityPolicy += String.format("dst-zone %s ", AliStringUtils.isEmpty(dto.getDstZone())?"any":dto.getDstZone());

        for (String join : srcAddressObject.getJoin().split(",")) {
            if (srcAddressObject.getCommandLine() != null && srcAddressObject.getCommandLine().contains("ipv6 address-object")) {
                sb.append(securityPolicy).append(String.format("src-address ipv6 address-object %s\n", join));
            } else {
                sb.append(securityPolicy).append("src-address " + (join.equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_ANY)?"any\n":String.format("address-object %s\n", join)));
            }
        }

        for (String join : dstAddressObject.getJoin().split(",")) {
            if (dstAddressObject.getCommandLine() != null && dstAddressObject.getCommandLine().contains("ipv6 address-object")) {
                sb.append(securityPolicy).append(String.format("dst-address ipv6 address-object %s\n", join));
            } else {
                sb.append(securityPolicy).append("dst-address " + (join.equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_ANY) ? "any\n" : String.format("address-object %s\n", join)));
            }
        }
        if(serviceObjectList != null && serviceObjectList.size() > 0) {
            for (PolicyObjectDTO serviceObject : serviceObjectList) {
                for (String join : serviceObject.getJoin().split(",")) {
                    sb.append(securityPolicy + String.format("service service-object %s\n", join));
                }
            }
        }else{
            sb.append(securityPolicy + "service any\n");
        }
        if(timeObject != null) {
            sb.append(securityPolicy + String.format("time-object %s\n", timeObject.getJoin()));
        }

        // 迪普004长连接命令行调整
        if(dto != null && ObjectUtils.isNotEmpty(dto.getIdleTimeout())){
            sb.append(securityPolicy + String.format("action %s\n", "advanced"));
            sb.append(securityPolicy + String.format("action %s %s\n", "long-session user-defined", dto.getIdleTimeout()/60 ));
        } else {
            sb.append(securityPolicy + String.format("action %s\n", dto.getAction()));
        }

        if(!AliStringUtils.isEmpty(dto.getDescription())){
            sb.append(securityPolicy + String.format("description %s\n", dto.getDescription()));
        }


        String swapRuleNameId = StringUtils.isNotBlank(dto.getSwapRuleNameId()) ? dto.getSwapRuleNameId() : "";
        int moveSeatCode = dto.getMoveSeatEnum().getCode();
        if (moveSeatCode == MoveSeatEnum.FIRST.getCode() && StringUtils.isNotBlank(swapRuleNameId)) {
            if(StringUtils.isNotEmpty(swapRuleNameId)) {
                sb.append(String.format("security-policy %s move before %s\n", dto.getName(), swapRuleNameId));
            }
        } else if (moveSeatCode == MoveSeatEnum.BEFORE.getCode() || moveSeatCode == MoveSeatEnum.AFTER.getCode()) {
            if(StringUtils.isNotEmpty(swapRuleNameId)) {
                sb.append(String.format("security-policy %s move %s %s\n", dto.getName(), dto.getMoveSeatEnum().getKey(), swapRuleNameId));
            }
        }

        return sb.toString();
    }

    public String createCommandLine(CommandlineDTO dto,Integer mergeProperty) {
        PolicyObjectDTO srcAddressObject = new PolicyObjectDTO();
        PolicyObjectDTO dstAddressObject = new PolicyObjectDTO();
        List<PolicyObjectDTO> serviceObjectList = new ArrayList<>();
        if(mergeProperty == 0){
            srcAddressObject = generateAddressObject(dto.getSrcIp(), dto.getName(), dto.getSrcAddressName(), dto.getSrcIpSystem());
        }else if(mergeProperty == 1){
            dstAddressObject = generateAddressObject(dto.getDstIp(), dto.getName(), dto.getDstAddressName(), dto.getDstIpSystem());
        }else if(mergeProperty == 2){
            serviceObjectList = generateServiceObject(dto.getServiceList(), dto.getServiceName(),dto.getIdleTimeout());
        }
        PolicyObjectDTO timeObject = generateTimeObject(dto.getStartTime(), dto.getEndTime(), dto.getName());

        StringBuilder sb = new StringBuilder();

        if(srcAddressObject.isObjectFlag() && StringUtils.isNotBlank(srcAddressObject.getCommandLine())) {
            sb.append(String.format("%s\n", srcAddressObject.getCommandLine()));
        }
        if(dstAddressObject.isObjectFlag() && StringUtils.isNotBlank(dstAddressObject.getCommandLine())) {
            sb.append(String.format("%s\n", dstAddressObject.getCommandLine()));
        }
        if(serviceObjectList != null && serviceObjectList.size() > 0) {
            for (PolicyObjectDTO serviceObject : serviceObjectList) {
                if(serviceObject.isObjectFlag() && StringUtils.isNotBlank(serviceObject.getCommandLine())) {
                    sb.append(serviceObject.getCommandLine());
                }
            }
            sb.append("\n");
        }
        if(timeObject != null) {
            sb.append(timeObject.getCommandLine());
            sb.append("\n");
        }

        String securityPolicy = String.format("security-policy %s ", dto.getName());

        securityPolicy += String.format("src-zone %s ", AliStringUtils.isEmpty(dto.getSrcZone())?"any":dto.getSrcZone());

        securityPolicy += String.format("dst-zone %s ", AliStringUtils.isEmpty(dto.getDstZone())?"any":dto.getDstZone());

        if (srcAddressObject.getCommandLine() != null && srcAddressObject.getCommandLine().contains("ipv6 address-object")) {
            sb.append(securityPolicy).append(String.format("src-address ipv6 address-object %s\n", srcAddressObject.getJoin()));
        } else if (mergeProperty == 0){
            sb.append(securityPolicy).append("src-address ").append(srcAddressObject.getJoin().equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_ANY) ? "any\n" : String.format("address-object %s\n", srcAddressObject.getJoin()));
        }
        if (dstAddressObject.getCommandLine() != null && dstAddressObject.getCommandLine().contains("ipv6 address-object")) {
            sb.append(securityPolicy).append(String.format("dst-address ipv6 address-object %s\n", dstAddressObject.getJoin()));
        } else if(mergeProperty == 1){
            sb.append(securityPolicy).append("dst-address ").append(dstAddressObject.getJoin().equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_ANY) ? "any\n" : String.format("address-object %s\n", dstAddressObject.getJoin()));
        }
        if(serviceObjectList != null && serviceObjectList.size() > 0) {
            for (PolicyObjectDTO serviceObject : serviceObjectList) {
                sb.append(securityPolicy).append(String.format("service service-object %s\n", serviceObject.getJoin()));
            }
        }else if (mergeProperty == 2){
            sb.append(securityPolicy).append("service any\n");
        }
        if(timeObject != null) {
            sb.append(securityPolicy).append(String.format("time-object %s\n", timeObject.getJoin()));
        }

        // 迪普004长连接命令行调整
        if(ObjectUtils.isNotEmpty(dto.getIdleTimeout())){
            sb.append(securityPolicy).append(String.format("action %s\n", "advanced"));
            sb.append(securityPolicy).append(String.format("action %s %s\n", "long-session user-defined", dto.getIdleTimeout() / 60));
        } else {
            sb.append(securityPolicy).append(String.format("action %s\n", dto.getAction()));
        }

        if(!AliStringUtils.isEmpty(dto.getDescription())){
            sb.append(securityPolicy).append(String.format("description %s\n", dto.getDescription()));
        }


        String swapRuleNameId = StringUtils.isNotBlank(dto.getSwapRuleNameId()) ? dto.getSwapRuleNameId() : "";
        int moveSeatCode = dto.getMoveSeatEnum().getCode();
        if (moveSeatCode == MoveSeatEnum.FIRST.getCode() && StringUtils.isNotBlank(swapRuleNameId)) {
            if(StringUtils.isNotEmpty(swapRuleNameId)) {
                sb.append(String.format("security-policy %s move before %s\n", dto.getName(), swapRuleNameId));
            }
        } else if (moveSeatCode == MoveSeatEnum.BEFORE.getCode() || moveSeatCode == MoveSeatEnum.AFTER.getCode()) {
            if(StringUtils.isNotEmpty(swapRuleNameId)) {
                sb.append(String.format("security-policy %s move %s %s\n", dto.getName(), dto.getMoveSeatEnum().getKey(), swapRuleNameId));
            }
        }

        return sb.toString();
    }

    public String editCommandLine(CommandlineDTO dto) {
        PolicyMergeDTO mergeDTO = dto.getMergeDTO();
        if (mergeDTO == null || StringUtils.isBlank(mergeDTO.getRuleName()) || StringUtils.isBlank(mergeDTO.getMergeField())) {
            logger.info("进行修改策略命令时，合并信息ruleName、mergeField 有为空的");
            return createCommandLine(dto);
        }

        String ruleName = mergeDTO.getRuleName();
        String mergeField =  mergeDTO.getMergeField();

        //正式开始编辑
        StringBuilder sb = new StringBuilder();
        PolicyObjectDTO srcAddressObject = generateAddressObject(dto.getSrcIp(), dto.getName(), dto.getSrcAddressName(), dto.getSrcIpSystem());
        PolicyObjectDTO dstAddressObject = generateAddressObject(dto.getDstIp(), dto.getName(), dto.getDstAddressName(), dto.getDstIpSystem());
        List<PolicyObjectDTO> serviceObjectList = generateServiceObject(dto.getServiceList(), dto.getServiceName(),dto.getIdleTimeout());

        if(mergeField.equals(PolicyConstants.SRC)) {
            if(srcAddressObject.isObjectFlag() && StringUtils.isNotBlank(srcAddressObject.getCommandLine())){
                sb.append(String.format("%s\n", srcAddressObject.getCommandLine()));
            }
            sb.append(String.format("security-policy %s src-address address-object %s", ruleName, srcAddressObject.getJoin()));
        }
        if(mergeField.equals(PolicyConstants.DST)) {
            if(dstAddressObject.isObjectFlag() && StringUtils.isNotBlank(dstAddressObject.getCommandLine())){
                sb.append(String.format("%s\n", dstAddressObject.getCommandLine()));
            }
            sb.append(String.format("security-policy %s dst-address address-object %s", ruleName, dstAddressObject.getJoin()));
        }
        if(mergeField.equals(PolicyConstants.SERVICE) && serviceObjectList != null && serviceObjectList.size() > 0) {
            for (PolicyObjectDTO serviceObject : serviceObjectList) {
                if(serviceObject.isObjectFlag() && StringUtils.isNotBlank(serviceObject.getCommandLine())){
                    sb.append(serviceObject.getCommandLine());
                }
            }
            sb.append("\n");

        }

        if(mergeField.equals(PolicyConstants.SERVICE)) {
            if(serviceObjectList != null && serviceObjectList.size() > 0) {
                for (PolicyObjectDTO serviceObject : serviceObjectList) {
                    sb.append(String.format("security-policy %s service service-object %s\n", ruleName, serviceObject.getJoin()));
                }
            }else{
                sb.append(String.format("security-policy %s service any\n"));
            }
        }

        return sb.toString();
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
    public PolicyObjectDTO generateAddressObject(String ipAddress, String ticket, String existsAddressName, String ipSystem) {
        PolicyObjectDTO dto = new PolicyObjectDTO();
        dto.setObjectFlag(true);

        if(AliStringUtils.isEmpty(ipAddress)) {
            dto.setJoin("any");
            return dto;
        }

        if (StringUtils.isNotBlank(existsAddressName)) {
            dto.setJoin(existsAddressName);
            return dto;
        }

        String addressCmd = "";

        StringBuilder sb = new StringBuilder();

        String[] arr = ipAddress.split(",");
        boolean isIpv6 = false;
        int index = 1;
        StringBuilder nameSb = new StringBuilder();
        for (String address : arr) {
            String setName = getIPNameKevin(1,address,true,ipSystem,"",arr.length,index);
            nameSb.append(",").append(setName);
            index++;
            sb.append(String.format("address-object %s ", setName));
            if (IpUtils.isIPRange(address)) {
                String startIp = IpUtils.getStartIpFromRange(address);
                String endIp = IpUtils.getEndIpFromRange(address);
                addressCmd = String.format("range %s %s\n", startIp, endIp);
            } else if (IpUtils.isIPSegment(address)) {
                addressCmd = String.format("%s\n", address);
            } else if (IpUtils.isIP(address)) {
                addressCmd = String.format("%s\n", address + "/32");
            } else if (address.contains(":")) {
                isIpv6 = true;
                //ipv6
                if (address.contains("/")) {
                    addressCmd = String.format("%s\n", address);
                } else if (address.contains("-")) {
                    String[] addrArray = address.split("-");
                    if (StringUtils.isNoneEmpty(addrArray[0], addrArray[1])) {
                        addressCmd = String.format("range %s %s\n", addrArray[0], addrArray[1]);
                    }
                } else {
                    addressCmd = String.format("%s/128\n", address);
                }
            }

            sb.append(addressCmd);
        }

        String command = sb.toString();
        if (isIpv6) {
            command = command.replaceAll("address-object", "ipv6 address-object");
        }
        if (nameSb.length() > 0) {
            nameSb.deleteCharAt(0);
        }

        dto.setName(nameSb.toString());
        dto.setJoin(nameSb.toString());
        dto.setCommandLine(command);
        return dto;
    }

    /**
     * 获取服务集对象文本
     * @param serviceDTOList 协议
     * @return 服务集对象
     */
    public List<PolicyObjectDTO> generateServiceObject(List<ServiceDTO> serviceDTOList, String existsServiceName,Integer idleTimeout) {
        List<PolicyObjectDTO> policyObjectList = new ArrayList<>();
        if(StringUtils.isNotBlank(existsServiceName)){
            PolicyObjectDTO dto = new PolicyObjectDTO();
            dto.setObjectFlag(true);
            dto.setJoin(existsServiceName);
            policyObjectList.add(dto);
            return policyObjectList;
        }

        for(ServiceDTO service : serviceDTOList) {
            int protocolNum = Integer.valueOf(service.getProtocol());
            String protocolString = ProtocolUtils.getProtocolByValue(protocolNum).toLowerCase();
            if (protocolString.equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_ANY)) {
                return null;
            }

            if(protocolString.equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_ICMP)) {
                StringBuilder sb = new StringBuilder();
                String setName = getServiceName(service);
                sb.append(String.format("service-object %s protocol icmp ", setName));
                if (StringUtils.isNotBlank(service.getType()) && StringUtils.isNotBlank(service.getCode())) {
                    sb.append(String.format("type %d code %d\n", Integer.valueOf(service.getType()), Integer.valueOf(service.getCode())));
                }
                sb.append("\n");
                PolicyObjectDTO dto = new PolicyObjectDTO();
                dto.setObjectFlag(true);
                dto.setName(setName);
                dto.setJoin(setName);
                dto.setCommandLine(sb.toString());
                policyObjectList.add(dto);
            } else {
                String[] srcPorts = service.getSrcPorts().split(",");
                String[] dstPorts = service.getDstPorts().split(",");

                for (String srcPort : srcPorts) {
                    for (String dstPort : dstPorts) {
                        StringBuilder sb = new StringBuilder();
                        String setName = getServiceNameKevin(protocolString, dstPort,idleTimeout);
                        sb.append(String.format("service-object %s protocol %s ", setName, protocolString));
                        if (!srcPort.equals(PolicyConstants.POLICY_STR_VALUE_ANY)
                                || !dstPort.equals(PolicyConstants.POLICY_STR_VALUE_ANY)) {
                            if (srcPort.equals(PolicyConstants.POLICY_STR_VALUE_ANY)) {
                                sb.append("src-port 0 to 65535 ");
                            } else if (PortUtils.isPortRange(srcPort)) {
                                String startPort = PortUtils.getStartPort(srcPort);
                                String endPort = PortUtils.getEndPort(srcPort);
                                sb.append(String.format("src-port %s to %s ", startPort, endPort));
                            } else {
                                sb.append(String.format("src-port %s ", srcPort));
                            }

                            if (dstPort.equals(PolicyConstants.POLICY_STR_VALUE_ANY)) {
                                sb.append("dst-port 0 to 65535 ");
                            } else if (PortUtils.isPortRange(dstPort)) {
                                String startPort = PortUtils.getStartPort(dstPort);
                                String endPort = PortUtils.getEndPort(dstPort);
                                sb.append(String.format("dst-port %s to %s ", startPort, endPort));
                            } else {
                                sb.append(String.format("dst-port %s  ", dstPort));
                            }
                        }
                        sb.append("\n");
                        PolicyObjectDTO dto = new PolicyObjectDTO();
                        dto.setObjectFlag(true);
                        dto.setName(setName);
                        dto.setJoin(setName);
                        dto.setCommandLine(sb.toString());
                        policyObjectList.add(dto);
                    }
                }
            }
        }

        return policyObjectList;
    }

    public int getMaxObejctNameLength() {
        return MAX_OBJECT_NAME_LENGTH;
    }

    /**
     * 生成时间区间对象
     * @param startTimeString 开始时间字符串
     * @param endTimeString 结束时间字符串
     * @return 时间区间对象
     */
    public PolicyObjectDTO generateTimeObject(String startTimeString, String endTimeString, String ticket) {
        if(AliStringUtils.isEmpty(startTimeString) || AliStringUtils.isEmpty(endTimeString)) {
            return null;
        }

        String startTime = formatTimeString(startTimeString);
        String endTime = formatTimeString(endTimeString);

//        String setName = String.format("%s_TR_%s", ticket, IdGen.getRandomNumberString());
//        String setName = String.format("to_%s", endTime);
        String setName = String.format("to%s",
                TimeUtils.transformDateFormat(endTimeString, TimeUtils.EUROPEAN_TIME_FORMAT, TimeUtils.COMMON_TIME_DAY_FORMAT));

        PolicyObjectDTO dto = new PolicyObjectDTO();
        dto.setName(setName);
        dto.setJoin(setName);
        dto.setCommandLine(String.format("time-object %s absolute start %s end %s ", setName, startTime, endTime));
        return dto;
    }



    private String formatTimeString(String timeString) {
        return TimeUtils.transformDateFormat(timeString, TimeUtils.EUROPEAN_TIME_FORMAT, TimeUtils.DPTECH_FIREWALL_FORMAT);
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
        name = String.format("%s%s", prefix, dstPorts);
        if (idleTimeout != null) {
            name = String.format("%s%sL", prefix, dstPorts);
        }
        if (PortUtils.isPortRange(dstPorts)) {
            String start = PortUtils.getStartPort(dstPorts);
            String end = PortUtils.getEndPort(dstPorts);
            name = String.format("%s%s%s%s", prefix, start, line, end);
            if (idleTimeout != null) {
                name = String.format("%s%s%s%sL", prefix, start,line,  end);
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


    public static void main(String[] args) {
        CommandlineDTO dto = CommandlineDTO.getInstanceDemo();
        SecurityDpTechR004ForHunanDL usg6000 = new SecurityDpTechR004ForHunanDL();
        String commandLine = usg6000.composite(dto);
        System.out.println("commandline:\n" + commandLine);

    }
}
