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
import com.abtnetworks.totems.common.utils.IdGen;
import com.abtnetworks.totems.common.utils.IpUtils;
import com.abtnetworks.totems.common.utils.PortUtils;
import com.abtnetworks.totems.common.utils.ProtocolUtils;
import com.abtnetworks.totems.common.utils.TimeUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * @author Administrator
 * @Title:
 * @Description: 天融信 TOS 010-020策略命令行生成
 * @date 2020/7/2
 */
@Slf4j
@Service
@CustomCli(value = DeviceModelNumberEnum.TOPSEC_TOS_010_020, type = PolicyEnum.SECURITY)
public class SecurityTopSecTp extends SecurityPolicyGenerator implements PolicyGenerator {

    private static Logger logger = Logger.getLogger(SecurityTopSecTp.class);

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

        dto.setSwapRuleNameId(settingDTO.getSwapNameId());
        dto.setIdleTimeout(policyDTO.getIdleTimeout());

        dto.setCurrentId(settingDTO.getPolicyId());
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



        PolicyObjectDTO srcAddress = generateAddressObject(srcIp, ticket, dto.getSrcAddressName());
        PolicyObjectDTO dstAddress = generateAddressObject(dstIp, ticket, dto.getDstAddressName());
        PolicyObjectDTO service = generateServiceObject(dto.getRestServiceList(), dto.getExistServiceNameList());
        PolicyObjectDTO time = generateTimeObject(startTime, endTime, ticket);

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
        if(time != null) {
            define.append(String.format("%s", time.getCommandLine()));
        }

        if(define.length() > 0) {
            sb.append("define\n");
            sb.append(define.toString());
            sb.append("end\n");
        }

        sb.append("firewall\n");

        String srcAddressString = AliStringUtils.isEmpty(srcAddress.getJoin())==true?"":String.format("src \'%s\' ", srcAddress.getJoin());
        String dstAddressString = AliStringUtils.isEmpty(dstAddress.getJoin())==true?"":String.format("dst \'%s\' ", dstAddress.getJoin());
        String serviceString = AliStringUtils.isEmpty(service.getJoin())==true?"":String.format("service \'%s\' ", service.getJoin());

        sb.append(String.format("policy add action %s %s%s" +
                        "%s%s%s", dto.getAction().equalsIgnoreCase("permit")?"accept":"deny", srcZoneJoin, dstZoneJoin, srcAddressString, dstAddressString, serviceString));
        if(time != null) {
            sb.append(String.format("schedule %s ", time.getJoin()));
        }
        if (!AliStringUtils.isEmpty(dto.getGroupName())) {
            sb.append(String.format("group-name %s ", dto.getGroupName()));
        }
        if(!AliStringUtils.isEmpty(dto.getDescription())){
            sb.append(String.format("comment \'%s\' ", dto.getDescription()));
        }

        String swapRuleNameId = StringUtils.isNotBlank(dto.getSwapRuleNameId()) ? dto.getSwapRuleNameId() : "";

        int moveSeatCode = dto.getMoveSeatEnum().getCode();
        if (moveSeatCode == MoveSeatEnum.BEFORE.getCode() ) {
            if(!AliStringUtils.isEmpty(swapRuleNameId)) {
                sb.append(String.format("before %s ", swapRuleNameId));
            }

        } else if (moveSeatCode == MoveSeatEnum.AFTER.getCode()) {


            if(!AliStringUtils.isEmpty(swapRuleNameId)) {
                sb.append("\n");
                sb.append(String.format("firewall policy move after %s\n", swapRuleNameId));

            }
        } else {
            sb.append("\n");
        }


        sb.append("\n");
        sb.append("end\n");

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

        String ticket = dto.getName();

        //正式开始编辑
        PolicyObjectDTO srcAddress = generateAddressObject(dto.getSrcIp(), ticket, dto.getSrcAddressName());
        PolicyObjectDTO dstAddress = generateAddressObject(dto.getDstIp(), ticket, dto.getDstAddressName());
        PolicyObjectDTO service = generateServiceObject(dto.getServiceList(), dto.getServiceName());

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

    public PolicyObjectDTO generateAddressObject(String ipAddress, String ticket, String existsAddressName) {
        PolicyObjectDTO dto = new PolicyObjectDTO();
        dto.setObjectFlag(true);
        if(StringUtils.isNotBlank(existsAddressName)){
            dto.setJoin(existsAddressName);
            return dto;
        }

        if(AliStringUtils.isEmpty(ipAddress) && AliStringUtils.isEmpty(existsAddressName)) {
            dto.setJoin("");
            return dto;
        }
        StringBuilder sb = new StringBuilder();

        //连接字符串，如果是多个对象，则用空格区分
        StringBuilder joinStr = new StringBuilder();

        String[] arr = ipAddress.split(",");
        for (String address : arr) {
            String name ;
            if(IpUtils.isIPRange(address)) {
                String startIp = IpUtils.getStartIpFromRange(address);
                String endIp = IpUtils.getEndIpFromRange(address);
                name =  String.format("R%s",address);
                sb.append(String.format("range add name %s ip1 %s ip2 %s\n", name, startIp, endIp));
            } else if (IpUtils.isIPSegment(address)) {
                String ip = IpUtils.getIpFromIpSegment(address);
                String maskBit = IpUtils.getMaskBitFromIpSegment(address);
                String pointIp = IpUtils.getMaskMap(maskBit);
                name =  String.format("N%s_%s",ip,maskBit);
                sb.append(String.format("subnet add name %s ipaddr %s mask %s\n", name, ip, pointIp));
            } else {
                name =  String.format("H%s",address);
                sb.append(String.format("host add name %s ipaddr %s\n", name, address));
            }
            joinStr.append(name+" ");
        }

        String join = joinStr.toString().trim();
        dto.setName(join);
        dto.setJoin(join);
        dto.setCommandLine(sb.toString());
        return dto;
    }

    private PolicyObjectDTO generateServiceObject(List<ServiceDTO> serviceDTOList, String existsServiceName ) {
        PolicyObjectDTO dto = new PolicyObjectDTO();
        dto.setObjectFlag(true);
        if(StringUtils.isNotBlank(existsServiceName)){
            dto.setJoin(existsServiceName);
            return dto;
        }

        String name = getServiceName(serviceDTOList);

        StringBuilder sb = new StringBuilder();
        for(ServiceDTO service : serviceDTOList) {
            int protocolNum = Integer.valueOf(service.getProtocol());
            String protocolString = ProtocolUtils.getProtocolByValue(protocolNum).toLowerCase();
            if(protocolString.equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_ANY)) {
                dto.setJoin(PolicyConstants.POLICY_STR_VALUE_ANY);
                return dto;
            }

            if(protocolString.equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_ICMP)) {
                sb.append(String.format("service add name %s protocol %s", name ,service.getProtocol()));
            }else{
                sb.append(String.format("service add name %s protocol %s port %s port2 %s", name ,service.getProtocol(), service.getSrcPorts(), service.getDstPorts()));
            }
            sb.append("\n");
        }

        dto.setCommandLine(sb.toString());
        dto.setName(name);
        dto.setJoin(name);

        return dto;
    }

    public PolicyObjectDTO generateServiceObject(List<ServiceDTO> serviceDTOList, List<String> existsServiceNameList ) {
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
            String name = getServiceName(service);

            nameSb.append(" ");
            nameSb.append(name);
            if (!protocolString.equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_ICMP)) {
                if (!service.getDstPorts().equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_ANY)) {
                    if (PortUtils.isPortRange(service.getDstPorts())) {
                        String start = PortUtils.getStartPort(service.getDstPorts());
                        String end = PortUtils.getEndPort(service.getDstPorts());
                        sb.append(String.format("service add name %s protocol %s port %s port2 %s", name, service.getProtocol(), start, end));
                    } else {
                        sb.append(String.format("service add name %s protocol %s port %s ", name, service.getProtocol(), service.getDstPorts()));
                    }
                }
            }
            sb.append("\n");
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
        if(startTime == null) {
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
        } catch(Exception e) {
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
        } catch(Exception e) {
            e.printStackTrace();
        }
        SimpleDateFormat dst = new SimpleDateFormat("hh:mm:ss");
        return dst.format(date);
    }

    @Override
    public String getServiceName(ServiceDTO service){
        StringBuilder nameSb = new StringBuilder();
        nameSb.append(ProtocolUtils.getProtocolByString(service.getProtocol().toLowerCase()));
        if(service.getDstPorts().equals(PolicyConstants.POLICY_STR_VALUE_ANY)) {
            //自定义服务大写
            return nameSb.toString().toUpperCase();
        } else {
            if(service.getDstPorts().equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_ANY)) {
                //自定义服务大写
                return nameSb.toString().toUpperCase();
            } else {
                nameSb.append(":");
                if (PortUtils.isPortRange(service.getDstPorts())) {
                    String start = PortUtils.getStartPort(service.getDstPorts());
                    String end = PortUtils.getEndPort(service.getDstPorts());
                    nameSb.append(start);
                    nameSb.append("-");
                    nameSb.append(end);
                } else {
                    nameSb.append(service.getDstPorts());
                }
            }
            return nameSb.toString();
        }
    }


    public static void main(String[] args) {
        CommandlineDTO dto = CommandlineDTO.getInstanceDemo();
        dto.setSrcIp("192.168.25.3/24");
        dto.setDstIp("192.168.2.2");
        SecurityTopSecTp topsec = new SecurityTopSecTp();
        String commandLine = topsec.composite(dto);
        System.out.println("commandline:\n" + commandLine);

    }
}
