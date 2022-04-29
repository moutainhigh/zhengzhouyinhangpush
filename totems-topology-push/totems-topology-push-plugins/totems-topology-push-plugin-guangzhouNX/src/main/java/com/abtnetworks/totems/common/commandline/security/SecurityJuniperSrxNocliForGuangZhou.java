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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @Author: wxx
 * @Date: 2021/5/17 09:20
 * @Desc:Juniper SRX NoCli
 */
@Service
@CustomCli(value = DeviceModelNumberEnum.SRX_NoCli, type = PolicyEnum.SECURITY)
public class SecurityJuniperSrxNocliForGuangZhou extends SecurityPolicyGenerator implements PolicyGenerator {

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
        String srcZone = StringUtils.isNotBlank(dto.getSrcZone()) ? dto.getSrcZone() : "";
        String dstZone = StringUtils.isNotBlank(dto.getDstZone()) ? dto.getDstZone() : "";
        boolean createObjFlag = dto.isCreateObjFlag();

        String name = String.format("%s", ticket);

        PolicyObjectDTO srcAddress = generateAddressObject(srcIp, srcZone, ticket, dto.getSrcAddressName(), dto.getSrcIpSystem());
        PolicyObjectDTO dstAddress = generateAddressObject(dstIp, dstZone, ticket, dto.getDstAddressName(), dto.getDstIpSystem());
        PolicyObjectDTO service = generateServiceObject(dto.getServiceList(), createObjFlag, dto.getServiceName());
        PolicyObjectDTO time = generateTimeObject(startTime, endTime, ticket);


        sb.append("configure\n");
        if(srcAddress.isObjectFlag() && StringUtils.isNotBlank(srcAddress.getCommandLine())) {
            sb.append(String.format("%s\n", srcAddress.getCommandLine()));
        }
        if(dstAddress.isObjectFlag() && StringUtils.isNotBlank(dstAddress.getCommandLine())) {
            sb.append(String.format("%s\n", dstAddress.getCommandLine()));
        }
        if (service.isObjectFlag() && StringUtils.isNotBlank(service.getCommandLine())) {
            if(dto.getIdleTimeout()!=null) {
                sb.append(String.format("%s inactivity-timeout %d \n", service.getCommandLine(), dto.getIdleTimeout()));
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
        //针对广州农信新增最终日志
        sb.append(String.format("set security policies  %s  %s policy %s then log session-init\n",srcZoneJoin,dstZoneJoin,name));
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
        String srcZone = StringUtils.isNotBlank(dto.getSrcZone()) ? dto.getSrcZone() : "";
        String dstZone = StringUtils.isNotBlank(dto.getDstZone()) ? dto.getDstZone() : "";
        PolicyObjectDTO srcAddress = generateAddressObject(dto.getSrcIp(), srcZone, dto.getName(), dto.getSrcAddressName(), dto.getSrcIpSystem());
        PolicyObjectDTO dstAddress = generateAddressObject(dto.getDstIp(), dstZone, dto.getName(), dto.getDstAddressName(), dto.getDstIpSystem());
        PolicyObjectDTO service = generateServiceObject(dto.getServiceList(), createObjFlag, dto.getServiceName());

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

    private PolicyObjectDTO generateAddressObject(String ipAddress, String zone, String ticket, String existsAddressName, String ipSystem) {
        PolicyObjectDTO dto = new PolicyObjectDTO();
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

                    name = String.format("%s_%s", ipSystem, ipAddress);
                } else {
                    name  = ipSystem + "_" + address;
                }
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
            if(IpUtils.isIPSegment(address)) {
                String addressObjectString = String.format("set security zones security-zone %s address-book address %s %s \n", zone , name, address);
                sb.append(addressObjectString);
            } else if (IpUtils.isIPRange(address)) {
                String startIp = IpUtils.getStartIpFromRange(address);
                String endIp = IpUtils.getEndIpFromRange(address);
                String addressObjectString = String.format("set security zones security-zone %s address-book address %s range-address %s to %s\n", zone, name, startIp, endIp);
                sb.append(addressObjectString);
            } else {
                String addressObjectString = String.format("set security zones security-zone %s address-book address %s %s/32 \n", zone , name, address);
                sb.append(addressObjectString);
            }

            /*if(arr.length > 1) {
                sb.append(String.format("set security zones security-zone %s address-book address-set %s address %s \n", zone, groupName, name));
            }*/
        }

        dto.setCommandLine(sb.toString());

        dto.setName(name.toString().replace(",", " "));
        dto.setJoin(dto.getName());

        return dto;
    }

    /**
     * 生成服务对象
     * @param serviceDTOList 服务列表
     * @return 服务对象
     */
    private PolicyObjectDTO generateServiceObject(List<ServiceDTO> serviceDTOList,  boolean createObjFlag, String existsServiceName) {
        PolicyObjectDTO dto = new PolicyObjectDTO();
        if ((serviceDTOList == null || serviceDTOList.isEmpty()) && StringUtils.isBlank(existsServiceName)) {
            return dto;
        }

        if(StringUtils.isNotBlank(existsServiceName)){
            dto.setObjectFlag(true);
            dto.setJoin(existsServiceName);
            return dto;
        }

        StringBuilder sb = new StringBuilder();

        //对象名称集合
        List<String> serviceNameList = new ArrayList<>();

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
        }else if(serviceNameList.size() > 1){
            String groupName = getServiceName(serviceDTOList);
            dto.setName(groupName);
            dto.setJoin(groupName);
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

        //String name = String.format("%s_TR_%s", ticket, IdGen.getRandomNumberString());
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


    public static void main(String[] args) {
        CommandlineDTO dto = CommandlineDTO.getInstanceDemo();
        SecurityJuniperSrxNocliForGuangZhou juniperSrx = new SecurityJuniperSrxNocliForGuangZhou();
        List<ServiceDTO> srcport=new ArrayList<>();
        ServiceDTO serviceDTO = new ServiceDTO();
        serviceDTO.setProtocol("6");
        serviceDTO.setSrcPorts("22");
        serviceDTO.setDstPorts("70-90");

        srcport.add(serviceDTO);
        dto.setSrcIp("127.0.0.1/22,2.2.2.2");
        dto.setDstIp("10.10.1.2-10.10.1.10");
        dto.setSrcIpSystem("");
        dto.setDstIpSystem("");
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
