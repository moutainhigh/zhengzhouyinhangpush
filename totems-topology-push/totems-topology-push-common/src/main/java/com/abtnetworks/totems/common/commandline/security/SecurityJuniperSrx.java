package com.abtnetworks.totems.common.commandline.security;

import com.abtnetworks.totems.command.line.dto.IpAddressSubnetIntDTO;
import com.abtnetworks.totems.command.line.enums.StatusTypeEnum;
import com.abtnetworks.totems.common.commandline.PolicyGenerator;
import com.abtnetworks.totems.common.commandline.SecurityPolicyGenerator;
import com.abtnetworks.totems.common.constants.PolicyConstants;
import com.abtnetworks.totems.common.enums.ActionEnum;
import com.abtnetworks.totems.common.enums.MoveSeatEnum;
import com.abtnetworks.totems.common.dto.*;
import com.abtnetworks.totems.common.dto.commandline.CommandlineDTO;
import com.abtnetworks.totems.common.dto.commandline.PolicyMergeDTO;
import com.abtnetworks.totems.common.dto.commandline.PolicyObjectDTO;
import com.abtnetworks.totems.common.dto.commandline.ServiceDTO;
import com.abtnetworks.totems.common.utils.*;
import com.abtnetworks.totems.vender.Juniper.security.SecurityJuniperSRXImpl;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Author: wenjiachang
 * @Date: 2019/1/29 9:36
 */
@Service
public class SecurityJuniperSrx extends SecurityPolicyGenerator implements PolicyGenerator {

    private static Logger logger = LoggerFactory.getLogger(SecurityJuniperSrx.class);

    private final int MAX_NAME_LENGTH = 63;

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
        dto.setAddressType(settingDTO.getAddressType());

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

        GeneratedObjectDTO generatedObjectDTO = cmdDTO.getGeneratedObject();
        generatedObjectDTO.setPolicyName(taskDTO.getTheme());
        String commandLine = composite(dto);
        generatedObjectDTO.setAddressObjectNameList(dto.getAddressObjectNameList());
        generatedObjectDTO.setAddressObjectGroupNameList(dto.getAddressObjectGroupNameList());
        generatedObjectDTO.setServiceObjectNameList(dto.getServiceObjectNameList());
        generatedObjectDTO.setServiceObjectGroupNameList(dto.getServiceObjectGroupNameList());
        generatedObjectDTO.setTimeObjectNameList(dto.getTimeObjectNameList());
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
        String srcZone = StringUtils.isNotBlank(dto.getSrcZone()) ? dto.getSrcZone() : "";
        String dstZone = StringUtils.isNotBlank(dto.getDstZone()) ? dto.getDstZone() : "";
        List<String> existServiceNameList = dto.getExistServiceNameList();
        List<ServiceDTO> restServiceList = dto.getRestServiceList();
        boolean createObjFlag = dto.isCreateObjFlag();
        boolean addressType = dto.getAddressType();

        String name = String.format("%s", ticket);

        PolicyObjectDTO srcAddress = generateAddressObject(srcIp, srcZone, ticket, dto.getSrcAddressName(), dto.getSrcIpSystem(),addressType);
        PolicyObjectDTO dstAddress = generateAddressObject(dstIp, dstZone, ticket, dto.getDstAddressName(), dto.getDstIpSystem(),addressType);
        PolicyObjectDTO service = generateServiceObject(dto.getServiceList(), createObjFlag, dto.getServiceName(),existServiceNameList,restServiceList);
        PolicyObjectDTO time = generateTimeObject(startTime, endTime, ticket);
        // 地址和服务对象不能共用公共方法，原因是有多个参数需要拼接
        recordObjectName(dto, srcAddress, dstAddress, service);
        // 记录时间对象
        recordCreateTimeObjectName(dto,time);

        sb.append("configure\n");
        if(srcAddress.isObjectFlag() && StringUtils.isNotBlank(srcAddress.getCommandLine())) {
            sb.append(String.format("%s\n", srcAddress.getCommandLine()));
        }
        if(dstAddress.isObjectFlag() && StringUtils.isNotBlank(dstAddress.getCommandLine())) {
            sb.append(String.format("%s\n", dstAddress.getCommandLine()));
        }
        if (service.isObjectFlag() && StringUtils.isNotBlank(service.getCommandLine())) {
            if(dto.getIdleTimeout()!=null) {
                String serviceCommandLine = service.getCommandLine();
                if (service.getCommandLine().substring(service.getCommandLine().length() - 2, service.getCommandLine().length()).contains("\n")) {
                    serviceCommandLine = service.getCommandLine().substring(0, service.getCommandLine().length() - 2);
                }
                sb.append(String.format("%s inactivity-timeout %d \n", serviceCommandLine, dto.getIdleTimeout()));
            }
            else { sb.append(String.format("%s  \n", service.getCommandLine()));
            }

        }
        if (time != null) {
            sb.append(String.format("%s\n", time.getCommandLine()));
        }

        //域拼接
        String srcZoneJoin = "";
        String dstZoneJoin = "";
        srcZoneJoin = String.format("from-zone %s", AliStringUtils.isEmpty(srcZone)?"any":srcZone);
        dstZoneJoin = String.format("to-zone %s", AliStringUtils.isEmpty(dstZone)?"any":dstZone);

        if(!AliStringUtils.isEmpty(dto.getDescription())){
            sb.append(String.format("set security policies %s %s policy %s description %s \n", srcZoneJoin, dstZoneJoin, name, dto.getDescription()));
        }
        sb.append(String.format("set security policies %s %s policy %s match source-address %s \n", srcZoneJoin, dstZoneJoin, name, srcAddress.getJoin()));
        sb.append(String.format("set security policies %s %s policy %s match destination-address %s \n", srcZoneJoin, dstZoneJoin, name, dstAddress.getJoin()));
        sb.append(String.format("set security policies %s %s policy %s match application %s \n", srcZoneJoin, dstZoneJoin, name, service.getJoin()));

        sb.append(String.format("set security policies %s %s policy %s then %s \n", srcZoneJoin, dstZoneJoin, name, dto.getAction().toLowerCase()));
        if( time != null) {
            sb.append(String.format("set security policies %s %s policy %s scheduler-name %s \n", srcZoneJoin, dstZoneJoin, name, time.getName()));
        }

        //没有策略则不添加移动语句
        String swapRuleNameId = StringUtils.isNotBlank(dto.getSwapRuleNameId()) ? dto.getSwapRuleNameId() : "";
        int moveSeatCode = dto.getMoveSeatEnum().getCode();
        if (moveSeatCode == MoveSeatEnum.FIRST.getCode() && StringUtils.isNotBlank(swapRuleNameId)) {
            sb.append(String.format("insert security policies %s %s policy %s before policy %s\n", srcZoneJoin, dstZoneJoin, name, swapRuleNameId));
        } else if (moveSeatCode == MoveSeatEnum.BEFORE.getCode() || moveSeatCode == MoveSeatEnum.AFTER.getCode()) {
            sb.append(String.format("insert security policies %s %s policy %s %s policy %s\n", srcZoneJoin, dstZoneJoin, name, dto.getMoveSeatEnum().getKey(), swapRuleNameId));
        }

        sb.append("commit\n");
        sb.append("exit\n");
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
        sb.append("cli\n");
        sb.append("configure\n");
        boolean createObjFlag = dto.isCreateObjFlag();
        boolean addressType = dto.getAddressType();
        List<String> existServiceNameList = dto.getExistServiceNameList();
        List<ServiceDTO> restServiceList = dto.getRestServiceList();
        String srcZone = StringUtils.isNotBlank(dto.getSrcZone()) ? dto.getSrcZone() : "";
        String dstZone = StringUtils.isNotBlank(dto.getDstZone()) ? dto.getDstZone() : "";
        PolicyObjectDTO srcAddress = generateAddressObject(dto.getSrcIp(), srcZone, dto.getName(), dto.getSrcAddressName(), dto.getSrcIpSystem(),addressType);
        PolicyObjectDTO dstAddress = generateAddressObject(dto.getDstIp(), dstZone, dto.getName(), dto.getDstAddressName(), dto.getDstIpSystem(),addressType);
        PolicyObjectDTO service = generateServiceObject(dto.getServiceList(), createObjFlag, dto.getServiceName(),existServiceNameList,restServiceList);

        // 地址和服务对象不能共用公共方法，原因是有多个参数需要拼接
        recordObjectName(dto, srcAddress, dstAddress, service);

        if (mergeField.equals(PolicyConstants.SRC) && srcAddress.isObjectFlag() && StringUtils.isNotBlank(srcAddress.getCommandLine())) {
            sb.append(srcAddress.getCommandLine());
        }
        if (mergeField.equals(PolicyConstants.DST) && dstAddress.isObjectFlag() && StringUtils.isNotBlank(dstAddress.getCommandLine())) {
            sb.append(dstAddress.getCommandLine());
        }
        if (mergeField.equals(PolicyConstants.SERVICE) && service.isObjectFlag() && StringUtils.isNotBlank(service.getCommandLine())) {
            sb.append(service.getCommandLine());
        }

        sb.append("set security policies ");
        if (StringUtils.isNotBlank(dto.getSrcZone())) {
            sb.append(String.format("from-zone %s ", dto.getSrcZone()));
        }
        if (StringUtils.isNotBlank(dto.getDstZone())) {
            sb.append(String.format("to-zone %s ", dto.getDstZone()));
        }
        sb.append(String.format("policy %s match ", ruleName));
        if (mergeField.equals(PolicyConstants.SRC)) {
            sb.append(String.format("source-address %s\n", srcAddress.getJoin()));
        } else if (mergeField.equals(PolicyConstants.DST)) {
            sb.append(String.format("destination-address %s\n", dstAddress.getJoin()));
        } else if (mergeField.equals(PolicyConstants.SERVICE) && StringUtils.isNotBlank(service.getName())) {
            sb.append(String.format("application %s\n", service.getJoin()));
        }
        sb.append("commit\n");

        return sb.toString();
    }


    @Override
    public String generatePostCommandline(CommandlineDTO dto) {
        return "";
    }

    private PolicyObjectDTO generateAddressObject(String ipAddress, String zone, String ticket, String existsAddressName, String ipSystem,boolean addressType) {
        PolicyObjectDTO dto = new PolicyObjectDTO();
        SecurityJuniperSRXImpl securityJuniperSRX = new SecurityJuniperSRXImpl();
        try {
        dto.setObjectFlag(true);

        if(AliStringUtils.isEmpty(ipAddress)) {
            dto.setJoin("any");
            return dto;
        }

        if(AliStringUtils.isEmpty(zone)) {
            zone = "any";
        }

        if(StringUtils.isNotBlank(existsAddressName)) {
            String prefix = zone + "_zone_";
            if(existsAddressName.startsWith(prefix)) {
                String name = existsAddressName.replaceFirst(prefix, "");
                dto.setJoin(name);
                return dto;
            }else{
                dto.setJoin(existsAddressName);
                return dto;
            }
        }
        StringBuilder sb = new StringBuilder();

        String[] arr = ipAddress.split(",");

        String groupName = String.format("%s_AG_%s", ticket, IdGen.getRandomNumberString());
        String name = "";
        int index = 0;
        int indexIpv6 = 0;
        String joinRollbackIp = "";
        if(addressType){
            for (String address : arr) {
                StringBuilder serialNum = new StringBuilder();
                if(StringUtils.isNotEmpty(ipSystem)){
                    if(arr.length == 1){
                        name = ipSystem;
                        groupName = ipSystem;
                        if(arr.length > 1){
                            serialNum.append("_").append(index +1);
                        }
                        // 对象名称长度限制，一个中文2个字符
                        name = strSub(name, getMaxNameLength(),"GB2312");
                        groupName = strSub(groupName, getMaxNameLength(),"GB2312");
                        // 对象名称长度限制
                        int len = 0;
                        int groupLen = 0;
                        try{
                            len = name.getBytes("GB2312").length;
                            groupLen = groupName.getBytes("GB2312").length;
                        }catch (Exception e) {
                            logger.error("字符串长度计算异常");
                        }
                        // 序列号长度
                        int serialLengh = serialNum.length();
                        if(len > getMaxNameLength() - (7 + serialLengh) ) {
                            name = strSub(name, getMaxNameLength() - (7 + serialLengh), "GB2312");
                        }
                        if(groupLen > getMaxNameLength() - 7) {
                            groupName = strSub(groupName, getMaxNameLength() - 7, "GB2312");
                        }
                        name = String.format("%s_%s", name, DateUtils.getDate().replace("-","").substring(2));
                        name =  name + serialNum.toString();
                    } else {
                        name  = ipSystem + "_" + address;
                    }
                } else {
                    name = String.format("%s_AO_%s", ticket, IdGen.getRandomNumberString());
                }
                joinRollbackIp = address;
                if(IPUtil.isSubnetMask(address) || IP6Utils.isIPv6Subnet(address)) {
                    //判断是 子网
                    String[] array = StringUtils.split(address, "/");
                    if(IP6Utils.isIPv6Subnet(address)){
                        // set security zones security-zone untrust address-book address test12_AO_6085 240E:360:4A02:32EE:948B:CFF5:CD04:7AFB/100
                        IpAddressSubnetIntDTO ipAddressSubnetIntDTO = new IpAddressSubnetIntDTO();
                        ipAddressSubnetIntDTO.setIp(array[0]);
                        ipAddressSubnetIntDTO.setMask(Integer.parseInt(array[1]));
                        Map<String,Object> map = new HashMap<>();
                        map.put("subnetIntAddressObjectName",name);
                        sb.append(securityJuniperSRX.generateSubnetIntIpV6CommandLine(StatusTypeEnum.ADD,new IpAddressSubnetIntDTO[]{ipAddressSubnetIntDTO},null,map,new String[]{zone}));
                    }else {
                        String addressObjectString = String.format("set security zones security-zone %s address-book address %s %s \n", zone , name, address);
                        sb.append(addressObjectString);
                    }
                } else if (IPUtil.isIPRange(address) || IPUtil.isIPv6Range(address)) {
                    //判断是范围
                    if (IPUtil.isIPv6Range(address)){
                        List<String> toSubnetList = IP6Utils.convertRangeToSubnet(address);
                        for (String subIpv6 : toSubnetList) {
                            if(StringUtils.isNotEmpty(ipSystem)){
                                if(toSubnetList.size() == 1){
                                    name = ipSystem;
                                    groupName = ipSystem;
                                    if(arr.length > 1){
                                        serialNum.append("_").append(indexIpv6 +1);
                                    }
                                    // 对象名称长度限制，一个中文2个字符
                                    name = strSub(name, getMaxNameLength(),"GB2312");
                                    groupName = strSub(groupName, getMaxNameLength(),"GB2312");
                                    // 对象名称长度限制
                                    int len = 0;
                                    int groupLen = 0;
                                    try{
                                        len = name.getBytes("GB2312").length;
                                        groupLen = groupName.getBytes("GB2312").length;
                                    }catch (Exception e) {
                                        logger.error("字符串长度计算异常");
                                    }
                                    // 序列号长度
                                    int serialLengh = serialNum.length();
                                    if(len > getMaxNameLength() - (7 + serialLengh) ) {
                                        name = strSub(name, getMaxNameLength() - (7 + serialLengh), "GB2312");
                                    }
                                    if(groupLen > getMaxNameLength() - 7) {
                                        groupName = strSub(groupName, getMaxNameLength() - 7, "GB2312");
                                    }
                                    name = String.format("%s_%s", name, DateUtils.getDate().replace("-","").substring(2));
                                    name =  name + serialNum.toString();
                                } else {
                                    name  = ipSystem + "_" + address;
                                }
                            } else {
                                name = String.format("%s_AO_%s", ticket, IdGen.getRandomNumberString());
                            }
                            sb.append(String.format("set security zones security-zone %s address-book address %s %s\n",zone,name,subIpv6));
                            if(toSubnetList.size() > 1) {
                                sb.append(String.format("set security zones security-zone %s address-book address-set %s address %s \n", zone, groupName, name));
                            }
                            indexIpv6++;
                        }
                    }else {
                        String startIp = IpUtils.getStartIpFromRange(address);
                        String endIp = IpUtils.getEndIpFromRange(address);
                        String addressObjectString = String.format("set security zones security-zone %s address-book address %s range-address %s to %s\n", zone, name, startIp, endIp);
                        sb.append(addressObjectString);
                    }
                } else if(IPUtil.isIP(address) || IPUtil.isIPv6(address)){
                    //判断是单IP
                    if(IPUtil.isIPv6(address)){
                        String addressObjectString = String.format("set security zones security-zone %s address-book address %s %s/128 \n", zone , name, address);
                        sb.append(addressObjectString);
                        // 单ip新建对象的时候拼子网掩码128,回滚的时候应该也带上
                        joinRollbackIp = address + "/128";
                    }else {
                        String addressObjectString = String.format("set security zones security-zone %s address-book address %s %s/32 \n", zone , name, address);
                        sb.append(addressObjectString);
                        // 单ip新建对象的时候拼子网掩码32,回滚的时候应该也带上
                        joinRollbackIp = address + "/32";
                    }

                }else {
                    //域名
                    Map<String,Object> map = new HashMap<>();
                    map.put("zone",zone);
                    sb.append(securityJuniperSRX.generateHostCommandLine(StatusTypeEnum.ADD,new String[]{address},map,new String[]{name}));
                }
                if(arr.length > 1) {
                    sb.append(String.format("set security zones security-zone %s address-book address-set %s address %s \n", zone, groupName, name));
                }
                index++;
            }
        }else {
            for (String address : arr) {
                StringBuilder serialNum = new StringBuilder();
                if(StringUtils.isNotEmpty(ipSystem)){
                    if(arr.length == 1){
                        name = ipSystem;
                        groupName = ipSystem;
                        if(arr.length > 1){
                            serialNum.append("_").append(index +1);
                        }
                        // 对象名称长度限制，一个中文2个字符
                        name = strSub(name, getMaxNameLength(),"GB2312");
                        groupName = strSub(groupName, getMaxNameLength(),"GB2312");
                        // 对象名称长度限制
                        int len = 0;
                        int groupLen = 0;
                        try{
                            len = name.getBytes("GB2312").length;
                            groupLen = groupName.getBytes("GB2312").length;
                        }catch (Exception e) {
                            logger.error("字符串长度计算异常");
                        }
                        // 序列号长度
                        int serialLengh = serialNum.length();
                        if(len > getMaxNameLength() - (7 + serialLengh) ) {
                            name = strSub(name, getMaxNameLength() - (7 + serialLengh), "GB2312");
                        }
                        if(groupLen > getMaxNameLength() - 7) {
                            groupName = strSub(groupName, getMaxNameLength() - 7, "GB2312");
                        }
                        name = String.format("%s_%s", name, DateUtils.getDate().replace("-","").substring(2));
                        name =  name + serialNum.toString();
                    } else {
                        name  = ipSystem + "_" + address;
                    }
                } else {
                    name = String.format("%s_AO_%s", ticket, IdGen.getRandomNumberString());
                }
                joinRollbackIp = address;
                if(IPUtil.isSubnetMask(address) || IP6Utils.isIPv6Subnet(address)) {
                    //判断是 子网
                    if(IP6Utils.isIPv6Subnet(address)){
                        // set security zones security-zone untrust address-book address test12_AO_6085 240E:360:4A02:32EE:948B:CFF5:CD04:7AFB/100
                        String addressObjectString = String.format("set security address-book global address %s %s \n", name , address);
                        sb.append(addressObjectString);
                    }else {
                        String addressObjectString = String.format("set security address-book global address %s %s \n", name , address);
                        sb.append(addressObjectString);
                    }
                } else if (IPUtil.isIPRange(address) || IPUtil.isIPv6Range(address)) {
                    //判断是范围
                    if (IPUtil.isIPv6Range(address)){
                        List<String> toSubnetList = IP6Utils.convertRangeToSubnet(address);
                        for (String subIpv6 : toSubnetList) {
                            if(StringUtils.isNotEmpty(ipSystem)){
                                if(toSubnetList.size() == 1){
                                    name = ipSystem;
                                    groupName = ipSystem;
                                    if(arr.length > 1){
                                        serialNum.append("_").append(indexIpv6 +1);
                                    }
                                    // 对象名称长度限制，一个中文2个字符
                                    name = strSub(name, getMaxNameLength(),"GB2312");
                                    groupName = strSub(groupName, getMaxNameLength(),"GB2312");
                                    // 对象名称长度限制
                                    int len = 0;
                                    int groupLen = 0;
                                    try{
                                        len = name.getBytes("GB2312").length;
                                        groupLen = groupName.getBytes("GB2312").length;
                                    }catch (Exception e) {
                                        logger.error("字符串长度计算异常");
                                    }
                                    // 序列号长度
                                    int serialLengh = serialNum.length();
                                    if(len > getMaxNameLength() - (7 + serialLengh) ) {
                                        name = strSub(name, getMaxNameLength() - (7 + serialLengh), "GB2312");
                                    }
                                    if(groupLen > getMaxNameLength() - 7) {
                                        groupName = strSub(groupName, getMaxNameLength() - 7, "GB2312");
                                    }
                                    name = String.format("%s_%s", name, DateUtils.getDate().replace("-","").substring(2));
                                    name =  name + serialNum.toString();
                                } else {
                                    name  = ipSystem + "_" + address;
                                }
                            } else {
                                name = String.format("%s_AO_%s", ticket, IdGen.getRandomNumberString());
                            }
                            sb.append(String.format("set security address-book global address %s %s\n",name,subIpv6));
                            if(toSubnetList.size() > 1) {
                                sb.append(String.format("set security address-book global address-set %s address %s \n", groupName, name));
                            }
                            indexIpv6++;
                        }
                    }else {
                        String startIp = IpUtils.getStartIpFromRange(address);
                        String endIp = IpUtils.getEndIpFromRange(address);
                        String addressObjectString = String.format("set security address-book global address %s range-address %s to %s\n", name, startIp, endIp);
                        sb.append(addressObjectString);
                    }
                } else if(IPUtil.isIP(address) || IPUtil.isIPv6(address)){
                    //判断是单IP
                    if(IPUtil.isIPv6(address)){
                        String addressObjectString = String.format("set security address-book global address %s %s/128 \n", name, address);
                        sb.append(addressObjectString);
                        // 单ip新建对象的时候拼子网掩码128,回滚的时候应该也带上
                        joinRollbackIp = address + "/128";
                    }else {
                        String addressObjectString = String.format("set security address-book global address %s %s/32 \n", name, address);
                        sb.append(addressObjectString);
                        // 单ip新建对象的时候拼子网掩码32,回滚的时候应该也带上
                        joinRollbackIp = address + "/32";
                    }

                }else {
                    //域名
                    String addressObjectString = String.format("set security address-book global address %s dns-name %s \n", name, address);
                    sb.append(addressObjectString);
                }
                if(arr.length > 1) {
                    sb.append(String.format("set security address-book global address-set %s address %s \n", groupName, name));
                }
                index++;
            }
        }

        boolean IPv6Range = indexIpv6 == 0 || indexIpv6 == 1;
        dto.setCommandLine(sb.toString());
        dto.setGroup(index == 1 && IPv6Range ? false : true);
        if(dto.isGroup()){
            dto.setJoinRollbackParam(String.format("%s,%s,%s",zone,groupName,name));
        }else{
            dto.setJoinRollbackParam(String.format("%s,%s,%s",zone,name,joinRollbackIp));
        }
        dto.setName(index == 1 && IPv6Range ? name : groupName);
        dto.setJoin(dto.getName());
        }catch (Exception e){
            logger.info("",e);
        }
        return dto;
    }

    /**
     * 生成服务对象
     * @param serviceDTOList 服务列表
     * @return 服务对象
     */
    private PolicyObjectDTO generateServiceObject(List<ServiceDTO> serviceDTOList,  boolean createObjFlag, String existsServiceName,List<String> existServiceNameList,List<ServiceDTO> restServiceList) {
        PolicyObjectDTO dto = new PolicyObjectDTO();
        if ((serviceDTOList == null || serviceDTOList.isEmpty()) && CollectionUtils.isEmpty(existServiceNameList)) {
            return dto;
        }



        StringBuilder sb = new StringBuilder();

        //对象名称集合
        List<String> serviceNameList = new ArrayList<>();

        List<ServiceDTO> serviceDTOLists = new ArrayList<>();
        for (ServiceDTO dtoList : serviceDTOList) {
            serviceDTOLists.add(dtoList);
        }
        if(CollectionUtils.isNotEmpty(existServiceNameList)){
            for (String s : existServiceNameList) {
                serviceNameList.add(s);
            }
            serviceDTOList.clear();
            for (ServiceDTO serviceDTO : restServiceList) {
                serviceDTOList.add(serviceDTO);
            }
        }

        for (ServiceDTO service : serviceDTOList) {
            int protocolNum = Integer.valueOf(service.getProtocol());
            String protocolString = ProtocolUtils.getProtocolByValue(protocolNum).toLowerCase();
            if (protocolString.equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_ANY)) {
                dto.setCommandLine("");
                dto.setJoin("any");
                return dto;
            }

            //只有1个服务，且端口是any ，或icmp type是空
            if (serviceDTOList.size() == 1) {
                if (protocolString.equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_ICMP)) {
                    //icmp协议，icmpType和icmpCode都为空
                    if (StringUtils.isBlank(service.getType()) && StringUtils.isBlank(service.getCode())) {
                        dto.setJoin("junos-icmp-all");
                        dto.setName(dto.getJoin());
                        return dto;
                    }
                }else if (protocolString.equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_ICMPV6)) {
                    //icmpv6协议，icmpType和icmpCode都为空
                    if (StringUtils.isBlank(service.getType()) && StringUtils.isBlank(service.getCode())) {
                        dto.setJoin("junos-icmp6-all");
                        dto.setName(dto.getJoin());
                        return dto;
                    }
                } else if (protocolString.equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_TCP) ||
                        protocolString.equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_UDP)) {
                    if (service.getDstPorts().equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_ANY)) {
                        dto.setCommandLine("junos-" + protocolString + "-any");
                        dto.setJoin(dto.getCommandLine());
                        return dto;
                    }
                }
            }

            //多个服务建对象
            if(protocolString.equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_ICMP)) {
                String objName = "";
                //只有ICMP，则不用建对象，直接添加到组
                if(StringUtils.isBlank(service.getType()) && StringUtils.isBlank(service.getCode())) {
                    objName = "junos-icmp-all";
                }else{
                    objName = PolicyConstants.POLICY_STR_VALUE_ICMP.toLowerCase();
                    sb.append(String.format("set applications application %s protocol icmp ", objName));

                    if (StringUtils.isNotBlank(service.getType())) {
                        sb.append(String.format("icmp-type %d ", Integer.valueOf(service.getType())));
                    }
                    if (StringUtils.isNotBlank(service.getCode())) {
                        sb.append(String.format("icmp-code %d", Integer.valueOf(service.getCode())));
                    }
                    sb.append("\n");
                }
                serviceNameList.add(objName);
            } else if(protocolString.equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_ICMPV6)) {
                String objName = "";
                //只有ICMPV6，则不用建对象，直接添加到组
                if(StringUtils.isBlank(service.getType()) && StringUtils.isBlank(service.getCode())) {
                    objName = "junos-icmp6-all";
                }else{
                    objName = PolicyConstants.POLICY_STR_VALUE_ICMP.toLowerCase();
                    sb.append(String.format("set applications application %s protocol icmp6 ", objName));

                    if (StringUtils.isNotBlank(service.getType())) {
                        sb.append(String.format("icmp6-type %d ", Integer.valueOf(service.getType())));
                    }
                    if (StringUtils.isNotBlank(service.getCode())) {
                        sb.append(String.format("icmp6-code %d", Integer.valueOf(service.getCode())));
                    }
                    sb.append("\n");
                }
                serviceNameList.add(objName);
            } else {

                //tcp、udp协议， 但是端口是any，则直接添加到组即可，不用建对象
                if(service.getDstPorts().equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_ANY)){
                    String name = String.format("junos-%s-any", protocolString.toLowerCase());
                    serviceNameList.add(name);
                    continue;
                }

                String[] srcPorts = service.getSrcPorts().split(",");
                String[] dstPorts = service.getDstPorts().split(",");
                for(String srcPortStr: srcPorts) {
                    for(String dstPortStr: dstPorts) {

                        String objName = getServiceNameByOne(protocolString, dstPortStr);
                        sb.append(String.format("set applications application %s protocol %s ", objName, protocolString));
                        //前面的判断已经过滤了srcPort和dstPort同时为any的情况，此时只有一个有值，仅显示有值的即可，若同时有值，则同时显示
                        if(!srcPortStr.equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_ANY)) {
                            String srcPortString = PortUtils.getPortString(srcPortStr, PortUtils.DASH_FORMAT);
                            sb.append(String.format("source-port %s ", srcPortString));
                        }
                        if(!dstPortStr.equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_ANY)) {
                            String dstPortString = PortUtils.getPortString(dstPortStr, PortUtils.DASH_FORMAT);
                            sb.append(String.format("destination-port %s ", dstPortString));
                        }
                        sb.append("\n");
                        serviceNameList.add(objName);
                    }
                }
            }
        }

        //将对象添加到组  如果只是1个对象，则不用建组，多个对象，需要建组
        if(serviceNameList.size() == 1){
            dto.setName(serviceNameList.get(0));
            dto.setJoin(serviceNameList.get(0));
            dto.setGroup(false);
        }else if(serviceNameList.size() > 1){
            String groupName = getServiceName(serviceDTOLists);
            dto.setName(groupName);
            dto.setJoin(groupName);
            dto.setGroup(true);
            for(String objName : serviceNameList){
                sb.append(String.format("set applications application-set %s application %s \n", groupName, objName));
            }
        }

        dto.setCommandLine(sb.toString());
        dto.setObjectFlag(true);
        return dto;
    }

    private PolicyObjectDTO generateTimeObject(String startTime, String endTime, final String ticket) {
        PolicyObjectDTO dto = new PolicyObjectDTO();
        if(startTime == null) {
            return null;
        }

        String name = String.format("%s_TR_%s", ticket, IdGen.getRandomNumberString());

        String commandline = String.format("set schedulers scheduler %s start-date %s stop-date %s \n", name,
                formatTimeString(startTime), formatTimeString(endTime));
        dto.setName(name);
        dto.setCommandLine(commandline);
        return dto;
    }

    private String formatTimeString(String timeString) {
        return TimeUtils.transformDateFormat(timeString, TimeUtils.EUROPEAN_TIME_FORMAT, TimeUtils.JUNIPER_SRX_TIME_FORMAT);
    }

    @Override
    public int getMaxNameLength() {
        return MAX_NAME_LENGTH;
    }

    /**
     * 记录创建对象名称
     * @param dto
     * @param srcAddress
     * @param dstAddress
     * @param service
     */
    private void recordObjectName(CommandlineDTO dto, PolicyObjectDTO srcAddress, PolicyObjectDTO dstAddress, PolicyObjectDTO service) {
        List<String> addressGroupNames = new ArrayList<>();
        List<String> addressNames = new ArrayList<>();
        List<String> serviceGroupNames = new ArrayList<>();
        List<String> serviceNames = new ArrayList<>();
        if (null != srcAddress && srcAddress.isGroup() && srcAddress.isObjectFlag() && StringUtils.isNotBlank(srcAddress.getJoinRollbackParam())) {
            addressGroupNames.add(srcAddress.getJoinRollbackParam());
            dto.setAddressObjectGroupNameList(addressGroupNames);
        } else if (null != srcAddress && srcAddress.isObjectFlag() && StringUtils.isNotBlank(srcAddress.getJoinRollbackParam())) {
            addressNames.add(srcAddress.getJoinRollbackParam());
            dto.setAddressObjectNameList(addressNames);
        }

        if (null != dstAddress && dstAddress.isGroup() && dstAddress.isObjectFlag() && StringUtils.isNotBlank(dstAddress.getJoinRollbackParam())) {
            addressGroupNames.add(dstAddress.getJoinRollbackParam());
            dto.setAddressObjectGroupNameList(addressGroupNames);
        } else if (null != dstAddress && dstAddress.isObjectFlag() && StringUtils.isNotBlank(dstAddress.getJoinRollbackParam())) {
            addressNames.add(dstAddress.getJoinRollbackParam());
            dto.setAddressObjectNameList(addressNames);
        }

        if (null != service && service.isGroup() && service.isObjectFlag() && StringUtils.isNotBlank(service.getName())) {
            serviceGroupNames.add(service.getName());
            dto.setServiceObjectGroupNameList(serviceGroupNames);
        } else if (null != service && service.isObjectFlag() && StringUtils.isNotBlank(service.getName())) {
            serviceNames.add(service.getName());
            dto.setServiceObjectNameList(serviceNames);
        }
    }


    public static void main(String[] args) {
        CommandlineDTO dto = CommandlineDTO.getInstanceDemo();
        SecurityJuniperSrx juniperSrx = new SecurityJuniperSrx();
        String commandLine = juniperSrx.composite(dto);
        System.out.println("commandline:\n" + commandLine);
    }
}
