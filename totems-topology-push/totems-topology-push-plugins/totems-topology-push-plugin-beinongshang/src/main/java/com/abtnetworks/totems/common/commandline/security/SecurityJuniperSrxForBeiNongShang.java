package com.abtnetworks.totems.common.commandline.security;

import com.abtnetworks.totems.common.annotation.CustomCli;
import com.abtnetworks.totems.common.commandline.PolicyGenerator;
import com.abtnetworks.totems.common.commandline.SecurityPolicyGenerator;
import com.abtnetworks.totems.common.constants.PolicyConstants;
import com.abtnetworks.totems.common.enums.ActionEnum;
import com.abtnetworks.totems.common.enums.DeviceModelNumberEnum;
import com.abtnetworks.totems.common.enums.MoveSeatEnum;
import com.abtnetworks.totems.common.dto.*;
import com.abtnetworks.totems.common.dto.commandline.CommandlineDTO;
import com.abtnetworks.totems.common.dto.commandline.PolicyMergeDTO;
import com.abtnetworks.totems.common.dto.commandline.PolicyObjectDTO;
import com.abtnetworks.totems.common.dto.commandline.ServiceDTO;
import com.abtnetworks.totems.common.enums.PolicyEnum;
import com.abtnetworks.totems.common.utils.*;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * @Author: wenjiachang
 * @Date: 2019/1/29 9:36
 */
@Service
@CustomCli(value = DeviceModelNumberEnum.SRX, type = PolicyEnum.SECURITY)
public class SecurityJuniperSrxForBeiNongShang extends SecurityPolicyGenerator implements PolicyGenerator {

    private static Logger logger = LoggerFactory.getLogger(SecurityJuniperSrxForBeiNongShang.class);

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
        logger.info("JuniperSrx??????????????????????????????=======================================================");
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

        //?????????
        String srcZoneJoin = "";
        String dstZoneJoin = "";
        srcZoneJoin = String.format("from-zone %s", AliStringUtils.isEmpty(srcZone)?"any":srcZone);
        dstZoneJoin = String.format("to-zone %s", AliStringUtils.isEmpty(dstZone)?"any":dstZone);

        name = (StringUtils.isEmpty(dto.getDescription())) ? name : descripTionCode(dto.getDescription());
        if(!AliStringUtils.isEmpty(dto.getDescription())){
            sb.append(String.format("set security policies %s %s policy %s description %s \n", srcZoneJoin, dstZoneJoin, name, descripTionCode(dto.getDescription())));
        }
        sb.append(String.format("set security policies %s %s policy %s match source-address %s \n", srcZoneJoin, dstZoneJoin, name, srcAddress.getJoin()));
        sb.append(String.format("set security policies %s %s policy %s match destination-address %s \n", srcZoneJoin, dstZoneJoin, name, dstAddress.getJoin()));
        sb.append(String.format("set security policies %s %s policy %s match application %s \n", srcZoneJoin, dstZoneJoin, name, service.getJoin()));

        sb.append(String.format("set security policies %s %s policy %s then %s \n", srcZoneJoin, dstZoneJoin, name, dto.getAction().toLowerCase()));

        sb.append(String.format("set security policies %s %s policy %s then log session-init \n", srcZoneJoin, dstZoneJoin, name));
        sb.append(String.format("set security policies %s %s policy %s then log session-close \n", srcZoneJoin, dstZoneJoin, name));
        if( time != null) {            sb.append(String.format("set security policies %s %s policy %s scheduler-name %s \n", srcZoneJoin, dstZoneJoin, name, time.getName()));
        }

        //????????????????????????????????????
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
            logger.info("??????????????????????????????????????????ruleName???mergeField ????????????");
            return createCommandLine(dto);
        }

        String ruleName = mergeDTO.getRuleName();
        String mergeField =  mergeDTO.getMergeField();

        //??????????????????
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
                    // ???????????????????????????????????????2?????????
                    name = strSub(name, getMaxNameLength(),"GB2312");
                    groupName = strSub(groupName, getMaxNameLength(),"GB2312");
                    // ????????????????????????
                    int len = 0;
                    int groupLen = 0;
                    try{
                        len = name.getBytes("GB2312").length;
                        groupLen = groupName.getBytes("GB2312").length;
                    }catch (Exception e) {
                        logger.error("???????????????????????????");
                    }
                    // ???????????????
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
            if(IpUtils.isIPSegment(address)) {
                String addressObjectString = String.format("set security zones security-zone %s address-book address %s %s \n", zone , address, address);
                sb.append(addressObjectString);
                if(arr.length > 1) {
                    sb.append(String.format("set security zones security-zone %s address-book address-set %s address %s \n", zone, groupName, address));
                }
                name = address;
            } else if (IpUtils.isIPRange(address)) {
                String startIp = IpUtils.getStartIpFromRange(address);
                String endIp = IpUtils.getEndIpFromRange(address);
                name = String.format("%s-%s", startIp,endIp);
                String addressObjectString = String.format("set security zones security-zone %s address-book address range%s range-address %s to %s\n", zone, String.format("%s-%s", startIp,endIp), startIp, endIp);
                sb.append(addressObjectString);
                if(arr.length > 1) {
                    sb.append(String.format("set security zones security-zone %s address-book address-set %s address %s \n", zone, groupName, name));
                }
            } else {
                String addressObjectString = String.format("set security zones security-zone %s address-book address %s/32 %s/32 \n", zone , address, address);
                sb.append(addressObjectString);
                if(arr.length > 1) {
                    sb.append(String.format("set security zones security-zone %s address-book address-set %s address %s \n", zone, groupName, address));
                }
                name = String.format("%s/32",address);
            }
            index++;
        }

        dto.setCommandLine(sb.toString());

        dto.setName(index == 1 ? name : groupName);
        dto.setJoin(dto.getName());

        return dto;
    }

    /**
     * ??????????????????
     * @param serviceDTOList ????????????
     * @return ????????????
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

        //??????????????????
        List<String> serviceNameList = new ArrayList<>();

        for (ServiceDTO service : serviceDTOList) {
            int protocolNum = Integer.valueOf(service.getProtocol());
            String protocolString = ProtocolUtils.getProtocolByValue(protocolNum).toLowerCase();
            if (protocolString.equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_ANY)) {
                dto.setCommandLine("");
                dto.setJoin("any");
                return dto;
            }

            //??????1????????????????????????any ??????icmp type??????
            if (serviceDTOList.size() == 1) {
                if (protocolString.equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_ICMP)) {
                    //icmp?????????icmpType???icmpCode?????????
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

            //?????????????????????
            if(protocolString.equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_ICMP)) {
                String objName = "";
                //??????ICMP??????????????????????????????????????????
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

                //tcp???udp????????? ???????????????any????????????????????????????????????????????????
                if(service.getDstPorts().equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_ANY)){
                    String name = String.format("junos-%s-any", protocolString.toLowerCase());
                    serviceNameList.add(name);
                    continue;
                }

                String[] srcPorts = service.getSrcPorts().split(",");
                String[] dstPorts = service.getDstPorts().split(",");
                for(String srcPortStr: srcPorts) {
                    for(String dstPortStr: dstPorts) {

                        String objName = getServiceNameByOne(protocolString.toUpperCase(), dstPortStr);
                        sb.append(String.format("set applications application %s protocol %s ", objName, protocolString));
                        //??????????????????????????????srcPort???dstPort?????????any???????????????????????????????????????????????????????????????????????????????????????????????????
//                        if(!srcPortStr.equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_ANY)) {
//                            String srcPortString = PortUtils.getPortString(srcPortStr, PortUtils.DASH_FORMAT);
//                            sb.append(String.format("source-port %s ", srcPortString));
//                        }
                        if(!dstPortStr.equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_ANY)) {
                            String dstPortString = PortUtils.getPortString(dstPortStr, PortUtils.DASH_FORMAT);
                            sb.append(String.format("destination-port %s ", dstPortStr));
                        }
                        sb.append("\n");
                        serviceNameList.add(objName);
                    }
                }
            }
        }

        //?????????????????????  ????????????1?????????????????????????????????????????????????????????
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
        logger.info("JuniperSrx??????????????????????????????=======================================================");
        dto.setObjectFlag(true);
        return dto;
    }

    /**??????????????????***/
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

    public String getServiceNameByOne(String protocolString, String dstPort) {
        if(dstPort.equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_ANY)){
            return protocolString;
        }
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("%s%s", protocolString, dstPort));
        return sb.toString();
    }

    public String getServiceName(ServiceDTO dto) {
        StringBuilder sb = new StringBuilder();
        String protocolString = ProtocolUtils.getProtocolByValue(Integer.valueOf(dto.getProtocol()));
        sb.append(protocolString);
        if (StringUtils.isBlank(dto.getDstPorts())) {
            return sb.toString();
        }
        if(protocolString.equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_ICMP)){
            return sb.toString();
        }
        if(dto.getDstPorts().equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_ANY) || dto.getDstPorts().equals(PolicyConstants.PORT_ANY)){
            return sb.toString();
        }
        String[] dstPorts = dto.getDstPorts().split(",");
        for (String dstPort : dstPorts) {
            if (PortUtils.isPortRange(dstPort)) {
                String startPort = PortUtils.getStartPort(dstPort);
                String endPort = PortUtils.getEndPort(dstPort);
                sb.append(String.format("_%s_%s", startPort, endPort));
            } else {
                sb.append(String.format("_%s", dstPort));
            }
        }
        return sb.toString();
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
     * ?????????_?????????_?????????4???
     * @param description
     * @return
     */
    private String descripTionCode(String description) {
        StringBuffer applyNum=new StringBuffer();
        applyNum.append(description + "_");
        SimpleDateFormat df = new SimpleDateFormat("yyMMdd");//??????????????????
        // new Date()???????????????????????????
        applyNum.append(df.format(new Date()));
        int random=(int) ((Math.random()*9000)+1000);
        applyNum.append("_");
        applyNum.append(random);
        return applyNum.toString();
    }

    public static void main(String[] args) {
        CommandlineDTO dto = CommandlineDTO.getInstanceDemo();
        dto.setName("aaaa");
        dto.setSrcIp("10.10.10.1");
        dto.setDstIp("102.11.111.0/24");
        dto.setDeviceUuid("5bb4529a48474663adcd89609cf4ef9f");
        dto.setIpType(0);
        dto.setDescription("bbbb");

        ServiceDTO serviceDTO1 = new ServiceDTO();
        serviceDTO1.setProtocol("6");
        serviceDTO1.setDstPorts("80,90,100-103");
        serviceDTO1.setSrcPorts("");

        dto.setServiceList(Arrays.asList(serviceDTO1));
        dto.setAction("PERMIT");
        SecurityJuniperSrxForBeiNongShang juniperSrx = new SecurityJuniperSrxForBeiNongShang();
        String commandLine = juniperSrx.composite(dto);
        System.out.println("commandline:\n" + commandLine);
    }
}
