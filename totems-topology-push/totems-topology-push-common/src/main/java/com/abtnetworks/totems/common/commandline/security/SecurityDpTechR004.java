package com.abtnetworks.totems.common.commandline.security;

import com.abtnetworks.totems.common.commandline.PolicyGenerator;
import com.abtnetworks.totems.common.commandline.SecurityPolicyGenerator;
import com.abtnetworks.totems.common.constants.PolicyConstants;
import com.abtnetworks.totems.common.dto.*;
import com.abtnetworks.totems.common.dto.commandline.CommandlineDTO;
import com.abtnetworks.totems.common.dto.commandline.PolicyMergeDTO;
import com.abtnetworks.totems.common.dto.commandline.PolicyObjectDTO;
import com.abtnetworks.totems.common.dto.commandline.ServiceDTO;
import com.abtnetworks.totems.common.enums.ActionEnum;
import com.abtnetworks.totems.common.enums.IpTypeEnum;
import com.abtnetworks.totems.common.enums.MoveSeatEnum;
import com.abtnetworks.totems.common.utils.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
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
public class SecurityDpTechR004 extends SecurityPolicyGenerator implements PolicyGenerator {

    private static Logger logger = Logger.getLogger(SecurityDpTechR004.class);

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
        dto.setSwapRuleName(settingDTO.getPolicyName());
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


        List<String> exitNameForService = new ArrayList<>();
        List<ServiceDTO> restForService = new ArrayList<>();
        // ???????????????????????????
        covertServiceParam(dto.getServiceName(),dto.getExistServiceNameList(),dto.getRestServiceList() ,restForService ,exitNameForService);

        List<PolicyObjectDTO> serviceObjectList = generateServiceObject(restForService);
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

//        String securityPolicy = String.format("security-policy %s ", dto.getName());
//
//        securityPolicy += String.format("src-zone %s ", AliStringUtils.isEmpty(dto.getSrcZone())?"any":dto.getSrcZone());
//
//            securityPolicy += String.format("dst-zone %s ", AliStringUtils.isEmpty(dto.getDstZone())?"any":dto.getDstZone());
//
//        if (srcAddressObject.getCommandLine() != null && srcAddressObject.getCommandLine().contains("ipv6 address-object")) {
//            sb.append(securityPolicy).append(String.format("src-address ipv6 address-object %s\n", srcAddressObject.getJoin()));
//        } else {
//            sb.append(securityPolicy).append("src-address " + (srcAddressObject.getJoin().equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_ANY)?"any\n":String.format("address-object %s\n", srcAddressObject.getJoin())));
//        }
//        if (dstAddressObject.getCommandLine() != null && dstAddressObject.getCommandLine().contains("ipv6 address-object")) {
//            sb.append(securityPolicy).append(String.format("dst-address ipv6 address-object %s\n", dstAddressObject.getJoin()));
//        } else {
//            sb.append(securityPolicy).append("dst-address " + (dstAddressObject.getJoin().equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_ANY)?"any\n":String.format("address-object %s\n", dstAddressObject.getJoin())));
//        }
        String securityPolicy = "";
        if (IpTypeEnum.IPV6.getCode().equals(dto.getIpType())) {
            sb.append(String.format("ipv6 security-policy %s\n", dto.getName()));
            sb.append(securityPolicy).append(String.format("src-zone %s\n", AliStringUtils.isEmpty(dto.getSrcZone()) ? "any" : dto.getSrcZone()));
            sb.append(securityPolicy).append(String.format("dst-zone %s\n", AliStringUtils.isEmpty(dto.getDstZone()) ? "any" : dto.getDstZone()));
            sb.append(securityPolicy).append(String.format("src-address ipv6 address-object %s\n", srcAddressObject.getJoin()));
            sb.append(securityPolicy).append(String.format("dst-address ipv6 address-object %s\n", dstAddressObject.getJoin()));
        } else if (IpTypeEnum.IPV4.getCode().equals(dto.getIpType())) {
            securityPolicy = String.format("security-policy %s ", dto.getName());

            securityPolicy += String.format("src-zone %s ", AliStringUtils.isEmpty(dto.getSrcZone()) ? "any" : dto.getSrcZone());
            securityPolicy += String.format("dst-zone %s ", AliStringUtils.isEmpty(dto.getDstZone()) ? "any" : dto.getDstZone());
            sb.append(securityPolicy).append("src-address " + (srcAddressObject.getJoin().equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_ANY) ? "any\n" : String.format("address-object %s\n", srcAddressObject.getJoin())));
            sb.append(securityPolicy).append("dst-address " + (dstAddressObject.getJoin().equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_ANY) ? "any\n" : String.format("address-object %s\n", dstAddressObject.getJoin())));
        }
        if(serviceObjectList != null && serviceObjectList.size() > 0) {
            for (PolicyObjectDTO serviceObject : serviceObjectList) {
                sb.append(securityPolicy + String.format("service service-object %s\n", serviceObject.getJoin()));
            }
        }
        if (null != exitNameForService && exitNameForService.size() > 0) {
            for (String existServiceName : exitNameForService) {
                sb.append(securityPolicy + String.format("service service-object %s\n", existServiceName));
            }
        }
        if (CollectionUtils.isEmpty(serviceObjectList) && CollectionUtils.isEmpty(exitNameForService)){
            sb.append(securityPolicy + "service any\n");
        }
        if(timeObject != null) {
            sb.append(securityPolicy + String.format("time-object %s\n", timeObject.getJoin()));
        }

        // ??????004????????????????????????
        if (dto != null && ObjectUtils.isNotEmpty(dto.getIdleTimeout())) {
            sb.append(securityPolicy + String.format("action %s\n", "advanced"));
            sb.append(securityPolicy + String.format("action %s %s\n", "long-session user-defined", dto.getIdleTimeout() / 60));
        } else {
            sb.append(securityPolicy + String.format("action %s\n", dto.getAction().toLowerCase()));
        }

        if (!AliStringUtils.isEmpty(dto.getDescription())) {
            sb.append(securityPolicy + String.format("description %s\n", dto.getDescription()));
        }


        String swapRuleName = StringUtils.isNotBlank(dto.getSwapRuleName()) ? dto.getSwapRuleName() : "";
        int moveSeatCode = dto.getMoveSeatEnum().getCode();
        if (moveSeatCode == MoveSeatEnum.FIRST.getCode() && StringUtils.isNotBlank(swapRuleName)) {
            if (StringUtils.isNotEmpty(swapRuleName)) {
                if (IpTypeEnum.IPV6.getCode().equals(dto.getIpType())) {
                    sb.append(String.format("ipv6 security-policy %s move before %s\n", dto.getName(), swapRuleName));
                } else {
                    sb.append(String.format("security-policy %s move before %s\n", dto.getName(), swapRuleName));
                }
            }
        } else if (moveSeatCode == MoveSeatEnum.BEFORE.getCode() || moveSeatCode == MoveSeatEnum.AFTER.getCode()) {
            if (StringUtils.isNotEmpty(swapRuleName)) {
                if (IpTypeEnum.IPV6.getCode().equals(dto.getIpType())) {
                    sb.append(String.format("ipv6 security-policy %s move %s %s\n", dto.getName(), dto.getMoveSeatEnum().getKey(), swapRuleName));
                } else if (IpTypeEnum.IPV4.getCode().equals(dto.getIpType())) {
                    sb.append(String.format("security-policy %s move %s %s\n", dto.getName(), dto.getMoveSeatEnum().getKey(), swapRuleName));
                }
            }
        }

        return sb.toString();
    }

    public String createMergeCommandLine(CommandlineDTO dto, Integer mergeProperty) {
        PolicyObjectDTO srcAddressObject = new PolicyObjectDTO();
        PolicyObjectDTO dstAddressObject = new PolicyObjectDTO();
        List<PolicyObjectDTO> serviceObjectList = new ArrayList<>();

        List<String> exitNameForService = new ArrayList<>();
        List<ServiceDTO> restForService = new ArrayList<>();

        // ???????????????????????????
        covertServiceParam(dto.getServiceName(),dto.getExistServiceNameList(),dto.getRestServiceList() ,restForService ,exitNameForService);
        if(mergeProperty == 0){
            srcAddressObject = generateAddressObject(dto.getSrcIp(), dto.getName(), dto.getSrcAddressName(), dto.getSrcIpSystem());
        }else if(mergeProperty == 1){
            dstAddressObject = generateAddressObject(dto.getDstIp(), dto.getName(), dto.getDstAddressName(), dto.getDstIpSystem());
        }else if(mergeProperty == 2){
            serviceObjectList = generateServiceObject(restForService);
        }

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
        if (null != exitNameForService && exitNameForService.size() > 0) {
            for (String existServiceName : exitNameForService) {
                sb.append(securityPolicy).append(String.format("service service-object %s\n", existServiceName));
            }
        }

        // ??????004????????????????????????
        if(ObjectUtils.isNotEmpty(dto.getIdleTimeout())){
            sb.append(securityPolicy).append(String.format("action %s\n", "advanced"));
            sb.append(securityPolicy).append(String.format("action %s %s\n", "long-session user-defined", dto.getIdleTimeout() / 60));
        } else {
            sb.append(securityPolicy).append(String.format("action %s\n", dto.getAction()));
        }
        return sb.toString();
    }

    public String editCommandLine(CommandlineDTO dto) {
        PolicyMergeDTO mergeDTO = dto.getMergeDTO();
        if (mergeDTO == null || StringUtils.isBlank(mergeDTO.getRuleName()) || StringUtils.isBlank(mergeDTO.getMergeField())) {
            logger.info("??????????????????????????????????????????ruleName???mergeField ????????????");
            return createCommandLine(dto);
        }

        String ruleName = mergeDTO.getRuleName();
        String mergeField =  mergeDTO.getMergeField();

        List<String> exitNameForService = new ArrayList<>();
        List<ServiceDTO> restForService = new ArrayList<>();
        // ???????????????????????????
        covertServiceParam(dto.getServiceName(),dto.getExistServiceNameList(),dto.getRestServiceList() ,restForService ,exitNameForService);

        //??????????????????
        StringBuilder sb = new StringBuilder();
        PolicyObjectDTO srcAddressObject = generateAddressObject(dto.getSrcIp(), dto.getName(), dto.getSrcAddressName(), dto.getSrcIpSystem());
        PolicyObjectDTO dstAddressObject = generateAddressObject(dto.getDstIp(), dto.getName(), dto.getDstAddressName(), dto.getDstIpSystem());
        List<PolicyObjectDTO> serviceObjectList = generateServiceObject(restForService);

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

            if (null != exitNameForService && exitNameForService.size() > 0) {
                for (String existServiceName : exitNameForService) {
                    sb.append(String.format("security-policy %s service service-object %s\n", ruleName, existServiceName));
                }
            }
        }

        return sb.toString();
    }

    @Override
    public String generatePostCommandline(CommandlineDTO dto) {
        return "";
    }

    /**
     * ??????????????????
     * @param ipAddress ip??????
     * @return ????????????
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
        String setName;
        if(StringUtils.isNotEmpty(ipSystem)){
            setName = ipSystem;
            // ???????????????????????????????????????2?????????
            setName = strSub(setName, getMaxObejctNameLength(),"GB2312");
            // ????????????????????????
            int len = 0;
            try{
                len = setName.getBytes("GB2312").length;
            }catch (Exception e) {
                logger.error("???????????????????????????");
            }
            if(len > getMaxObejctNameLength() -7 ) {
                setName = strSub(setName, getMaxObejctNameLength() -7, "GB2312");
            }
            setName = String.format("%s_%s", setName, DateUtils.getDate().replace("-","").substring(2));
        } else {
            setName = String.format("%s_AO_%s", ticket, IdGen.getRandomNumberString());
        }
        for (String address : arr) {
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

        dto.setName(setName);
        dto.setJoin(setName);
        dto.setCommandLine(command);
        return dto;
    }

    /**
     * ??????????????????????????????????????????????????????
     * @param serviceObjectName
     * @param existServiceNameList
     * @param restServiceList
     * @param restForService
     * @param exitNameForService
     */
    public void covertServiceParam(String serviceObjectName,List<String> existServiceNameList,List<ServiceDTO> restServiceList ,List<ServiceDTO> restForService ,List<String> exitNameForService){
        if(StringUtils.isNotBlank(serviceObjectName)){
            exitNameForService.clear();
            exitNameForService.add(serviceObjectName);
        }else {
            if(CollectionUtils.isNotEmpty(existServiceNameList)){
                exitNameForService.addAll(existServiceNameList);
            }
            if(CollectionUtils.isNotEmpty(restServiceList)){
                restForService.addAll(restServiceList);
            }

        }
    }

    /**
     * ???????????????????????????
     * @param serviceDTOList ??????
     * @return ???????????????
     */
    public List<PolicyObjectDTO> generateServiceObject(List<ServiceDTO> serviceDTOList) {
        List<PolicyObjectDTO> policyObjectList = new ArrayList<>();

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
                        String setName = getServiceNameByOne(protocolString, dstPort);
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
     * ????????????????????????
     * @param startTimeString ?????????????????????
     * @param endTimeString ?????????????????????
     * @return ??????????????????
     */
    public PolicyObjectDTO generateTimeObject(String startTimeString, String endTimeString, String ticket) {
        if(AliStringUtils.isEmpty(startTimeString) || AliStringUtils.isEmpty(endTimeString)) {
            return null;
        }

        String startTime = formatTimeString(startTimeString);
        String endTime = formatTimeString(endTimeString);

        String setName = String.format("%s_TR_%s", ticket, IdGen.getRandomNumberString());

        PolicyObjectDTO dto = new PolicyObjectDTO();
        dto.setName(setName);
        dto.setJoin(setName);
        dto.setCommandLine(String.format("time-object %s absolute start %s end %s ", setName, startTime, endTime));
        return dto;
    }



    private String formatTimeString(String timeString) {
        return TimeUtils.transformDateFormat(timeString, TimeUtils.EUROPEAN_TIME_FORMAT, TimeUtils.DPTECH_FIREWALL_FORMAT);
    }

    public static void main(String[] args) {
        CommandlineDTO dto = CommandlineDTO.getInstanceDemo();
        SecurityDpTechR004 usg6000 = new SecurityDpTechR004();
        String commandLine = usg6000.composite(dto);
        System.out.println("commandline:\n" + commandLine);

    }
}
