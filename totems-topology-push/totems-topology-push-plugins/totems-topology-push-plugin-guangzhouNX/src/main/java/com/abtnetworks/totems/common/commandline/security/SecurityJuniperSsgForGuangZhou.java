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
import com.abtnetworks.totems.common.enums.ActionEnum;
import com.abtnetworks.totems.common.enums.DeviceModelNumberEnum;
import com.abtnetworks.totems.common.enums.MoveSeatEnum;
import com.abtnetworks.totems.common.enums.PolicyEnum;
import com.abtnetworks.totems.common.utils.*;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @Author: wxx
 * @Date: 2021/5/17 09:20
 * @Desc:Juniper SSG
 */
@Service
@CustomCli(value = DeviceModelNumberEnum.SSG, type = PolicyEnum.SECURITY)
public class SecurityJuniperSsgForGuangZhou extends SecurityPolicyGenerator implements PolicyGenerator {

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
            return createCommandLine(dto);
        }else{
            return editCommandLine(dto);
        }
    }

    public String createCommandLine(CommandlineDTO dto) {
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
        //最大长度31位，超出部分截断处理。1个中文=2个字符
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

        //如果是多个对象，则还需要输出命令行
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
        //针对广州农信新增最终日志
        sb.append("set log session-init\n");


        return sb.toString();
    }


    public String editCommandLine(CommandlineDTO dto) {
        PolicyMergeDTO mergeDTO = dto.getMergeDTO();
        if (mergeDTO == null || StringUtils.isBlank(mergeDTO.getRuleId()) || StringUtils.isBlank(mergeDTO.getMergeField())) {
            logger.info("进行修改策略命令时，合并信息ruleID、mergeField 有为空的");
            return createCommandLine(dto);
        }
        String ruleId = mergeDTO.getRuleId();
        String mergeField = mergeDTO.getMergeField();

        //正式开始编辑
        StringBuilder sb = new StringBuilder();
        boolean createObjFlag = dto.isCreateObjFlag();
        PolicyObjectDTO srcAddress = generateAddressObject(dto.getSrcIp(), dto.getSrcZone(), dto.getName(), PolicyConstants.SRC, dto.getSrcAddressName(), dto.getSrcIpSystem());
        PolicyObjectDTO dstAddress = generateAddressObject(dto.getDstIp(), dto.getDstZone(), dto.getName(), PolicyConstants.DST, dto.getDstAddressName(), dto.getDstIpSystem());
        PolicyObjectDTO service = generateServiceObject(dto.getServiceList(), dto.getName(), createObjFlag, dto.getServiceName());

        String firstJoin = "";
        //创建对象
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

        //修改
        sb.append(String.format("set policy id %s\n", ruleId));
        sb.append(firstJoin + "\n");

        //如果是多个对象，则还需要输出命令行
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
         * 1个组只能引用32个对象
         * 如果是1个地址，则直接引用地址对象即可，如果是多个，则放进地址组再引用组
         */

        String[] arr = ipAddress.split(",");
        //转存的list
        List<String> addressList = new ArrayList<>();

        //预先判断是否有范围，有则解析成单个的主机
        for (String address : arr) {
            if (IpUtils.isIPRange(address)) {
                String startIp = IpUtils.getStartIpFromRange(address);
                String endIp = IpUtils.getEndIpFromRange(address);
                //取最后一个字符，循环中间值
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
            String name="";
            if(StringUtils.isNotEmpty(ipSystem)){
                name = ipSystem;
                // 对象名称长度限制，一个中文2个字符
                name = strSub(name, MAX_OBJECT_NAME_LENGTH,"GB2312");
                // 对象名称长度限制
                int len = 0;
                try{
                    len = name.getBytes("GB2312").length;
                }catch (Exception e) {
                    logger.error("字符串长度计算异常");
                }
                // 序列号长度
                if(len > MAX_OBJECT_NAME_LENGTH - 7) {
                    name = strSub(name, MAX_OBJECT_NAME_LENGTH - 7, "GB2312");
                    name = String.format("%s_%s", name, DateUtils.getDate().replace("-","").substring(2));
                }
                name = String.format("%s_%s", ipSystem, ipAddress);
            } else {
                if (address != null && IpUtils.isIPSegment(address)) {
                    String mask = IpUtils.getMaskBitFromIpSegment(address);
                    if (StringUtils.isNotBlank(mask) && mask.equalsIgnoreCase("32")) {
                        name = "ip_" + address;
                    } else {
                        String arr2[] = address.split("/");
                        name = "net_" + arr2[0] + "/" + arr2[1];
                    }
                } else {
                    name = "ip_" + address;
                }
            }


            dto.setName(name);
            dto.setJoin(name);
            String commandLine = joinAddress(address, zone, name);
            dto.setCommandLine(commandLine);
            return dto;
        }

        StringBuilder sb = new StringBuilder();
        //组名称集合
        List<String> groupNameList = new ArrayList<>();
        //当前组名称
        String currentGroupName = String.format("%s_AG_%s", ticket, IdGen.getRandomNumberString());

        if(StringUtils.isNotEmpty(ipSystem)){

            currentGroupName = ipSystem;

            currentGroupName = strSub(currentGroupName, MAX_OBJECT_NAME_LENGTH,"GB2312");

            int groupLen = 0;
            try {
                groupLen = currentGroupName.getBytes("GB2312").length;
            } catch (UnsupportedEncodingException e) {
                logger.error("字节转化异常",e);
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
            String name="";
            String netname="";
            if(StringUtils.isNotEmpty(ipSystem)) {
                name = ipSystem;
                // 对象名称长度限制，一个中文2个字符
                name = strSub(name, MAX_OBJECT_NAME_LENGTH,"GB2312");
                // 对象名称长度限制
                int len = 0;
                try{
                    len = name.getBytes("GB2312").length;

                }catch (Exception e) {
                    logger.error("字符串长度计算异常");
                }
                // 序列号长度
                serialNum.append("_").append(index +1);
                int serialLength = serialNum.length();
                if(len > MAX_OBJECT_NAME_LENGTH - (7 + serialLength) ) {
                    name = strSub(name, MAX_OBJECT_NAME_LENGTH - (7 + serialLength), "GB2312");
                }
                name = String.format("%s_%s", name, DateUtils.getDate().replace("-","").substring(2));
                name =  name + serialNum.toString();
                name = String.format("%s_%s", ipSystem, ipAddress);
            }else{
                if (address != null && IpUtils.isIPSegment(address)) {
                    String mask = IpUtils.getMaskBitFromIpSegment(address);
                    if (StringUtils.isNotBlank(mask) && mask.equalsIgnoreCase("32")) {
                        name = "ip_" + address;
                    } else {
                        String arr2[] = address.split("/");
                        name = "net_" + arr2[0] + "/" + arr2[1];
                    }
                } else  {
                    name = "ip_" + address;
                }
            }

            String commandLine = joinAddress(address, zone, name);
            if(StringUtils.isBlank(commandLine)){
                continue;
            }
            //commandLine += String.format("set group address %s %s add %s\n", zone, currentGroupName, name);
            sb.append(commandLine);
            index++;
        }

        dto.setCommandLine(sb.toString());

        //只有1个组，则直接引用
        if (groupNameList.size() == 1) {
            dto.setName(currentGroupName);
            dto.setJoin(currentGroupName);
        } else {
            //有多个组
            dto.setJoin(groupNameList.get(0));  //衔接时，默认取第一个
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
     * 生成服务对象
     * @param serviceDTOList 服务列表
     * @param ticket 工单号
     * @return 服务对象
     */
    private PolicyObjectDTO generateServiceObject(List<ServiceDTO> serviceDTOList, String ticket, boolean createObjFlag, String existsServiceName) {
        PolicyObjectDTO dto = new PolicyObjectDTO();
        if(StringUtils.isNotBlank(existsServiceName)){
            dto.setObjectFlag(true);
            dto.setJoin(existsServiceName);
            return dto;
        }

        if(serviceDTOList.size() == 0) {
            logger.info("服务组为空...");
            return dto;
        }

        /**
         * 1个组只能引用32个对象， 每个对象里面，只能有8条，即后面只能跟8个+
         * 如果是1个服务，则直接引用服务对象即可，如果是多个，则放进服务组再引用组
         */
        List<String> groupNameList = new ArrayList<>();
        Map<String, String> groupNameMap = new HashMap<>();

        List<String> serviceNameList = new ArrayList<>();
        //根据服务名称、端口生成名称
        Map<String, String> serviceNameMap = new HashMap<>();
        StringBuilder sb = new StringBuilder();

        for(ServiceDTO service : serviceDTOList) {
            int protocolNum = Integer.valueOf(service.getProtocol());
            String protocolString = ProtocolUtils.getProtocolByValue(protocolNum).toLowerCase();
            //any协议，包含所有，遇到any，直接返回
            if(protocolString.equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_ANY)) {
                dto.setCommandLine("any");
                dto.setJoin(dto.getCommandLine());
                dto.setObjectFlag(false);
                return dto;
            }

            //不建对象，且只有1条服务，  内容是 tcp |  udp | icmp ，any端口
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
                    //Type不为空的话，使用type，反之默认为1， Code为空则默认为0
                    if(StringUtils.isBlank(service.getType())) {
                        service.setType("1");
                    }
                    if(StringUtils.isBlank(service.getCode())){
                        service.setCode("0");
                    }
                    sb.append(String.format("\nset service \"%s\" protocol icmp type %d code %d\n", name, Integer.valueOf(service.getType()), Integer.valueOf(service.getCode())));
                }
            }else{
                //tcp、udp协议， 但是端口是any，则直接添加到组即可，不用建对象
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
                //初始，第一次的对象名
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

                        //每个对象，限制8条
                        if (objIndex % 8 == 0 && objIndex > 0 && !firstLine ) {
                            firstLine = true;
                            //超过8条，新建对象
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


        //只有1个对象，则直接引用
        if (serviceNameList.size() == 1) {
            String name = serviceNameMap.get(serviceNameList.get(0));
            dto.setName(name);
            dto.setJoin(name);
        } else {
            //多个对象，分组处理
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
                icmp 不建对象，直接添加到组，但：协议名称是icmp-any，组名称是icmp，
                tcp、udp，没有端口时，也是直接添加到组，tcp-any，组名称，需要去掉-any，同icmp一致
                 示例：set group service tcp_80_1000-2000_udp_10-20_icmp add icmp-any
                 */
                if(value.substring(value.length()-4).equalsIgnoreCase("-" + PolicyConstants.POLICY_STR_VALUE_ANY)) {
                    value = value.substring(0, value.length()-4);
                }
                tempName.append(value+"_");
                groupNameMap.put(groupName, tempName.toString());
            }

            //去掉=
            for (Map.Entry<String, String> entry : groupNameMap.entrySet()) {
                String key = entry.getKey();
                String value = entry.getValue();
                if (value.substring(value.length() - 1).equals("_")) {
                    value = value.substring(0, value.length() - 1);
                    //group name限制长度
                    if(value.length() > getMaxNameLength()) {
                        String shortName = value.substring(0, getMaxNameLength()-6);
                        value = String.format("%s_etcsg", shortName.substring(0, shortName.lastIndexOf("_")));
                    }
                    groupNameMap.put(key, value);
                }
            }


            //衔接时，默认取第一个
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

        //服务名称替换
        String command =  sb.toString();
        for(Map.Entry<String, String> entry : serviceNameMap.entrySet()){
            command = command.replace(entry.getKey(), entry.getValue());
        }
        //服务组名称替换
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

        // 生成的ssg命令行中时间对象名称过长会导致下发失败，所以去掉最后的随机数
        // String name = String.format("%s_TR_%s", ticket, IdGen.getRandomNumberString());
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        Date startDate = new Date();
        Date endDate = new Date();
        try {
            startDate = sdf.parse(startTime);
            endDate = sdf.parse(endTime);
        } catch (Exception e) {
            e.printStackTrace();
        }
        SimpleDateFormat dst = new SimpleDateFormat("yyyyMMddHHmm");
        String finstartTime=dst.format(startDate).substring(2,12);
        String finendTime=dst.format(endDate).substring(2,12);

        String name=finstartTime+"_"+finendTime;

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
        SecurityJuniperSsgForGuangZhou juniperSrx = new SecurityJuniperSsgForGuangZhou();
        List<ServiceDTO> srcport=new ArrayList<>();
        ServiceDTO serviceDTO = new ServiceDTO();
        serviceDTO.setProtocol("6");
        serviceDTO.setSrcPorts("22");
        serviceDTO.setDstPorts("33");

        srcport.add(serviceDTO);
        dto.setSrcIp("127.0.0.1/10,2.2.2.2");
        dto.setDstIp("1.1.1.1-3.3.3.3");
        dto.setSrcIpSystem("aa");
        dto.setDstIpSystem("aa");
        dto.setStartTime("2020-06-15 12:00:00");
        dto.setEndTime("2020-06-17 12:00:00");
        dto.setServiceList(srcport);

//
       /* dto.setSrcIp("1111:a1a1::1111");
        dto.setDstIp("1111:a1a1::1121-1111:a1a1::1112");*/
        dto.setIpType(0);

        String commandLine = juniperSrx.composite(dto);
        System.out.println("commandline:\n" + commandLine);
    }
}