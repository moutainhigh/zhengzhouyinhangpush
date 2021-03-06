package com.abtnetworks.totems.common.commandline.security;

import com.abtnetworks.totems.common.commandline.PolicyGenerator;
import com.abtnetworks.totems.common.commandline.SecurityPolicyGenerator;
import com.abtnetworks.totems.common.constants.PolicyConstants;
import com.abtnetworks.totems.common.dto.*;
import com.abtnetworks.totems.common.dto.commandline.CommandlineDTO;
import com.abtnetworks.totems.common.dto.commandline.ServiceDTO;
import com.abtnetworks.totems.common.entity.NodeEntity;
import com.abtnetworks.totems.common.enums.*;
import com.abtnetworks.totems.common.utils.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service

public class SecurityKFW extends SecurityPolicyGenerator implements PolicyGenerator {


    @Override
    public String generate(CmdDTO cmdDTO) {
        CommandlineDTO dto = new CommandlineDTO();
        PolicyDTO policyDTO = cmdDTO.getPolicy();
        BeanUtils.copyProperties(policyDTO, dto);
        DeviceDTO deviceDTO = cmdDTO.getDevice();
        BeanUtils.copyProperties(deviceDTO, dto);
        NodeEntity nodeEntity = deviceDTO.getNodeEntity();
        dto.setIp(nodeEntity.getIp());
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
        dto.setHasVsys(deviceDTO.isHasVsys());
        dto.setExistSrcAddressList(existObjectDTO.getExistSrcAddressList());
        dto.setRestSrcAddressList(existObjectDTO.getRestSrcAddressList());
        dto.setExistDstAddressList(existObjectDTO.getExistDstAddressList());
        dto.setRestDstAddressList(existObjectDTO.getRestDstAddressList());

        dto.setSwapRuleNameId(settingDTO.getSwapNameId());
        dto.setCurrentId(settingDTO.getPolicyId());
        GeneratedObjectDTO generatedDto = cmdDTO.getGeneratedObject();
        generatedDto.setPolicyName(dto.getName());
        return composite(dto);
    }

    @Override
    public String generatePreCommandline(CommandlineDTO dto) {
        return "";
    }

    @Override
    public String generatePostCommandline(CommandlineDTO dto) {
        return "";
    }

    private String formatTimeString(String timeString) {
        return TimeUtils.transformDateFormat(timeString, TimeUtils.EUROPEAN_TIME_FORMAT, TimeUtils.FORTINET_FORMAT);
    }

    @Override
    public String getServiceName(List<ServiceDTO> serviceDTOList) {
        return "";
    }

    @Override
    public String getServiceName(ServiceDTO dto) {
        return "";
    }

    @Override
    public String getServiceNameByOne(String protocolString, String dstPort) {
        return "";
    }

    @Override
    public String generateCommandline(CommandlineDTO dto) {
        if (dto.getMergeDTO() == null || dto.isMustCreateFlag()) {
            return createCommandLine(dto);
        } else {
            return editCommandLine(dto);
        }
    }

    public String createCommandLine(CommandlineDTO dto) {
        List<ServiceDTO> serviceList = dto.getRestServiceList();
        List<String> srcAddrList = dto.getRestSrcAddressList();
        List<String> dstAddrList = dto.getRestDstAddressList();
        List<String> srcNameList = dto.getExistSrcAddressList();
        List<String> dstNameList = dto.getExistDstAddressList();
        List<String> srcAddressNameList = new ArrayList<>();
        List<String> dstAddressNameList = new ArrayList<>();
        String srcAddressName = dto.getSrcAddressName();
        String dstAddressName = dto.getDstAddressName();
        String theme = dto.getName();
        StringBuilder sb = new StringBuilder();

        // ????????????IPV4
        if(ObjectUtils.isEmpty(dto.getIpType())){
            dto.setIpType(IpTypeEnum.IPV4.getCode());
        }

        if (dto.isVsys()) {
            sb.append("define vdom\n");
            sb.append("edit " + dto.getVsysName() + "\n");
        } else {
            if (dto.isHasVsys()) {
                sb.append("define vdom\n");
                sb.append("edit root\n");
            }
        }
        if (StringUtils.isNotEmpty(srcAddressName)) {
            //???????????????????????????????????????
//            sb.append( createAddressObject(srcAddresses,srcAddressName));
            //??????????????????????????????
            srcAddressNameList.add(srcAddressName);
        } else {
            if (srcAddrList.size() == 0 && srcNameList.size() == 0) {
                srcAddressNameList.add("any");
            } else {
                int index = 1;
                for (String srcAddress : srcAddrList) {
                    sb.append(createAddressObject(srcAddress, srcAddressNameList, dto, true, index, srcAddrList.size()));
                    index ++;
                }

                for (String srcName : srcNameList) {
                    srcAddressNameList.add("\"" + srcName + "\"");
                }
            }
        }
        if (StringUtils.isNotEmpty(dstAddressName)) {
            //???????????????????????????????????????
//            sb.append( createAddressObject(dstAddresses,dstAddressName));
            //??????????????????????????????
            dstAddressNameList.add(dstAddressName);
        } else {
            if (dstAddrList.size() == 0 && dstNameList.size() == 0) {
                dstAddressNameList.add("any");
            } else {
                int index = 1;
                for (String dstAddress : dstAddrList) {
                    sb.append(createAddressObject(dstAddress, dstAddressNameList, dto, false, index, dstAddrList.size()));
                    index ++;
                }

                for (String dstName : dstNameList) {
                    dstAddressNameList.add("\"" + dstName + "\"");
                }
            }
        }
        List<String> serviceNameList = new ArrayList<>();
        for (String existServiceName : dto.getExistServiceNameList()) {
            serviceNameList.add(String.format("\"%s\"", existServiceName));
        }

        if (serviceList.size() > 0) {
            //?????????any??????service?????????ALL
            if (ProtocolUtils.getProtocolByString(serviceList.get(0).getProtocol()).equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_ANY)) {
                serviceNameList.add("ANY");
            } else {
                StringBuilder servicesb = new StringBuilder();
                servicesb.append(createServiceObject(serviceList, serviceNameList));
                if (servicesb.toString().trim().length() > 0) {
                    sb.append(servicesb.toString());
                }
            }
        }


        String timeObjectName = "always";
        if (dto.getStartTime() != null) {
            sb.append(preCreateTimeObject());
            timeObjectName = String.format("\"to%s\"",
                    TimeUtils.transformDateFormat(dto.getEndTime(), TimeUtils.EUROPEAN_TIME_FORMAT, TimeUtils.COMMON_TIME_DAY_FORMAT));
            sb.append(createTimeObject(formatTimeString(dto.getStartTime()), formatTimeString(dto.getEndTime()), timeObjectName));
        }

        sb.append(preCreatePolicy());
        List<String> srcItfList = new ArrayList<>();
        if (AliStringUtils.isEmpty(dto.getSrcZone())) {
            if (StringUtils.isNotEmpty(dto.getSrcItf())) {
                srcItfList.add(dto.getSrcItf());
            }
        } else {
            srcItfList.add(dto.getSrcZone());
        }

        List<String> dstItfList = new ArrayList<>();
        if (StringUtils.isEmpty(dto.getDstZone())) {
            if (StringUtils.isNotEmpty(dto.getDstItf())) {
                dstItfList.add(dto.getDstItf());
            }
        } else {
            dstItfList.add(dto.getDstZone());

        }
        int moveSeatCode = dto.getMoveSeatEnum().getCode();
        String moveBefore = null;
        String moveAfter = null;
        if (moveSeatCode == MoveSeatEnum.FIRST.getCode() || moveSeatCode == MoveSeatEnum.BEFORE.getCode()) {
            moveBefore = dto.getSwapRuleNameId();
        } else if (moveSeatCode == MoveSeatEnum.AFTER.getCode()) {
            moveAfter = dto.getSwapRuleNameId();
        }
        sb.append(createPolicy(theme,String.valueOf(dto.getCurrentId()), srcItfList, dstItfList, srcAddressNameList,
                dstAddressNameList, serviceNameList, timeObjectName, dto.getDescription(), moveBefore, moveAfter,
                dto.getIp(), dto.getSrcZone(), dto.getAction()));
        if (dto.isVsys()) {
            sb.append("end\n");
        }
        return sb.toString();
    }

    public String editCommandLine(CommandlineDTO dto) {
        StringBuilder sb = new StringBuilder();
        return sb.toString();
    }


    /**
     * ??????????????????,subnet????????????iprange????????????????????????????????????????????????
     * config firewall address
     * edit nana2
     * set subnet 3.3.3.3/32
     * next
     * edit nana3
     * set type iprange
     * set start-ip 1.1.1.1
     * set end-ip 1.1.1.10
     * next
     */

    private String preCreateAddressObject() {
        return "define firewall address\n";
    }

    /**
     * ???????????????????????????
     *
     * @param srcIp
     * @param addressObjectName
     * @return
     */
    private String createAddressObject(String srcIp, String addressObjectName) {
        StringBuilder sb = new StringBuilder();

        sb.append(String.format("edit %s\n", addressObjectName));
        if (IpUtils.isIPRange(srcIp)) {
            String startIp = IpUtils.getStartIpFromIpAddress(srcIp);
            String endIp = IpUtils.getEndIpFromIpAddress(srcIp);
            sb.append("set type iprange\n");
            sb.append(String.format("set start-ip %s\n", startIp));
            sb.append(String.format("set end-ip %s\n", endIp));
        } else {
            if (IpUtils.isIPSegment(srcIp)) {
                sb.append("set type ipmask\n");
                sb.append(String.format("set subnet %s/%s\n", IpUtils.getIpFromIpSegment(srcIp), IpUtils.getMaskBitFromIpSegment(srcIp)));
            } else {
                sb.append("set type ipmask\n");
                sb.append(String.format("set subnet %s/32\n", srcIp));
            }
        }
        sb.append("next\n\n");

        return sb.toString();
    }

    private String createAddressObject(String ip, List<String> addressNameList, CommandlineDTO dto, boolean isSrcIp,int index, int length) {
        StringBuilder sb = new StringBuilder();
        String addressObjectName = "";
        sb.append(preCreateAddressObject());
        if (IpUtils.isIPRange(ip)) {
            String startIp = IpUtils.getStartIpFromIpAddress(ip);
            String endIp = IpUtils.getEndIpFromIpAddress(ip);
            if(isSrcIp){
                if(StringUtils.isNotEmpty(dto.getSrcIpSystem())){
                    if(length == 1 ){
                        addressObjectName = dto.getSrcIpSystem();
                    } else {
                        addressObjectName = dto.getSrcIpSystem() +"_" + index;
                    }
                } else {
                    addressObjectName = String.format("\"%s-%s\"", startIp, endIp);
                }
            } else {
                if(StringUtils.isNotEmpty(dto.getDstIpSystem())){
                    if(length == 1 ){
                        addressObjectName = dto.getDstIpSystem();
                    } else {
                        addressObjectName = dto.getDstIpSystem() +"_" + index;
                    }
                } else {
                    addressObjectName = String.format("\"%s-%s\"", startIp, endIp);
                }
            }
            sb.append(String.format("edit %s\n", addressObjectName));
            sb.append("set type iprange\n");
            sb.append(String.format("set start-ip %s\n", startIp));
            sb.append(String.format("set end-ip %s\n", endIp));
        } else {
            if (IpUtils.isIPSegment(ip)) {
                String ipFromIpSegment = IpUtils.getIpFromIpSegment(ip);
                String maskBitFromIpSegment = IpUtils.getMaskBitFromIpSegment(ip);
                if(isSrcIp){
                    if(StringUtils.isNotEmpty(dto.getSrcIpSystem())){
                        if(length == 1 ){
                            addressObjectName = dto.getSrcIpSystem();
                        } else {
                            addressObjectName = dto.getSrcIpSystem() +"_" + index;
                        }
                    } else {
                        addressObjectName = String.format("\"%s/%s\"", ipFromIpSegment, maskBitFromIpSegment);
                    }
                } else {
                    if(StringUtils.isNotEmpty(dto.getDstIpSystem())){
                        if(length == 1 ){
                            addressObjectName = dto.getDstIpSystem();
                        } else {
                            addressObjectName = dto.getDstIpSystem() +"_" + index;
                        }
                    } else {
                        addressObjectName = String.format("\"%s/%s\"", ipFromIpSegment, maskBitFromIpSegment);
                    }
                }

                sb.append(String.format("edit %s\n", addressObjectName));
                sb.append("set type ipmask\n");
                sb.append(String.format("set subnet %s/%s\n", ipFromIpSegment, maskBitFromIpSegment));
            } else {
                if(isSrcIp){
                    if(StringUtils.isNotEmpty(dto.getSrcIpSystem())){
                        if(length == 1 ){
                            addressObjectName = dto.getSrcIpSystem();
                        } else {
                            addressObjectName = dto.getSrcIpSystem() +"_" + index;
                        }
                    } else {
                        addressObjectName = String.format("\"%s/32\"", ip);
                    }
                    sb.append(String.format("edit %s\n", addressObjectName));
                    sb.append("set type ipmask\n");
                    sb.append(String.format("set subnet %s/32\n", ip));
                } else {
                    if(IpUtils.isIP(ip)){
                        if(StringUtils.isNotEmpty(dto.getDstIpSystem())){
                            if(length == 1 ){
                                addressObjectName = dto.getDstIpSystem();
                            } else {
                                addressObjectName = dto.getDstIpSystem() +"_" + index;
                            }
                        } else {
                            addressObjectName = String.format("\"%s/32\"", ip);
                        }
                        sb.append(String.format("edit %s\n", addressObjectName));
                        sb.append("set type ipmask\n");
                        sb.append(String.format("set subnet %s/32\n", ip));
                    } else {
                        if(dto.getIpType().intValue() == IpTypeEnum.URL.getCode()){
                            if(StringUtils.isNotEmpty(dto.getDstIpSystem())){
                                if(length == 1 ){
                                    addressObjectName = dto.getDstIpSystem() ;
                                } else {
                                    addressObjectName = dto.getDstIpSystem() +"_" + index;
                                }
                            } else {
                                addressObjectName = String.format("%s_AO_%s",dto.getName(), IdGen.getRandomNumberString());;
                            }
                            sb.append(String.format("edit %s\n", addressObjectName));
                            sb.append("set type fqdn\n");
                            sb.append(String.format("set fqdn %s\n", ip));
                        } else {
                            if(StringUtils.isNotEmpty(dto.getDstIpSystem())){
                                if(length == 1 ){
                                    addressObjectName = dto.getDstIpSystem() ;
                                } else {
                                    addressObjectName = dto.getDstIpSystem() + "_" + index;
                                }
                            } else {
                                addressObjectName = String.format("\"%s/32\"", ip);
                            }
                            sb.append(String.format("edit %s\n", addressObjectName));
                            sb.append("set type ipmask\n");
                            sb.append(String.format("set subnet %s/32\n", ip));
                        }
                    }
                }
            }
        }
        sb.append("next\n");
        sb.append("end\n\n");
        addressNameList.add(addressObjectName);
        return sb.toString();
    }


    /**
     * ?????????????????????,?????????member???????????????????????????????????????
     * config firewall addrgrp
     * edit test_group1
     * set member nana apple1 nana2
     * next
     */

    private String preCreateAddressGroup() {
        return "define firewall address group\n";
    }

    private String createAddressGroup(String addressGroupName, List<String> addressObjectNameList) {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("edit %s\n", addressGroupName));
        sb.append("set member ");
        for (String name : addressObjectNameList) {
            sb.append(String.format("%s ", name));
        }
        sb.append("\n");
        sb.append("next\nend\n\n");
        return sb.toString();
    }

    /**
     * ??????????????????
     * #1 ?????????TCP/UDP/SCTP?????????????????????tcp/udp?????????????????????????????????????????????????????????????????????????????????
     * #2 ????????????????????????????????????????????????????????????????????????
     * #3 ???????????????any???????????????????????????????????????any??????????????????0-65535
     * #4 ?????????????????????????????????????????????set tcp-portrange 80
     * #5 ??????ICMP????????????ICMP????????????
     * #6 ???????????????tcp???udp??????????????????????????????6???17????????????ser5
     * config firewall service custom
     * edit ser1
     * set protocol TCP/UDP/SCTP
     * set tcp-portrange 80-90:100-200
     * set udp-portrange 500-501:600-601
     * next
     * <p>
     * edit ser2
     * set tcp-portrange 80
     * next
     * <p>
     * edit ser3
     * set protocol ICMP
     * next
     * <p>
     * edit ser4
     * set protocol IP
     * next
     * <p>
     * edit ser5
     * set protocol IP
     * set protocol-number 6
     * next
     */

    private String preCreateServiceObject() {
        return "define firewall service custom\n";
    }

    private String createServiceObject(ServiceDTO service, List<String> serviceNameList) {
        StringBuilder sb = new StringBuilder();
        String protocol = ProtocolUtils.getProtocolByString(service.getProtocol());
        //????????????????????????????????????TCP_ALL???ICMP_ALL???UDP_ALL???
        if (service.getDstPorts().equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_ANY)) {
            if(protocol.equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_ICMP)){
                serviceNameList.add(String.format("%s_ANY", protocol.toUpperCase()));
            }else {
                serviceNameList.add(String.format("%s", protocol.toUpperCase()));
            }
            return "";
        }

        String dstPort = service.getDstPorts();
        String[] dstPortList = dstPort.split(",");

        //??????????????????????????????port?????????????????????
        for (String port : dstPortList) {
            sb.append(preCreateServiceObject());
            String serviceName = String.format("\"service_%s\"", protocol);
            if (!protocol.equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_ICMP)) {
                if (PortUtils.isPortRange(port)) {
                    String startPort = PortUtils.getStartPort(port);
                    String endPort = PortUtils.getEndPort(port);
                    serviceName = String.format("\"%s%s-%s\"", protocol.substring(0, 1).toUpperCase(), startPort, endPort);
                } else {
                    serviceName = String.format("\"%s%s\"", protocol.substring(0, 1).toUpperCase(), port);
                }
            }

            sb.append(String.format("edit %s\n", serviceName));
            sb.append(String.format("set protocol TCP/UDP/SCTP\n"));
            if (protocol.equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_TCP)) {
                if (PortUtils.isPortRange(port)) {
                    String startPort = PortUtils.getStartPort(port);
                    String endPort = PortUtils.getEndPort(port);
                    sb.append(String.format("set tcp-portrange %s-%s\n", startPort, endPort));
                } else {
                    sb.append(String.format("set tcp-portrange %s\n", port));
                }
            } else if (protocol.equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_UDP)) {
                if (PortUtils.isPortRange(port)) {
                    String startPort = PortUtils.getStartPort(port);
                    String endPort = PortUtils.getEndPort(port);
                    sb.append(String.format("set udp-portrange %s-%s\n", startPort, endPort));
                } else {
                    sb.append(String.format("set udp-portrange %s\n", port));
                }
            } else {
                sb.append(String.format("set protocol %s\n", protocol.toUpperCase()));
            }
            serviceNameList.add(serviceName);
            sb.append("next\n");
            sb.append("end\n\n");
        }
        return sb.toString();
    }


    private String createServiceObject(List<ServiceDTO> serviceDTOList, List<String> existServiceNameList) {
        StringBuilder sb = new StringBuilder();

        for (ServiceDTO serviceDTO : serviceDTOList) {
            String command = createServiceObject(serviceDTO, existServiceNameList);
            sb.append(command);
            sb.append("\n");
        }
        return sb.toString();
    }

    /**
     * ?????????????????????
     * config firewall service group
     * edit ser_gr1
     * set member ser1 ser2 ser3 ser4 HTTP
     * next
     */

    String preCreateServiceGroup() {
        return "define firewall serivce group\n";
    }

    void createServiceGroup(String serviceGroupName, List<String> serviceNameList) {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("edit %s\n", serviceGroupName));
        sb.append("set member ");
        for (String serviceName : serviceNameList) {
            sb.append(String.format("%s ", serviceName));
        }
        sb.append("next \n");

    }

    String preCreateTimeObject() {
        return "define firewall schedule onetime\n";
    }

    String createTimeObject(String startTime, String endTime, String timeObjectName) {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("edit %s\n", timeObjectName));
        sb.append(String.format("set start %s\n", startTime));
        sb.append(String.format("set end %s\n", endTime));
        sb.append("next\nend\n\n");
        return sb.toString();
    }

    /**
     * ????????????????????????????????????
     * #?????????????????????????????????????????????????????????????????????
     * #?????????????????????enable????????????disable
     * #???/???????????????/?????????????????????????????????????????????????????????????????????????????????
     * #????????????TCP/UDP/ICMP?????????????????????set service ALL_TCP ALL_UDP ALL_ICMP
     * ?????????any????????????set service ALL
     * #????????????????????????????????????any????????????????????????always???set schedule always
     * config firewall policy
     * edit 22
     * set srcintf port1 port2
     * set dstintf port3
     * set srcaddr nana1 333
     * set dstaddr all
     * set service ser1 ser2
     * set schedule time1
     * set action accept
     * set status enable
     * set comments ??????
     * next
     */
    String preCreatePolicy() {
        return "define firewall policy\n";
    }

    String createPolicy(String theme,String policyName, List<String> srcItfList, List<String> dstItfList,
                        List<String> srcAddressList, List<String> dstAddressList,
                        List<String> serviceNameList, String timeObjectName, String description,
                        String moveBefore, String moveAfter, String deviceManageIp,
                        String srcZone, String action) {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("edit %s\n", policyName));
        if (CollectionUtils.isNotEmpty(srcItfList)) {
            sb.append("set srcintf ");
            for (String itf : srcItfList) {
                sb.append(String.format("%s ", itf));
            }
            sb.append("\n");
        }
        else{
            sb.append("set srcintf any\n");
        }


        if (CollectionUtils.isNotEmpty(dstItfList)) {
            sb.append("set dstintf ");
            for (String itf : dstItfList) {
                sb.append(String.format("%s ", itf));
            }
            sb.append("\n");
        }
        else{
            sb.append("set dstintf any\n");
        }
        if (CollectionUtils.isNotEmpty(srcAddressList)) {
            sb.append("set srcaddr ");
            for (String addressObject : srcAddressList) {
                sb.append(String.format("%s ", addressObject));
            }
            sb.append("\n");
        }

        if (CollectionUtils.isNotEmpty(dstAddressList)) {
            sb.append("set dstaddr ");
            for (String addressObject : dstAddressList) {
                sb.append(String.format("%s ", addressObject));
            }
            sb.append("\n");
        }

        if (CollectionUtils.isNotEmpty(serviceNameList)) {
            sb.append("set service ");
            for (String service : serviceNameList) {
                sb.append(String.format("%s ", service));
            }
            sb.append("\n");
        }


        sb.append(String.format("set schedule %s\n", timeObjectName));

        // sb.append("set action accept\n");
        action = action.toLowerCase();
        if ("permit".equals(action)) {
            action = "accept";
        }
        sb.append(String.format("set action %s\n", action));

        // ??????ip???????????????internet??????????????????????????????????????????
        if (deviceManageIp != null && srcZone != null) {
            // ??????????????????
            if (deviceManageIp.indexOf("(") > -1) {
                deviceManageIp = deviceManageIp.substring(0, deviceManageIp.indexOf("("));
            }
//            if (tianaInternetInfo.containsKey(deviceManageIp)) {
//                String tiananSrcZone = String.valueOf(tianaInternetInfo.get(deviceManageIp));
//                List<String> srcZoneList = Arrays.asList(tiananSrcZone.split(","));
//                if (srcZoneList.contains(srcZone)) {
//                    sb.append("set utm-status enable\n");
//                    sb.append("set ips-sensor \"ips-protect\"\n");
//                    sb.append("set ssl-ssh-profile \"certificate-inspection\"\n");
//                }
//            }
        }

        if (!AliStringUtils.isEmpty(description)) {
            //????????????????????????
            if (description.trim().length() > 0) {
                sb.append(String.format("set comments %s\n", description));
            }
        }

        sb.append("next\n");

        if (!AliStringUtils.isEmpty(moveBefore)) {
            sb.append(String.format("move %s before %s\n", policyName, moveBefore));
        } else if (!AliStringUtils.isEmpty(moveAfter)) {
            sb.append(String.format("move %s after %s\n", policyName, moveAfter));
        }


        sb.append("end\n");

        return sb.toString();
    }

    public static void main(String[] args) {
        CommandlineDTO dto = CommandlineDTO.getInstanceDemo();
        SecurityKFW securityKFW = new SecurityKFW();

        dto.setIpType(0);
        List<String> restSrc = new ArrayList<>();
        restSrc.add("");
//        restSrc.add("");
        dto.setRestSrcAddressList(restSrc);
        List<String> restDst = new ArrayList<>();
        restDst.add("");
        restDst.add("");

        dto.setRestDstAddressList(restDst);
        List<ServiceDTO> serviceDTOS = new ArrayList<>();
        ServiceDTO serviceDTO = new ServiceDTO();
        serviceDTO.setProtocol("6");
        serviceDTO.setDstPorts("42");
        ServiceDTO serviceDTO2 = new ServiceDTO();
        serviceDTO2.setProtocol("1");
        serviceDTO2.setDstPorts("any");
        serviceDTOS.add(serviceDTO);
        serviceDTOS.add(serviceDTO2);
        dto.setServiceList(serviceDTOS);
        dto.setRestServiceList(serviceDTOS);
//        dto.setIdleTimeout(7834);
//        dto.setStartTime("2008-1-3 10:33:13");
//        dto.setEndTime("2008-1-3 10:33:16");
        String commandLine = securityKFW.composite(dto);
        System.out.println("commandline:\n" + commandLine);
    }
}
