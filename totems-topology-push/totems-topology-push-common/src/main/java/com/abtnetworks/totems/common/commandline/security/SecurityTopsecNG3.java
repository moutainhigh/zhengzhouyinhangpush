package com.abtnetworks.totems.common.commandline.security;

import com.abtnetworks.totems.common.commandline.PolicyGenerator;
import com.abtnetworks.totems.common.commandline.SecurityPolicyGenerator;
import com.abtnetworks.totems.common.constants.PolicyConstants;
import com.abtnetworks.totems.common.dto.*;
import com.abtnetworks.totems.common.dto.commandline.CommandlineDTO;
import com.abtnetworks.totems.common.dto.commandline.PolicyMergeDTO;
import com.abtnetworks.totems.common.dto.commandline.PolicyObjectDTO;
import com.abtnetworks.totems.common.dto.commandline.ServiceDTO;
import com.abtnetworks.totems.common.dto.commandline.device.TopSecFirstPolicyDTO;
import com.abtnetworks.totems.common.enums.ActionEnum;
import com.abtnetworks.totems.common.enums.DeviceModelNumberEnum;
import com.abtnetworks.totems.common.enums.MoveSeatEnum;
import com.abtnetworks.totems.common.utils.*;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * @author lifei
 * @desc NG3安全策略命令行
 * @date 2021/9/13 11:04
 */
@Service
public class SecurityTopsecNG3 extends SecurityPolicyGenerator implements PolicyGenerator {

    public final int MAX_OBJECT_NAME_LENGTH = 30;
    private static Logger logger = Logger.getLogger(SecurityTopsecNG2.class);

    @Autowired
    private SecurityTopsec securityTopsecService;


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
        dto.setRestSrcAddressList(existObjectDTO.getRestSrcAddressList());
        dto.setRestDstAddressList(existObjectDTO.getRestDstAddressList());
        dto.setExistSrcAddressList(existObjectDTO.getExistSrcAddressList());
        dto.setExistDstAddressList(existObjectDTO.getExistDstAddressList());
        dto.setSrcAddressName(existObjectDTO.getSrcAddressObjectName());
        dto.setDstAddressName(existObjectDTO.getDstAddressObjectName());
        dto.setServiceName(existObjectDTO.getServiceObjectName());
        dto.setExistServiceNameList(existObjectDTO.getExistServiceNameList());
        dto.setRestServiceList(existObjectDTO.getRestServiceList());
        dto.setSwapRuleNameId(settingDTO.getSwapNameId());
        dto.setIdleTimeout(policyDTO.getIdleTimeout());

        dto.setCurrentId(settingDTO.getPolicyId());
        GeneratedObjectDTO generatedDto = cmdDTO.getGeneratedObject();
        generatedDto.setPolicyName(dto.getName());
        dto.setGeneratedDto(generatedDto);
        String  commandLine = composite(dto);
        generatedDto.setAddressTypeMap(dto.getAddressTypeMap());
        generatedDto.setServiceObjectNameList(dto.getServiceObjectNameList());
        generatedDto.setTimeObjectNameList(dto.getTimeObjectNameList());
        return commandLine;
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
        String srcZoneJoin = StringUtils.isNotBlank(dto.getSrcZone()) ? "srcarea '" + dto.getSrcZone() + "' " : "";
        String dstZoneJoin = StringUtils.isNotBlank(dto.getDstZone()) ? "dstarea '" + dto.getDstZone() + "' " : "";

        PolicyObjectDTO srcAddress = generateAddressObject(dto.getSrcIp(), ticket, dto.getSrcAddressName(), dto.getSrcIpSystem(),dto.getIpType(),dto.getRestSrcAddressList(),dto.getExistSrcAddressList());
        PolicyObjectDTO dstAddress = generateAddressObject(dto.getDstIp(), ticket, dto.getDstAddressName(), dto.getDstIpSystem(),dto.getIpType(),dto.getRestDstAddressList(),dto.getExistDstAddressList());

        List<String> exitNameForService = new ArrayList<>();
        List<ServiceDTO> restForService = new ArrayList<>();
        covertServiceParam(dto.getServiceName(),dto.getExistServiceNameList(),dto.getRestServiceList() ,restForService ,exitNameForService);
        PolicyObjectDTO service = new PolicyObjectDTO();
        if(DeviceModelNumberEnum.TOPSEC_NG4.equals(dto.getModelNumber())){
            service = generateServiceObjectForNG4(restForService, exitNameForService,dto.getIpType());
        }else {
            service = generateServiceObject(restForService, exitNameForService,dto.getIpType());
        }
        SecurityTopsec securityTopsec = new SecurityTopsec();
        PolicyObjectDTO time = securityTopsec.generateTimeObject(startTime, endTime, ticket);

        StringBuilder define = new StringBuilder();

        if(srcAddress.isObjectFlag() && StringUtils.isNotBlank(srcAddress.getCommandLine())){
            define.append(String.format("%s", srcAddress.getCommandLine()));
        }
        if(dstAddress.isObjectFlag() && StringUtils.isNotBlank(dstAddress.getCommandLine())){
            define.append(String.format("%s", dstAddress.getCommandLine()));
        }
        if(StringUtils.isNotBlank(service.getCommandLine())) {
            define.append(String.format("%s", service.getCommandLine()));
        }

        if(dto.getIdleTimeout()!=null){
            sb.append("permanent yes session-timeout ").append(dto.getIdleTimeout()).append(" ");
        }
        if(time != null) {
            define.append(String.format("%s", time.getCommandLine()));
        }

        if(define.length() > 0) {
            sb.append("define\n");
            sb.append(define.toString());
            sb.append("end\n");
        }

        sb.append("firewall\n");

        String srcAddressString = AliStringUtils.isEmpty(srcAddress.getJoin())==true?"":String.format("src \'%s\' ", srcAddress.getJoin().trim());
        String dstAddressString = AliStringUtils.isEmpty(dstAddress.getJoin())==true?"":String.format("dst \'%s\' ", dstAddress.getJoin().trim());
        String serviceString = AliStringUtils.isEmpty(service.getJoin())==true?"": "IP".equalsIgnoreCase(service.getJoin()) ? "" : String.format("service \'%s\' ", service.getJoin().trim());
        String name = dto.getBusinessName();
        sb.append(String.format("policy add name %s action %s %s%s" +
                "%s%s%s",name, dto.getAction().equalsIgnoreCase("permit")?"accept":"deny", srcZoneJoin, dstZoneJoin, srcAddressString, dstAddressString, serviceString));
        if(time != null) {
            sb.append(String.format("schedule %s ", time.getJoin()));
        }

        if (!AliStringUtils.isEmpty(dto.getGroupName())) {
            sb.append(String.format("group_name %s ", dto.getGroupName()));
        }



        String swapRuleNameId = StringUtils.isNotBlank(dto.getSwapRuleNameId()) ? dto.getSwapRuleNameId() : "";
        int moveSeatCode = dto.getMoveSeatEnum().getCode();
        if (moveSeatCode == MoveSeatEnum.BEFORE.getCode()) {
            if(!AliStringUtils.isEmpty(dto.getDescription())){
                sb.append(String.format("comment %s ", dto.getDescription()));
            }
            if(!AliStringUtils.isEmpty(swapRuleNameId)) {
                sb.append(String.format("before %s ", swapRuleNameId));
            }
            sb.append("\n");
        } else if (moveSeatCode == MoveSeatEnum.AFTER.getCode()) {
            if(!AliStringUtils.isEmpty(dto.getDescription())){
                sb.append(String.format("comment %s ", dto.getDescription()));
            }
            sb.append("\n");
            sb.append(String.format("firewall policy move after %s\n", swapRuleNameId));
        } else {
            TopSecFirstPolicyDTO topSecFirstPolicyDTO = securityTopsecService.getFirstPolicyId(dto.getDeviceUuid(), dto.getRuleListUuid(), dto.getGroupName());
            if (StringUtils.isEmpty(dto.getGroupName()) && StringUtils.isNotEmpty(topSecFirstPolicyDTO.getGroupName())) {
                String groupName = String.format("\'%s\'", topSecFirstPolicyDTO.getGroupName().trim());
                sb.append(String.format("group_name %s ", groupName));
            }
            if (!AliStringUtils.isEmpty(dto.getDescription())) {
                sb.append(String.format("comment %s ", dto.getDescription()));
            }
            // KSH-5667
            String deviceName = null != dto.getModelNumber() ? dto.getModelNumber().getKey() : DeviceModelNumberEnum.TOPSEC_NG2.getKey();
            if (DeviceModelNumberEnum.TOPSEC_NG3.getKey().equals(deviceName)) {
                // 走新的NG3的逻辑
                if (!AliStringUtils.isEmpty(topSecFirstPolicyDTO.getRuleName())) {
                    sb.append(String.format("before %s ", topSecFirstPolicyDTO.getRuleName()));
                }
            }else {
                // 走之前NG2的逻辑
                if (!AliStringUtils.isEmpty(topSecFirstPolicyDTO.getRuleId())) {
                    sb.append(String.format("before %s ", topSecFirstPolicyDTO.getRuleId()));
                }
            }
            sb.append("\n");
        }


        sb.append("end\n");

        return sb.toString();
    }

    private PolicyObjectDTO generateServiceObjectForNG4(List<ServiceDTO> serviceDTOList, List<String> existsServiceNameList, Integer ipType) {
        PolicyObjectDTO dto = new PolicyObjectDTO();
        if (serviceDTOList.size() == 0 && existsServiceNameList.size() == 0) {
            dto.setJoin("");
            return dto;
        }

        StringBuilder nameSb = new StringBuilder("");
        if (existsServiceNameList.size() > 0) {
            for (String name : existsServiceNameList) {
                nameSb.append(" ");
                nameSb.append(name);
            }
        }

        StringBuilder sb = new StringBuilder("");
        for (ServiceDTO service : serviceDTOList) {
            int protocolNum = Integer.valueOf(service.getProtocol());
            String protocolString = ProtocolUtils.getProtocolByValue(protocolNum).toLowerCase();
            if (protocolString.equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_ANY)) {
                dto.setJoin("");
                return dto;
            }

            if(StringUtils.isNotEmpty(service.getDstPorts())){
                String[]  ports = service.getDstPorts().split(",");
                for(String port : ports){
                    String name = getServiceName(service,ipType,port);

                    nameSb.append(" ");
                    nameSb.append(name);
                    if(ipType!=null&&ipType==0) {
                        //ipv4
                        if (!protocolString.equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_ICMP)) {
                            if (!port.equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_ANY)) {
                                if (PortUtils.isPortRange(port)) {
                                    String start = PortUtils.getStartPort(port);
                                    String end = PortUtils.getEndPort(port);
                                    sb.append(String.format("service add name %s protocol %s port1 %s port2 %s \n", name, service.getProtocol(), start, end));
                                } else {
                                    sb.append(String.format("service add name %s protocol %s port1 %s \n", name, service.getProtocol(), port));
                                }
                            }
                        }
                    }else{
                        if (protocolString.equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_ICMPV6)) {
                            sb.append(String.format("service add name ICMPv6 protocol 58 \n"));
                        } else {
                            if (!port.equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_ANY)) {
                                if (PortUtils.isPortRange(port)) {
                                    String start = PortUtils.getStartPort(port);
                                    String end = PortUtils.getEndPort(port);
                                    sb.append(String.format("service add name %s protocol %s port1 %s port2 %s \n", name, service.getProtocol(), start, end));
                                } else {
                                    sb.append(String.format("service add name %s protocol %s port1 %s \n", name, service.getProtocol(), port));
                                }
                            }
                        }
                    }
                }
            }
        }

        if (nameSb.length() > 0) {
            nameSb.deleteCharAt(0);
        }

        dto.setCommandLine(sb.toString());
        dto.setName(nameSb.toString());
        dto.setJoin(nameSb.toString());

        return dto;
    }

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

    public String editCommandLine(CommandlineDTO dto) {

        PolicyMergeDTO mergeDTO = dto.getMergeDTO();
        if (mergeDTO == null || StringUtils.isBlank(mergeDTO.getRuleId()) || StringUtils.isBlank(mergeDTO.getMergeField())) {
            logger.info("进行修改策略命令时，合并信息ruleID、mergeField 有为空的");
            return createCommandLine(dto);
        }
        String ruleId = mergeDTO.getRuleId();
        String mergeField = mergeDTO.getMergeField();

        String ticket = dto.getName();

        //正式开始编辑
        PolicyObjectDTO srcAddress = generateAddressObject(dto.getSrcIp(), ticket, dto.getSrcAddressName(), dto.getSrcIpSystem(),dto.getIpType(),dto.getRestSrcAddressList(),dto.getExistSrcAddressList());
        PolicyObjectDTO dstAddress = generateAddressObject(dto.getDstIp(), ticket, dto.getDstAddressName(), dto.getDstIpSystem(),dto.getIpType(),dto.getRestDstAddressList(),dto.getExistDstAddressList());
        List<String> exitNameForService = new ArrayList<>();
        List<ServiceDTO> restForService = new ArrayList<>();
        covertServiceParam(dto.getServiceName(),dto.getExistServiceNameList(),dto.getRestServiceList() ,restForService ,exitNameForService);
        PolicyObjectDTO service = generateServiceObject(restForService, exitNameForService,dto.getIpType());

        StringBuilder sb = new StringBuilder();

        if(mergeField.equals(PolicyConstants.SRC) && srcAddress.isObjectFlag() && StringUtils.isNotBlank(srcAddress.getCommandLine())) {
            sb.append(String.format("%s\n", srcAddress.getCommandLine()));
        }else if(mergeField.equals(PolicyConstants.DST) && dstAddress.isObjectFlag() && StringUtils.isNotBlank(dstAddress.getCommandLine())) {
            sb.append(String.format("%s\n", dstAddress.getCommandLine()));
        }else if(mergeField.equals(PolicyConstants.SERVICE) && service.isObjectFlag() && StringUtils.isNotBlank(service.getCommandLine())) {
            sb.append(String.format("%s\n", service.getCommandLine()));
        }

        sb.append(String.format("policy modify %s ", ruleId));

        if(mergeField.equals(PolicyConstants.SRC)) {
            sb.append(String.format(" src %s\n", srcAddress.getJoin()));
        }
        if(mergeField.equals(PolicyConstants.DST)) {
            sb.append(String.format("dst %s\n", dstAddress.getJoin()));
        }
        if(mergeField.equals(PolicyConstants.SERVICE) && service != null && StringUtils.isNotBlank(service.getName())) {
            sb.append(String.format("service %s\n", service.getJoin()));
        }

        return sb.toString();
    }

    @Override
    public String generatePostCommandline(CommandlineDTO dto) {
        return "";
    }

    public PolicyObjectDTO generateAddressObject(String ipAddress, String ticket, String existsAddressName,String ipSystem,Integer ipType, List<String> restAddressList, List<String> exitAddressNameList) {
        PolicyObjectDTO policyObjectDTO = new PolicyObjectDTO();

        StringBuilder sb = new StringBuilder();
        if (StringUtils.isNotEmpty(existsAddressName) ) {
            if(CollectionUtils.isNotEmpty(exitAddressNameList)){
                exitAddressNameList.clear();
            }
            policyObjectDTO.setFirstIpNameJoin(existsAddressName);
            exitAddressNameList.add(existsAddressName);
        } else {

            for (String srcAddress : restAddressList) {
                sb.append(generateAddressObject(srcAddress, ticket, exitAddressNameList,ipSystem,ipType));
            }
        }
        policyObjectDTO.setExistAddressNameList(exitAddressNameList);
        StringBuffer join = new StringBuffer();
        if(CollectionUtils.isNotEmpty(exitAddressNameList)){
            for (String name: exitAddressNameList) {
                join.append(name).append(" ");
                if(StringUtils.isBlank(policyObjectDTO.getFirstIpNameJoin())){
                    policyObjectDTO.setFirstIpNameJoin(name);
                }
            }
        }
        if(join.length() > 0){
            join.deleteCharAt(join.lastIndexOf(" "));
        }
        policyObjectDTO.setJoin(join.toString());
        policyObjectDTO.setObjectFlag(true);
        policyObjectDTO.setCommandLine(sb.toString());
        return policyObjectDTO;
    }

    public StringBuilder generateAddressObject(String ipAddress, String ticket, List<String> existSrcAddressList,String ipSystem,Integer ipType) {




        StringBuilder sb = new StringBuilder();

        //连接字符串，如果是多个对象，则用空格区分


        String[] arr = ipAddress.split(",");
        int index = 1;
        for (String address : arr) {
            StringBuilder serialNum = new StringBuilder();
            String name;
            if(StringUtils.isNotEmpty(ipSystem)){
                name = ipSystem;
                if(arr.length > 1){
                    serialNum.append("_").append(index);
                }
                // 对象名称长度限制，一个中文2个字符
                name = strSub(name, getMaxObejctNameLength(),"GB2312");
                // 对象名称长度限制
                int len = 0;
                // 序列号长度
                int serialLength = serialNum.length();
                try{
                    len = name.getBytes("GB2312").length;
                }catch (Exception e) {
                    logger.error("字符串长度计算异常");
                }
                if(len > getMaxObejctNameLength() -(7 + serialLength) ) {
                    name = strSub(name, getMaxObejctNameLength() -(7 + serialLength), "GB2312");
                }
                name = String.format("%s_%s", name, DateUtils.getDate().replace("-","").substring(2));
                name = name + serialNum;
            } else {
                name = String.format("%s_AO_%s", ticket, IdGen.getRandomNumberString());
            }
//            ipv6
            if(ipType!=null&&ipType==1){
                if (IpUtils.isIPv6Range(address)) {
                    String startIp = IpUtils.getRangeStartIPv6(address);
                    String endIp = IpUtils.getRangeEndIPv6(address);
                    sb.append(String.format("range add name %s ip1 %s ip2 %s\n", name, startIp, endIp));
                } else if (IpUtils.isIPv6Subnet(address)) {

                    sb.append(String.format("subnet add name %s ipaddr %s \n", name, address));
                } else {
                    sb.append(String.format("host add name %s ipaddr %s\n", name, address));
                }
            }else{
                if (IpUtils.isIPRange(address)) {
                    String startIp = IpUtils.getStartIpFromRange(address);
                    String endIp = IpUtils.getEndIpFromRange(address);
                    sb.append(String.format("range add name %s ip1 %s ip2 %s\n", name, startIp, endIp));
                } else if (IpUtils.isIPSegment(address)) {
                    String ip = IpUtils.getIpFromIpSegment(address);

                    sb.append(String.format("subnet add name %s ipaddr %s mask %s\n", name, ip, IPUtil.getMaskBitFromIpSegment(address)));
                } else {
                    sb.append(String.format("host add name %s ipaddr %s\n", name, address));
                }}


            existSrcAddressList.add(name);
            index ++;
        }


        return sb;
    }
    public int getMaxObejctNameLength() {
        return MAX_OBJECT_NAME_LENGTH;
    }
    private PolicyObjectDTO generateServiceObject(List<ServiceDTO> serviceDTOList, String existsServiceName,Integer ipType) {
        PolicyObjectDTO dto = new PolicyObjectDTO();
        dto.setObjectFlag(true);
        if(StringUtils.isNotBlank(existsServiceName)){
            dto.setJoin(existsServiceName);
            return dto;
        }

        String name = getServiceName(serviceDTOList,ipType);

        StringBuilder sb = new StringBuilder();
        for(ServiceDTO service : serviceDTOList) {
            int protocolNum = Integer.valueOf(service.getProtocol());
            String protocolString = ProtocolUtils.getProtocolByValue(protocolNum).toLowerCase();
            if (protocolString.equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_ANY)) {
                dto.setJoin(PolicyConstants.POLICY_STR_VALUE_ANY);
                return dto;
            }

            if (protocolString.equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_ICMP)) {
                sb.append(String.format("service add name %s protocol %s", name, service.getProtocol()));
            } else {

                if (PortUtils.isPortRange(service.getDstPorts())) {
                    String start = PortUtils.getStartPort(service.getDstPorts());
                    String end = PortUtils.getEndPort(service.getDstPorts());
                    sb.append(String.format("service add name %s protocol %s ports \'%s-%s\'", name, service.getProtocol(), start, end));
                } else {
                    sb.append(String.format("service add name %s protocol %s ports \'%s\' ", name, service.getProtocol(), service.getDstPorts()));
                }
            }
            sb.append("\n");
        }

        dto.setCommandLine(sb.toString());
        dto.setName(name);
        dto.setJoin(name);

        return dto;
    }

    /**获取服务名称***/
    public String getServiceName(List<ServiceDTO> serviceDTOList,Integer ipType){
        StringBuilder nameSb = new StringBuilder();
        int number = 0;
        for (ServiceDTO dto : serviceDTOList) {
            if (number != 0) {
                nameSb.append("_");
            }
            nameSb.append(getServiceName(dto));
            number++;
        }

        String name = nameSb.toString();
        if(name.length() > getMaxNameLength()) {
            String shortName = name.substring(0, getMaxNameLength()-4);
            name = String.format("%s_etc", shortName.substring(0, shortName.lastIndexOf("_")));
        }
        return name;
    }

    public PolicyObjectDTO generateServiceObject(List<ServiceDTO> serviceDTOList, List<String> existsServiceNameList,Integer ipType) {
        PolicyObjectDTO dto = new PolicyObjectDTO();
        if (serviceDTOList.size() == 0 && existsServiceNameList.size() == 0) {
            dto.setJoin("");
            return dto;
        }

        StringBuilder nameSb = new StringBuilder("");
        if (existsServiceNameList.size() > 0) {
            for (String name : existsServiceNameList) {
                nameSb.append(" ");
                nameSb.append(name);
                if(StringUtils.isBlank(dto.getFirstServiceJoin())){
                    dto.setFirstServiceJoin(name);
                }
            }
        }

        StringBuilder sb = new StringBuilder("");
        for (ServiceDTO service : serviceDTOList) {
            int protocolNum = Integer.valueOf(service.getProtocol());
            String protocolString = ProtocolUtils.getProtocolByValue(protocolNum).toLowerCase();
            if (protocolString.equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_ANY)) {
                dto.setJoin("");
                return dto;
            }

            if(StringUtils.isNotEmpty(service.getDstPorts())){
                String[]  ports = service.getDstPorts().split(",");
                for(String port : ports){
                    String name = getServiceName(service,ipType,port);

                    nameSb.append(" ");
                    nameSb.append(name);
                    if(StringUtils.isBlank(dto.getFirstServiceJoin())){
                        dto.setFirstServiceJoin(name);
                    }
                    if(ipType!=null&&ipType==0) {
                        //ipv4
                        if (!protocolString.equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_ICMP)) {
                            if (!port.equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_ANY)) {
                                if (PortUtils.isPortRange(port)) {
                                    String start = PortUtils.getStartPort(port);
                                    String end = PortUtils.getEndPort(port);
                                    sb.append(String.format("service add name %s protocol %s ports \'%s-%s\' \n", name, service.getProtocol(), start, end));
                                } else {
                                    sb.append(String.format("service add name %s protocol %s ports \'%s\' \n", name, service.getProtocol(), port));
                                }
                            }
                        }
                    }else{
                        if (protocolString.equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_ICMPV6)) {
                            sb.append(String.format("service add name ICMPv6 protocol 58 \n"));
                        } else {
                            if (!port.equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_ANY)) {
                                if (PortUtils.isPortRange(port)) {
                                    String start = PortUtils.getStartPort(port);
                                    String end = PortUtils.getEndPort(port);
                                    sb.append(String.format("service add name %s protocol %s ports \'%s-%s\' \n", name, service.getProtocol(), start, end));
                                } else {
                                    sb.append(String.format("service add name %s protocol %s ports \'%s\' \n", name, service.getProtocol(), port));
                                }
                            }
                        }
                    }
                }
            }
        }

        if (nameSb.length() > 0) {
            nameSb.deleteCharAt(0);
        }

        dto.setCommandLine(sb.toString());
        dto.setName(nameSb.toString());
        dto.setJoin(nameSb.toString());

        return dto;
    }

    public String getServiceName(ServiceDTO service, Integer ipType,String port) {
        StringBuilder nameSb = new StringBuilder();
        nameSb.append(ProtocolUtils.getProtocolByString(service.getProtocol().toLowerCase()));
        int protocolNum = Integer.valueOf(service.getProtocol());
        String protocol=ProtocolUtils.getProtocolByValue(protocolNum);
//        ipv6
        if(ipType!=null&&ipType==1) {
            if (protocol.equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_ICMPV6)) {
                if (port.equals(PolicyConstants.POLICY_STR_VALUE_ANY)) {
                    //自定义服务大写
                    return nameSb.toString().toUpperCase().replace("V", "v");
                }
            }else{
                if (port.equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_ANY)) {
                    //自定义服务大写
                    return nameSb.toString().toUpperCase();
                } else {
                    nameSb.append("_");
                    if (PortUtils.isPortRange(port)) {
                        String start = PortUtils.getStartPort(port);
                        String end = PortUtils.getEndPort(port);
                        nameSb.append(start);
                        nameSb.append("_");
                        nameSb.append(end);
                    } else {
                        nameSb.append(port);
                    }
                }
            }
        }
//        ipv4
        else {
            if (protocol.equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_ICMP)) {
                if (port.equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_ANY)) {
                    //自定义服务大写
                    return nameSb.toString().toUpperCase();
                }
            }
            else {
                if (port.equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_ANY)) {
                    //自定义服务大写
                    return nameSb.toString().toUpperCase();
                } else {
                    nameSb.append("_");
                    if (PortUtils.isPortRange(port)) {
                        String start = PortUtils.getStartPort(port);
                        String end = PortUtils.getEndPort(port);
                        nameSb.append(start);
                        nameSb.append("_");
                        nameSb.append(end);
                    } else {
                        nameSb.append(port);
                    }
                }
            }
        }
        return nameSb.toString();
    }
}
