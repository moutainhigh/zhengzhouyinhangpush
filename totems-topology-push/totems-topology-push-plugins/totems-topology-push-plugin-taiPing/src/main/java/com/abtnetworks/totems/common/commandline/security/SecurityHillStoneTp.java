package com.abtnetworks.totems.common.commandline.security;

import com.abtnetworks.totems.common.annotation.CustomCli;
import com.abtnetworks.totems.common.commandline.PolicyGenerator;
import com.abtnetworks.totems.common.commandline.SecurityPolicyGenerator;
import com.abtnetworks.totems.common.constants.PolicyConstants;
import com.abtnetworks.totems.common.dto.CmdDTO;
import com.abtnetworks.totems.common.dto.DeviceDTO;
import com.abtnetworks.totems.common.dto.ExistObjectDTO;
import com.abtnetworks.totems.common.dto.GeneratedObjectDTO;
import com.abtnetworks.totems.common.dto.PolicyDTO;
import com.abtnetworks.totems.common.dto.SettingDTO;
import com.abtnetworks.totems.common.dto.TaskDTO;
import com.abtnetworks.totems.common.dto.commandline.CommandlineDTO;
import com.abtnetworks.totems.common.dto.commandline.PolicyMergeDTO;
import com.abtnetworks.totems.common.dto.commandline.PolicyObjectDTO;
import com.abtnetworks.totems.common.dto.commandline.ServiceDTO;
import com.abtnetworks.totems.common.enums.ActionEnum;
import com.abtnetworks.totems.common.enums.DeviceModelNumberEnum;
import com.abtnetworks.totems.common.enums.MoveSeatEnum;
import com.abtnetworks.totems.common.enums.PolicyEnum;
import com.abtnetworks.totems.common.utils.AliStringUtils;
import com.abtnetworks.totems.common.utils.IpUtils;
import com.abtnetworks.totems.common.utils.PortUtils;
import com.abtnetworks.totems.common.utils.ProtocolUtils;
import com.abtnetworks.totems.common.utils.TimeUtils;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author Administrator
 * @Title:
 * @Description: 山石策略命令行生成
 * @date 2020/7/2
 */
@Slf4j
@Service
@Component
@CustomCli(value = DeviceModelNumberEnum.HILLSTONE, type = PolicyEnum.SECURITY)
public class SecurityHillStoneTp extends SecurityPolicyGenerator implements PolicyGenerator {

    private static Logger logger = Logger.getLogger(SecurityHillStoneTp.class);

    private static Set<Integer> allowType = new HashSet<>();

    private final String DESTINATION_ADDRESS = "destination-address";

    private final int MAX_NAME_LENGTH = 95;

    private final int DAY_SECOND = 24 * 60 * 60;


    public SecurityHillStoneTp() {
        init();
    }

    private static void init() {
        allowType.add(3);
        allowType.add(4);
        allowType.add(5);
        allowType.add(8);
        allowType.add(11);
        allowType.add(12);
        allowType.add(13);
        allowType.add(15);
    }

    @Override
    public String generate(CmdDTO cmdDTO) {
        logger.info("cmdDTO is " + cmdDTO);
        logger.info("SecurityHillStoneTp==================");
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

        dto.setExistSrcAddressList(existObjectDTO.getExistSrcAddressList());
        dto.setRestSrcAddressList(existObjectDTO.getRestSrcAddressList());
        dto.setExistDstAddressList(existObjectDTO.getExistDstAddressList());
        dto.setRestDstAddressList(existObjectDTO.getRestDstAddressList());
        dto.setSwapRuleNameId(settingDTO.getSwapNameId());
        dto.setIdleTimeout(policyDTO.getIdleTimeout());

        GeneratedObjectDTO generatedDto = cmdDTO.getGeneratedObject();
        generatedDto.setPolicyName(dto.getName());
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
        sb.append("configure\n");

        boolean createObjFlag = dto.isCreateObjFlag();
        String ticket = dto.getName();
        PolicyObjectDTO srcAddressObject = generateAddressObject(dto.getSrcIp(), ticket, "src", createObjFlag, dto.getSrcAddressName());
        PolicyObjectDTO dstAddressObject = generateAddressObject(dto.getDstIp(), ticket, "dst", createObjFlag, dto.getDstAddressName());
        PolicyObjectDTO serviceObject = generateServiceObject(dto.getRestServiceList(), dto.getExistServiceNameList(), dto.getIdleTimeout());

        if (mergeField.equals(PolicyConstants.SRC) && srcAddressObject.isObjectFlag() && StringUtils.isNotBlank(srcAddressObject.getCommandLine())) {
            sb.append(srcAddressObject.getCommandLine());
        }
        if (mergeField.equals(PolicyConstants.DST) && dstAddressObject.isObjectFlag() && StringUtils.isNotBlank(dstAddressObject.getCommandLine())) {
            sb.append(dstAddressObject.getCommandLine());
        }
        if (mergeField.equals(PolicyConstants.SERVICE) && serviceObject.isObjectFlag() && StringUtils.isNotBlank(serviceObject.getCommandLine())) {
            sb.append(serviceObject.getCommandLine());
        }

        sb.append(String.format("rule id %s\n", ruleId));
        if (mergeField.equals(PolicyConstants.SRC)) {
            if (srcAddressObject.isObjectFlag()) {
                sb.append(srcAddressObject.getJoin());
            } else {
                sb.append(srcAddressObject.getCommandLine());
            }
        } else if (mergeField.equals(PolicyConstants.DST)) {
            if (dstAddressObject.isObjectFlag()) {
                sb.append(dstAddressObject.getJoin());
            } else {
                sb.append(dstAddressObject.getCommandLine());
            }
        } else if (mergeField.equals(PolicyConstants.SERVICE)) {
            if (serviceObject.isObjectFlag() && StringUtils.isNotBlank(serviceObject.getJoin())) {
                sb.append(serviceObject.getJoin());
            } else {
                sb.append(serviceObject.getCommandLine());
            }
        }

        sb.append("exit\n");
        sb.append("end\n");

        return sb.toString();
    }

    public String createCommandLine(CommandlineDTO dto) {
        StringBuilder sb = new StringBuilder();
        String ticket = dto.getName();
        String srcIp = dto.getSrcIp();
        String dstIp = dto.getDstIp();
        String startTime = dto.getStartTime();
        String endTime = dto.getEndTime();
        boolean createObjFlag = dto.isCreateObjFlag();
        PolicyObjectDTO srcAddress = generateAddressObject(dto.getExistSrcAddressList(), dto.getRestSrcAddressList(), ticket, "src", createObjFlag);
        PolicyObjectDTO dstAddress = generateAddressObject(dto.getExistDstAddressList(), dto.getRestDstAddressList(), ticket, "dst", createObjFlag);
        PolicyObjectDTO serviceObject = generateServiceObject(dto.getRestServiceList(), dto.getExistServiceNameList(), dto.getIdleTimeout());
        PolicyObjectDTO time = generateTimeObject(startTime, endTime, ticket);

        sb.append("configure\n");

        //定义对象
        if (srcAddress.isObjectFlag() && StringUtils.isNotBlank(srcAddress.getCommandLine())) {
            sb.append(String.format("%s\n", srcAddress.getCommandLine()));
        }
        if (dstAddress.isObjectFlag() && StringUtils.isNotBlank(dstAddress.getCommandLine())) {
            sb.append(String.format("%s\n", dstAddress.getCommandLine()));
        }

        //山石比较特殊，即使指定了不创建对象，但有具体的源、目的端口时，也会创建对象的
        if (serviceObject != null && serviceObject.isObjectFlag() && StringUtils.isNotBlank(serviceObject.getCommandLine())) {
            sb.append(String.format("%s\n", serviceObject.getCommandLine()));
        }
        if (time != null) {
            sb.append(String.format("%s\n", time.getCommandLine()));
        }

        sb.append("rule ");
        String swapRuleNameId = StringUtils.isNotBlank(dto.getSwapRuleNameId()) ? dto.getSwapRuleNameId() : "";
        int moveSeatCode = dto.getMoveSeatEnum().getCode();
        if (moveSeatCode == MoveSeatEnum.FIRST.getCode()) {
            sb.append("top \n");
        } else if (moveSeatCode == MoveSeatEnum.BEFORE.getCode() || moveSeatCode == MoveSeatEnum.AFTER.getCode()) {
            if(StringUtils.isNotEmpty(swapRuleNameId)){
                sb.append(String.format("%s %s\n",dto.getMoveSeatEnum().getKey(), swapRuleNameId));
            }else{
                sb.append("\n");
            }

        } else {
            sb.append("\n");
        }

        if (StringUtils.isNotEmpty(dto.getAction())) {
            sb.append(String.format("   action %s\n", dto.getAction().toLowerCase()));
        }
        String srcZone = StringUtils.isNotBlank(dto.getSrcZone()) ? dto.getSrcZone() : "Any";

        sb.append(String.format("   src-zone \"%s\"\n", srcZone));
        String dstZone = StringUtils.isNotBlank(dto.getDstZone()) ? dto.getDstZone() : "Any";

        sb.append(String.format("   dst-zone \"%s\"\n", dstZone));

        if (StringUtils.isNotBlank(srcAddress.getJoin())) {
            sb.append(srcAddress.getJoin());
        } else {
            sb.append(srcAddress.getName());
        }
        if (StringUtils.isNotBlank(dstAddress.getJoin())) {
            sb.append(dstAddress.getJoin());
        } else {
            sb.append(dstAddress.getName());
        }
        if (serviceObject != null && serviceObject.isObjectFlag() && StringUtils.isNotBlank(serviceObject.getJoin())) {
            sb.append(serviceObject.getJoin());
        } else if (serviceObject != null && StringUtils.isNotBlank(serviceObject.getCommandLine())) {
            sb.append("   ").append(serviceObject.getName());
        }
        if (!AliStringUtils.isEmpty(dto.getDescription())) {
            sb.append(String.format("   description \"%s\"\n", dto.getDescription()));
        }

        if (StringUtils.isNotEmpty(dto.getBusinessName())) {
            sb.append(String.format("   name \"%s\"\n", dto.getBusinessName()));
        }

        if (time != null) {
            sb.append(String.format("   schedule %s\n", time.getName()));
        }

        sb.append("exit\n");
        sb.append("end\n");

        return sb.toString();
    }

    @Override
    public String generatePostCommandline(CommandlineDTO dto) {
        return "";
    }

    public PolicyObjectDTO generateAddressObject(List<String> existAddressList, List<String> restAddressList, String ticket,
                                                 String ipPrefix, boolean createObjFlag) {
        PolicyObjectDTO policyObjectDTO = new PolicyObjectDTO();
        policyObjectDTO.setName("   \"any\"");
        policyObjectDTO.setJoin("   " + ipPrefix + "-addr \"any\"\n");
        policyObjectDTO.setObjectFlag(true);
        if (restAddressList.size() > 0) {
            StringBuilder sb = new StringBuilder();
            for (String addr : restAddressList) {
                sb.append(",");
                sb.append(addr);
            }
            if (sb.length() > 0) {
                sb.deleteCharAt(0);
            }
            policyObjectDTO = generateAddressObject(sb.toString(), ticket, ipPrefix, createObjFlag, "");
        }

        if (existAddressList.size() > 0) {
            for (String existName : existAddressList) {
                policyObjectDTO.setJoin((policyObjectDTO.getJoin().contains("any") ? "" : policyObjectDTO.getJoin()) +"   "+ ipPrefix + "-addr \"" + existName + "\"\n");
            }
        }

        logger.info("policyObjectDTO is " + JSONObject.toJSONString(policyObjectDTO));
        return policyObjectDTO;
    }

    public PolicyObjectDTO generateAddressObject(String ipAddress, String ticket, String ipPrefix, boolean createObjFlag, String existsAddressName) {
        PolicyObjectDTO dto = new PolicyObjectDTO();

        if (AliStringUtils.isEmpty(ipAddress) || ipAddress.equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_ANY)) {
            dto.setJoin("any\n");
            dto.setName("any");
            dto.setObjectFlag(true);
            return dto;
        }

        if (StringUtils.isNotBlank(existsAddressName)) {
            dto.setObjectFlag(true);
            if (existsAddressName.indexOf("\"") < 0) {
                existsAddressName = "\"" + existsAddressName + "\"";
            }
            dto.setJoin(existsAddressName);
            dto.setName(existsAddressName);
            return dto;
        }

        dto.setObjectFlag(createObjFlag);

        String[] arr = ipAddress.split(",");
        StringBuilder sb = new StringBuilder();
        for (String address : arr) {
            // 是创建对象
            if (createObjFlag) {
                formatFullAddress(address, sb, ipPrefix, createObjFlag, dto, ticket);
                sb.append("exit\n");
                dto.setCommandLine(sb.toString());
            } else {
                //直接显示内容
                formatFullAddress(address, sb, ipPrefix, createObjFlag, dto, ticket);
                dto.setCommandLine(sb.toString());
                dto.setName(sb.toString());
            }
        }
        return dto;
    }

    private void formatFullAddress(String address, StringBuilder sb, String ipPrefix, boolean createObjFlag, PolicyObjectDTO dto, String ticket) {
        String name;
        String fullStr = "";
        if (IpUtils.isIPSegment(address)) {
            fullStr = String.format("ip %s\n", address);
            name = String.format("N%s", address);
        } else if (IpUtils.isIPRange(address)) {
            String startIp = IpUtils.getStartIpFromRange(address);
            String endIp = IpUtils.getEndIpFromRange(address);
            fullStr = String.format("range %s %s\n", startIp, endIp);
            name = String.format("R%s", address);
        } else {
            fullStr = String.format("ip %s/32\n", address);
            name = String.format("H%s", address);
        }
        dto.setName(name);
        sb.append(String.format("address \"%s\"\n", name));
        if (dto.getJoin() != null) {
            dto.setJoin(dto.getJoin() + "   " + ipPrefix + "-addr \"" + name + "\"\n");
        } else {
            dto.setJoin("   " + ipPrefix + "-addr \"" + dto.getName() + "\"\n");
        }

        if (createObjFlag) {
            sb.append(fullStr);
        } else {
            sb.append(ipPrefix + "-" + fullStr);
        }
    }


    //协议没有端口时，衔接
    private String getServiceNameByNoPort(ServiceDTO service) {
        String command = "";
        int protocolNum = Integer.valueOf(service.getProtocol());
        String protocolString = ProtocolUtils.getProtocolByValue(protocolNum).toUpperCase();
        if (protocolString.equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_ANY)) {
            command = "any";
        } else if (protocolString.equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_TCP) ||
                protocolString.equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_UDP)) {
            if (StringUtils.isBlank(service.getDstPorts()) || service.getDstPorts().equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_ANY)) {
                command = protocolString + "-ANY";
            }
        } else if (protocolString.equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_ICMP)) {
            command = protocolString ;
        }
        return command;
    }

    private PolicyObjectDTO generateTimeObject(String startTime, String endTime, String ticket) {
        if (startTime == null) {
            return null;
        }
        PolicyObjectDTO dto = new PolicyObjectDTO();
        StringBuilder sb = new StringBuilder();
        String name = String.format("\"to%s\"",
                TimeUtils.transformDateFormat(endTime, TimeUtils.EUROPEAN_TIME_FORMAT, TimeUtils.COMMON_TIME_DAY_FORMAT));
        sb.append(String.format("schedule %s\n", name));
        sb.append(String.format("absolute start %s end %s\n", formatTimeString(startTime), formatTimeString(endTime)));
        sb.append("exit\n");

        dto.setName(name);
        dto.setCommandLine(sb.toString());
        return dto;
    }

    private String formatTimeString(String timeString) {
        return TimeUtils.transformDateFormat(timeString, TimeUtils.EUROPEAN_TIME_FORMAT, TimeUtils.HILLSTONE_TIME_FORMAT);
    }

    @Override
    public int getMaxNameLength() {
        return MAX_NAME_LENGTH;
    }

    @Override
    public String getServiceName(List<ServiceDTO> serviceDTOList) {
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
        if (name.length() > getMaxNameLength()) {
            String shortName = name.substring(0, getMaxNameLength() - 6);
            name = String.format("%s_etcsg", shortName.substring(0, shortName.lastIndexOf("_")));
        }
        return name;
    }


    public PolicyObjectDTO generateServiceObject(List<ServiceDTO> serviceDTOList, List<String> existServiceNameList, Integer idleTimeout) {
        logger.info("idleTimeout is " + idleTimeout);
        PolicyObjectDTO dto = new PolicyObjectDTO();

        StringBuilder sb = new StringBuilder();

        boolean groupFlag = false;
        //对象名称集合, 不一定会建组，建组条件：有2组及以上协议，其中有一个协议，不带端口
        List<String> serviceNameList = new ArrayList<>();

        //直接写内容，当端口是any时，可以直接写内容，但有具体端口时，就必须创建对象
//        if (CollectionUtils.isNotEmpty(serviceDTOList) && existServiceNameList.size() == 0) {
//            //无端口时，有返回值，  有端口就需要建对象，是没有返回值的
//            dto.setJoin("");
//            for (ServiceDTO serviceDTO: serviceDTOList ) {
//                String command = getServiceNameByNoPort(serviceDTO);
//                if (StringUtils.isNotBlank(command)) {
//                    dto.setObjectFlag(true);
//                    dto.setJoin(dto.getJoin()+String.format("   service \"%s\"\n", command));
//
//                }
//            }
//
//            return dto;
//        }

        //多个服务，必须建对象或组
        boolean createObjFlag = true;
        groupFlag = true;
        dto.setObjectFlag(createObjFlag);

        if (existServiceNameList.size() > 0) {
            for (String existServiceName : existServiceNameList) {
                serviceNameList.add(existServiceName);
            }
        }


        //多个，建对象
        for (ServiceDTO service : serviceDTOList) {
            int protocolNum = Integer.valueOf(service.getProtocol());
            String protocolString = ProtocolUtils.getProtocolByValue(protocolNum);

            if (protocolString.equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_ANY)) {
                dto.setJoin("   service \"Any\" \n");
                dto.setCommandLine("");
                return dto;
            }


            if (StringUtils.isEmpty(service.getDstPorts()) || service.getDstPorts().equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_ANY)) {
                String command = getServiceNameByNoPort(service);
                if (StringUtils.isNotBlank(command)) {
                    groupFlag = true;
                    serviceNameList.add(command);
                    continue;
                }
            }

            String name = String.format("%s:%s", protocolString.toUpperCase(), service.getDstPorts());
            if (idleTimeout != null) {
                name = String.format("%s:%sL", protocolString.toUpperCase(), service.getDstPorts());
            }
            if (PortUtils.isPortRange(service.getDstPorts())) {
                String start = PortUtils.getStartPort(service.getDstPorts());
                String end = PortUtils.getEndPort(service.getDstPorts());
                name = String.format("%s:%s-%s", protocolString.toUpperCase(), start, end);
                if (idleTimeout != null) {
                    name = String.format("\"%s:%s-%sL\"", protocolString.toUpperCase(), start, end);
                }
            }
            serviceNameList.add(name);
            sb.append(String.format("service \"%s\"", name));
            sb.append("\n");
            dto.setName(name);

            //定义对象有多种情况
            if (protocolString.equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_TCP)
                    || protocolString.equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_UDP)) {


                String[] dstPorts = service.getDstPorts().split(",");
                //源、目的都有具体的值，或源不为空，目的为空
                /*if(srcPorts != null && srcPorts.length >0) {
                    for(String srcPort: srcPorts) {
                        for(String dstPort: dstPorts) {
                            sb.append(String.format("%s dst-port %s src-port %s\n", protocolString, PortUtils.getPortString(dstPort, PortUtils.BLANK_FORMAT), PortUtils.getPortString(srcPort, PortUtils.BLANK_FORMAT)));
                        }
                    }
                }*/
                //当协议为tcp/udp协议，源端口为any，目的端口为具体值,源端口不显示
                for (String dstPort : dstPorts) {
                    sb.append(String.format("%s dst-port %s", protocolString.toLowerCase(), PortUtils.getPortString(dstPort, PortUtils.BLANK_FORMAT)));
                    if (idleTimeout != null) {
                        int day = idleTimeout / DAY_SECOND;
                        if ((idleTimeout % DAY_SECOND) > 0) {
                            day = day + 1;
                        }
                        sb.append(String.format(" timeout-day %d", day));
                    }
                    sb.append("\n");
                }
            } else if (protocolString.equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_ICMP)) {
                //icmpType为空的话，默认为icmp type 3，
                if (StringUtils.isBlank(service.getType()) || !allowType.contains(service.getType())) {
                    sb.append("icmp type 3\n");
                } else if (StringUtils.isNotBlank(service.getType()) && allowType.contains(service.getType())) {
                    //icmpType不为空的话，若icmpType为3,4,5,8,11,12,13,15，则正常生成icmp type 和 code信息， 否则设定为icmp type 3
                    //有code增加code，没有code则为空字符串
                    sb.append(String.format("icmp type %d %s\n", service.getType(), service.getCode() == null ? "" : String.format("code %d", Integer.valueOf(service.getCode()))));
                }
            }
            sb.append("exit\n");
        }


        //要建组, 用多个服务替代
        if (groupFlag) {
            StringBuilder joinSb = new StringBuilder();
            for (String objName : serviceNameList) {
                if (objName.indexOf("\"") < 0) {
                    objName = "\"" + objName + "\"";
                }
                joinSb.append(String.format("   service %s\n", objName));
            }
            dto.setJoin(joinSb.toString());
        } else {
            dto.setJoin("   service " + dto.getName() + "\n");
        }


        dto.setCommandLine(sb.toString());
        return dto;
    }

    public static void main(String[] args) {
        CommandlineDTO dto = CommandlineDTO.getInstanceDemo();
        dto.setSrcIp("1.3.2.3,15.3.5.6");
        dto.setDstIp("");
        List<ServiceDTO> serviceDTOS = dto.getRestServiceList();
        serviceDTOS.clear();
        ServiceDTO serviceDTO = new ServiceDTO();
        serviceDTO.setProtocol("6");
        serviceDTOS.add(serviceDTO);
        ServiceDTO serviceDTO1 = new ServiceDTO();
        serviceDTO1.setProtocol("17");
        serviceDTOS.add(serviceDTO1);
        ServiceDTO serviceDTO2 = new ServiceDTO();
        serviceDTO2.setProtocol("1");
        serviceDTOS.add(serviceDTO2);
        SecurityHillStoneTp hillStoneR5 = new SecurityHillStoneTp();
        String commandLine = hillStoneR5.composite(dto);
        System.out.println("commandline:\n" + commandLine);
    }
}
