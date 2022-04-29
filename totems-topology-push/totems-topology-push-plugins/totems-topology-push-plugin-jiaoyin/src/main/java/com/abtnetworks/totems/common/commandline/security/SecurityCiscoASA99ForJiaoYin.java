package com.abtnetworks.totems.common.commandline.security;

import com.abtnetworks.totems.common.annotation.CustomCli;
import com.abtnetworks.totems.common.commandline.PolicyGenerator;
import com.abtnetworks.totems.common.commandline.SecurityPolicyGenerator;
import com.abtnetworks.totems.common.constants.PolicyConstants;
import com.abtnetworks.totems.common.dto.*;
import com.abtnetworks.totems.common.dto.commandline.CommandlineDTO;
import com.abtnetworks.totems.common.dto.commandline.PolicyObjectDTO;
import com.abtnetworks.totems.common.dto.commandline.ServiceDTO;
import com.abtnetworks.totems.common.enums.*;
import com.abtnetworks.totems.common.utils.*;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * @desc    思科ASA9.9版本,支持域名
 * @author lps
 * @date
 */
@Slf4j
@Service
@CustomCli(value = DeviceModelNumberEnum.CISCO_ASA_99, type = PolicyEnum.SECURITY)
public class SecurityCiscoASA99ForJiaoYin extends SecurityPolicyGenerator implements PolicyGenerator {

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

        dto.setSpecialExistObject(cmdDTO.getSpecialExistObject());
        log.info("思科 ASA 8.6 dto is" + JSONObject.toJSONString(dto, true));

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
        // 默认设置IPV4
        if(ObjectUtils.isEmpty(dto.getIpType())){
            dto.setIpType(IpTypeEnum.IPV4.getCode());
        }

        ExistObjectRefDTO specialObject = dto.getSpecialExistObject();

        SecurityCiscoASAForJiaoYin securityCiscoASAForJiaoYin = new SecurityCiscoASAForJiaoYin();

        PolicyObjectDTO srcAddress = generateAddressObject(dto.getSrcIp(), ticket, createObjFlag, specialObject.getSrcAddressObjectName(), dto.getIpType(), dto.getSrcIpSystem());
        PolicyObjectDTO dstAddress = generateAddressObject(dto.getDstIp(), ticket, createObjFlag, specialObject.getDstAddressObjectName(), dto.getIpType(), dto.getDstIpSystem());
        PolicyObjectDTO serviceObject = generateServiceObject(dto.getServiceList(), createObjFlag, specialObject.getServiceObjectName());

        String commandLine = securityCiscoASAForJiaoYin.commonLine(srcAddress, dstAddress, serviceObject, dto);

        return commandLine;
    }

    @Override
    public String generatePostCommandline(CommandlineDTO dto) {
        return "";
    }

    public PolicyObjectDTO generateAddressObject(String ipAddress, String ticket, boolean createObjFlag,
                                                 RefObjectDTO refObjectDTO, Integer ipType, String ipSystem) {
        PolicyObjectDTO dto = new PolicyObjectDTO();

        //地址为空，表示any
        if (AliStringUtils.isEmpty(ipAddress)) {
            dto.setObjectFlag(true);
            dto.setJoin("any");
            return dto;
        }

        //复用对象非空，则直接复用
        if (refObjectDTO != null) {
            if (refObjectDTO.getObjectTypeEnum().equals(DeviceObjectTypeEnum.NETWORK_GROUP_OBJECT)) {
                dto.setJoin("object-group " + refObjectDTO.getRefName() + " ");
            } else {
                dto.setJoin("object " + refObjectDTO.getRefName() + " ");
            }

            dto.setName(refObjectDTO.getRefName());
            return dto;
        }

        dto.setObjectFlag(createObjFlag);
        String[] arr = ipAddress.split(",");

        StringBuilder sb = new StringBuilder();
        List<String> list = new ArrayList<>();
        String objName ;
        if(StringUtils.isNotEmpty(ipSystem)){
            objName = ipSystem;
        } else {
            objName = String.format("%s_AO_%s", ticket, IdGen.getRandomNumberString());
        }

        // 是创建对象
        if (createObjFlag && StringUtils.isNotBlank(ipSystem)) {
            //只有一个，直接创建对象，引用即可
            if (arr.length == 1) {
                sb.append(getAddressObject(arr[0], objName, null,ipType));
                dto.setJoin("object " + objName);
            } else {
                //创建对象、对象组
                boolean containsUrl = false;
                List<String> objectJoinList = new ArrayList<>();
                List<String> urlJoinList = new ArrayList<>();
                for (String ip : arr) {
                    //多个地址混合时，仅子网和范围建对象，单IP不建
                    if (IpUtils.isIPSegment(ip) || IpUtils.isIPRange(ip)) {
                        String refObjName = String.format("%s_AG_%s", ticket, IdGen.getRandomNumberString());
                        sb.append(getAddressObject(ip, refObjName, null,ipType));
                        objectJoinList.add("network object " + refObjName);
                    } else if(IpUtils.isIP(ip)){
                        objectJoinList.add("network host " + ip);
                    } else {
                        containsUrl = true;
                        sb.append(getURLAddressObject(ip, ticket,urlJoinList));
                    }
                }

                //建地址组，去引用哪些对象
                if(containsUrl){
                    sb.append(String.format("object-group network %s\n", objName));
                } else {
                    sb.append(String.format("object-group network %s\n", objName));
                }
                for (String joinStr : objectJoinList) {
                    sb.append(joinStr + "\n");
                }
                for (String joinStr : urlJoinList) {
                    sb.append(joinStr + "\n");
                }
                sb.append("exit\n");
                dto.setJoin("object-group " + objName + " ");
            }

            dto.setCommandLine(sb.toString());
            dto.setName(objName);
            sb.append(String.format("object-group network %s \n", objName));
            dto.setObjectFlag(true);
        } else {
            //直接显示内容
            for (String ip : arr) {
                getAddressObject(ip, "", list,ipType);
            }
            dto.setCommandLineList(list);
            dto.setObjectFlag(false);
        }

        return dto;
    }

    public String getAddressObject(String ipAddress, String name, List<String> list, Integer ipType) {
        StringBuilder sb = new StringBuilder();

        if (StringUtils.isNotBlank(name)) {
            sb.append(String.format("object network %s\n", name));
        }

        String fullStr = "";
        if (IpUtils.isIPRange(ipAddress)) {
            String start = IpUtils.getStartIpFromIpAddress(ipAddress);
            String end = IpUtils.getEndIpFromIpAddress(ipAddress);
            fullStr = String.format("range %s %s", start, end);
        } else if (IpUtils.isIPSegment(ipAddress)) {
            //获取ip
            String ip = IpUtils.getIpFromIpSegment(ipAddress);
            //获取网段数
            String maskBit = IpUtils.getMaskBitFromIpSegment(ipAddress);
            //获取网段的ip
            String mask = IpUtils.getMaskByMaskBit(maskBit);
            //将ip和mask转二进制后，进行与计算，得到十进制的子网段ip地址
            String ipDecimal = IpUtils.calcIpAndByBinary(IpUtils.getBinaryIp(ip), IpUtils.getBinaryIp(mask));
            fullStr = String.format("subnet %s %s", ipDecimal, mask);
        } else if(IpUtils.isIP(ipAddress)){
            fullStr = String.format("host %s", ipAddress);
        } else {
            if(ipAddress.contains(":")){
                // TODO ipv6类型
            }
            if(ipType.intValue() == IpTypeEnum.URL.getCode()){
                fullStr = String.format("fqdn %s", ipAddress);
            }
        }

        sb.append(fullStr + "\n");
        sb.append("exit\n");
        if (list != null) {
            list.add(fullStr);
        }

        return sb.toString();
    }

    public String getURLAddressObject(String ipAddress, String ticket, List<String> urlJoinList) {
        StringBuilder sb = new StringBuilder();
        String objName = String.format("%s_AO_%s", ticket, IdGen.getRandomNumberString());
        if (StringUtils.isNotBlank(objName)) {
            sb.append(String.format("object network %s\n", objName));
        }

        String fullStr = String.format("fqdn %s", ipAddress);
        String groupStr = String.format("network-object object %s", objName);
        sb.append(fullStr + "\n");
        sb.append("exit\n");
        if (urlJoinList != null) {
            urlJoinList.add(groupStr);
        }
        return sb.toString();
    }


    public PolicyObjectDTO generateServiceObject(List<ServiceDTO> serviceDTOList, boolean createObjFlag, RefObjectDTO refObjectDTO ){
        PolicyObjectDTO dto = new PolicyObjectDTO();

        if (refObjectDTO != null) {
            dto.setObjectFlag(true);
            if (refObjectDTO.getObjectTypeEnum().equals(DeviceObjectTypeEnum.SERVICE_GROUP_OBJECT)) {
                dto.setJoin(String.format("object-group %s ", refObjectDTO.getRefName()));
            } else {
                dto.setJoin(String.format("object %s ", refObjectDTO.getRefName()));
            }
            return dto;
        }

        if(!createObjFlag){
            return dto;
        }

        if(serviceDTOList.size() == 1 && serviceDTOList.get(0).getDstPorts().equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_ANY)) {
            dto.setObjectFlag(false);
            String str = ProtocolUtils.getProtocolByValue(Integer.valueOf(serviceDTOList.get(0).getProtocol()));
            dto.setJoin(str.equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_ANY)?"ip":str);
            return dto;
        }

        StringBuilder sb = new StringBuilder();

        String name = getServiceName(serviceDTOList);
        dto.setName(name);
        dto.setJoin(String.format("object-group %s ", name));
        sb.append(String.format("object-group service %s \n", name));

        for(ServiceDTO service : serviceDTOList){
            int protocolNum = Integer.valueOf(service.getProtocol());
            String protocolString = ProtocolUtils.getProtocolByValue(protocolNum).toLowerCase();
            if(protocolString.equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_ICMP)) {
                sb.append(String.format("service-object %s ", protocolString));
                if(StringUtils.isNotBlank(service.getType())){
                    sb.append(String.format("%d ", Integer.valueOf(service.getType())));
                }
                sb.append("\n");
            }else{

                if(service.getDstPorts().equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_ANY)){
                    sb.append(String.format("service-object %s\n", protocolString));
                    continue;
                }

                String[] ports = service.getDstPorts().split(",");
                for(String port : ports){
                    if(PortUtils.isPortRange(port)) {
                        sb.append(String.format("port-object %s range %s \n", protocolString, PortUtils.getPortString(port, PortUtils.BLANK_FORMAT)));
                    } else if(port.equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_ANY)){
                        //2019-03-18 修改：如果端口为any，显示保持为空
                        sb.append(" ");
                    } else {
                        sb.append(String.format("port-object %s eq %s \n", protocolString, PortUtils.getPortString(port, PortUtils.BLANK_FORMAT)));
                    }
                }
            }
        }
        sb.append("exit\n");
        dto.setCommandLine(sb.toString());
        dto.setObjectFlag(true);
        return dto;
    }


    @Override
    public int getMaxNameLength() {
        return MAX_NAME_LENGTH;
    }

    public static void main(String[] args) {
        CommandlineDTO dto = CommandlineDTO.getInstanceDemo();
        SecurityCiscoASA99ForJiaoYin cisco = new SecurityCiscoASA99ForJiaoYin();
        String commandLine = cisco.composite(dto);
        System.out.println("commandline:\n" + commandLine);
    }
}
