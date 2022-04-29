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
import com.abtnetworks.totems.common.dto.commandline.device.TopSecFirstPolicyDTO;
import com.abtnetworks.totems.common.enums.*;
import com.abtnetworks.totems.common.ro.ResultRO;
import com.abtnetworks.totems.common.utils.*;
import com.abtnetworks.totems.whale.baseapi.ro.DeviceFilterRuleListRO;
import com.abtnetworks.totems.whale.baseapi.service.WhaleDevicePolicyClient;
import com.alibaba.fastjson.JSON;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * @Author: wenjiachang
 * @Date: 2019/1/29 10:16
 */

@Service
@CustomCli(value = DeviceModelNumberEnum.TOPSEC_TOS_010_020, type = PolicyEnum.SECURITY)
public class SecurityTopsecTos010020ForBeiNongShang extends SecurityPolicyGenerator implements PolicyGenerator {

    private static Logger logger = Logger.getLogger(SecurityTopsecTos010020ForBeiNongShang.class);

    public final int MAX_OBJECT_NAME_LENGTH = 30;

    @Autowired
    private WhaleDevicePolicyClient whaleDevicePolicyClient;

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
        dto.setSwapRuleNameId(settingDTO.getSwapNameId());
        dto.setIdleTimeout(policyDTO.getIdleTimeout());

        dto.setCurrentId(settingDTO.getPolicyId());
        GeneratedObjectDTO generatedDto = cmdDTO.getGeneratedObject();
        generatedDto.setPolicyName(dto.getName());
        dto.setGeneratedDto(generatedDto);
        return composite(dto);
    }

    @Override
    public String generatePreCommandline(CommandlineDTO dto) {
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
        logger.info("TopsecTos010020进行策略命令生成开始=======================================================");
        StringBuilder sb = new StringBuilder();
        String ticket = dto.getName();
        String srcIp = dto.getSrcIp();
        String dstIp = dto.getDstIp();
        String startTime = dto.getStartTime();
        String endTime = dto.getEndTime();
        StringBuffer rollbackShowCmd = new StringBuffer();

        String srcZoneJoin = StringUtils.isNotBlank(dto.getSrcZone()) ? "srcarea '" + dto.getSrcZone() + "' " : "";
        String dstZoneJoin = StringUtils.isNotBlank(dto.getDstZone()) ? "dstarea '" + dto.getDstZone() + "' " : "";

        PolicyObjectDTO srcAddress = generateAddressObject(srcIp, ticket, dto.getSrcAddressName(), dto.getSrcIpSystem(),dto.getIpType());
        PolicyObjectDTO dstAddress = generateAddressObject(dstIp, ticket, dto.getDstAddressName(), dto.getDstIpSystem(),dto.getIpType());
        PolicyObjectDTO service = generateServiceObject(dto.getRestServiceList(), dto.getExistServiceNameList(),dto.getIpType());
        PolicyObjectDTO time = generateTimeObject(startTime, endTime, ticket);

        StringBuilder define = new StringBuilder();

        if (srcAddress.isObjectFlag() && StringUtils.isNotBlank(srcAddress.getCommandLine())) {
            define.append(String.format("%s", srcAddress.getCommandLine()));
        }
        if (dstAddress.isObjectFlag() && StringUtils.isNotBlank(dstAddress.getCommandLine())) {
            define.append(String.format("%s", dstAddress.getCommandLine()));
        }
        if (StringUtils.isNotBlank(service.getCommandLine())) {
            define.append(String.format("%s", service.getCommandLine()));
        }
        if (time != null) {
            define.append(String.format("%s", time.getCommandLine()));
        }

        if (define.length() > 0) {
            sb.append("define\n");
            sb.append(define.toString());
            sb.append("end\n");
        }
        Integer  ipType = dto.getIpType();
        //            ipv6
        if(ipType!=null&&ipType==1){
            sb.append("firewall6\n");
        }else {
            sb.append("firewall\n");
        }

        String srcAddressString = AliStringUtils.isEmpty(srcAddress.getJoin()) == true ? "" : String.format("src \'%s\' ", srcAddress.getJoin());
        String dstAddressString = AliStringUtils.isEmpty(dstAddress.getJoin()) == true ? "" : String.format("dst \'%s\' ", dstAddress.getJoin());
        String serviceString = AliStringUtils.isEmpty(service.getJoin()) == true ? "" : String.format("service \'%s\' ", service.getJoin());


        sb.append(String.format("policy add action %s %s%s" +
                "%s%s%s",  dto.getAction().equalsIgnoreCase("permit") ? "accept" : "deny", srcZoneJoin, dstZoneJoin, srcAddressString, dstAddressString, serviceString));
        rollbackShowCmd.append(srcZoneJoin).append(dstZoneJoin);
        if(StringUtils.isNotBlank(srcAddress.getFirstIpNameJoin())){
            String rollbackSrcIp = AliStringUtils.isEmpty(srcAddress.getJoin()) == true ? "" : String.format("src \'%s\' ", srcAddress.getFirstIpNameJoin());
            rollbackShowCmd.append(rollbackSrcIp);
        }
        if(StringUtils.isNotBlank(dstAddress.getFirstIpNameJoin())){
            String rollbackDstIp = AliStringUtils.isEmpty(dstAddress.getJoin()) == true ? "" : String.format("dst \'%s\' ", dstAddress.getFirstIpNameJoin());
            rollbackShowCmd.append(rollbackDstIp);
        }
        if(StringUtils.isNotBlank(service.getFirstServiceJoin())){
            String rollbackService = AliStringUtils.isEmpty(service.getJoin()) == true ? "" : String.format("service \'%s\' ", service.getFirstServiceJoin());
            rollbackShowCmd.append(rollbackService);
        }
        if(dto.getIdleTimeout()!=null){
            sb.append(String.format("permanent yes session-timeout %d ",dto.getIdleTimeout()));

        }

        if (time != null) {
            sb.append(String.format("schedule %s ", time.getJoin()));
            rollbackShowCmd.append(String.format("schedule \'%s\' ", time.getJoin()));
        }

        if (!AliStringUtils.isEmpty(dto.getGroupName())) {
            sb.append(String.format("group-name %s ", dto.getGroupName()));
            rollbackShowCmd.append(String.format("group-name \'%s\'", dto.getGroupName()));
        }



        String swapRuleNameId = StringUtils.isNotBlank(dto.getSwapRuleNameId()) ? dto.getSwapRuleNameId() : "";
        int moveSeatCode = dto.getMoveSeatEnum().getCode();
        if (moveSeatCode == MoveSeatEnum.BEFORE.getCode()) {
            if (!AliStringUtils.isEmpty(dto.getDescription())) {
                sb.append(String.format("comment %s ", descripTionCode(dto.getDescription())));
            }
            if(!AliStringUtils.isEmpty(swapRuleNameId)) {
                sb.append(String.format("before %s ", swapRuleNameId));
                sb.append("\n");
            }

        } else if (moveSeatCode == MoveSeatEnum.AFTER.getCode()) {
            if (!AliStringUtils.isEmpty(dto.getDescription())) {
                sb.append(String.format("comment %s ", descripTionCode(dto.getDescription())));
            }
            if (!AliStringUtils.isEmpty(swapRuleNameId)) {
                sb.append("\n");
                //            ipv6
                if(ipType!=null&&ipType==1){
                    sb.append("firewall6 ");
                }else {
                    sb.append("firewall ");
                }
                sb.append(String.format("policy move after %s\n", swapRuleNameId));
            }
        } else if(moveSeatCode == MoveSeatEnum.FIRST.getCode()) {
            TopSecFirstPolicyDTO topSecFirstPolicyDTO = getFirstPolicyId(dto.getDeviceUuid(), dto.getRuleListUuid(), dto.getGroupName(),ipType);
            if(StringUtils.isEmpty(dto.getGroupName()) && StringUtils.isNotEmpty(topSecFirstPolicyDTO.getGroupName())){
                sb.append(String.format("group-name %s ", topSecFirstPolicyDTO.getGroupName()));
                rollbackShowCmd.append(String.format("group-name \'%s\'", topSecFirstPolicyDTO.getGroupName()));
            }
            if (!AliStringUtils.isEmpty(dto.getDescription())) {
                sb.append(String.format("comment %s ", descripTionCode(dto.getDescription())));
            }
            String ruleId = topSecFirstPolicyDTO.getRuleId();
            if (!AliStringUtils.isEmpty(ruleId)) {
                sb.append(String.format("before %s ", ruleId));
                sb.append("\n");
            }else{
                sb.append("\n");
            }
        }else{
            sb.append("\n");
        }


        sb.append("\n");
        sb.append("end\n");
        sb.append("ha sync to-peer\n");
        logger.info(String.format("获取生成回滚命令行%s",rollbackShowCmd.toString()));
        dto.getGeneratedDto().setRollbackShowCmd(rollbackShowCmd.toString());
        dto.getGeneratedDto().setIpType(ipType);
        logger.info("TopsecTos010020进行策略命令生成完毕=======================================================");
        return sb.toString();
    }

    public TopSecFirstPolicyDTO getFirstPolicyId(String deviceUuid, String ruleListUuid, String groupName,Integer ipType) {
        ResultRO<List<DeviceFilterRuleListRO>> resultRO = whaleDevicePolicyClient.getFilterRuleList(deviceUuid, ruleListUuid);
        if (resultRO == null) {
            return null;
        }
        logger.debug(String.format("天融信(%s)策略集(%s)相关策略数据为：\n-----------------------------------\n", deviceUuid, ruleListUuid)
                + JSON.toJSONString(resultRO) + "\n-----------------------------------\n");
        List<DeviceFilterRuleListRO> list = resultRO.getData();

        TopSecFirstPolicyDTO topSecFirstPolicyDTO = new TopSecFirstPolicyDTO();
        if (CollectionUtils.isNotEmpty(list)) {
            for (DeviceFilterRuleListRO ruleListRO : list) {
                String ipType1 = ruleListRO.getIpType();
                if (StringUtils.isNotEmpty(groupName)) {
                    //查找策略列表描述中的*group-name:总部上网*，查找group-name为总部上网的所有策略，找到组内第一条策略的ID 12246，直接*before 12246*
                    String description = ruleListRO.getDescription();

                    if (StringUtils.isNotEmpty(description) && description.contains("Group:")) {
                        String name = description.substring(description.indexOf("Group:") + 6, description.indexOf(".")).trim();
                        if (StringUtils.isNotEmpty(name) && name.equals(groupName)) {
                            String ruleId = ruleListRO.getRuleId();

                            //            ipv6
                            if(ipType!=null&&ipType==1){
                                if(StringUtils.isNotBlank(ipType1) && ipType1.equalsIgnoreCase(IPTypeEnum.IP6.name())){
                                    topSecFirstPolicyDTO.setRuleId(ruleId);
                                    topSecFirstPolicyDTO.setGroupName(groupName);
                                    return topSecFirstPolicyDTO;
                                }
                            }else {
                                if(StringUtils.isNotBlank(ipType1) && ipType1.equalsIgnoreCase(IPTypeEnum.IP4.name())){
                                    topSecFirstPolicyDTO.setRuleId(ruleId);
                                    topSecFirstPolicyDTO.setGroupName(groupName);
                                    return topSecFirstPolicyDTO;
                                }
                            }

                        }
                    } else {
                        continue;
                    }
                } else {
                    //查找策略列表第一条策略的ID，直接before到第一条策略
                    logger.info("找到策略:" + ruleListRO.getRuleId());
                    String ruleId = ruleListRO.getRuleId();
                    String description = ruleListRO.getDescription();
                    //            ipv6
                    if(ipType!=null&&ipType==1){
                        if(StringUtils.isNotBlank(ipType1) && ipType1.equalsIgnoreCase(IPTypeEnum.IP6.name())){
                            topSecFirstPolicyDTO.setRuleId(ruleId);

                            if( StringUtils.isNotEmpty(description) && description.contains("Group:")){
                                String name = description.substring(description.indexOf("Group:") + 6, description.indexOf(".")).trim();
                                topSecFirstPolicyDTO.setGroupName(name);
                            }
                            return topSecFirstPolicyDTO;
                        }
                    }else {
                        if(StringUtils.isNotBlank(ipType1) && ipType1.equalsIgnoreCase(IPTypeEnum.IP4.name())){
                            topSecFirstPolicyDTO.setRuleId(ruleId);
                            if( StringUtils.isNotEmpty(description) && description.contains("Group:")){
                                String name = description.substring(description.indexOf("Group:") + 6, description.indexOf(".")).trim();
                                topSecFirstPolicyDTO.setGroupName(name);
                            }
                            return topSecFirstPolicyDTO;
                        }
                    }


                }
            }
        }
        return topSecFirstPolicyDTO;
    }

    /**
     * 工单号_时间戳_随机数4位
     * @param description
     * @return
     */
    private String descripTionCode(String description) {
        StringBuffer applyNum=new StringBuffer();
        applyNum.append(description + "_");
        SimpleDateFormat df = new SimpleDateFormat("yyMMdd");//设置日期格式
        // new Date()为获取当前系统时间
        applyNum.append(df.format(new Date()));
        int random=(int) ((Math.random()*9000)+1000);
        applyNum.append("_");
        applyNum.append(random);
        return applyNum.toString();
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
        PolicyObjectDTO srcAddress = generateAddressObject(dto.getSrcIp(), ticket, dto.getSrcAddressName(), dto.getSrcIpSystem(),dto.getIpType());
        PolicyObjectDTO dstAddress = generateAddressObject(dto.getDstIp(), ticket, dto.getDstAddressName(), dto.getDstIpSystem(),dto.getIpType());
        PolicyObjectDTO service = generateServiceObject(dto.getServiceList(), dto.getServiceName(),dto.getIpType());

        StringBuilder sb = new StringBuilder();

        if (mergeField.equals(PolicyConstants.SRC) && srcAddress.isObjectFlag() && StringUtils.isNotBlank(srcAddress.getCommandLine())) {
            sb.append(String.format("%s\n", srcAddress.getCommandLine()));
        } else if (mergeField.equals(PolicyConstants.DST) && dstAddress.isObjectFlag() && StringUtils.isNotBlank(dstAddress.getCommandLine())) {
            sb.append(String.format("%s\n", dstAddress.getCommandLine()));
        } else if (mergeField.equals(PolicyConstants.SERVICE) && service.isObjectFlag() && StringUtils.isNotBlank(service.getCommandLine())) {
            sb.append(String.format("%s\n", service.getCommandLine()));
        }

        sb.append(String.format("policy modify %s ", ruleId));

        if (mergeField.equals(PolicyConstants.SRC)) {
            sb.append(String.format(" src %s\n", srcAddress.getJoin()));
        }
        if (mergeField.equals(PolicyConstants.DST)) {
            sb.append(String.format("dst %s\n", dstAddress.getJoin()));
        }
        if (mergeField.equals(PolicyConstants.SERVICE) && service != null && StringUtils.isNotBlank(service.getName())) {
            sb.append(String.format("service %s\n", service.getJoin()));
        }

        return sb.toString();
    }

    @Override
    public String generatePostCommandline(CommandlineDTO dto) {
        return "";
    }

    public PolicyObjectDTO generateAddressObject(String ipAddress, String ticket, String existsAddressName, String ipSystem, Integer ipType) {
        PolicyObjectDTO dto = new PolicyObjectDTO();
        dto.setObjectFlag(true);
        if (StringUtils.isNotBlank(existsAddressName)) {
            dto.setJoin(existsAddressName);
            dto.setFirstIpNameJoin(existsAddressName);
            return dto;
        }

        if (AliStringUtils.isEmpty(ipAddress) && AliStringUtils.isEmpty(existsAddressName)) {
            dto.setJoin("");
            return dto;
        }
        StringBuilder sb = new StringBuilder();
        //连接字符串，如果是多个对象，则用空格区分
        StringBuilder joinStr = new StringBuilder();

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
                    sb.append(String.format("ipv6_range add name %s ip1 %s ip2 %s\n", name, startIp, endIp));
                } else if (IpUtils.isIPv6Subnet(address)) {
                    String ipSegmentStartIPv6 = IP6Utils.getIpSegmentStartIPv6(address);
                    String maskIPv6 = IP6Utils.getIpSegmentMaskIPv6(address);
                    sb.append(String.format("ipv6_subnet add name %s ipaddr %s prefix-len %s\n", name, ipSegmentStartIPv6,maskIPv6));
                } else {
                    sb.append(String.format("ipv6_host add name %s ipaddr %s\n", name, address));
                }
            }else{
                if (IpUtils.isIPRange(address)) {
                    String startIp = IpUtils.getStartIpFromRange(address);
                    String endIp = IpUtils.getEndIpFromRange(address);
                    name = String.format("%s-%s", startIp,endIp);
                    sb.append(String.format("range add name range%s ip1 %s ip2 %s\n", name, startIp, endIp));
                } else if (IpUtils.isIPSegment(address)) {
                    String ip = IpUtils.getIpFromIpSegment(address);
                    String maskBit = IpUtils.getMaskBitFromIpSegment(address);
                    name = String.format("%s/%s", ip,maskBit);
                    sb.append(String.format("subnet add name %s ipaddr %s mask %s\n", name, ip, maskBit));
                } else {
                    name = String.format("%s/32", address);
                    sb.append(String.format("host add name %s ipaddr %s\n", name, address));
                }}

            if (StringUtils.isBlank(dto.getFirstIpNameJoin())) {
                dto.setFirstIpNameJoin(name);
            }

            joinStr.append(name + " ");
            index ++;
        }
        String join = joinStr.toString().trim();
        dto.setName(join);
        dto.setJoin(join);
        dto.setCommandLine(sb.toString());
        return dto;
    }

    public int getMaxObejctNameLength() {
        return MAX_OBJECT_NAME_LENGTH;
    }

    private PolicyObjectDTO generateServiceObject(List<ServiceDTO> serviceDTOList, String existsServiceName,Integer ipType) {
        PolicyObjectDTO dto = new PolicyObjectDTO();
        dto.setObjectFlag(true);
        if (StringUtils.isNotBlank(existsServiceName)) {
            dto.setJoin(existsServiceName);
            return dto;
        }
        StringBuilder nameSb = new StringBuilder();
        StringBuilder sb = new StringBuilder();
        sb.append("define\n");
        for (ServiceDTO service : serviceDTOList) {
            int protocolNum = Integer.valueOf(service.getProtocol());
            String protocolString = ProtocolUtils.getProtocolByValue(protocolNum).toLowerCase();
            if (protocolString.equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_ANY)) {
                dto.setJoin("");
                return dto;
            }
            if (protocolString.equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_ICMPV6)) {
                nameSb.append(" ");
                nameSb.append("ICMPv6");
                if(StringUtils.isBlank(dto.getFirstServiceJoin())){
                    dto.setFirstServiceJoin("ICMPv6");
                }
            } else {
                if (StringUtils.isNotEmpty(service.getDstPorts())) {
                    String[] ports = service.getDstPorts().split(",");
                    for (String port : ports) {
                        String name = getServiceName(service, ipType, port);
                        nameSb.append(" ");
                        nameSb.append(name);
                        if(StringUtils.isBlank(dto.getFirstServiceJoin())){
                            dto.setFirstServiceJoin(name);
                        }
                        if (ipType != null && ipType == 0) {
                            //ipv4
                            if (!service.getDstPorts().equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_ANY)) {
                                if (PortUtils.isPortRange(port)) {
                                    String start = PortUtils.getStartPort(port);
                                    String end = PortUtils.getEndPort(port);
                                    sb.append(String.format("service add name %s protocol %s port %s port2 %s \n", name, service.getProtocol(), start, end));
                                } else {
                                    sb.append(String.format("service add name %s protocol %s port %s \n", name, service.getProtocol(), port));
                                }
                            }

                        } else {
                            if (!port.equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_ANY)) {
                                if (PortUtils.isPortRange(port)) {
                                    String start = PortUtils.getStartPort(port);
                                    String end = PortUtils.getEndPort(port);
                                    sb.append(String.format("service add name %s protocol %s port %s port2 %s \n", name, service.getProtocol(), start, end));
                                } else {
                                    sb.append(String.format("service add name %s protocol %s port %s \n", name, service.getProtocol(), port));
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
        sb.append("end\n");
        dto.setCommandLine(sb.toString());
        dto.setName(nameSb.toString());
        dto.setJoin(nameSb.toString());

        return dto;
    }

    public PolicyObjectDTO generateServiceObject(List<ServiceDTO> serviceDTOList, List<String> existsServiceNameList, Integer ipType) {
        PolicyObjectDTO dto = new PolicyObjectDTO();
        if (serviceDTOList.size() == 0 && existsServiceNameList.size() == 0) {
            dto.setJoin("");
            return dto;
        }

        StringBuilder nameSb = new StringBuilder();
        if (existsServiceNameList.size() > 0) {
            for (String name : existsServiceNameList) {
                nameSb.append(" ");
                nameSb.append(name);
                if(StringUtils.isBlank(dto.getFirstServiceJoin())){
                    dto.setFirstServiceJoin(name);
                }
            }
        }

        StringBuilder sb = new StringBuilder();
        for (ServiceDTO service : serviceDTOList) {
            int protocolNum = Integer.valueOf(service.getProtocol());
            String protocolString = ProtocolUtils.getProtocolByValue(protocolNum).toLowerCase();
            if (protocolString.equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_ANY)) {
                dto.setJoin("");
                return dto;
            }
            if (protocolString.equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_ICMPV6)) {
                nameSb.append(" ");
                nameSb.append("ICMPv6");
                if(StringUtils.isBlank(dto.getFirstServiceJoin())){
                    dto.setFirstServiceJoin("ICMPv6");
                }
            } else {
                if (StringUtils.isNotEmpty(service.getDstPorts())) {
                    String[] ports = service.getDstPorts().split(",");
                    for (String port : ports) {
                        String name = getServiceName(service, ipType, port);
                        nameSb.append(" ");
                        nameSb.append(name);
                        if(StringUtils.isBlank(dto.getFirstServiceJoin())){
                            dto.setFirstServiceJoin(name);
                        }
                        if (ipType != null && ipType == 0) {
                            //ipv4
                            if (!service.getDstPorts().equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_ANY)) {
                                if (PortUtils.isPortRange(port)) {
                                    String start = PortUtils.getStartPort(port);
                                    String end = PortUtils.getEndPort(port);
                                    sb.append(String.format("service add name %s protocol %s port %s port2 %s \n", name, service.getProtocol(), start, end));
                                } else {
                                    sb.append(String.format("service add name %s protocol %s port %s \n", name, service.getProtocol(), port));
                                }
                            }

                        } else {
                            if (!port.equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_ANY)) {
                                if (PortUtils.isPortRange(port)) {
                                    String start = PortUtils.getStartPort(port);
                                    String end = PortUtils.getEndPort(port);
                                    sb.append(String.format("service add name %s protocol %s port %s port2 %s \n", name, service.getProtocol(), start, end));
                                } else {
                                    sb.append(String.format("service add name %s protocol %s port %s \n", name, service.getProtocol(), port));
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

    public PolicyObjectDTO generateTimeObject(String startTime, String endTime, String ticket) {
        if (startTime == null) {
            return null;
        }
        PolicyObjectDTO object = new PolicyObjectDTO();
        String name = String.format("%s_TR_%s", ticket, IdGen.getRandomNumberString());
        StringBuilder sb = new StringBuilder();

        sb.append(String.format("schedule add name %s cyctype yearcyc sdate %s stime %s " +
                        " edate %s etime %s\n", name, getDate(startTime), getTime(startTime),
                getDate(endTime), getTime(endTime)));

        object.setName(name);
        object.setJoin(name);
        object.setCommandLine(sb.toString());
        return object;
    }

    private String getRandomString() {
        return IdGen.randomBase62(PolicyConstants.POLICY_INT_RAMDOM_ID_LENGTH);
    }

    private String formatTimeString(String timeString) {
        return TimeUtils.transformDateFormat(timeString, TimeUtils.EUROPEAN_TIME_FORMAT, TimeUtils.HUAWEI_TIME_FORMAT);
    }

    private String getDate(String timeString) {
        SimpleDateFormat sdf = new SimpleDateFormat(TimeUtils.EUROPEAN_TIME_FORMAT);
        Date date = new Date();
        try {
            date = sdf.parse(timeString);
        } catch (Exception e) {
            e.printStackTrace();
        }
        SimpleDateFormat dst = new SimpleDateFormat("yyyy-MM-dd");
        return dst.format(date);
    }

    private String getTime(String timeString) {
        SimpleDateFormat sdf = new SimpleDateFormat(TimeUtils.EUROPEAN_TIME_FORMAT);
        Date date = new Date();
        try {
            date = sdf.parse(timeString);
        } catch (Exception e) {
            e.printStackTrace();
        }
        SimpleDateFormat dst = new SimpleDateFormat("hh:mm:ss");
        return dst.format(date);
    }

    public String getServiceName(ServiceDTO service, Integer ipType, String port) {
        StringBuilder nameSb = new StringBuilder();
        nameSb.append(ProtocolUtils.getProtocolByString(service.getProtocol().toLowerCase()));
        int protocolNum = Integer.valueOf(service.getProtocol());
        String protocol= ProtocolUtils.getProtocolByValue(protocolNum);
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
                    if (PortUtils.isPortRange(port)) {
                        String start = PortUtils.getStartPort(port);
                        String end = PortUtils.getEndPort(port);
                        nameSb.append(start);
                        nameSb.append("-");
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
                    if (PortUtils.isPortRange(port)) {
                        String start = PortUtils.getStartPort(port);
                        String end = PortUtils.getEndPort(port);
                        nameSb.append(start);
                        nameSb.append("-");
                        nameSb.append(end);
                    } else {
                        nameSb.append(port);
                    }
                }
            }
        }
        return nameSb.toString();
    }

    /**获取服务名称***/
    public String getServiceName(List<ServiceDTO> serviceDTOList){
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


    public static void main(String[] args) {
        CommandlineDTO commandlineDTO = new CommandlineDTO();
        commandlineDTO.setName("A20190");
        commandlineDTO.setSrcIp("2.2.2.2");
        commandlineDTO.setDstIp("1.1.1.1");
        commandlineDTO.setDstZone("trust");
        commandlineDTO.setSrcZone("trust");
        commandlineDTO.setDescription("aaaaa");

        ServiceDTO serviceDTO1 = new ServiceDTO();
        serviceDTO1.setProtocol("6");
        serviceDTO1.setDstPorts("2132,2022,20-39");

//        ServiceDTO serviceDTO1 = new ServiceDTO();
//        serviceDTO1.setProtocol("1");

        ServiceDTO serviceDTO2 = new ServiceDTO();
        serviceDTO2.setProtocol("6");
        serviceDTO2.setDstPorts("20-40");

        ServiceDTO serviceDTO4 = new ServiceDTO();
        serviceDTO4.setProtocol("17");
        serviceDTO4.setDstPorts("45-80,8765");

        ServiceDTO serviceDTO3 = new ServiceDTO();
        serviceDTO3.setProtocol("17");
        serviceDTO3.setDstPorts("6768");
        commandlineDTO.setRestServiceList(Arrays.asList(serviceDTO4));
        commandlineDTO.setAction("deny");
        commandlineDTO.setMoveSeatEnum(MoveSeatEnum.AFTER);

        SecurityTopsecTos010020ForBeiNongShang topsec = new SecurityTopsecTos010020ForBeiNongShang();

        String commandLine = topsec.composite(commandlineDTO);
        System.out.println("commandline:\n" + commandLine);

    }



}
