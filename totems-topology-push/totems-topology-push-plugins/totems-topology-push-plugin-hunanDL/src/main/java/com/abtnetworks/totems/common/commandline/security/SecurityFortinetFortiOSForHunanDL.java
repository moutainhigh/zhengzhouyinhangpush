package com.abtnetworks.totems.common.commandline.security;

import com.abtnetworks.totems.common.annotation.CustomCli;
import com.abtnetworks.totems.common.commandline.PolicyGenerator;
import com.abtnetworks.totems.common.commandline.SecurityPolicyGenerator;
import com.abtnetworks.totems.common.constants.PolicyConstants;
import com.abtnetworks.totems.common.dto.*;
import com.abtnetworks.totems.common.dto.commandline.CommandlineDTO;
import com.abtnetworks.totems.common.dto.commandline.ServiceDTO;
import com.abtnetworks.totems.common.entity.NodeEntity;
import com.abtnetworks.totems.common.enums.*;
import com.abtnetworks.totems.common.utils.*;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@CustomCli(value = DeviceModelNumberEnum.FORTINET, type = PolicyEnum.SECURITY)
public class SecurityFortinetFortiOSForHunanDL extends SecurityPolicyGenerator implements PolicyGenerator {

    private static Logger logger = Logger.getLogger(SecurityFortinetFortiOSForHunanDL.class);

    private static String serviceKey = "FORTINET_Service";

    private static String ipKey = "FORTINET_IP";

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
        String commandLine = composite(dto);
        generatedDto.setAddressObjectNameList(dto.getAddressObjectNameList());
        generatedDto.setServiceObjectNameList(dto.getServiceObjectNameList());
        generatedDto.setTimeObjectNameList(dto.getTimeObjectNameList());
        return commandLine;
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

    /**
     * 1.createAddressObject??????????????????
     * 2.createServiceObject
     * 3.preCreateTimeObject
     * 4.createTimeObject
     * 5.preCreatePolicy
     * 6.createPolicy
     */
    public String createCommandLine(CommandlineDTO dto) {
        logger.info("?????????????????????????????????");
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
            sb.append("config vdom\n");
            sb.append("edit " + dto.getVsysName() + "\n");
        } else {
            if (dto.isHasVsys()) {
                sb.append("config vdom\n");
                sb.append("edit root\n");
            }
        }
        // ???????????????????????????????????????
        List<String> createAddressObject = new ArrayList<>();
        if (StringUtils.isNotEmpty(srcAddressName)) {
            //???????????????????????????????????????
//            sb.append( createAddressObject(srcAddresses,srcAddressName));
            //??????????????????????????????
            srcAddressNameList.add(srcAddressName);
        } else {
            if (srcAddrList.size() == 0 && srcNameList.size() == 0) {
                srcAddressNameList.add("all");
            } else {
                int index = 1;
                for (String srcAddress : srcAddrList) {
                    sb.append(createAddressObject(srcAddress, srcAddressNameList, dto, true, index, srcAddrList.size(),createAddressObject));
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

        }

        if (ObjectUtils.isNotEmpty(dstAddrList)) {
            int index = 1;
            for (String dstAddress : dstAddrList) {
                sb.append(createAddressObject(dstAddress, dstAddressNameList, dto, false, index, dstAddrList.size(),createAddressObject));
                index ++;
            }

            for (String dstName : dstNameList) {
                dstAddressNameList.add("\"" + dstName + "\"");
            }
        }

        dto.setAddressObjectNameList(createAddressObject);

        List<String> serviceNameList = new ArrayList<>();
        for (String existServiceName : dto.getExistServiceNameList()) {
            serviceNameList.add(String.format("\"%s\"", existServiceName));
        }

        // ???????????????????????????????????????
        List<String> createServiceNameList = new ArrayList<>();

        if (serviceList.size() > 0) {
            //?????????any??????service?????????ALL
            if (ProtocolUtils.getProtocolByString(serviceList.get(0).getProtocol()).equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_ANY)) {
                serviceNameList.add("ALL");
            } else {
                StringBuilder servicesb = new StringBuilder();
                servicesb.append(createServiceObject(serviceList, serviceNameList,createServiceNameList));
                if (servicesb.toString().trim().length() > 0) {
                    sb.append(servicesb.toString());
                }
            }
        }

        dto.setServiceObjectNameList(createServiceNameList);

        List<String> timeObjectNames = new ArrayList<>();
        String timeObjectName = "always";
        if (dto.getStartTime() != null) {
            sb.append(preCreateTimeObject());
            timeObjectName = String.format("\"end%s\"",
                    TimeUtils.transformDateFormat(dto.getEndTime(), TimeUtils.EUROPEAN_TIME_FORMAT, TimeUtils.COMMON_TIME_DAY_FORMAT));
            sb.append(createTimeObject(formatTimeString(dto.getStartTime()), formatTimeString(dto.getEndTime()), timeObjectName));
            timeObjectNames.add(timeObjectName);
        }

        dto.setTimeObjectNameList(timeObjectNames);

        sb.append(preCreatePolicy());
        List<String> srcItfList = new ArrayList<>();
        logger.info("????????????" + dto.getSrcZone());
        if (AliStringUtils.isEmpty(dto.getSrcZone())) {
            if (StringUtils.isNotEmpty(dto.getSrcItf())) {
                logger.info("???????????????" + dto.getSrcItf());
                srcItfList.add(dto.getSrcItf());
            }
        } else {
            srcItfList.add(dto.getSrcZone());
        }

        List<String> dstItfList = new ArrayList<>();
        logger.info("???????????????" + dto.getSrcZone());
        if (StringUtils.isEmpty(dto.getDstZone())) {
            if (StringUtils.isNotEmpty(dto.getDstItf())) {
                logger.info("??????????????????" + dto.getSrcItf());
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
        return "config firewall address\n";
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
                sb.append(String.format("set subnet %s/%s\n", IpUtils.getIpFromIpSegment(srcIp), IpUtils.getMaskBitFromIpSegment(srcIp)));
            } else {
                sb.append(String.format("set subnet %s/32\n", srcIp));
            }
        }
        sb.append("next\n\n");

        return sb.toString();
    }

    private String createAddressObject(String ip, List<String> addressNameList, CommandlineDTO dto, boolean isSrcIp,int index, int length,List<String> createAddressObject) {
        StringBuilder sb = new StringBuilder();
        String addressObjectName = null;
        addressObjectName = getIPNameKevin(dto.getIpType(), ip, isSrcIp, dto.getSrcIpSystem(), dto.getDstIpSystem(), length, index);
        sb.append(preCreateAddressObject());
        if (IpUtils.isIPRange(ip)) {
            String startIp = IpUtils.getStartIpFromIpAddress(ip);
            String endIp = IpUtils.getEndIpFromIpAddress(ip);
            sb.append(String.format("edit %s\n", addressObjectName));
            sb.append("set type iprange\n");
            sb.append(String.format("set start-ip %s\n", startIp));
            sb.append(String.format("set end-ip %s\n", endIp));
            createAddressObject.add(addressObjectName);
        } else {
            if (IpUtils.isIPSegment(ip)) {
                String ipFromIpSegment = IpUtils.getIpFromIpSegment(ip);
                String maskBitFromIpSegment = IpUtils.getMaskBitFromIpSegment(ip);

                sb.append(String.format("edit %s\n", addressObjectName));
                sb.append(String.format("set subnet %s/%s\n", ipFromIpSegment, maskBitFromIpSegment));
                createAddressObject.add(addressObjectName);

            } else {
                if(isSrcIp){
                    sb.append(String.format("edit %s\n", addressObjectName));
                    sb.append(String.format("set subnet %s/32\n", ip));
                    createAddressObject.add(addressObjectName);
                } else {
                    if(IpUtils.isIP(ip)){
                        sb.append(String.format("edit %s\n", addressObjectName));
                        sb.append(String.format("set subnet %s/32\n", ip));
                        createAddressObject.add(addressObjectName);

                    } else {
                        //?????????????????????????????????
                        sb.append(String.format("edit %s\n", addressObjectName));
                        sb.append("set type fqdn\n");
                        sb.append(String.format("set fqdn %s\n", ip));
                        createAddressObject.add(addressObjectName);
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
        return "config firewall address group\n";
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
        return "config firewall service custom\n";
    }

    private String createServiceObject(ServiceDTO service, List<String> serviceNameList,List<String> createServiceNameList) {
        StringBuilder sb = new StringBuilder();
        String protocol = ProtocolUtils.getProtocolByString(service.getProtocol());
        //????????????????????????????????????TCP_ALL???ICMP_ALL???UDP_ALL???
        if (service.getDstPorts().equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_ANY)) {
            serviceNameList.add(String.format("ALL_%s", protocol.toUpperCase()));
            return "";
        }

        String dstPort = service.getDstPorts();
        String[] dstPortList = dstPort.split(",");

        //??????????????????????????????port?????????????????????
        for (String port : dstPortList) {
            sb.append(preCreateServiceObject());
            String serviceName = getServiceNameKevin(serviceKey, protocol, port, null);
            sb.append(String.format("edit %s\n", serviceName));
            if (protocol.equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_TCP)) {
                sb.append("set protocol TCP/UDP/SCTP\n");
                if (PortUtils.isPortRange(port)) {
                    String startPort = PortUtils.getStartPort(port);
                    String endPort = PortUtils.getEndPort(port);
                    sb.append(String.format("set tcp-portrange %s-%s\n", startPort, endPort));
                } else {
                    sb.append(String.format("set tcp-portrange %s\n", port));
                }
            } else if (protocol.equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_UDP)) {
                sb.append("set protocol TCP/UDP/SCTP\n");
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


    private String createServiceObject(List<ServiceDTO> serviceDTOList, List<String> existServiceNameList,List<String> createServiceNameList) {
        StringBuilder sb = new StringBuilder();

        for (ServiceDTO serviceDTO : serviceDTOList) {
            String command = createServiceObject(serviceDTO, existServiceNameList,createServiceNameList);
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
        return "config firewall serivce group\n";
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
        return "config firewall schedule onetime\n";
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
        return "config firewall policy\n";
    }

    String createPolicy(String theme,String policyName, List<String> srcItfList, List<String> dstItfList,
                        List<String> srcAddressList, List<String> dstAddressList,
                        List<String> serviceNameList, String timeObjectName, String description,
                        String moveBefore, String moveAfter, String deviceManageIp,
                        String srcZone, String action) {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("edit %s\n", policyName));
        //todo:???????????? ????????????
//        sb.append("set name ").append(String.format("%s_AO_%s\n",theme, IdGen.getRandomNumberString()));
        if (CollectionUtils.isNotEmpty(srcItfList)) {
            sb.append("set srcintf ");
            for (String itf : srcItfList) {
                sb.append(String.format("%s ", itf));
            }
            sb.append("\n");
        }


        if (CollectionUtils.isNotEmpty(dstItfList)) {
            sb.append("set dstintf ");
            for (String itf : dstItfList) {
                sb.append(String.format("%s ", itf));
            }
            sb.append("\n");
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
        // ???????????????????????????????????????????????????
        sb.append("set logtraffic all\n");

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
                sb.append(String.format("set comment %s\n", description));
            }
        }
        sb.append("set match-vip disable\n");
        sb.append("next\n");

        if (!AliStringUtils.isEmpty(moveBefore)) {
            sb.append(String.format("move %s before %s\n", policyName, moveBefore));
        } else if (!AliStringUtils.isEmpty(moveAfter)) {
            sb.append(String.format("move %s after %s\n", policyName, moveAfter));
        }


        sb.append("\nend\n\n");

        return sb.toString();
    }

    public static void main(String[] args) {
        CommandlineDTO dto = CommandlineDTO.getInstanceDemo();
        SecurityFortinetFortiOSForHunanDL fortinet = new SecurityFortinetFortiOSForHunanDL();
        String commandLine = fortinet.composite(dto);
        System.out.println("commandline:\n" + commandLine);
    }

    public String createCommandLine(CommandlineDTO dto, Integer mergeProperty) {
        List<ServiceDTO> serviceList = dto.getRestServiceList();
        List<String> srcAddrList = dto.getRestSrcAddressList();
        List<String> dstAddrList = dto.getRestDstAddressList();
        List<String> srcNameList = dto.getExistSrcAddressList();
        List<String> dstNameList = dto.getExistDstAddressList();
        List<String> srcAddressNameList = new ArrayList<>();
        List<String> dstAddressNameList = new ArrayList<>();
        String srcAddressName = dto.getSrcAddressName();
        String dstAddressName = dto.getDstAddressName();
        StringBuilder sb = new StringBuilder();

        // ????????????IPV4
        if(ObjectUtils.isEmpty(dto.getIpType())){
            dto.setIpType(IpTypeEnum.IPV4.getCode());
        }

        if (dto.isVsys()) {
            sb.append("config vdom\n");
            sb.append("edit " + dto.getVsysName() + "\n");
        } else {
            if (dto.isHasVsys()) {
                sb.append("config vdom\n");
                sb.append("edit root\n");
            }
        }
        // ???????????????????????????????????????
        List<String> createAddressObject = new ArrayList<>();
        if (StringUtils.isNotEmpty(srcAddressName)) {
            //???????????????????????????????????????
//            sb.append( createAddressObject(srcAddresses,srcAddressName));
            //??????????????????????????????
            srcAddressNameList.add(srcAddressName);
        } else {
            if (srcAddrList.size() == 0 && srcNameList.size() == 0) {
                srcAddressNameList.add("all");
            } else {
                int index = 1;
                for (String srcAddress : srcAddrList) {
                    sb.append(createAddressObject(srcAddress, srcAddressNameList, dto, true, index, srcAddrList.size(),createAddressObject));
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

        }

        if (ObjectUtils.isNotEmpty(dstAddrList)) {
            int index = 1;
            for (String dstAddress : dstAddrList) {
                sb.append(createAddressObject(dstAddress, dstAddressNameList, dto, false, index, dstAddrList.size(),createAddressObject));
                index ++;
            }

            for (String dstName : dstNameList) {
                dstAddressNameList.add("\"" + dstName + "\"");
            }
        }

        dto.setAddressObjectNameList(createAddressObject);

        List<String> serviceNameList = new ArrayList<>();
        for (String existServiceName : dto.getExistServiceNameList()) {
            serviceNameList.add(String.format("\"%s\"", existServiceName));
        }
        // ???????????????????????????????????????
        List<String> createServiceNameList = new ArrayList<>();

        if (serviceList.size() > 0) {
            //?????????any??????service?????????ALL
            if (ProtocolUtils.getProtocolByString(serviceList.get(0).getProtocol()).equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_ANY)) {
                serviceNameList.add("ALL");
            } else {
                StringBuilder servicesb = new StringBuilder();
                servicesb.append(createServiceObject(serviceList, serviceNameList,createServiceNameList));
                if (servicesb.toString().trim().length() > 0) {
                    sb.append(servicesb.toString());
                }
            }
        }
        dto.setServiceObjectNameList(createServiceNameList);

        sb.append(preCreatePolicy());
        sb.append(createPolicy(String.valueOf(dto.getCurrentId()), srcAddressNameList, dstAddressNameList, serviceNameList,mergeProperty));
        if (dto.isVsys()) {
            sb.append("end\n");
        }
        return sb.toString();
    }

    private String createPolicy(String policyName, List<String> srcAddressList, List<String> dstAddressList, List<String> serviceNameList,Integer mergeProperty) {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("edit %s\n", policyName));
        if (CollectionUtils.isNotEmpty(srcAddressList) && mergeProperty==0) {
            sb.append("set srcaddr ");
            for (String addressObject : srcAddressList) {
                sb.append(String.format("%s ", addressObject));
            }
            sb.append("\n");
        }
        if (CollectionUtils.isNotEmpty(dstAddressList) && mergeProperty==1) {
            sb.append("set dstaddr ");
            for (String addressObject : dstAddressList) {
                sb.append(String.format("%s ", addressObject));
            }
            sb.append("\n");
        }
        if (CollectionUtils.isNotEmpty(serviceNameList) && mergeProperty==2) {
            sb.append("set service ");
            for (String service : serviceNameList) {
                sb.append(String.format("%s ", service));
            }
            sb.append("\n");
        }
        sb.append("next\n");
        sb.append("\nend\n\n");
        return sb.toString();
    }

    public static String getServiceNameKevin(String key, String protocolString,String dstPorts,Integer idleTimeout) {
        logger.info("??????utils??????ip??????");
        String name ;
        String servicePrefix = "tcp_,udp_,icmp,-";
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
        name = String.format("\"%s%s\"", prefix, dstPorts);
        if (idleTimeout != null) {
            name = String.format("\"%s%sL\"", prefix, dstPorts);
        }
        if (PortUtils.isPortRange(dstPorts)) {
            String start = PortUtils.getStartPort(dstPorts);
            String end = PortUtils.getEndPort(dstPorts);
            name = String.format("\"%s%s%s%s\"", prefix, start,line, end);
            if (idleTimeout != null) {
                name = String.format("\"%s%s%s%sL\"", prefix,start, line,  end);
            }
        }
        return name;
    }


    public static String getIPNameKevin(Integer ipType, String address, boolean isSrcIp, String srcIpSystem, String dstIpSystem, int length, int index){
        logger.info("??????utils??????service??????,ipType:" + ipType);
        String ipPrefix = ",,,3";
        String[] arr = ipPrefix.split(",");
        String isIP = arr[0];//??????ip??????
        String iPRange = arr[1];//ip????????????
        String iPSegment = arr[2];//ip????????????
        String random = "";//????????????????????????????????????????????????
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
        if (ipType.intValue() == IpTypeEnum.IPV4.getCode()) {
            if (IpUtils.isIPSegment(address)) {
                return iPSegment + address;
            } else if (IpUtils.isIPRange(address)) {
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
            } else {
                return isIP + address;
            }
        } else if (ipType.intValue() == IpTypeEnum.IPV6.getCode()) {
            // ipv6
            if (address.contains("/")) {
                return iPSegment + address;
            } else if (address.contains("-")) {
                return iPRange + address;
            } else {
                return isIP + address;
            }
        } else {
            // ???????????????URL??????
            return address;
        }
    }

}
