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
 * @Author: Administrator
 * @Date:
 */
@Slf4j
@Service
@CustomCli(value = DeviceModelNumberEnum.CISCO_ASA_86, type = PolicyEnum.SECURITY)
public class SecurityCiscoASA86ForKunShan extends SecurityPolicyGenerator implements PolicyGenerator {

    private static Logger logger = LoggerFactory.getLogger(SecurityCiscoASA86ForKunShan.class);

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
        log.info("?????? ASA 8.6 dto is" + JSONObject.toJSONString(dto, true));

        String commandLine = composite(dto);

        GeneratedObjectDTO generatedDto = cmdDTO.getGeneratedObject();
        //????????????????????????????????????????????????????????????????????????????????????
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

        SecurityCiscoASAForKunShan securityCiscoASAForKunShan = new SecurityCiscoASAForKunShan();

        PolicyObjectDTO srcAddress = generateAddressObject(dto.getSrcIp(), ticket, dto.getSrcIpSystem(), createObjFlag, specialObject.getSrcAddressObjectName());
        PolicyObjectDTO dstAddress = generateAddressObject(dto.getDstIp(), ticket, dto.getDstIpSystem(), createObjFlag, specialObject.getDstAddressObjectName());
        PolicyObjectDTO serviceObject = generateServiceObject(dto.getServiceList(), createObjFlag, specialObject.getServiceObjectName(),ticket);

        String commandLine = securityCiscoASAForKunShan.commonLine(srcAddress, dstAddress, serviceObject, dto);

        return commandLine;
    }
    public String generateCommandline(CommandlineDTO dto,Integer mergeProperty) {
        String ticket = dto.getName();
        boolean createObjFlag = dto.isCreateObjFlag();
        ExistObjectRefDTO specialObject = dto.getSpecialExistObject();
        SecurityCiscoASAForKunShan securityCiscoASAForKunShan = new SecurityCiscoASAForKunShan();
        PolicyObjectDTO srcAddress = generateAddressObject(dto.getSrcIp(), ticket,dto.getSrcIpSystem(), createObjFlag, specialObject.getSrcAddressObjectName());
        PolicyObjectDTO dstAddress = generateAddressObject(dto.getDstIp(), ticket, dto.getDstIpSystem(),createObjFlag, specialObject.getDstAddressObjectName());
        PolicyObjectDTO serviceObject = generateServiceObject(dto.getServiceList(), createObjFlag, specialObject.getServiceObjectName(),ticket);

        String commandLine = securityCiscoASAForKunShan.commonLine(srcAddress, dstAddress, serviceObject, dto,mergeProperty);

        return commandLine;
    }

    @Override
    public String generatePostCommandline(CommandlineDTO dto) {
        return "";
    }

    public PolicyObjectDTO generateAddressObject(String ipAddress, String ticket,  String ipSystem, boolean createObjFlag,
                                                 RefObjectDTO refObjectDTO) {
        PolicyObjectDTO dto = new PolicyObjectDTO();

        //?????????????????????any
        if (AliStringUtils.isEmpty(ipAddress)) {
            dto.setObjectFlag(true);
            dto.setJoin("any");
            return dto;
        }

        //????????????????????????????????????
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


        // ???????????????
        if (createObjFlag) {
            String objName = "";
            //????????????????????????????????????????????????
            if (arr.length == 1) {
                objName = getAddressName(ipAddress);
                if(StringUtils.isNotEmpty(ipSystem)){
                    objName =  String.format("%s_%s", ipSystem, IdGen.getRandomNumberString());
                }
                sb.append(getAddressObject(arr[0], objName, null));
                dto.setJoin("object " + objName);
                sb.substring(0,sb.length()-1);
            } else {
                //????????????????????????
                List<String> objectJoinList = new ArrayList<>();
                for (String ip : arr) {
                    //?????????????????????????????????????????????????????????IP??????
                    if (IpUtils.isIPSegment(ip) || IpUtils.isIPRange(ip)) {
                        objName = getAddressName(ip);
//                        String refObjName = String.format("%s_AG_%s", ticket, IdGen.getRandomNumberString());
                        sb.append(getAddressObject(ip, objName, null));
                        objectJoinList.add("network object " + objName);
                    } else {
                        objectJoinList.add("network host " + ip);
                    }

                }
                if(StringUtils.isNotEmpty(ipSystem)){
                    objName =  String.format("G_%s_%s", ipSystem, IdGen.getRandomNumberString());
                } else {
                    objName = String.format("G_%s_%s", ticket, IdGen.getRandomNumberString());
                }
                //????????????????????????????????????
                sb.append(String.format("object-group network %s\n", objName));
                for (String joinStr : objectJoinList) {
                    sb.append(joinStr + "\n");
                }
                dto.setJoin("object-group " + objName + " ");
            }
            /*sb.append(String.format("object-group network %s \n", objName));
            formatFullAddress(arr, list, sb);
            sb.append("exit\n");
            dto.setName(objName);
            dto.setCommandLine(sb.toString());
            dto.setJoin("object-group " + dto.getName() + " ");
            dto.setObjectFlag(true);*/
            sb.substring(0,sb.length()-1);
            sb.append("exit\n");
            dto.setCommandLine(sb.toString());
            dto.setName(objName);
            sb.append(String.format("object-group network %s \n", objName));
            dto.setObjectFlag(true);

        } else {
            //??????????????????
            for (String ip : arr) {
                getAddressObject(ip, "", list);
            }
            dto.setCommandLineList(list);
            dto.setObjectFlag(false);
        }

        return dto;
    }
    private String getAddressName(String ipAddress) {
        String name = "";
        if (ipAddress != null && !ipAddress.contains("/") && !ipAddress.contains("-")) {
            name = "H_" + ipAddress;
        } else if (name != null && ipAddress.contains("/")) {
            String arr2[] = ipAddress.split("/");
            name = "N_" + arr2[0]+"_"+arr2[1];
        }else {
            name = "N_" + ipAddress;
        }
        return name;
    }
    public void formatFullAddress(String[] arr, List<String> list, StringBuilder sb) {
        for (String address : arr) {
            String fullStr = "";
            boolean ipSegment = IpUtils.isIPSegment(address);
            if (IpUtils.isIPSegment(address)) {
                //??????ip
                String ip = IpUtils.getIpFromIpSegment(address);
                //???????????????
                String maskBit = IpUtils.getMaskBitFromIpSegment(address);
                //???????????????ip
                String mask = IpUtils.getMaskByMaskBit(maskBit);
                //???ip???mask???????????????????????????????????????????????????????????????ip??????
                String ipDecimal = IpUtils.calcIpAndByBinary(IpUtils.getBinaryIp(ip), IpUtils.getBinaryIp(mask));
//                fullStr = String.format(" %s %s ", ipDecimal, mask);
//                sb.append(String.format("network-object %s \n", fullStr));
                if (StringUtils.isNotBlank(mask) && address.equalsIgnoreCase("32")) {
                    sb.append(String.format("address H_%s \n", address));
                }else {
                    sb.append(String.format("address N_%s \n", address));
                }
                sb.append(String.format("ip %s\n", address));
                sb.append("exit\n");
                list.add(fullStr);
            } else if (IpUtils.isIPRange(address)) {
                String startIp = IpUtils.getStartIpFromRange(address);
                String endIp = IpUtils.getEndIpFromRange(address);

                sb.append(String.format("address N_%s \n", address));
                sb.append(String.format(" range %s %s \n", startIp,endIp));
                sb.append(String.format("exit \n"));
                //???????????????????????????????????????
//                String[] startIpArr = startIp.split("\\.");
//                String[] endIpArr = endIp.split("\\.");
//                Integer startIp_lastNum = Integer.valueOf(startIpArr[3]);
//                Integer endIp_lastNum = Integer.valueOf(endIpArr[3]);
//                for (int i = startIp_lastNum; i <= endIp_lastNum; i++) {
//                    fullStr = String.format(" host %s.%s.%s.%s", startIpArr[0], startIpArr[1], startIpArr[2], i);
//                    sb.append(String.format("network-object %s \n", fullStr));
//                    list.add(fullStr);
//                }
            } else {
                sb.append(String.format("address H_%s \n", address));
                sb.append(String.format(" ip %s/32 \n", address));
                sb.append(String.format("exit \n"));
            }
        }
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
            //??????ip
            String ip = IpUtils.getIpFromIpSegment(ipAddress);
            //???????????????
            String maskBit = IpUtils.getMaskBitFromIpSegment(ipAddress);
            //???????????????ip
            String mask = IpUtils.getMaskByMaskBit(maskBit);
            //???ip???mask???????????????????????????????????????????????????????????????ip??????
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


    public PolicyObjectDTO generateServiceObject(List<ServiceDTO> serviceDTOList, boolean createObjFlag, RefObjectDTO refObjectDTO, String ticket){
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
        if(serviceDTOList.size()==1) {
            String[] ports = serviceDTOList.get(0).getDstPorts().split(",");
            if (ports.length == 1) {
                dto.setJoin(String.format("object %s ", name));
                sb.append(String.format("object service %s \n", name));
            }else {
                name =  String.format("SG_%s_%s", ticket, IdGen.getRandomNumberString());
                dto.setJoin(String.format("object-group %s ", name));
                sb.append(String.format("object-group service %s \n", name));
            }
        }else {
            name =  String.format("SG_%s_%s", ticket, IdGen.getRandomNumberString());
            dto.setJoin(String.format("object-group %s ", name));
            sb.append(String.format("object-group service %s \n", name));
        }

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
                        //2019-03-18 ????????????????????????any?????????????????????
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
        restSrc.add("1.1.1.1");
        restSrc.add("1.1.1.1/24");
        restSrc.add("1.1.1.1-1.1.1.100");
//        restSrc.add("1.3.2.55");
        dto.setRestSrcAddressList(restSrc);
        List<String> restDst = new ArrayList<>();
        restDst.add("2.2.2.2");
        restDst.add("2.2.2.2/24");
        restDst.add("2.2.2.2-2.2.2.100");
//        restDst.add("10.80.32.14-10.80.32.23");
        dto.setRestDstAddressList(restDst);
        List<ServiceDTO> serviceList = new ArrayList<>();
        ServiceDTO serviceDTO = new ServiceDTO();
        serviceDTO.setProtocol("6");
        serviceDTO.setDstPorts("6812");
        serviceDTO.setDstPorts("6817");
        serviceDTO.setSrcPorts("");
        serviceList.add(serviceDTO);
        ServiceDTO serviceDTO2 = new ServiceDTO();
        serviceDTO2.setProtocol("17");
        serviceDTO2.setDstPorts("768");
        serviceDTO2.setSrcPorts("");
        serviceList.add(serviceDTO2);
        dto.setRestServiceList(serviceList);
        SecurityCiscoASA86ForKunShan cisco = new SecurityCiscoASA86ForKunShan();
        String commandLine = cisco.composite(dto);
        System.out.println("commandline:\n" + commandLine);
    }
}
