package com.abtnetworks.totems.common.commandline.security;

import com.abtnetworks.totems.common.annotation.CustomCli;
import com.abtnetworks.totems.common.commandline.PolicyGenerator;
import com.abtnetworks.totems.common.commandline.SecurityPolicyGenerator;
import com.abtnetworks.totems.common.constants.PolicyConstants;
import com.abtnetworks.totems.common.dto.CmdDTO;
import com.abtnetworks.totems.common.dto.DeviceDTO;
import com.abtnetworks.totems.common.dto.ExistObjectDTO;
import com.abtnetworks.totems.common.dto.GeneratedObjectDTO;
import com.abtnetworks.totems.common.dto.PolicyDTO;
import com.abtnetworks.totems.common.dto.SettingDTO;
import com.abtnetworks.totems.common.dto.TaskDTO;
import com.abtnetworks.totems.common.dto.commandline.CommandlineDTO;
import com.abtnetworks.totems.common.dto.commandline.PolicyMergeDTO;
import com.abtnetworks.totems.common.dto.commandline.PolicyObjectDTO;
import com.abtnetworks.totems.common.dto.commandline.ServiceDTO;
import com.abtnetworks.totems.common.enums.ActionEnum;
import com.abtnetworks.totems.common.enums.DeviceModelNumberEnum;
import com.abtnetworks.totems.common.enums.MoveSeatEnum;
import com.abtnetworks.totems.common.enums.PolicyEnum;
import com.abtnetworks.totems.common.utils.AliStringUtils;
import com.abtnetworks.totems.common.utils.IdGen;
import com.abtnetworks.totems.common.utils.IpUtils;
import com.abtnetworks.totems.common.utils.PortUtils;
import com.abtnetworks.totems.common.utils.ProtocolUtils;
import com.abtnetworks.totems.common.utils.TimeUtils;
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
public class SecurityDpTechR004ForJY extends SecurityPolicyGenerator implements PolicyGenerator {

    private static Logger logger = Logger.getLogger(SecurityDpTechR004.class);

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
        List<PolicyObjectDTO> serviceObjectList = generateServiceObject(dto.getServiceList(), dto.getServiceName());
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

        securityPolicy += String.format("src-zone any ");

            securityPolicy += String.format("dst-zone any ");

        if (srcAddressObject.getCommandLine() != null && srcAddressObject.getCommandLine().contains("ipv6 address-object")) {
            sb.append(securityPolicy).append(String.format("src-address ipv6 address-object %s\n", srcAddressObject.getJoin()));
        } else {
            sb.append(securityPolicy).append("src-address " + (srcAddressObject.getJoin().equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_ANY)?"any\n":String.format("address-object %s\n", srcAddressObject.getJoin())));
        }
        if (dstAddressObject.getCommandLine() != null && dstAddressObject.getCommandLine().contains("ipv6 address-object")) {
            sb.append(securityPolicy).append(String.format("dst-address ipv6 address-object %s\n", dstAddressObject.getJoin()));
        } else {
            sb.append(securityPolicy).append("dst-address " + (dstAddressObject.getJoin().equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_ANY)?"any\n":String.format("address-object %s\n", dstAddressObject.getJoin())));
        }
        if(serviceObjectList != null && serviceObjectList.size() > 0) {
            for (PolicyObjectDTO serviceObject : serviceObjectList) {
                sb.append(securityPolicy + String.format("service service-object %s\n", serviceObject.getJoin()));
            }
        }else{
            sb.append(securityPolicy + "service any\n");
        }
        if(timeObject != null) {
            sb.append(securityPolicy + String.format("time-object %s\n", timeObject.getJoin()));
        }

        // ??????004????????????????????????
        if(dto != null && ObjectUtils.isNotEmpty(dto.getIdleTimeout())){
            sb.append(securityPolicy + String.format("action %s\n", "advanced"));
            sb.append(securityPolicy + String.format("action %s \n", "long-session global" ));
        } else {
            sb.append(securityPolicy + String.format("action %s\n", dto.getAction()));
        }

        if(!AliStringUtils.isEmpty(dto.getDescription())){

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
            logger.info("??????????????????????????????????????????ruleName???mergeField ????????????");
            return createCommandLine(dto);
        }

        String ruleName = mergeDTO.getRuleName();
        String mergeField =  mergeDTO.getMergeField();

        //??????????????????
        StringBuilder sb = new StringBuilder();
        PolicyObjectDTO srcAddressObject = generateAddressObject(dto.getSrcIp(), dto.getName(), dto.getSrcAddressName(), dto.getSrcIpSystem());
        PolicyObjectDTO dstAddressObject = generateAddressObject(dto.getDstIp(), dto.getName(), dto.getDstAddressName(), dto.getDstIpSystem());
        List<PolicyObjectDTO> serviceObjectList = generateServiceObject(dto.getServiceList(), dto.getServiceName());

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

            setName = String.format("%s_AO_%s ", ticket, IdGen.getRandomNumberString());

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
    @Override
    public String getServiceNameByOne(String protocolString, String dstPort) {
        if (dstPort.equalsIgnoreCase("any")) {
            return protocolString;
        } else {
            StringBuilder sb = new StringBuilder();
            sb.append(String.format("%s-%s", protocolString.substring(0, 3).toUpperCase(), dstPort));
            return sb.toString().toUpperCase();
        }
    }
    /**
     * ???????????????????????????
     * @param serviceDTOList ??????
     * @return ???????????????
     */
    public List<PolicyObjectDTO> generateServiceObject(List<ServiceDTO> serviceDTOList, String existsServiceName) {
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
                                sb.append(String.format("src-port %s to %s", srcPort,srcPort));
                            }

                            if (dstPort.equals(PolicyConstants.POLICY_STR_VALUE_ANY)) {
                                sb.append("dst-port 0 to 65535 ");
                            } else if (PortUtils.isPortRange(dstPort)) {
                                String startPort = PortUtils.getStartPort(dstPort);
                                String endPort = PortUtils.getEndPort(dstPort);
                                sb.append(String.format("dst-port %s to %s ", startPort, endPort));
                            } else {
                                sb.append(String.format("dst-port %s to %s", dstPort,dstPort));
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

    /**
     * ????????????????????????
     * @param startTimeString ?????????????????????
     * @param endTimeString ?????????????????????
     * @return ??????????????????
     */
    private PolicyObjectDTO generateTimeObject(String startTimeString, String endTimeString, String ticket) {
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
        dto.setSrcIp("12.3.4.7");
        dto.setDstIp("2.567.24.8");
        dto.setStartTime("2019-12-3 12:2:0");
        dto.setEndTime("2019-12-3 12:2:32");

        List<ServiceDTO> srcport = new ArrayList<>();
        ServiceDTO service = new ServiceDTO();


        service.setDstPorts("25,21");
        ServiceDTO service1 = new ServiceDTO();
        service1.setProtocol("17");
        service.setProtocol("0");


        ServiceDTO service2 = new ServiceDTO();
        service2.setProtocol("1");
        srcport.add(service);
        srcport.add(service1);
        srcport.add(service2);
        dto.setServiceList(srcport);
        SecurityDpTechR004ForJY usg6000 = new SecurityDpTechR004ForJY();


        String commandLine = usg6000.composite(dto);
        System.out.println("commandline:\n" + commandLine);

    }
}
