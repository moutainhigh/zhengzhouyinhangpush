package com.abtnetworks.totems.common.commandline.security;

import com.abtnetworks.totems.common.annotation.CustomCli;
import com.abtnetworks.totems.common.commandline.PolicyGenerator;
import com.abtnetworks.totems.common.commandline.SecurityPolicyGenerator;
import com.abtnetworks.totems.common.constants.PolicyConstants;
import com.abtnetworks.totems.common.dto.*;
import com.abtnetworks.totems.common.dto.commandline.CommandlineDTO;
import com.abtnetworks.totems.common.dto.commandline.PolicyObjectDTO;
import com.abtnetworks.totems.common.dto.commandline.ServiceDTO;
import com.abtnetworks.totems.common.enums.ActionEnum;
import com.abtnetworks.totems.common.enums.DeviceModelNumberEnum;
import com.abtnetworks.totems.common.enums.DeviceObjectTypeEnum;
import com.abtnetworks.totems.common.enums.PolicyEnum;
import com.abtnetworks.totems.common.utils.*;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * @Author: wxx
 * @Date: 2021/5/20 16:38
 */
@Slf4j
@Service
@CustomCli(value = DeviceModelNumberEnum.CISCO_ASA_86, type = PolicyEnum.SECURITY)
public class SecurityCiscoASA86ForHuaRui extends SecurityPolicyGenerator implements PolicyGenerator {

    private static Logger logger = LoggerFactory.getLogger(SecurityCiscoASA86ForHuaRui.class);

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
        StringBuilder sb = new StringBuilder();

        if (dto.isVsys()) {
            sb.append("changeto context system \n");
            sb.append("changeto context " + dto.getVsysName() + "\n");
        }
        return sb.toString();
    }

    @Override
    public String generateCommandline(CommandlineDTO dto) {

        boolean createObjFlag = dto.isCreateObjFlag();
        String ticket = dto.getName();

        ExistObjectRefDTO specialObject = dto.getSpecialExistObject();

        SecurityCiscoASAForHuaRui securityCiscoASAForHuaRui = new SecurityCiscoASAForHuaRui();

        PolicyObjectDTO srcAddress = generateAddressObject(dto.getSrcIp(), ticket, createObjFlag, specialObject.getSrcAddressObjectName(), dto.getSrcIpSystem());
        PolicyObjectDTO dstAddress = generateAddressObject(dto.getDstIp(), ticket, createObjFlag, specialObject.getDstAddressObjectName(), dto.getSrcIpSystem());
        PolicyObjectDTO serviceObject = generateServiceObject(dto.getServiceList(), createObjFlag, specialObject.getServiceObjectName());
        String commandLine = securityCiscoASAForHuaRui.commonLine(srcAddress, dstAddress, serviceObject, dto);
        return commandLine;
    }
    public String generateCommandline(CommandlineDTO dto,Integer mergeProperty) {
        String ticket = dto.getName();
        boolean createObjFlag = dto.isCreateObjFlag();
        ExistObjectRefDTO specialObject = dto.getSpecialExistObject();
        SecurityCiscoASAForHuaRui securityCiscoASAForHuaRui = new SecurityCiscoASAForHuaRui();
        PolicyObjectDTO srcAddress = generateAddressObject(dto.getSrcIp(), ticket, createObjFlag, specialObject.getSrcAddressObjectName(), dto.getSrcIpSystem());
        PolicyObjectDTO dstAddress = generateAddressObject(dto.getDstIp(), ticket, createObjFlag, specialObject.getDstAddressObjectName(), dto.getSrcIpSystem());
        PolicyObjectDTO serviceObject = generateServiceObject(dto.getServiceList(), createObjFlag, specialObject.getServiceObjectName());

        String commandLine = securityCiscoASAForHuaRui.commonLine(srcAddress, dstAddress, serviceObject, dto,mergeProperty);

        return commandLine;
    }

    @Override
    public String generatePostCommandline(CommandlineDTO dto) {
        return "";
    }

    public PolicyObjectDTO generateAddressObject(String ipAddress, String ticket, boolean createObjFlag,
                                                 RefObjectDTO refObjectDTO,String ipSystem) {
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
        String objName ="";

        if(StringUtils.isNotEmpty(ipSystem)){

            objName = String.format("%s_%s", ipSystem, ipAddress);
        } else {
            if (ipAddress != null && IpUtils.isIPSegment(ipAddress)) {
                String mask = IpUtils.getMaskBitFromIpSegment(ipAddress);
                if (StringUtils.isNotBlank(mask) && mask.equalsIgnoreCase("32")) {
                    objName = "ip_" + ipAddress;
                } else {
                    String arr2[] = ipAddress.split("/");
                    objName = "net_" + arr2[0] + "/" + arr2[1];
                }

            } else if (IpUtils.isIPv6Subnet(ipAddress)) {
                objName = "net_" + ipAddress;
            } else {
                objName = "ip_" + ipAddress;
            }
        }


        // 是创建对象
        if (createObjFlag) {
            //只有一个，直接创建对象，引用即可
            if (arr.length == 1) {
                sb.append(getAddressObject(arr[0], objName, null));
                dto.setJoin("object " + objName);
                sb.substring(0,sb.length()-1);
            } else {
                //创建对象、对象组
                List<String> objectJoinList = new ArrayList<>();
                for (String ip : arr) {
                    //多个地址混合时，仅子网和范围建对象，单IP不建
                    if (IpUtils.isIPSegment(ip) || IpUtils.isIPRange(ip)) {

                        String refObjName = objName;
                        sb.append(getAddressObject(ip, refObjName, null));
                        objectJoinList.add("network object " + refObjName);
                    } else {
                        objectJoinList.add("network host " + ip);
                    }
                }

                //建地址组，去引用哪些对象
                sb.append(String.format("object-group network %s\n", objName));
                for (String joinStr : objectJoinList) {
                    sb.append(joinStr + "\n");
                }

                dto.setJoin("object-group " + objName + " ");
            }
            sb.substring(0,sb.length()-1);
            sb.append("exit\n");
            dto.setCommandLine(sb.toString());
            dto.setName(objName);
            sb.append(String.format("object-group network %s \n", objName));
            dto.setObjectFlag(true);
        } else {
            //直接显示内容
            for (String ip : arr) {
                getAddressObject(ip, "", list);
            }
            dto.setCommandLineList(list);
            dto.setObjectFlag(false);
        }

        return dto;
    }

    public String getAddressObject(String ipAddress, String name, List<String> list) {
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
        } else {
            fullStr = String.format("host %s", ipAddress);
        }

        sb.append(fullStr + "\n\n");

        if (list != null) {
            list.add(fullStr);
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

        String name = getServiceName(serviceDTOList).toUpperCase();
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
                        sb.append(String.format("service-object %s destination range %s \n", protocolString, PortUtils.getPortString(port, PortUtils.BLANK_FORMAT)));
                    } else if(port.equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_ANY)){
                        //2019-03-18 修改：如果端口为any，显示保持为空
                        sb.append(" ");
                    } else {
                        sb.append(String.format("service-object %s destination eq %s \n", protocolString, PortUtils.getPortString(port, PortUtils.BLANK_FORMAT)));
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
        List<String> restSrc = new ArrayList<>();
        dto.setRestSrcAddressList(restSrc);
        List<String> restDst = new ArrayList<>();
        dto.setRestDstAddressList(restDst);

        List<ServiceDTO> srcport=new ArrayList<>();
        ServiceDTO serviceDTO = new ServiceDTO();
        serviceDTO.setProtocol("6");
        serviceDTO.setSrcPorts("20");
        serviceDTO.setDstPorts("22-90");
        srcport.add(serviceDTO);

        dto.setServiceList(srcport);

        dto.setSrcIp("127.0.0.1/10");
        dto.setDstIp("1.1.1.1,5.5.5.5");
        dto.setSrcIpSystem("");
        dto.setDstIpSystem("");
        dto.setStartTime("2020-06-15 12:00:00");
        dto.setEndTime("2020-06-17 12:00:00");
        dto.setIpType(0);

        SecurityCiscoASA86ForHuaRui hillStoneR5 = new SecurityCiscoASA86ForHuaRui();
        String commandLine = hillStoneR5.composite(dto);
        System.out.println("commandline:\n" + commandLine);


    }
}
