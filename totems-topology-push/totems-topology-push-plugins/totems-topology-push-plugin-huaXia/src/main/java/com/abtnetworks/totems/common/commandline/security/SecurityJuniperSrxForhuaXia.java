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
import org.apache.kafka.common.protocol.types.Field;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * @Author: wenjiachang
 * @Date: 2019/1/29 9:36
 */
@Service
@CustomCli(value = DeviceModelNumberEnum.SRX, type = PolicyEnum.SECURITY)
public class SecurityJuniperSrxForhuaXia extends SecurityPolicyGenerator implements PolicyGenerator {

    private static Logger logger = LoggerFactory.getLogger(SecurityJuniperSrxForhuaXia.class);

    private final int MAX_NAME_LENGTH = 63;
    private final int HOUR_SECOND = 60 * 60;

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
        if(dto.getIdleTimeout()!=null){
            name = String.format("%s_long", ticket);
        }

        PolicyObjectDTO srcAddress = generateAddressObject(srcIp, srcZone, ticket, dto.getSrcAddressName(), dto.getSrcIpSystem());
        PolicyObjectDTO dstAddress = generateAddressObject(dstIp, dstZone, ticket, dto.getDstAddressName(), dto.getDstIpSystem());
        PolicyObjectDTO service = generateServiceObject(dto.getServiceList(), createObjFlag, dto.getServiceName(),dto.getIdleTimeout());
        PolicyObjectDTO time = generateTimeObject(startTime, endTime, ticket);


        sb.append("configure\n");
        if(srcAddress.isObjectFlag() && StringUtils.isNotBlank(srcAddress.getCommandLine())) {
            sb.append(String.format("%s\n", srcAddress.getCommandLine()));
        }
        if(dstAddress.isObjectFlag() && StringUtils.isNotBlank(dstAddress.getCommandLine())) {
            sb.append(String.format("%s\n", dstAddress.getCommandLine()));
        }
        if (service.isObjectFlag() && StringUtils.isNotBlank(service.getCommandLine())) {

           sb.append(String.format("%s  \n", service.getCommandLine()));

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

        String[] srr=dto.getSrcIp().split(",");
        String[] drr=dto.getDstIp().split(",");



        for(String sr:srr) {
            if(StringUtils.isNotEmpty(sr)) {
                if(IpUtils.isIPRange(sr)){
                    String startIp = IpUtils.getStartIpFromRange(sr);
                    int endindexof=1+IpUtils.getEndIpFromRange(sr).lastIndexOf(".");


                    String endIp = IpUtils.getEndIpFromRange(sr).substring(endindexof);
                    sr = String.format("%s-%s", startIp, endIp);
                    sb.append(String.format("set security policies %s %s policy %s match source-address %s  \n", srcZoneJoin, dstZoneJoin, name, sr));

                }
                else if(IpUtils.isIP(sr)) {
                    sb.append(String.format("set security policies %s %s policy %s match source-address %s/32  \n", srcZoneJoin, dstZoneJoin, name, sr));
                }
                else{
                    sb.append(String.format("set security policies %s %s policy %s match source-address %s  \n", srcZoneJoin, dstZoneJoin, name, sr));
                }
            }
            else{
                sb.append(String.format("set security policies %s %s policy %s match source-address any \n", srcZoneJoin, dstZoneJoin, name));
            }
        }
        for(String ds:drr) {
            if(StringUtils.isNotEmpty(ds)) {
                if(IpUtils.isIPRange(ds)){
                    String startIp = IpUtils.getStartIpFromRange(ds);
                    int endindexof=1+IpUtils.getEndIpFromRange(ds).lastIndexOf(".");


                    String endIp = IpUtils.getEndIpFromRange(ds).substring(endindexof);
                    ds = String.format("%s-%s", startIp, endIp);
                    sb.append(String.format("set security policies %s %s policy %s match destination-address %s  \n", srcZoneJoin, dstZoneJoin, name, ds));
                }
                else if(IpUtils.isIP(ds)) {
                    sb.append(String.format("set security policies %s %s policy %s match destination-address %s/32  \n", srcZoneJoin, dstZoneJoin, name, ds));
                }
                else{
                    sb.append(String.format("set security policies %s %s policy %s match destination-address %s  \n", srcZoneJoin, dstZoneJoin, name, ds));
                }
            }
            else{
                sb.append(String.format("set security policies %s %s policy %s match destination-address any \n", srcZoneJoin, dstZoneJoin, name));
            }
        }
        for(int i=0;i<dto.getServiceList().size();i++){
            int protocol=Integer.parseInt(dto.getServiceList().get(i).getProtocol());

            if(StringUtils.isNotEmpty(dto.getServiceList().get(i).getDstPorts())&&!dto.getServiceList().get(i).getDstPorts().equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_ANY)&&!ProtocolUtils.getProtocolByString(dto.getServiceList().get(i).getProtocol()).toLowerCase().equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_ICMP)) {

                String[] dstport = dto.getServiceList().get(i).getDstPorts().split(",");
                for(String s:dstport) {
                    String join= ProtocolUtils.getProtocolByValue(protocol).toUpperCase()+s;

                    if(dto.getIdleTimeout()!=null){
                         join  = String.format("%s_long",join);
                    }
                    sb.append(String.format("set security policies %s %s policy %s match application %s \n", srcZoneJoin, dstZoneJoin, name, join));
                }
            }else if(ProtocolUtils.getProtocolByString(dto.getServiceList().get(i).getProtocol()).toLowerCase().equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_ICMP)){
                    sb.append(String.format("set security policies %s %s policy %s match application junos-icmp-all\n", srcZoneJoin, dstZoneJoin, name));
            }
            else{
                sb.append(String.format("set security policies %s %s policy %s match application junos-%s-any \n", srcZoneJoin, dstZoneJoin, name,ProtocolUtils.getProtocolByString(dto.getServiceList().get(i).getProtocol()).toLowerCase()));
            }
        }

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
        String srcZone = StringUtils.isNotBlank(dto.getSrcZone()) ? dto.getSrcZone() : "";
        String dstZone = StringUtils.isNotBlank(dto.getDstZone()) ? dto.getDstZone() : "";
        PolicyObjectDTO srcAddress = generateAddressObject(dto.getSrcIp(), srcZone, dto.getName(), dto.getSrcAddressName(), dto.getSrcIpSystem());
        PolicyObjectDTO dstAddress = generateAddressObject(dto.getDstIp(), dstZone, dto.getName(), dto.getDstAddressName(), dto.getDstIpSystem());
        PolicyObjectDTO service = generateServiceObject(dto.getServiceList(), createObjFlag, dto.getServiceName(),dto.getIdleTimeout());

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


        String name = "";
        int index = 0;
        for (String address : arr) {

            if(IpUtils.isIPRange(address)){
                String startIp = IpUtils.getStartIpFromRange(address);
                int endindexof=1+IpUtils.getEndIpFromRange(address).lastIndexOf(".");


                String endIp = IpUtils.getEndIpFromRange(address).substring(endindexof);
                name = String.format("%s-%s", startIp, endIp);
            }
            else if (IpUtils.isIP(address)){
                name=String.format("%s/32",address);
            }
            else {
                name = address;
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


            index++;

        }
        dto.setJoin(name);

        dto.setCommandLine(sb.toString());




        return dto;
    }

    /**
     * 生成服务对象
     * @param serviceDTOList 服务列表
     * @return 服务对象
     */
    private PolicyObjectDTO generateServiceObject(List<ServiceDTO> serviceDTOList,  boolean createObjFlag, String existsServiceName,Integer idleTimeout) {
        PolicyObjectDTO dto = new PolicyObjectDTO();
        if ((serviceDTOList == null || serviceDTOList.isEmpty()) && StringUtils.isBlank(existsServiceName)) {
            return dto;
        }

        if(StringUtils.isNotBlank(existsServiceName)){
            if(idleTimeout!=null){

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
                            sb.append(String.format("set applications application %s protocol junos-icmp-all ", objName));

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
                                if(idleTimeout !=null){
                                    objName  = String.format("%s_long",objName);
                                }
                                sb.append(String.format("set applications application %s protocol %s ", objName, protocolString));
                                //前面的判断已经过滤了srcPort和dstPort同时为any的情况，此时只有一个有值，仅显示有值的即可，若同时有值，则同时显示
//                        if(!srcPortStr.equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_ANY)) {
//                            String srcPortString = PortUtils.getPortString(srcPortStr, PortUtils.DASH_FORMAT);
//                            sb.append(String.format("source-port %s ", srcPortString));
//                        }
                                if(idleTimeout!=null) {
                                    if(!dstPortStr.equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_ANY)) {
                                        String dstPortString = PortUtils.getPortString(dstPortStr, PortUtils.DASH_FORMAT);
                                        sb.append(String.format("destination-port %s inactivity-timeout %d\n", dstPortString, idleTimeout));
                                    }
                                }else{
                                    String dstPortString = PortUtils.getPortString(dstPortStr, PortUtils.DASH_FORMAT);
                                    sb.append(String.format("destination-port %s \n", dstPortString));
                                }

                                serviceNameList.add(objName);
                            }
                        }
                    }
                }

                //将对象添加到组  如果只是1个对象，则不用建组，多个对象，需要建组

                dto.setName(serviceNameList.get(0));
                dto.setJoin(serviceNameList.get(0));
                dto.setCommandLine(sb.toString());
                dto.setJoin(existsServiceName);
                dto.setObjectFlag(true);
                return dto;

            }else {
                dto.setObjectFlag(true);
                dto.setJoin(existsServiceName);
                return dto;
            }
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
                    sb.append(String.format("set applications application %s protocol junos-icmp-all ", objName));

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
                        if(idleTimeout !=null){
                            objName  = String.format("%s_long",objName);
                        }
                        sb.append(String.format("set applications application %s protocol %s ", objName, protocolString));
                        //前面的判断已经过滤了srcPort和dstPort同时为any的情况，此时只有一个有值，仅显示有值的即可，若同时有值，则同时显示
//                        if(!srcPortStr.equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_ANY)) {
//                            String srcPortString = PortUtils.getPortString(srcPortStr, PortUtils.DASH_FORMAT);
//                            sb.append(String.format("source-port %s ", srcPortString));
//                        }
                        if(idleTimeout!=null) {
                        if(!dstPortStr.equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_ANY)) {
                            String dstPortString = PortUtils.getPortString(dstPortStr, PortUtils.DASH_FORMAT);
                            sb.append(String.format("destination-port %s inactivity-timeout %d\n", dstPortString, idleTimeout));
                            }
                        }else{
                            String dstPortString = PortUtils.getPortString(dstPortStr, PortUtils.DASH_FORMAT);
                            sb.append(String.format("destination-port %s \n", dstPortString));
                        }

                        serviceNameList.add(objName);
                    }
                }
            }
        }

        //将对象添加到组  如果只是1个对象，则不用建组，多个对象，需要建组

            dto.setName(serviceNameList.get(0));
            dto.setJoin(serviceNameList.get(0));


        dto.setCommandLine(sb.toString());
        dto.setObjectFlag(true);
        return dto;
    }

    @Override
    public String getServiceNameByOne(String protocolString, String dstPort) {
        if(dstPort.equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_ANY)){
            return protocolString;
        }


            StringBuilder sb = new StringBuilder();
            sb.append(String.format("%s%s", protocolString, dstPort));
            return sb.toString().toUpperCase();

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


    public static void main(String[] args) {
        CommandlineDTO dto = CommandlineDTO.getInstanceDemo();
        List<String> restSrc = new ArrayList<>();
        restSrc.add("1.3.2.5");
        restSrc.add("1.3.2.55");
        dto.setRestSrcAddressList(restSrc);
        List<String> restDst = new ArrayList<>();
        restDst.add("1.3.2.56");
        restDst.add("1.3.88.55");
        dto.setSrcIp("3.3.3.3,12.3.1.3/24,32.4.4.4-23.4.2.4");
        dto.setDstIp("3.3.3.3,12.3.1.3/24,32.4.4.4-23.4.2.4");
        dto.setRestDstAddressList(restDst);
        List<ServiceDTO> serviceDTOS = new ArrayList<>();
        ServiceDTO serviceDTO = new ServiceDTO();
        ServiceDTO serviceDTO2= new ServiceDTO();
        serviceDTO.setProtocol("17");
        serviceDTO2.setProtocol("6");
        serviceDTO.setDstPorts("32");
        serviceDTO2.setDstPorts("99");
        serviceDTO.setSrcPorts("24");
        serviceDTO2.setSrcPorts("41");


        List<String> existServiceNameList = new ArrayList<>();
        existServiceNameList.add("fff");
        dto.setServiceName("fff");

        dto.setIdleTimeout(324);
        dto.setStartTime("2008-1-3 10:33:13");
        dto.setEndTime("2008-1-3 10:33:18");
        serviceDTOS.add(serviceDTO);
        serviceDTOS.add(serviceDTO2);
        dto.setServiceList(serviceDTOS);
        dto.setRestServiceList(serviceDTOS);
        SecurityJuniperSrxForhuaXia juniperSrx = new SecurityJuniperSrxForhuaXia();
        String commandLine = juniperSrx.composite(dto);
        System.out.println("commandline:\n" + commandLine);
    }
}
