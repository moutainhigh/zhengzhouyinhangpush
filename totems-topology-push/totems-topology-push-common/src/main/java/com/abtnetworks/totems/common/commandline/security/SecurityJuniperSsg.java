package com.abtnetworks.totems.common.commandline.security;

import com.abtnetworks.totems.common.commandline.PolicyGenerator;
import com.abtnetworks.totems.common.commandline.SecurityPolicyGenerator;
import com.abtnetworks.totems.common.constants.PolicyConstants;
import com.abtnetworks.totems.common.dto.*;
import com.abtnetworks.totems.common.dto.commandline.*;
import com.abtnetworks.totems.common.enums.ActionEnum;
import com.abtnetworks.totems.common.enums.MoveSeatEnum;
import com.abtnetworks.totems.common.utils.*;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Author: wenjiachang
 * @Date: 2019/1/23 11:18
 */
@Service
public class SecurityJuniperSsg extends SecurityPolicyGenerator implements PolicyGenerator {

    private static Logger logger = Logger.getLogger(SecurityJuniperSsg.class);

    private int MAX_NAME_LENGTH = 24;

    private final int MAX_OBJECT_NAME_LENGTH = 63;

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
        return composite(dto);
    }

    @Override
    public String generatePreCommandline(CommandlineDTO dto) {
        return "";
    }

    @Override
    public String generateCommandline(CommandlineDTO dto) {
        if(dto.getMergeDTO() == null || dto.isMustCreateFlag()) {
            return createMergeCommandLine(dto);
        }else{
            return editCommandLine(dto);
        }
    }

    public String createMergeCommandLine(CommandlineDTO dto) {
        StringBuilder sb = new StringBuilder();

        String ticket = dto.getName();
        String srcIp = dto.getSrcIp();
        String dstIp = dto.getDstIp();
        String startTime = dto.getStartTime();
        String endTime = dto.getEndTime();
        boolean createObjFlag = dto.isCreateObjFlag();

        PolicyObjectDTO srcAddress = generateAddressObject(srcIp, dto.getSrcZone(), ticket, PolicyConstants.SRC, dto.getSrcAddressName(), dto.getSrcIpSystem());
        PolicyObjectDTO dstAddress = generateAddressObject(dstIp, dto.getDstZone(), ticket, PolicyConstants.DST, dto.getDstAddressName(), dto.getDstIpSystem());
        PolicyObjectDTO service = generateServiceObject(dto.getServiceList(), ticket, createObjFlag, dto.getServiceName());
        PolicyObjectDTO time = generateTimeObject(startTime, endTime, ticket);

        if (srcAddress.isObjectFlag() && StringUtils.isNotBlank(srcAddress.getCommandLine())) {
            sb.append(String.format("%s\n", srcAddress.getCommandLine()));
        }
        if (dstAddress.isObjectFlag() && StringUtils.isNotBlank(dstAddress.getCommandLine())) {
            sb.append(String.format("%s\n", dstAddress.getCommandLine()));
        }
        if (service.isObjectFlag() && StringUtils.isNotBlank(service.getCommandLine())) {
            sb.append(String.format("%s\n", service.getCommandLine()));
        }
        if (time != null) {
            sb.append(String.format("%s\n", time.getCommandLine()));
        }
        String businessName = dto.getBusinessName();
        //????????????31?????????????????????????????????1?????????=2?????????
        businessName = strSub(businessName, 31, "GB2312");
        String swapRuleNameId = StringUtils.isNotBlank(dto.getSwapRuleNameId()) ? dto.getSwapRuleNameId() : "";
        int moveSeatCode = dto.getMoveSeatEnum().getCode();

        sb.append("set policy ");
        if (moveSeatCode == MoveSeatEnum.FIRST.getCode()) {
            sb.append("top ");
        }

        sb.append(String.format("name \"%s\" from \"%s\" to  \"%s\" \"%s\" \"%s\" \"%s\" %s ", businessName,
                dto.getSrcZone()==null?"any":dto.getSrcZone(), dto.getDstZone()==null?"any":dto.getDstZone(), srcAddress.getJoin(), dstAddress.getJoin(), service.getJoin(), dto.getAction().toLowerCase()));
        if( time != null) {
            sb.append(String.format("schedule \"%s\"", time.getName()));
        }
        sb.append("\n");

        //???????????????????????????????????????????????????
        if(srcAddress.getCommandLineList() != null && !srcAddress.getCommandLineList().isEmpty()) {
            for(String command : srcAddress.getCommandLineList()){
                sb.append(command);
            }
        }

        if(dstAddress.getCommandLineList() != null && !dstAddress.getCommandLineList().isEmpty()) {
            for(String command : dstAddress.getCommandLineList()){
                sb.append(command);
            }
        }

        if(service.getCommandLineList() != null && !service.getCommandLineList().isEmpty()) {
            for(String command : service.getCommandLineList()){
                sb.append(command);
            }
        }

        if (moveSeatCode == MoveSeatEnum.BEFORE.getCode() || moveSeatCode == MoveSeatEnum.AFTER.getCode()) {
            sb.append(String.format("set policy move ID %s %s\n", dto.getMoveSeatEnum().getKey(), swapRuleNameId));
        }

        return sb.toString();
    }

    public String createMergeCommandLine(CommandlineDTO dto, Integer mergeProperty) {
        StringBuilder sb = new StringBuilder();

        String ticket = dto.getName();
        String srcIp = dto.getSrcIp();
        String dstIp = dto.getDstIp();
        boolean createObjFlag = dto.isCreateObjFlag();

        PolicyObjectDTO srcAddress = new PolicyObjectDTO();
        PolicyObjectDTO dstAddress = new PolicyObjectDTO();

        PolicyObjectDTO service = new PolicyObjectDTO();

        if(mergeProperty == 0){
            srcAddress = generateAddressObject(srcIp, dto.getSrcZone(), ticket, PolicyConstants.SRC, dto.getSrcAddressName(), dto.getSrcIpSystem());
        }else if(mergeProperty == 1){
            dstAddress = generateAddressObject(dstIp, dto.getDstZone(), ticket, PolicyConstants.DST, dto.getDstAddressName(), dto.getDstIpSystem());
        }else if(mergeProperty == 2){
            service = generateServiceObject(dto.getServiceList(), ticket, createObjFlag, dto.getServiceName());
        }

        if (srcAddress.isObjectFlag() && StringUtils.isNotBlank(srcAddress.getCommandLine())) {
            sb.append(String.format("%s\n", srcAddress.getCommandLine()));
        }
        if (dstAddress.isObjectFlag() && StringUtils.isNotBlank(dstAddress.getCommandLine())) {
            sb.append(String.format("%s\n", dstAddress.getCommandLine()));
        }
        if (service.isObjectFlag() && StringUtils.isNotBlank(service.getCommandLine())) {
            sb.append(String.format("%s\n", service.getCommandLine()));
        }
        PolicyRecommendSecurityPolicyDTO mergeDTO = dto.getSecurityPolicy();
        String ruleId = null== mergeDTO ? dto.getName() : mergeDTO.getRuleId();

        //??????
        sb.append(String.format("set policy id %s\n", ruleId));

        if(StringUtils.isNotBlank(srcAddress.getJoin())){
            sb.append(String.format("set src-address \"%s\"\n", srcAddress.getJoin()));
        }

        if(StringUtils.isNotBlank(dstAddress.getJoin())){
            sb.append(String.format("set dst-address \"%s\"\n", srcAddress.getJoin()));
        }

        if(StringUtils.isNotBlank(service.getJoin())){
            sb.append(String.format("set service \"%s\"\n", service.getJoin()));
        }



        //???????????????????????????????????????????????????
        if(srcAddress.getCommandLineList() != null && !srcAddress.getCommandLineList().isEmpty()) {
            for(String command : srcAddress.getCommandLineList()){
                sb.append(command);
            }
        }

        if(dstAddress.getCommandLineList() != null && !dstAddress.getCommandLineList().isEmpty()) {
            for(String command : dstAddress.getCommandLineList()){
                sb.append(command);
            }
        }

        if(service.getCommandLineList() != null && !service.getCommandLineList().isEmpty()) {
            for(String command : service.getCommandLineList()){
                sb.append(command);
            }
        }
        sb.append("exit\n");
        return sb.toString();
    }


    public String editCommandLine(CommandlineDTO dto) {
        PolicyMergeDTO mergeDTO = dto.getMergeDTO();
        if (mergeDTO == null || StringUtils.isBlank(mergeDTO.getRuleId()) || StringUtils.isBlank(mergeDTO.getMergeField())) {
            logger.info("??????????????????????????????????????????ruleID???mergeField ????????????");
            return createMergeCommandLine(dto);
        }
        String ruleId = mergeDTO.getRuleId();
        String mergeField = mergeDTO.getMergeField();

        //??????????????????
        StringBuilder sb = new StringBuilder();
        boolean createObjFlag = dto.isCreateObjFlag();
        PolicyObjectDTO srcAddress = generateAddressObject(dto.getSrcIp(), dto.getSrcZone(), dto.getName(), PolicyConstants.SRC, dto.getSrcAddressName(), dto.getSrcIpSystem());
        PolicyObjectDTO dstAddress = generateAddressObject(dto.getDstIp(), dto.getDstZone(), dto.getName(), PolicyConstants.DST, dto.getDstAddressName(), dto.getDstIpSystem());
        PolicyObjectDTO service = generateServiceObject(dto.getServiceList(), dto.getName(), createObjFlag, dto.getServiceName());

        String firstJoin = "";
        //????????????
        if (mergeField.equals(PolicyConstants.SRC)) {
            if(srcAddress.isObjectFlag() && StringUtils.isNotBlank(srcAddress.getCommandLine())){
                sb.append(String.format("%s\n", srcAddress.getCommandLine()));
            }
            firstJoin = "set src-address " + srcAddress.getJoin();
        }
        if (mergeField.equals(PolicyConstants.DST)) {
            if(dstAddress.isObjectFlag() && StringUtils.isNotBlank(dstAddress.getCommandLine())){
                sb.append(String.format("%s\n", dstAddress.getCommandLine()));
            }
            firstJoin = "set dst-address " + dstAddress.getJoin();
        }
        if (mergeField.equals(PolicyConstants.SERVICE)) {
            if(service.isObjectFlag() && StringUtils.isNotBlank(service.getCommandLine())){
                sb.append(String.format("%s\n", service.getCommandLine()));
            }
            firstJoin = "set service " + service.getJoin();
        }

        //??????
        sb.append(String.format("set policy id %s\n", ruleId));
        sb.append(firstJoin + "\n");

        //???????????????????????????????????????????????????
        if(mergeField.equals(PolicyConstants.SRC) && srcAddress.getCommandLineList() != null && !srcAddress.getCommandLineList().isEmpty()) {
            for(String command : srcAddress.getCommandLineList()){
                sb.append(command);
            }
        }

        if(mergeField.equals(PolicyConstants.DST) && dstAddress.getCommandLineList() != null && !dstAddress.getCommandLineList().isEmpty()) {
            for(String command : dstAddress.getCommandLineList()){
                sb.append(command);
            }
        }

        if(mergeField.equals(PolicyConstants.SERVICE) && service.getCommandLineList() != null && !service.getCommandLineList().isEmpty()) {
            for(String command : service.getCommandLineList()){
                sb.append(command);
            }
        }

        return sb.toString();
    }

    @Override
    public String generatePostCommandline(CommandlineDTO dto) {
        return "";
    }

    private PolicyObjectDTO generateAddressObject(String ipAddress, String zone, String ticket, String type, String existsAddressName, String ipSystem) {
        PolicyObjectDTO dto = new PolicyObjectDTO();
        dto.setObjectFlag(true);

        if(AliStringUtils.isEmpty(ipAddress)){
            dto.setJoin("any");
            return dto;
        }

        if(StringUtils.isNotBlank(existsAddressName)){
            String prefix = zone + "-";
            if(existsAddressName.startsWith(prefix)) {
                String name = existsAddressName.replaceFirst(prefix, "");
                dto.setJoin(name);
                return dto;
            }
        }

        /**
         * 1??????????????????32?????????
         * ?????????1????????????????????????????????????????????????????????????????????????????????????????????????
         */

        String[] arr = ipAddress.split(",");
        //?????????list
        List<String> addressList = new ArrayList<>();

        //????????????????????????????????????????????????????????????
        for (String address : arr) {
            if (IpUtils.isIPRange(address)) {
                String startIp = IpUtils.getStartIpFromRange(address);
                String endIp = IpUtils.getEndIpFromRange(address);
                //???????????????????????????????????????
                String[] startIpArr = startIp.split("\\.");
                String[] endIpArr = endIp.split("\\.");
                Integer startIp_lastNum = Integer.valueOf(startIpArr[3]);
                Integer endIp_lastNum = Integer.valueOf(endIpArr[3]);
                for (int i = startIp_lastNum; i <= endIp_lastNum; i++) {
                    String ip = String.format("%s.%s.%s.%s", startIpArr[0], startIpArr[1], startIpArr[2], i);
                    addressList.add(ip);
                }
            }  else{
                addressList.add(address);
            }
        }

        if(addressList.size() == 1) {
            String address = addressList.get(0);
            String name;
            if(StringUtils.isNotEmpty(ipSystem)){
                name = ipSystem;
                // ???????????????????????????????????????2?????????
                name = strSub(name, MAX_OBJECT_NAME_LENGTH,"GB2312");
                // ????????????????????????
                int len = 0;
                try{
                    len = name.getBytes("GB2312").length;
                }catch (Exception e) {
                    logger.error("???????????????????????????");
                }
                // ???????????????
                if(len > MAX_OBJECT_NAME_LENGTH - 7) {
                    name = strSub(name, MAX_OBJECT_NAME_LENGTH - 7, "GB2312");
                }
                name = String.format("%s_%s", name, DateUtils.getDate().replace("-","").substring(2));
            } else {
                name = String.format("%s_AO_%s", ticket, IdGen.getRandomNumberString());
            }


            dto.setName(name);
            dto.setJoin(name);
            String commandLine = joinAddress(address, zone, name);
            dto.setCommandLine(commandLine);
            return dto;
        }

        StringBuilder sb = new StringBuilder();
        //???????????????
        List<String> groupNameList = new ArrayList<>();
        //???????????????
        String currentGroupName = String.format("%s_AG_%s", ticket, IdGen.getRandomNumberString());

        if(StringUtils.isNotEmpty(ipSystem)){

            currentGroupName = ipSystem;

            currentGroupName = strSub(currentGroupName, MAX_OBJECT_NAME_LENGTH,"GB2312");

            int groupLen = 0;
            try {
                groupLen = currentGroupName.getBytes("GB2312").length;
            } catch (UnsupportedEncodingException e) {
                logger.error("??????????????????",e);
            }


            if(groupLen > MAX_OBJECT_NAME_LENGTH - 7) {
                currentGroupName = strSub(currentGroupName, MAX_OBJECT_NAME_LENGTH - 7, "GB2312");
            }


        }
        int index = 0;
        for (String address : addressList) {
            StringBuilder serialNum = new StringBuilder();
            if (index % 32 == 0) {
                String groupName =  String.format("%s_AG_%s", ticket, IdGen.getRandomNumberString());
                groupNameList.add(groupName);
            }
            String name;
            if(StringUtils.isNotEmpty(ipSystem)) {
                name = ipSystem;
                // ???????????????????????????????????????2?????????
                name = strSub(name, MAX_OBJECT_NAME_LENGTH,"GB2312");
                // ????????????????????????
                int len = 0;
                try{
                    len = name.getBytes("GB2312").length;

                }catch (Exception e) {
                    logger.error("???????????????????????????");
                }
                // ???????????????
                serialNum.append("_").append(index +1);
                int serialLength = serialNum.length();
                if(len > MAX_OBJECT_NAME_LENGTH - (7 + serialLength) ) {
                    name = strSub(name, MAX_OBJECT_NAME_LENGTH - (7 + serialLength), "GB2312");
                }
                name = String.format("%s_%s", name, DateUtils.getDate().replace("-","").substring(2));
                name =  name + serialNum.toString();
            }else{
                name = String.format("%s_AO_%s", ticket, IdGen.getRandomNumberString());
            }

            String commandLine = joinAddress(address, zone, name);
            if(StringUtils.isBlank(commandLine)){
                continue;
            }
            commandLine += String.format("set group address \"%s\" %s add %s\n", StringUtils.isBlank(zone) ? "any" : zone, currentGroupName, name);
            sb.append(commandLine);
            index++;
        }

        dto.setCommandLine(sb.toString());

        //??????1????????????????????????
        if (groupNameList.size() == 1) {
            dto.setName(currentGroupName);
            dto.setJoin(currentGroupName);
        } else {
            //????????????
            dto.setJoin(groupNameList.get(0));  //??????????????????????????????
            List<String> joinList = new ArrayList<>();
            for (int i = 1; i < groupNameList.size(); i++) {
                String commandLine = "";
                if (type.equalsIgnoreCase(PolicyConstants.SRC)) {
                    commandLine = String.format("set src-address %s\n", groupNameList.get(i));
                } else {
                    commandLine = String.format("set dst-address %s\n", groupNameList.get(i));
                }
                joinList.add(commandLine);
            }
            dto.setCommandLineList(joinList);
        }

        return dto;
    }

    private String joinAddress(String address, String zone, String name) {
        StringBuilder sb = new StringBuilder();
        if(IpUtils.isIPSegment(address)) {
            String ip = IpUtils.getIpFromIpSegment(address);
            String maskBit = IpUtils.getMaskBitFromIpSegment(address);
            String mask = IpUtils.getMaskByMaskBit(maskBit);
            String addressObjectString = String.format("set address \"%s\" \"%s\" %s %s \n", StringUtils.isBlank(zone)?"any":zone , name, ip, mask);
            sb.append(addressObjectString);
        } else if (IpUtils.isIPRange(address)) {

        } else {
            String addressObjectString = String.format("set address \"%s\" \"%s\" %s %s \n", StringUtils.isBlank(zone)?"any":zone, name, address, "255.255.255.255");
            sb.append(addressObjectString);
        }
        return sb.toString();
    }

    /**
     * ??????????????????
     * @param serviceDTOList ????????????
     * @param ticket ?????????
     * @return ????????????
     */
    private PolicyObjectDTO generateServiceObject(List<ServiceDTO> serviceDTOList, String ticket, boolean createObjFlag, String existsServiceName) {
        PolicyObjectDTO dto = new PolicyObjectDTO();
        if(StringUtils.isNotBlank(existsServiceName)){
            dto.setObjectFlag(true);
            dto.setJoin(existsServiceName);
            return dto;
        }

        if(serviceDTOList.size() == 0) {
            logger.info("???????????????...");
            return dto;
        }

        /**
         * 1??????????????????32???????????? ??????????????????????????????8????????????????????????8???+
         * ?????????1????????????????????????????????????????????????????????????????????????????????????????????????
         */
        List<String> groupNameList = new ArrayList<>();
        Map<String, String> groupNameMap = new HashMap<>();

        List<String> serviceNameList = new ArrayList<>();
        //???????????????????????????????????????
        Map<String, String> serviceNameMap = new HashMap<>();
        StringBuilder sb = new StringBuilder();

        for(ServiceDTO service : serviceDTOList) {
            int protocolNum = Integer.valueOf(service.getProtocol());
            String protocolString = ProtocolUtils.getProtocolByValue(protocolNum).toLowerCase();
            //any??????????????????????????????any???????????????
            if(protocolString.equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_ANY)) {
                dto.setCommandLine("any");
                dto.setJoin(dto.getCommandLine());
                dto.setObjectFlag(false);
                return dto;
            }

            //????????????????????????1????????????  ????????? tcp |  udp | icmp ???any??????
            if(serviceDTOList.size() == 1) {
                if (protocolString.equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_TCP) ||
                        protocolString.equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_UDP)) {
                    if (service.getDstPorts().equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_ANY)) {
                        dto.setCommandLine(protocolString + "-any");
                        dto.setJoin(dto.getCommandLine());
                        dto.setObjectFlag(false);
                        return dto;
                    }
                }else if(protocolString.equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_ICMP)) {
                    if (StringUtils.isBlank(service.getType())) {
                        dto.setJoin(String.format("%s-any", protocolString.toLowerCase()));
                        dto.setObjectFlag(false);
                        return dto;
                    }
                }
            }

            if (protocolString.equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_ICMP)) {
                if(StringUtils.isBlank(service.getType()) && StringUtils.isBlank(service.getCode())) {
                    String name = String.format("%s-any", protocolString.toLowerCase());
                    serviceNameList.add(name);
                    serviceNameMap.put(name, name);
                }else{
                    String name = PolicyConstants.POLICY_STR_VALUE_ICMP.toLowerCase();
                    serviceNameList.add(name);
                    serviceNameMap.put(name, name);
                    //Type????????????????????????type??????????????????1??? Code??????????????????0
                    if(StringUtils.isBlank(service.getType())) {
                        service.setType("1");
                    }
                    if(StringUtils.isBlank(service.getCode())){
                        service.setCode("0");
                    }
                    sb.append(String.format("\nset service \"%s\" protocol icmp type %d code %d\n", name, Integer.valueOf(service.getType()), Integer.valueOf(service.getCode())));
                }
            }else{
                //tcp???udp????????? ???????????????any????????????????????????????????????????????????
                if(service.getDstPorts().equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_ANY)){
                    String name = String.format("%s-any", protocolString.toLowerCase());
                    serviceNameList.add(name);
                    serviceNameMap.put(name, name);
                    continue;
                }

                String[] srcPorts = service.getSrcPorts().split(",");
                String[] dstPorts = service.getDstPorts().split(",");

                boolean firstLine = true;
                int objIndex = 0;
                StringBuilder tempName = new StringBuilder();
                tempName.append(protocolString);
                //??????????????????????????????
                String name = String.format("%s_SO_%s", ticket, IdGen.getRandomNumberString());
                serviceNameList.add(name);
                for (String srcPort : srcPorts) {
                    for (String dstPort : dstPorts) {
                        if(tempName.length() == 0){
                            tempName.append(protocolString);
                        }
                        tempName.append("_" + dstPort);
                        String tempNameString = tempName.toString();
                        if(tempNameString.length() > getMaxNameLength()) {
                            String shortName = tempNameString.substring(0, getMaxNameLength()-5);
                            tempNameString = String.format("%s_etcs", shortName.substring(0, shortName.lastIndexOf("_")));
                        }
                        serviceNameMap.put(name, tempNameString);
                        if (!PortUtils.isPortRange(srcPort) && !PolicyConstants.POLICY_STR_VALUE_ANY.equalsIgnoreCase(srcPort)) {
                            srcPort = srcPort + "-" + srcPort;
                        }
                        if (!PortUtils.isPortRange(dstPort) && !PolicyConstants.POLICY_STR_VALUE_ANY.equalsIgnoreCase(dstPort)) {
                            dstPort = dstPort + "-" + dstPort;
                        }

                        if (firstLine) {
                            sb.append(String.format("\nset service \"%s\" protocol %s %s %s\n", name, protocolString,
                                    PolicyConstants.POLICY_STR_VALUE_ANY.equalsIgnoreCase(srcPort)?"":String.format("src-port %s", PortUtils.getPortString(srcPort, PortUtils.DASH_FORMAT)),
                                    PolicyConstants.POLICY_STR_VALUE_ANY.equalsIgnoreCase(dstPort)?"":String.format("dst-port %s", PortUtils.getPortString(dstPort, PortUtils.DASH_FORMAT))));
                            //sb.append("\n");
                        } else {
                            sb.append(String.format("set service \"%s\" + %s %s %s\n", name, protocolString,
                                    String.format("src-port %s", PortUtils.getPortString(srcPort, PortUtils.DASH_FORMAT)),
                                    PolicyConstants.POLICY_STR_VALUE_ANY.equalsIgnoreCase(dstPort)?"":String.format("dst-port %s", PortUtils.getPortString(dstPort, PortUtils.DASH_FORMAT))));
                            objIndex++;
                        }

                        //?????????????????????8???
                        if (objIndex % 8 == 0 && objIndex > 0 && !firstLine ) {
                            firstLine = true;
                            //??????8??????????????????
                            name = String.format("%s_SO_%s", ticket, IdGen.getRandomNumberString());
                            serviceNameList.add(name);
                            tempName.setLength(0);
                        }else{
                            firstLine = false;
                        }
                    }
                }

            }
        }

        dto.setObjectFlag(true);


        //??????1???????????????????????????
        if (serviceNameList.size() == 1) {
            String name = serviceNameMap.get(serviceNameList.get(0));
            dto.setName(name);
            dto.setJoin(name);
        } else {
            //???????????????????????????
            String groupName = "";
            StringBuilder tempName = new StringBuilder();
            for (int i = 0; i < serviceNameMap.size(); i++) {
                if (i % 32 == 0) {
                    groupName =  String.format("%s_SG_%s", ticket, IdGen.getRandomNumberString());
                    groupNameList.add(groupName);
                    tempName.setLength(0);
                }
                String key = serviceNameList.get(i);
                String value = serviceNameMap.get(key);
                sb.append(String.format("set group service %s add %s\n", groupName, value));
                /*
                icmp ?????????????????????????????????????????????????????????icmp-any???????????????icmp???
                tcp???udp????????????????????????????????????????????????tcp-any???????????????????????????-any??????icmp??????
                 ?????????set group service tcp_80_1000-2000_udp_10-20_icmp add icmp-any
                 */
                if(value.substring(value.length()-4).equalsIgnoreCase("-" + PolicyConstants.POLICY_STR_VALUE_ANY)) {
                    value = value.substring(0, value.length()-4);
                }
                tempName.append(value+"_");
                groupNameMap.put(groupName, tempName.toString());
            }

            //??????=
            for (Map.Entry<String, String> entry : groupNameMap.entrySet()) {
                String key = entry.getKey();
                String value = entry.getValue();
                if (value.substring(value.length() - 1).equals("_")) {
                    value = value.substring(0, value.length() - 1);
                    //group name????????????
                    if(value.length() > getMaxNameLength()) {
                        String shortName = value.substring(0, getMaxNameLength()-6);
                        value = String.format("%s_etcsg", shortName.substring(0, shortName.lastIndexOf("_")));
                    }
                    groupNameMap.put(key, value);
                }
            }


            //??????????????????????????????
            String name = groupNameMap.get(groupNameList.get(0));
            dto.setJoin(name);
            dto.setName(name);
            List<String> joinList = new ArrayList<>();
            for (int i = 1; i < groupNameMap.size(); i++) {
                String commandLine = "";
                String key = groupNameList.get(i);
                String valule = groupNameMap.get(key);
                commandLine = String.format("set service %s\n", valule);
                joinList.add(commandLine);
            }
            dto.setCommandLineList(joinList);
        }

        //??????????????????
        String command =  sb.toString();
        for(Map.Entry<String, String> entry : serviceNameMap.entrySet()){
            command = command.replace(entry.getKey(), entry.getValue());
        }
        //?????????????????????
        for(Map.Entry<String, String> entry : groupNameMap.entrySet()){
            command = command.replace(entry.getKey(), entry.getValue());
        }
        dto.setCommandLine(command);
        return dto;
    }


    private PolicyObjectDTO generateTimeObject(String startTime, String endTime, final String ticket) {
        PolicyObjectDTO dto = new PolicyObjectDTO();
        if(startTime == null) {
            return null;
        }

        // ?????????ssg??????????????????????????????????????????????????????????????????????????????????????????
        // String name = String.format("%s_TR_%s", ticket, IdGen.getRandomNumberString());
        String name = String.format("%s_TR", ticket);

        String commandline = String.format("set scheduler \"%s\" once start %s stop %s comment \"%s\"", name,
                formatTimeString(startTime), formatTimeString(endTime), name);
        dto.setName(name);
        dto.setCommandLine(commandline);
        return dto;
    }

    private String formatTimeString(String timeString) {
        return TimeUtils.transformDateFormat(timeString, TimeUtils.EUROPEAN_TIME_FORMAT, TimeUtils.JUNIPER_SSG_TIME_FOMRAT);
    }

    @Override
    public int getMaxNameLength() {
        return MAX_NAME_LENGTH;
    }


    public static void main(String[] args) {
        CommandlineDTO dto = CommandlineDTO.getInstanceDemo();
        SecurityJuniperSsg juniperSsg = new SecurityJuniperSsg();
        String commandLine = juniperSsg.composite(dto);
        System.out.println("commandline:\n" + commandLine);
    }
}
