package com.abtnetworks.totems.common.commandline.security;

import com.abtnetworks.totems.common.commandline.PolicyGenerator;
import com.abtnetworks.totems.common.commandline.SecurityPolicyGenerator;
import com.abtnetworks.totems.common.constants.PolicyConstants;
import com.abtnetworks.totems.common.dto.*;
import com.abtnetworks.totems.common.dto.commandline.CommandlineDTO;
import com.abtnetworks.totems.common.dto.commandline.PolicyObjectDTO;
import com.abtnetworks.totems.common.dto.commandline.ServiceDTO;
import com.abtnetworks.totems.common.enums.ActionEnum;
import com.abtnetworks.totems.common.enums.DeviceObjectTypeEnum;
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
 * @Author: wenjiachang
 * @Date: 2019/1/29 9:38
 */
@Slf4j
@Service
public class SecurityCiscoASA86 extends SecurityPolicyGenerator implements PolicyGenerator {

    private static Logger logger = LoggerFactory.getLogger(SecurityCiscoASA86.class);

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
        generatedDto.setAddressObjectNameList(dto.getAddressObjectNameList());
        generatedDto.setAddressObjectGroupNameList(dto.getAddressObjectGroupNameList());
        generatedDto.setServiceObjectNameList(dto.getServiceObjectNameList());
        generatedDto.setServiceObjectGroupNameList(dto.getServiceObjectGroupNameList());
        generatedDto.setTimeObjectNameList(dto.getTimeObjectNameList());
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

        SecurityCiscoASA securityCiscoASA = new SecurityCiscoASA();

        PolicyObjectDTO srcAddress = generateAddressObject(dto.getSrcIp(), ticket, createObjFlag, specialObject.getSrcAddressObjectName());
        PolicyObjectDTO dstAddress = generateAddressObject(dto.getDstIp(), ticket, createObjFlag, specialObject.getDstAddressObjectName());
        PolicyObjectDTO serviceObject = generateServiceObject(dto.getServiceList(), createObjFlag, specialObject.getServiceObjectName());

        // 记录创建对象名称
        recordCreateAddrAndServiceObjectName(dto, srcAddress, dstAddress, null,null);
        // 记录创建服务名称
        recordCreateServiceObjectNames(dto,serviceObject);

        String description = "";
        if(!AliStringUtils.isEmpty(dto.getDescription())){
            description = dto.getDescription();
        }

        String commandLine = securityCiscoASA.commonLine(srcAddress, dstAddress, serviceObject, dto,description);

        return commandLine;
    }

    public String createMergeCommandLine(CommandlineDTO dto, Integer mergeProperty) {
        String ticket = dto.getName();
        boolean createObjFlag = dto.isCreateObjFlag();
        ExistObjectRefDTO specialObject = dto.getSpecialExistObject();
        SecurityCiscoASA securityCiscoASA = new SecurityCiscoASA();
        PolicyObjectDTO srcAddress = generateAddressObject(dto.getSrcIp(), ticket, createObjFlag, specialObject.getSrcAddressObjectName());
        PolicyObjectDTO dstAddress = generateAddressObject(dto.getDstIp(), ticket, createObjFlag, specialObject.getDstAddressObjectName());
        PolicyObjectDTO serviceObject = generateServiceObject(dto.getServiceList(), createObjFlag, specialObject.getServiceObjectName());

        // 记录创建对象名称
        recordCreateAddrAndServiceObjectName(dto, srcAddress, dstAddress, null,null);
        // 记录创建服务名称
        recordCreateServiceObjectNames(dto,serviceObject);

        String commandLine = securityCiscoASA.commonLine(srcAddress, dstAddress, serviceObject, dto,mergeProperty);

        return commandLine;
    }

    @Override
    public String generatePostCommandline(CommandlineDTO dto) {
        return "";
    }

    public PolicyObjectDTO generateAddressObject(String ipAddress, String ticket, boolean createObjFlag,
                                                 RefObjectDTO refObjectDTO) {
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
        List<String>  createObjectGroupName = new ArrayList<>();
        List<String>  createObjectName = new ArrayList<>();
        dto.setObjectFlag(createObjFlag);
        String[] arr = ipAddress.split(",");

        StringBuilder sb = new StringBuilder();
        List<String> list = new ArrayList<>();
        String objName = String.format("%s_AO_%s", ticket, IdGen.getRandomNumberString());

        // 是创建对象
        if (createObjFlag) {
            //只有一个，直接创建对象，引用即可
            if (arr.length == 1) {
                sb.append(getAddressObject(arr[0], objName, null));
                dto.setJoin("object " + objName);
                sb.substring(0,sb.length()-1);
                createObjectName.add(objName);
            } else {
                //创建对象、对象组
                List<String> objectJoinList = new ArrayList<>();
                for (String ip : arr) {
                    //多个地址混合时，仅子网和范围建对象，单IP不建
                    if (IpUtils.isIPSegment(ip) || IpUtils.isIPRange(ip)) {
                        String refObjName = String.format("%s_AG_%s", ticket, IdGen.getRandomNumberString());
                        sb.append(getAddressObject(ip, refObjName, null));
                        objectJoinList.add("network object " + refObjName);
                        createObjectName.add(refObjName);
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
                dto.setGroup(true);
                sb.substring(0,sb.length()-1);
                sb.append("exit\n");
                createObjectGroupName.add(objName);
            }

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
        dto.setCreateObjectName(createObjectName);
        dto.setCreateGroupObjectName(createObjectGroupName);
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

        sb.append(fullStr + "\n");
        sb.append("exit\n");

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
        List<String>  createServiceGroupObjectNames = new ArrayList<>();
        String name = getServiceName(serviceDTOList);
        dto.setName(name);
        dto.setJoin(String.format("object-group %s ", name));
        sb.append(String.format("object-group service %s \n", name));
        createServiceGroupObjectNames.add(name);
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
        dto.setCreateServiceGroupObjectNames(createServiceGroupObjectNames);
        return dto;
    }


    @Override
    public int getMaxNameLength() {
        return MAX_NAME_LENGTH;
    }

    public static void main(String[] args) {
        SecurityCiscoASA86 ciscoASA = new SecurityCiscoASA86();
        String jsonStr = "{\n" +
                "\t\"action\":\"PERMIT\",\n" +
                "\t\"businessName\":\"cisco4\",\n" +
                "\t\"ciscoEnable\":false,\n" +
                "\t\"ciscoInterfaceCreate\":true,\n" +
                "\t\"ciscoInterfacePolicyName\":\"DaiWaiMange_in\",\n" +
                "\t\"createObjFlag\":true,\n" +
                "\t\"description\":\"fa\",\n" +
                "\t\"deviceUuid\":\"27d387ec3f354107ae23598e62fe248d\",\n" +
                "\t\"dstIp\":\"164.12.36.3,2.2.2.1/23\",\n" +
                "\t\"dstItf\":\"KaiFaRenYuan\",\n" +
                "\t\"dstItfAlias\":\"KaiFaRenYuan\",\n" +
                "\t\"dstZonePriority\":0,\n" +
                "\t\"existDstAddressList\":[],\n" +
                "\t\"existServiceNameList\":[],\n" +
                "\t\"existSrcAddressList\":[],\n" +
                "\t\"hasVsys\":false,\n" +
                "\t\"moveSeatEnum\":\"FIRST\",\n" +
                "\t\"mustCreateFlag\":false,\n" +
                "\t\"name\":\"cisco4\",\n" +
                "\t\"restDstAddressList\":[],\n" +
                "\t\"restServiceList\":[],\n" +
                "\t\"restSrcAddressList\":[],\n" +
                "\t\"serviceList\":[\n" +
                "\t\t{\n" +
                "\t\t\t\"dstPorts\":\"any\",\n" +
                "\t\t\t\"protocol\":\"0\",\n" +
                "\t\t\t\"srcPorts\":\"any\"\n" +
                "\t\t}\n" +
                "\t],\n" +
                "\t\"specialExistObject\":{\n" +
                "\t\t\"existDstAddressList\":[],\n" +
                "\t\t\"existPostDstAddressList\":[],\n" +
                "\t\t\"existPostServiceNameList\":[],\n" +
                "\t\t\"existPostSrcAddressList\":[],\n" +
                "\t\t\"existServiceNameList\":[],\n" +
                "\t\t\"existSrcAddressList\":[],\n" +
                "\t\t\"restDstAddressList\":[],\n" +
                "\t\t\"restPostDstAddressList\":[],\n" +
                "\t\t\"restPostServiceList\":[],\n" +
                "\t\t\"restPostSrcAddressList\":[],\n" +
                "\t\t\"restServiceList\":[],\n" +
                "\t\t\"restSrcAddressList\":[]\n" +
                "\t},\n" +
                "\t\"srcIp\":\"1.1.1.1-1.1.1.10,192.168.23.6,172.16.1.36-172.16.1.105,161.24.2.0/24\",\n" +
                "\t\"srcItf\":\"DaiWaiMange\",\n" +
                "\t\"srcItfAlias\":\"DaiWaiMange\",\n" +
                "\t\"srcZonePriority\":0,\n" +
                "\t\"topFlag\":false,\n" +
                "\t\"vsys\":false,\n" +
                "\t\"vsysName\":\"\"\n" +
                "}\n";
        CommandlineDTO cmdDTO = JSONObject.parseObject(jsonStr, CommandlineDTO.class);

        String cmd = ciscoASA.generateCommandline(cmdDTO);
        System.out.println(cmd);

    }
}
