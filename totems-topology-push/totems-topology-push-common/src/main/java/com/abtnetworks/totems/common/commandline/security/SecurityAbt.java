package com.abtnetworks.totems.common.commandline.security;

import com.abtnetworks.totems.common.commandline.PolicyGenerator;
import com.abtnetworks.totems.common.commandline.SecurityPolicyGenerator;
import com.abtnetworks.totems.common.constants.PolicyConstants;
import com.abtnetworks.totems.common.enums.ActionEnum;
import com.abtnetworks.totems.common.enums.MoveSeatEnum;
import com.abtnetworks.totems.common.utils.*;
import com.abtnetworks.totems.common.dto.*;
import com.abtnetworks.totems.common.dto.commandline.CommandlineDTO;
import com.abtnetworks.totems.common.dto.commandline.PolicyMergeDTO;
import com.abtnetworks.totems.common.dto.commandline.PolicyObjectDTO;
import com.abtnetworks.totems.common.dto.commandline.ServiceDTO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @Author: wenjiachang
 * @Date: 2019/1/11 15:26
 */

@Slf4j
@Service
public class SecurityAbt extends SecurityPolicyGenerator implements PolicyGenerator {

    private static Logger logger = Logger.getLogger(SecurityAbt.class);

    private static int abtId = 5000;

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
        PolicyObjectDTO srcAddressObject = generateAddressObject(dto.getSrcIp(), dto.getName(), dto.getSrcAddressName());
        PolicyObjectDTO dstAddressObject = generateAddressObject(dto.getDstIp(), dto.getName(), dto.getDstAddressName());
        PolicyObjectDTO serviceObject = generateServiceObject(dto.getServiceList(), dto.getServiceName());

        StringBuilder sb = new StringBuilder();
        sb.append("enable\n");
        sb.append("configure terminal\n");

        if (srcAddressObject.isObjectFlag() && StringUtils.isNotBlank(srcAddressObject.getCommandLine())) {
            sb.append(String.format("%s\n", srcAddressObject.getCommandLine()));
        }
        if (dstAddressObject.isObjectFlag() && StringUtils.isNotBlank(dstAddressObject.getCommandLine())) {
            sb.append(String.format("%s\n", dstAddressObject.getCommandLine()));
        }
        if (serviceObject.isObjectFlag() && StringUtils.isNotBlank(serviceObject.getCommandLine())) {
            sb.append(String.format("%s\n", serviceObject.getCommandLine()));
        }

        String srcZone = "any";
        if(StringUtils.isNotBlank(dto.getSrcItf())){
            srcZone = dto.getSrcItf();
        }

        String dstZone = "any";
        if(StringUtils.isNotBlank(dto.getDstItf())){
            dstZone = dto.getDstItf();
        }
        String action =  StringUtils.isNotBlank(dto.getAction())?dto.getAction().toLowerCase():"permit";
        sb.append(String.format("policy %s %s %s %s %s any any always %s %d\n",srcZone, dstZone,
                srcAddressObject.getJoin(), dstAddressObject.getJoin(), serviceObject.getJoin(), action, abtId));
        if(!AliStringUtils.isEmpty(dto.getDescription())){
            sb.append(String.format("description %s\n", dto.getDescription()));
        }
        String swapRuleNameId = StringUtils.isNotBlank(dto.getSwapRuleNameId()) ? dto.getSwapRuleNameId() : "";
        int moveSeatCode = dto.getMoveSeatEnum().getCode();
        if (moveSeatCode == MoveSeatEnum.BEFORE.getCode() || moveSeatCode == MoveSeatEnum.AFTER.getCode()) {
            sb.append(String.format("policy move %d %s %s\n", abtId, dto.getMoveSeatEnum().getKey(), swapRuleNameId));
        }
        sb.append("end\n");

        abtId = abtId + 1;
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

        //正式开始编辑
        PolicyObjectDTO srcAddressObject = generateAddressObject(dto.getSrcIp(), dto.getName(), dto.getSrcAddressName());
        PolicyObjectDTO dstAddressObject = generateAddressObject(dto.getDstIp(), dto.getName(), dto.getDstAddressName());
        PolicyObjectDTO serviceObject = generateServiceObject(dto.getServiceList(), dto.getServiceName());

        StringBuilder sb = new StringBuilder();
        sb.append("enable\n");
        sb.append("configure terminal\n");

        if(mergeField.equals(PolicyConstants.SRC) && srcAddressObject.isObjectFlag() && StringUtils.isNotBlank(srcAddressObject.getCommandLine())) {
            sb.append(String.format("%s\n", srcAddressObject.getCommandLine()));
        }
        if(mergeField.equals(PolicyConstants.DST) && dstAddressObject.isObjectFlag() && StringUtils.isNotBlank(dstAddressObject.getCommandLine())) {
            sb.append(String.format("%s\n", dstAddressObject.getCommandLine()));
        }
        if(mergeField.equals(PolicyConstants.SERVICE) && serviceObject.isObjectFlag() && StringUtils.isNotBlank(serviceObject.getCommandLine())) {
            sb.append(String.format("%s\n", serviceObject.getCommandLine()));
        }

        sb.append(String.format("policy  %s\n", ruleId));
        if (mergeField.equals(PolicyConstants.SRC)) {
            sb.append(String.format("source-address %s\n", srcAddressObject.getJoin()));
        } else if (mergeField.equals(PolicyConstants.DST)) {
            sb.append(String.format("dest-address %s\n", dstAddressObject.getJoin()));
        } else if (mergeField.equals(PolicyConstants.SERVICE)) {
            sb.append(String.format("service %s\n", serviceObject.getJoin()));
        }
        return sb.toString();
    }

    @Override
    public String generatePostCommandline(CommandlineDTO dto) {
        return "";
    }

    /**
     * 获取地址对象
     * @param ipAddress ip地址
     * @return 地址对象
     */
    private PolicyObjectDTO generateAddressObject(String ipAddress, String ticket, String existsAddressName) {
        logger.info("abt获取地址对象，入参ipAddress:" + ipAddress + ", existsAddressName:" + existsAddressName);
        PolicyObjectDTO dto = new PolicyObjectDTO();
        dto.setObjectFlag(true);
        if(StringUtils.isNotBlank(existsAddressName)){
            dto.setJoin(existsAddressName);
            return dto;
        }

        if(StringUtils.isBlank(ipAddress)){
            //为空，则填充any，不建对象
            dto.setObjectFlag(false);
            dto.setJoin("any");
            return dto;
        }

        String addressCmd = "";

        StringBuilder sb = new StringBuilder();
        sb.append("address ");
        String setName = String.format("%s_AO_%s", ticket, IdGen.getRandomNumberString());
        sb.append(setName);

        sb.append("\n");

        String[] arr = ipAddress.split(",");
        for (String address : arr) {
            if (IpUtils.isIPRange(address)) {
                String startIp = IpUtils.getStartIpFromRange(address);
                String endIp = IpUtils.getEndIpFromRange(address);
                addressCmd = String.format("ip range %s %s\n", startIp, endIp);
            } else if (IpUtils.isIPSegment(address)) {
                addressCmd = String.format("ip subnet %s\n", address);
            } else {
                addressCmd = String.format("ip address %s\n", address);
            }
            sb.append(addressCmd);
        }
        sb.append("exit\n");
        dto.setName(setName);
        dto.setJoin(setName);
        dto.setCommandLine(sb.toString());
        return dto;
    }

    /**
     * 获取服务集对象文本
     * @return 服务集对象
     */
    private PolicyObjectDTO generateServiceObject(List<ServiceDTO> serviceDTOList, String existsServiceName ) {
        PolicyObjectDTO dto = new PolicyObjectDTO();
        dto.setObjectFlag(true);
        if(StringUtils.isNotBlank(existsServiceName)){
            dto.setJoin(existsServiceName);
            return dto;
        }

        StringBuilder sb = new StringBuilder();
        String setName = getServiceName(serviceDTOList);
        sb.append(String.format("service %s\n", setName));

        for(ServiceDTO service : serviceDTOList){
            int protocolNum = Integer.valueOf(service.getProtocol());
            String protocolString = ProtocolUtils.getProtocolByValue(protocolNum);

            if(protocolString.equals(PolicyConstants.POLICY_STR_VALUE_ANY)) {
                dto.setJoin(PolicyConstants.POLICY_STR_VALUE_ANY);
                return dto;
            } else if (protocolNum == Integer.valueOf(PolicyConstants.POLICY_NUM_VALUE_ICMP)) {
                sb.append("icmp type 8 code 0 255 \n");
            } else {

                String[] srcPorts = service.getSrcPorts().split(",");
                String[] dstPorts = service.getDstPorts().split(",");
                for(String dstPort : dstPorts) {
                    for(String srcPort : srcPorts) {
                        sb.append(String.format("%s ", protocolString.toLowerCase()));
                        if (dstPort.equals(PolicyConstants.POLICY_STR_VALUE_ANY)) {
                            sb.append("dst-port 0 65535 ");
                        } else if (PortUtils.isPortRange(dstPort)) {
                            String startPort = PortUtils.getStartPort(dstPort);
                            String endPort = PortUtils.getEndPort(dstPort);
                            sb.append(String.format("dst-port %s %s ", startPort, endPort));
                        } else {
                            sb.append(String.format("dst-port %s %s ", dstPort, dstPort));
                        }
                        if (srcPort.equals(PolicyConstants.POLICY_STR_VALUE_ANY)) {
                            sb.append("src-port 0 65535 ");
                        } else if (PortUtils.isPortRange(srcPort)) {
                            String startPort = PortUtils.getStartPort(srcPort);
                            String endPort = PortUtils.getEndPort(srcPort);
                            sb.append(String.format("src-port %s %s ", startPort, endPort));
                        } else {
                            sb.append(String.format("src-port %s %s ", srcPort, srcPort));
                        }
                    }

                    sb.append("\n");
                }
            }
        }
        sb.append("\n");

        dto.setName(setName);
        dto.setJoin(setName);
        dto.setCommandLine(sb.toString());
        return dto;
    }

    private String getRandomString() {
        return IdGen.randomBase62(PolicyConstants.POLICY_INT_RAMDOM_ID_LENGTH);
    }


    public static void main(String[] args) {
        CommandlineDTO dto = CommandlineDTO.getInstanceDemo();
        SecurityAbt abt = new SecurityAbt();
        String commandLine = abt.composite(dto);
        System.out.println("commandline:\n" + commandLine);

    }
}
