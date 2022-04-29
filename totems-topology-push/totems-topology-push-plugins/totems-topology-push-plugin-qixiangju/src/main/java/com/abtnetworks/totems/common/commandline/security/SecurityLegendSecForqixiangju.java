package com.abtnetworks.totems.common.commandline.security;

import com.abtnetworks.totems.common.annotation.CustomCli;
import com.abtnetworks.totems.common.commandline.PolicyGenerator;
import com.abtnetworks.totems.common.commandline.SecurityPolicyGenerator;
import com.abtnetworks.totems.common.constants.CommonConstants;
import com.abtnetworks.totems.common.constants.PolicyConstants;
import com.abtnetworks.totems.common.dto.*;
import com.abtnetworks.totems.common.dto.commandline.CommandlineDTO;
import com.abtnetworks.totems.common.dto.commandline.PolicyObjectDTO;
import com.abtnetworks.totems.common.dto.commandline.ServiceDTO;
import com.abtnetworks.totems.common.enums.ActionEnum;
import com.abtnetworks.totems.common.enums.DeviceModelNumberEnum;
import com.abtnetworks.totems.common.enums.MoveSeatEnum;
import com.abtnetworks.totems.common.enums.PolicyEnum;
import com.abtnetworks.totems.common.utils.*;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author Administrator
 * @Title:
 * @Description: 网神安全策略命令行生成
 * @date 2020/7/2
 */
@Slf4j
@Service
@CustomCli(value = DeviceModelNumberEnum.LEGEND_SEC_NSG, type = PolicyEnum.SECURITY)
public class SecurityLegendSecForqixiangju extends SecurityPolicyGenerator implements PolicyGenerator {

    private static Logger logger = LoggerFactory.getLogger(SecurityLegendSecForqixiangju.class);

    private final String IN_KEY = "in";

    private final int MAX_NAME_LENGTH = 65;

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

        dto.setCreateObjFlag(settingDTO.isCreateObject());

        ExistObjectDTO existObjectDTO = cmdDTO.getExistObject();
        dto.setSrcAddressName(existObjectDTO.getSrcAddressObjectName());
        dto.setDstAddressName(existObjectDTO.getDstAddressObjectName());
        dto.setServiceName(existObjectDTO.getServiceObjectName());
        dto.setExistSrcAddressList(existObjectDTO.getExistSrcAddressList());
        dto.setExistDstAddressList(existObjectDTO.getExistDstAddressList());
        dto.setExistServiceNameList(existObjectDTO.getExistServiceNameList());
        dto.setRestServiceList(existObjectDTO.getRestServiceList());

        log.info("dto is" + JSONObject.toJSONString(dto, true));
        String commandLine = composite(dto);
        GeneratedObjectDTO generatedDto = cmdDTO.getGeneratedObject();
        generatedDto.setPolicyName(dto.getName());
        return commandLine;
    }

    @Override
    public String generatePreCommandline(CommandlineDTO dto) {
        StringBuilder sb = new StringBuilder();
        sb.append("config terminal\n");
        if (dto.isVsys()) {
            sb.append("vsys change " + dto.getVsysName() + "\n");
            sb.append("config terminal\n");
        }
        return sb.toString();
    }

    @Override
    public String generateCommandline(CommandlineDTO dto) {

        boolean createObjFlag = dto.isCreateObjFlag();
        String ticket = dto.getName();
        boolean isIpV6 = false;
        if (IpUtils.isIPv6(dto.getSrcIp()) || IpUtils.isIPv6(dto.getDstIp())) {
            isIpV6 = true;
        }
        //创建服务、地址对象
        PolicyObjectDTO srcAddress = generateAddressObject(dto.getSrcIp(), ticket, createObjFlag, dto.getSrcAddressName(), "sip");
        PolicyObjectDTO dstAddress = generateAddressObject(dto.getDstIp(), ticket, createObjFlag, dto.getDstAddressName(), "dip");
        PolicyObjectDTO serviceObject = generateServiceObject(dto.getRestServiceList(), createObjFlag, isIpV6, dto.getExistServiceNameList());
        return commonLine(srcAddress, dstAddress, serviceObject, dto);
    }


    public String commonLine(PolicyObjectDTO srcAddress, PolicyObjectDTO dstAddress, PolicyObjectDTO serviceObject,
                             CommandlineDTO dto) {

        String name = dto.getBusinessName();

        String ticket = dto.getName();

        String startTime = dto.getStartTime();
        String endTime = dto.getEndTime();
        Integer idleTimeout = dto.getIdleTimeout();

        boolean createObjFlag = dto.isCreateObjFlag();
        PolicyObjectDTO time = generateTimeObject(startTime, endTime, ticket);

        StringBuilder sb = new StringBuilder();

        //定义对象
        if (createObjFlag && StringUtils.isNotBlank(srcAddress.getCommandLine())) {
            sb.append(String.format("%s\n", srcAddress.getCommandLine()));
        }
        if (createObjFlag && StringUtils.isNotBlank(dstAddress.getCommandLine())) {
            sb.append(String.format("%s\n", dstAddress.getCommandLine()));
        }
        if (createObjFlag && StringUtils.isNotBlank(serviceObject.getCommandLine())) {
            sb.append(String.format("%s\n", serviceObject.getCommandLine()));
        }
        if (time != null) {
            sb.append(String.format("%s\n", time.getCommandLine()));
        }


        boolean isAllAny = CommonConstants.ANY.equalsIgnoreCase(srcAddress.getJoin()) && CommonConstants.ANY.equalsIgnoreCase(dstAddress.getJoin())
                && CommonConstants.ANY.equalsIgnoreCase(serviceObject.getJoin()) && CommonConstants.ANY.equalsIgnoreCase(dto.getSrcZone())
                && CommonConstants.ANY.equalsIgnoreCase(dto.getDstZone()) && CommonConstants.DENY.equalsIgnoreCase(dto.getAction());
        String setName = String.format("%s_policy ", ticket);

        sb.append("security policy ");

        if (isAllAny) {
            sb.append(setName).append(" sip ").append(srcAddress.getJoin()).append("dip ").append(dstAddress.getJoin())
                    .append(" szone ").append(CommonConstants.ANY).append(" dzone ").append(CommonConstants.ANY)
                    .append(" service ").append(serviceObject.getJoin()).append("action ").append(dto.getAction().toLowerCase())
                    .append(" enable ").append(CommonConstants.LINE_BREAK);


        } else {
            sb.append(setName).append(" sip  ").append(srcAddress.getJoin()).append(" dip ").append(dstAddress.getJoin());
            if (StringUtils.isNotBlank(dto.getSrcZone())) {
                sb.append(" szone ").append(dto.getSrcZone());
            } else {
                sb.append(" szone ").append(CommonConstants.ANY);
            }
            if (StringUtils.isNotBlank(dto.getDstZone())) {
                sb.append(" dzone ").append(dto.getDstZone());
            } else {
                sb.append(" dzone ").append(CommonConstants.ANY);
            }



            if (StringUtils.isNotEmpty(serviceObject.getJoin())) {
                sb.append(" service ");
                sb.append(serviceObject.getJoin());
            }

            sb.append(" action ").append(dto.getAction().toLowerCase())
                    .append(" enable ").append(CommonConstants.LINE_BREAK);
        }


        if (time != null) {
            sb.append("security policy ").append(setName).append(" schedule ").append(time.getJoin()).append(CommonConstants.LINE_BREAK);
        }

        String swapRuleNameId = StringUtils.isNotBlank(dto.getSwapRuleNameId()) ? dto.getSwapRuleNameId() : "";
        int moveSeatCode = dto.getMoveSeatEnum().getCode();
        if (moveSeatCode == MoveSeatEnum.FIRST.getCode()) {
            sb.append("security policy ").append(setName).append(" top\n");
        } else if (moveSeatCode == MoveSeatEnum.BEFORE.getCode()) {
            if(StringUtils.isNotEmpty(swapRuleNameId)){
                sb.append("security policy ").append(String.format("%s %s  %s\n", setName, dto.getMoveSeatEnum().getKey(), swapRuleNameId));
            }
        } else if (moveSeatCode == MoveSeatEnum.AFTER.getCode()) {
            if(StringUtils.isNotEmpty(swapRuleNameId)){
                sb.append("security policy ").append(String.format("%s %s %s\n", setName, dto.getMoveSeatEnum().getKey(), swapRuleNameId));
            }
        } else {
            sb.append("\n");
        }


        if (idleTimeout != null) {
            sb.append(String.format("security policy %s long access enable\n", name));
        }

        sb.append("end\n");
        sb.append("save config\n");


        return sb.toString();
    }

    @Override
    public String generatePostCommandline(CommandlineDTO dto) {
        return "";
    }

    public PolicyObjectDTO generateAddressObject(String ipAddress, String ticket, boolean createObjFlag, String existsAddressName, String ipPrefix) {
        PolicyObjectDTO dto = new PolicyObjectDTO();
        if (AliStringUtils.isEmpty(ipAddress)) {

            dto.setJoin("any");
            return dto;
        }
        dto.setObjectFlag(true);
        if (StringUtils.isNotBlank(existsAddressName)) {
            dto.setJoin(existsAddressName);
            dto.setName(existsAddressName);
            return dto;
        }
        String join = "";
        if (AliStringUtils.isEmpty(ipAddress)) {
            dto.setJoin(join);
            dto.setCommandLine("");
            return dto;
        }
        boolean isIpV6 = false;
        //若为IPv6地址，
        if (IpUtils.isIPv6(ipAddress)) {
            isIpV6 = true;
        }


        String[] arr = ipAddress.split(",");
        StringBuilder sb = new StringBuilder();
        String setName = String.format("%s_AO_%s ", ticket, IdGen.getRandomNumberString());

        join = ipPrefix + "_" + setName + " ";
        String addressCmd = "";
        if (isIpV6) {
            // 是创建对象

            sb.append("object address ").append(join).append(CommonConstants.LINE_BREAK);

            for (String address : arr) {
                //ipv6
                if (address.contains("/")) {
                    String[] addrArray = address.split("/");
                    if (StringUtils.isNotEmpty(addrArray[0])) {
                        addressCmd = String.format("network %s/%s\n", addrArray[0].toLowerCase(), addrArray[1]);
                    }
                } else if (address.contains("-")) {
                    String[] addrArray = address.split("-");
                    if (StringUtils.isNoneEmpty(addrArray[0], addrArray[1])) {
                        addressCmd = String.format("range %s-%s\n", addrArray[0].toLowerCase(), addrArray[1].toLowerCase());
                    }
                } else {
                    addressCmd = String.format("host %s/64\n", address.toLowerCase());
                }
                sb.append(addressCmd);
            }
        } else {
            // 是创建对象

            sb.append("object address ").append(join).append(CommonConstants.LINE_BREAK);

            for (String address : arr) {
                if (IpUtils.isIPRange(address)) {
                    String startIp = IpUtils.getStartIpFromRange(address);
                    String endIp = IpUtils.getEndIpFromRange(address);
                    addressCmd = String.format("range %s-%s\n", startIp, endIp);
                    sb.append(addressCmd);
                } else if (IpUtils.isIPSegment(address)) {
                    String ip = IpUtils.getIpFromIpSegment(address);
                    String maskBit = IpUtils.getMaskBitFromIpSegment(address);
                    addressCmd = String.format("network %s %s\n", ip, maskBit);
                    sb.append(addressCmd);
                } else {
                    sb.append("host ").append(address).append(CommonConstants.LINE_BREAK);
                }
            }

        }
        sb.append("exit").append(CommonConstants.LINE_BREAK);
        dto.setCommandLine(sb.toString());
        dto.setJoin(join);
        return dto;
    }


    public PolicyObjectDTO generateServiceObject(List<ServiceDTO> serviceDTOList, boolean createObjFlag, boolean isIpv6, List<String> existsServiceNames) {
        PolicyObjectDTO dto = new PolicyObjectDTO();
        StringBuilder sb = new StringBuilder();
        dto.setObjectFlag(true);
        String join = "";
        StringBuffer nameGroup = new StringBuffer();
        if (CollectionUtils.isEmpty(serviceDTOList)) {
            if (CollectionUtils.isNotEmpty(existsServiceNames)) {
                if (existsServiceNames.size() > 1) {
                    for (String existServiceName : existsServiceNames) {
                        nameGroup.append(existServiceName).append("_");

                    }
                    nameGroup.deleteCharAt(nameGroup.lastIndexOf("_"));

                    String serviceName = String.format("%s_group_%s", nameGroup, IdGen.getRandomNumberString());
                    sb.append("object service-group ").append(serviceName).append(CommonConstants.LINE_BREAK);
                    for (String existServiceName : existsServiceNames) {
                        sb.append(String.format("service %s\n", existServiceName));
                    }

                    join = String.format("%s", serviceName);
                    sb.append("exit\n");
                    dto.setCommandLine(sb.toString());
                } else {
                    join = String.format("%s", existsServiceNames.get(0));
                }
                dto.setJoin(join);
            } else {
                dto.setJoin("any");
            }

            return dto;
        }
        String name = getServiceName(serviceDTOList);

        dto.setName(name);
        if (!CommonConstants.ANY.equalsIgnoreCase(name)) {
            sb.append(String.format("object service custom  %s \n", name));
        }
        for (ServiceDTO service : serviceDTOList) {
            int protocolNum = Integer.valueOf(service.getProtocol());
            String srcPorts1 = service.getSrcPorts();
            String dstPorts1 = service.getDstPorts();
            if (StringUtils.isEmpty(srcPorts1)) {
                srcPorts1 = PolicyConstants.POLICY_STR_VALUE_ANY;
            }
            if (StringUtils.isEmpty(dstPorts1)) {
                dstPorts1 = PolicyConstants.POLICY_STR_VALUE_ANY;
            }
            String protocolString = ProtocolUtils.getProtocolByValue(protocolNum).toLowerCase();
            if (protocolString.equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_ANY)) {
                dto.setJoin(PolicyConstants.POLICY_STR_VALUE_ANY);
                return dto;
            }
            if (protocolString.equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_ICMP) && !isIpv6) {
                sb.append("service-item icmp type 0 min-code 0 max-code 15").append(CommonConstants.LINE_BREAK);

            } else if (protocolString.equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_ICMP) && isIpv6) {
                sb.append("service-item icmpv6 type 0 min-code 0 max-code 0 ").append(CommonConstants.LINE_BREAK);
            } else if ((protocolString.equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_TCP) ||
                    protocolString.equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_UDP))) {

                String[] srcPorts = srcPorts1.split(",");
                String[] dstPorts = dstPorts1.split(",");
                //是TCP/UPD协议
                //源为any，目的端口有值，则仅显示目的端口
                if (srcPorts1.equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_ANY) && !dstPorts1.equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_ANY)) {
                    for (String dstPort : dstPorts) {
                        sb.append(String.format("service-item %s src-port 1 65535 dst-port %s %s \n", protocolString, PortUtils.getPortString(dstPort, PortUtils.BLANK_FORMAT),PortUtils.getPortString(dstPort, PortUtils.BLANK_FORMAT)));
                    }

                } else if (!srcPorts1.equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_ANY) && dstPorts1.equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_ANY)) {
                    //源端口有值，目的端口any，则仅显示源端口
                    for (String srcPort : srcPorts) {
                        sb.append(String.format("service-item %s src-port %s dst-port 1 65535 \n", protocolString, PortUtils.getPortString(srcPort, PortUtils.BLANK_FORMAT)));
                    }
                } else {
                    //源和目的端口都有具体的值、或者都为any
                    for (String srcPort : srcPorts) {
                        for (String dstPort : dstPorts) {

                            if (srcPort.equals(PolicyConstants.POLICY_STR_VALUE_ANY) && dstPort.equals(PolicyConstants.POLICY_STR_VALUE_ANY)) {
                                sb.append(String.format("service-item %s src-port 1 65535 dst-port 1 65535 \n", protocolString));
                            } else {
                                sb.append(String.format("service-item %s src-port %s dst-port %s \n", protocolString, PortUtils.getPortString(srcPort, PortUtils.BLANK_FORMAT), PortUtils.getPortString(dstPort, PortUtils.BLANK_FORMAT)));
                            }

                        }
                    }
                }

            }
        }
        int sizeTotal = 0;
        if(CollectionUtils.isNotEmpty(existsServiceNames)){
            sizeTotal = existsServiceNames.size();
        }

        boolean isCreate = StringUtils.isNotEmpty(name) && !CommonConstants.ANY.equalsIgnoreCase(name);
        if (isCreate) {
            nameGroup.append(name).append("_");
        }
        if (sizeTotal > 1) {
            String serviceName;
            //创建组
            for (String existServiceName : existsServiceNames) {
                nameGroup.append(existServiceName).append("_");
            }
            serviceName = nameGroup.deleteCharAt(nameGroup.lastIndexOf("_")).toString();
            sb.append("exit\n");
            serviceName = String.format("%s_group_%s", serviceName, IdGen.getRandomNumberString());
            sb.append("object service-group ").append(serviceName).append(CommonConstants.LINE_BREAK);
            if (isCreate) {
                sb.append(String.format("service %s\n", name));
            }

            for (String existServiceName : existsServiceNames) {
                sb.append(String.format("service %s\n", existServiceName));
            }
            join = String.format("%s", serviceName);

        } else {
            if(sizeTotal == 1 && isCreate ){
                String serviceName = name+"_"+existsServiceNames.get(0);
                sb.append("exit\n");
                serviceName = String.format("%s_group_%s", serviceName, IdGen.getRandomNumberString());
                sb.append("object service-group ").append(serviceName).append(CommonConstants.LINE_BREAK);
                sb.append(String.format("service %s\n", name));
                sb.append(String.format("service %s\n", existsServiceNames.get(0)));
                join = String.format("%s", serviceName);
            }else if(sizeTotal == 1 && !isCreate){
                join = String.format("%s", existsServiceNames.get(0));
            }else{
                join = String.format("%s", name);
            }

        }
        sb.append("exit\n");

        dto.setJoin(join);
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
        sb.append(String.format("object schedule  %s \n", name));
        String startDate = formatDateString(startTime, TimeUtils.LEGEND_DATE_FORMAT);
        String endDate = formatDateString(endTime, TimeUtils.LEGEND_DATE_FORMAT);
        sb.append("schedule-once startdate ").append(startDate).append(" starttime ").append(formatDateString(startTime, TimeUtils.LEGEND_TIME_FORMAT))
                .append(" enddate ").append(endDate).append(" endtime ").append(formatDateString(endTime, TimeUtils.LEGEND_TIME_FORMAT)).append(CommonConstants.LINE_BREAK);
        sb.append("exit\n");
        dto.setName(name);
        dto.setCommandLine(sb.toString());
        dto.setJoin(name);
        return dto;
    }

    private String formatDateString(String timeString, String format) {
        return TimeUtils.transformDateFormat(timeString, TimeUtils.EUROPEAN_TIME_FORMAT, format);
    }

    @Override
    public int getMaxNameLength() {
        return MAX_NAME_LENGTH;
    }

    public static void main(String[] args) {
        SecurityLegendSecForqixiangju securityLegendSec = new SecurityLegendSecForqixiangju();
        CommandlineDTO dto = CommandlineDTO.getInstanceDemo();
        String commandLine = securityLegendSec.composite(dto);
        System.out.println("commandline:\n" + commandLine);


    }
}
