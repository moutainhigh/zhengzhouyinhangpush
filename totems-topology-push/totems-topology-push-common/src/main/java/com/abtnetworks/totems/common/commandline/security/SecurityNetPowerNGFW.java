package com.abtnetworks.totems.common.commandline.security;

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
import com.abtnetworks.totems.common.utils.*;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.catalina.valves.HealthCheckValve;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service

public class SecurityNetPowerNGFW extends SecurityPolicyGenerator implements PolicyGenerator {

    private static Logger logger = Logger.getLogger(SecurityNetPowerNGFW.class);


    @Override
    public String generate(CmdDTO cmdDTO) {
        log.info("cmdDTO is " + JSONObject.toJSONString(cmdDTO, true));
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
        dto.setCreateObjFlag(settingDTO.isCreateObject());
        dto.setSwapRuleNameId(settingDTO.getSwapNameId());

        TaskDTO taskDTO = cmdDTO.getTask();
        dto.setName(taskDTO.getTheme());
        dto.setBusinessName(taskDTO.getTheme());
        dto.setCiscoInterfaceCreate(settingDTO.isCreateCiscoItfRuleList());
        dto.setCiscoInterfacePolicyName(settingDTO.getCiscoItfRuleListName());
        dto.setCreateObjFlag(settingDTO.isCreateObject());
        dto.setOutBound(settingDTO.isOutBound());

        ExistObjectDTO existObjectDTO = cmdDTO.getExistObject();
        dto.setSrcAddressName(existObjectDTO.getSrcAddressObjectName());
        dto.setDstAddressName(existObjectDTO.getDstAddressObjectName());
        dto.setServiceName(existObjectDTO.getServiceObjectName());
        dto.setExistServiceNameList(existObjectDTO.getExistServiceNameList());
        dto.setRestServiceList(existObjectDTO.getRestServiceList());

        log.info("dto is" + JSONObject.toJSONString(dto, true));
        String commandLine = composite(dto);

        GeneratedObjectDTO generatedDto = cmdDTO.getGeneratedObject();
        //思科特殊处理，在进行回滚时，使用的是整个策略，而不是名称
        generatedDto.setPolicyName(commandLine);

        return commandLine;
    }

    @Override
    public String generatePreCommandline(CommandlineDTO dto) {
        return "";
    }

    @Override
    public String generateCommandline(CommandlineDTO dto) {

        boolean createObjFlag = dto.isCreateObjFlag();
        String ticket = dto.getName();

        //创建服务、地址对象
        PolicyObjectDTO srcAddress = generateAddressObject(dto.getSrcIp(), ticket, createObjFlag, dto.getSrcAddressName(), dto.getSrcIpSystem());
        PolicyObjectDTO dstAddress = generateAddressObject(dto.getDstIp(), ticket, createObjFlag, dto.getDstAddressName(), dto.getDstIpSystem());
        PolicyObjectDTO serviceObject = generateServiceObject(dto.getServiceList(), createObjFlag, dto.getServiceName());

        return commonLine(srcAddress, dstAddress, serviceObject, dto);
    }


    //提取公共, 供思科 ASA 2个版本使用
    public String commonLine(PolicyObjectDTO srcAddress, PolicyObjectDTO dstAddress, PolicyObjectDTO serviceObject,
                             CommandlineDTO dto) {



        String ticket = dto.getName();

        String startTime = dto.getStartTime();
        String endTime = dto.getEndTime();


        PolicyObjectDTO time = generateTimeObject(startTime, endTime, ticket);
        StringBuilder sb = new StringBuilder();
        sb.append("config\n");
        //定义对象
        if (srcAddress.isObjectFlag() && StringUtils.isNotBlank(srcAddress.getCommandLine())) {
            sb.append(String.format("%s\n", srcAddress.getCommandLine()));
        }
        if (dstAddress.isObjectFlag() && StringUtils.isNotBlank(dstAddress.getCommandLine())) {
            sb.append(String.format("%s\n", dstAddress.getCommandLine()));
        }
        if (serviceObject.isObjectFlag() && StringUtils.isNotBlank(serviceObject.getCommandLine())) {
            sb.append(String.format("%s\n", serviceObject.getCommandLine()));
        }
        if (time != null) {
            sb.append(String.format("%s\n", time.getCommandLine()));
        }

        String name = dto.getBusinessName();
        //对象式命令行 或，服务、源、目的地址都是建对象或复用的

        sb.append(String.format("app-security policy %s ", name));
        if(StringUtils.isNotEmpty(dto.getDescription())){
            sb.append(String.format("comment %s \n", dto.getDescription()));
        }else{
            sb.append("\n");
        }
        sb.append(String.format("element "));

        if(StringUtils.isNotEmpty(dto.getSrcZone())){
            sb.append(String.format("src-zone %s ", dto.getSrcZone()));
        }
        if(StringUtils.isNotEmpty(dto.getDstZone())){
            sb.append(String.format("dst-zone %s ", dto.getDstZone()));
        }
        if(StringUtils.isNotEmpty(srcAddress.getName())){
            sb.append(String.format("src-ip object %s ", srcAddress.getName()));
        }
        if(StringUtils.isNotEmpty(dstAddress.getName())){
            sb.append(String.format("dst-ip object %s ",  dstAddress.getName()));
        }

        if(StringUtils.isNotEmpty(serviceObject.getName())){
            sb.append(String.format("protocol-object %s ", serviceObject.getName()));
        }


        if (time != null) {
            sb.append(time.getJoin());
        }
        if (StringUtils.isNotEmpty( dto.getAction())){
            if( dto.getAction().toLowerCase().equals("permit")) {

                sb.append(String.format("action %s\n", dto.getAction().toLowerCase().replaceAll("permit", "accept")));
            } else {
                sb.append(String.format("action %s\n", dto.getAction().toLowerCase().replaceAll("deny", "drop")));
            }
        }
        sb.append("return\n");


        //移动位置，

        String swapRuleNameId = StringUtils.isNotBlank(dto.getSwapRuleNameId()) ? dto.getSwapRuleNameId() : "";
        int moveSeatCode = dto.getMoveSeatEnum().getCode();
        if (moveSeatCode == MoveSeatEnum.FIRST.getCode()) {
            //置顶
            sb.append(String.format("app-security-move %s in-front-of position 1\n", name));

        } else if (moveSeatCode == MoveSeatEnum.BEFORE.getCode()) {
            if (StringUtils.isNotEmpty(swapRuleNameId)) {
                //
                if (StringUtils.isNumeric(swapRuleNameId)) {
                    sb.append(String.format("app-security-move %s in-front-of position  %s\n", name, swapRuleNameId));
                } else {
                    sb.append(String.format("app-security-move %s in-front-of app-security %s\n", name, swapRuleNameId));
                }

            } else {
                sb.append("\n");
            }

        } else if (moveSeatCode == MoveSeatEnum.AFTER.getCode()) {
            if (StringUtils.isNotEmpty(swapRuleNameId)) {
                if (StringUtils.isNumeric(swapRuleNameId)) {
                    //如果是数字就是策略id
                    sb.append(String.format("app-security-move %s behind position  %s\n", name, swapRuleNameId));
                } else {
                    //就是名字
                    sb.append(String.format("app-security-move %s behind app-security %s\n", name, swapRuleNameId));
                }

            } else {
                sb.append("\n");
            }
        }

        sb.append("end\n");

        return sb.toString();
    }

    @Override
    public String generatePostCommandline(CommandlineDTO dto) {
        return "";
    }

    public PolicyObjectDTO generateAddressObject(String ipAddress, String ticket, boolean createObjFlag, String existsAddressName, String ipSystem) {
        PolicyObjectDTO dto = new PolicyObjectDTO();
        if (AliStringUtils.isEmpty(ipAddress)) {
            dto.setObjectFlag(true);
            dto.setJoin("any");
            dto.setName("");
            return dto;
        }

        if (StringUtils.isNotBlank(existsAddressName)) {
            dto.setJoin(existsAddressName + " ");
            dto.setName(existsAddressName);
            return dto;
        }

        dto.setObjectFlag(createObjFlag);
        String[] arr = ipAddress.split(",");

        StringBuilder sb = new StringBuilder();
        // 是创建对象

        String addressCmd = "";
        String objName;
        if (StringUtils.isNotEmpty(ipSystem)) {
            objName = ipSystem;
        } else {
            objName = String.format("%s_AG_%s", ticket, IdGen.getRandomNumberString());
        }
        StringBuilder sbGroup = new StringBuilder();

        if (arr.length > 1) {
            sbGroup.append(String.format("object address-group %s \n", objName));


        }
        for (String address : arr) {
            String setName = String.format("%s_AO_%s", ticket, IdGen.getRandomNumberString());
            if (IpUtils.isIPRange(address)) {
                String startIp = IpUtils.getStartIpFromRange(address);
                String endIp = IpUtils.getEndIpFromRange(address);
                addressCmd = String.format("object address %s range %s %s\n", setName, startIp, endIp);
            } else if (IpUtils.isIPSegment(address)) {
                String startIp = IpUtils.getIpFromIpSegment(address);
                String mark = IpUtils.getMaskBitFromIpSegment(address);
                String endMark = IpUtils.getMaskMap(mark);
                addressCmd = String.format("object address %s net ip %s %s\n", setName, startIp, endMark);
            } else if (IpUtils.isIP(address)) {
                addressCmd = String.format("object address %s net ip %s\n", setName, address);
            }
            sb.append(addressCmd);
            if (arr.length > 1) {
                sbGroup.append(String.format("member %s\n", setName));

            } else if (arr.length == 1) {
                objName = setName;
            }


        }
        if(arr.length > 1){
            sb.append(sbGroup);
            sb.append("return\n");
        }else{

        }

        dto.setName(objName);



        //todo 一个对象时是否新建地址组对象
        dto.setCommandLine(sb.toString());
        return dto;
    }


    public PolicyObjectDTO generateServiceObject(List<ServiceDTO> serviceDTOList, boolean createObjFlag, String existsServiceName) {
        PolicyObjectDTO dto = new PolicyObjectDTO();

        if (StringUtils.isNotBlank(existsServiceName)) {
            dto.setJoin(existsServiceName + "\n");
            dto.setName(existsServiceName);
            return dto;
        }
        StringBuilder sb = new StringBuilder();
        List<String> serverNames = new ArrayList<>();

        for (ServiceDTO service : serviceDTOList) {
            int protocolNum = Integer.valueOf(service.getProtocol());
            String protocolString = ProtocolUtils.getProtocolByValue(protocolNum).toLowerCase();
            if (StringUtils.isEmpty(protocolString) || protocolString.equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_ANY)) {
                dto.setName("");
                return dto;
            }

            if (!protocolString.equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_ICMP)) {
                String dstPort = service.getDstPorts();
                if (StringUtils.isNotBlank(dstPort) && !dstPort.equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_ANY)) {
                    String[] dstPorts = dstPort.split(",");
                    for (String port : dstPorts) {


                        if (PortUtils.isPortRange(port)) {
                            String startPort = PortUtils.getStartPort(port);
                            String endPort = PortUtils.getEndPort(port);
                            String name = String.format("%s_%s_%s", protocolString, startPort, endPort);
                            sb.append(String.format("object service %s protocol %s", name, protocolString));
                            sb.append(String.format(" dport port-range %s %s\n", startPort, endPort));
                            serverNames.add(name);
                        } else {
                            String name = String.format("%s_%s", protocolString, port);
                            sb.append(String.format("object service %s protocol %s", name, protocolString));
                            sb.append(String.format(" dport %s\n", port));
                            serverNames.add(name);
                        }
                    }
                } else {
                    serverNames.add(protocolString + "_any");

                }

            } else {
                serverNames.add(protocolString.toLowerCase());
            }
        }


        if (CollectionUtils.isNotEmpty(serverNames) && serverNames.size() > 1) {
            String nameGroup = getServiceName(serviceDTOList);
            sb.append(String.format("object service-group %s \n", nameGroup));
            for (String serverName : serverNames) {
                sb.append(String.format("member %s \n", serverName));

            }
            sb.append("return\n");
            dto.setName(nameGroup);
        } else {
            dto.setName(serverNames.get(0));
        }


        dto.setCommandLine(sb.toString());
        dto.setObjectFlag(true);
        return dto;
    }


    /**
     * 生成时间对象
     *
     * @param startTime
     * @param endTime
     * @param ticket
     * @return
     */
    private PolicyObjectDTO generateTimeObject(String startTime, String endTime, String ticket) {
        PolicyObjectDTO dto = new PolicyObjectDTO();
        if (startTime == null) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        String name = String.format("%s_TR_%s", ticket, IdGen.getRandomNumberString());


        sb.append(String.format("object time %s once start %s stop %s", name, formatTimeString(startTime), formatTimeString(endTime)));

        dto.setName(name);
        dto.setCommandLine(sb.toString());
        dto.setJoin(String.format("schedule %s ", name));
        return dto;
    }

    private String formatTimeString(String timeString) {
        return TimeUtils.transformDateFormat(timeString, TimeUtils.EUROPEAN_TIME_FORMAT, TimeUtils.LEAD_TIME_FORMAT);
    }


    @Override
    public int getMaxNameLength() {
        return MAX_NAME_LENGTH;
    }

    public static void main(String[] args) {
        CommandlineDTO dto = CommandlineDTO.getInstanceDemo();
        SecurityNetPowerNGFW securityNetPowerNGFW = new SecurityNetPowerNGFW();
        dto.setSrcIp("12.3.4.7,2.3.5.6");
        dto.setDstIp("2.567.24.8,22.32.45");
        dto.setStartTime("2019-12-3 12:2:0");
        dto.setEndTime("2019-12-3 12:2:32");

        List<ServiceDTO> srcport = new ArrayList<>();
        ServiceDTO service = new ServiceDTO();
//        service.setProtocol("6");
//
//        service.setDstPorts("25,21");
        ServiceDTO service1 = new ServiceDTO();
        service1.setProtocol("17");
        service.setProtocol("6");
//        service1.setDstPorts(" ");
        dto.setServiceName("");
//        ServiceDTO service2 = new ServiceDTO();
//        service2.setProtocol("1");
        srcport.add(service);
        srcport.add(service1);
//        srcport.add(service2);
        dto.setServiceList(srcport);

//        System.out.println(IpUtils.getMaskMap("32"));
        String commandLine = securityNetPowerNGFW.composite(dto);
        System.out.println("commandline:\n" + commandLine);
    }
}
