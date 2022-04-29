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
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Service;

import javax.security.auth.callback.TextOutputCallback;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * @Author: wenjiachang
 * @Date: 2019/1/29 9:38
 */
@Slf4j
@Service
@CustomCli(value = DeviceModelNumberEnum.CISCO_ASA_86, type = PolicyEnum.SECURITY)
public class SecurityCiscoASA86ForZZYH extends SecurityPolicyGenerator implements PolicyGenerator {

    private static Logger logger = LoggerFactory.getLogger(SecurityCiscoASA86ForZZYH.class);

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

        PolicyObjectDTO srcAddress = generateAddressObject(dto.getSrcIp(), ticket, createObjFlag, specialObject.getSrcAddressObjectName(), dto.getDescription(), dto.getSrcIpSystem());
        PolicyObjectDTO dstAddress = generateAddressObject(dto.getDstIp(), ticket, createObjFlag, specialObject.getDstAddressObjectName(), dto.getDescription(), dto.getDstIpSystem());
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
        PolicyObjectDTO srcAddress = generateAddressObject(dto.getSrcIp(), ticket, createObjFlag, specialObject.getSrcAddressObjectName(), dto.getDescription(), dto.getSrcIpSystem());
        PolicyObjectDTO dstAddress = generateAddressObject(dto.getDstIp(), ticket, createObjFlag, specialObject.getDstAddressObjectName(), dto.getDescription(), dto.getDstIpSystem());
        PolicyObjectDTO serviceObject = generateServiceObject(dto.getServiceList(), createObjFlag, specialObject.getServiceObjectName());

        // 记录创建对象名称
        recordCreateAddrAndServiceObjectName(dto, srcAddress, dstAddress, null,null);
        // 记录创建服务名称
        recordCreateServiceObjectNames(dto,serviceObject);

        String commandLine = commonLine(srcAddress, dstAddress, serviceObject, dto, mergeProperty);

        return commandLine;
    }

    public String commonLine(PolicyObjectDTO srcAddress, PolicyObjectDTO dstAddress, PolicyObjectDTO serviceObject, CommandlineDTO dto, Integer mergeProperty) {
        StringBuilder sb = new StringBuilder();
        sb.append("configure terminal\n");
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
        String interfaceName = dto.getCiscoInterfacePolicyName();

        String businessName = String.format("%s", dto.getBusinessName());

        //思科新建策略，默认是置顶的、最前，不分前后
        String line1 = "";
        //是否置顶
        Boolean isSetTop = false;
        //存在接口信息，则就是编辑
        if (StringUtils.isNotBlank(interfaceName)) {
            businessName = interfaceName;
            String swapRuleNameId = StringUtils.isNotBlank(dto.getSwapRuleNameId()) ? dto.getSwapRuleNameId() : "";
            int moveSeatCode = dto.getMoveSeatEnum().getCode();
            if (moveSeatCode == MoveSeatEnum.BEFORE.getCode()) {
                line1 = String.format("line %s", swapRuleNameId);
            } else if (moveSeatCode == MoveSeatEnum.AFTER.getCode()) {
                int lineNum = -1;
                try {
                    lineNum = Integer.valueOf(swapRuleNameId);
                } catch (Exception e) {
                    logger.info("放在某条策略之后的名称应为数字ID！");
                }
                line1 = String.format("line %d", lineNum + 1);
            } else if (moveSeatCode == MoveSeatEnum.FIRST.getCode()) {
                line1 = "line 1";
                isSetTop = true;
            }
        }
        //思科新建策略，默认是置顶的、最前，不分前后
        sb.append(String.format("access-list %s extended %s %s %s %s\n", businessName, dto.getAction().toLowerCase(), serviceObject.getJoin(), srcAddress.getJoin(), dstAddress.getJoin()));
        sb.append("\nend");

        return sb.toString();
    }

    @Override
    public String generatePostCommandline(CommandlineDTO dto) {
        return "";
    }

    public PolicyObjectDTO generateAddressObject(String ipAddress, String ticket, boolean createObjFlag,
                                                 RefObjectDTO refObjectDTO, String description, String ipSystem) {
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
//        String objName = String.format("%s_AO_%s", ticket, IdGen.getRandomNumberString());

        // 是创建对象
        if (createObjFlag) {
            //只有一个，直接创建对象，引用即可
            if (arr.length == 1) {
                String addressObject = getAddressObject(arr[0], ipSystem, null, "");
                sb.append(addressObject);
                String newAddressObject = "";
                String[] split = addressObject.split("\n");
                for (String s : split) {
                    if (s.contains("object network ")) {
                        newAddressObject = s.replace("object network ", "");
                        break;
                    }
                }
                dto.setJoin("object " + newAddressObject);
                sb.substring(0,sb.length()-1);
                createObjectName.add(ticket);
            } else {
                //创建对象、对象组
                List<String> objectJoinList = new ArrayList<>();
                for (String ip : arr) {
                    //多个地址混合时，仅子网和范围建对象，单IP不建
                    if (IpUtils.isIPSegment(ip) || IpUtils.isIPRange(ip)) {
//                        String refObjName = String.format("%s_AG_%s", ticket, IdGen.getRandomNumberString());
                        String addressObject = getAddressObject(ip, ipSystem, null, "multiple");
                        sb.append(addressObject);
                        String newAddressObject = "";
                        String[] split = addressObject.split("\n");
                        for (String s : split) {
                            if (s.contains("object network ")) {
                                newAddressObject = s.replace("object network ", "");
                                break;
                            }
                        }
                        objectJoinList.add("network object " + newAddressObject);
                        createObjectName.add(ticket);
                    } else {
                        objectJoinList.add("network host " + ip);
                    }
                }

                //建地址组，去引用哪些对象
                if (StringUtils.isEmpty(ipSystem)) {
                    if (StringUtils.isEmpty(description)) {
                        sb.append(String.format("object-group network %s\n", LocalDate.now()+"_"+IdGen.getRandomNumberString()));
                    } else {
                        sb.append(String.format("object-group network %s\n", description));
                    }
                }else {
                    if (StringUtils.isEmpty(description)) {
                        sb.append(String.format("object-group network %s\n", ipSystem));
                    } else {
                        sb.append(String.format("object-group network %s\n", ipSystem + "_" + description));
                    }
                }

                for (String joinStr : objectJoinList) {
                    sb.append(joinStr + "\n");
                }
                if (StringUtils.isEmpty(ipSystem)) {
                    if (StringUtils.isEmpty(description)) {
                        dto.setJoin("object-group " + LocalDate.now()+"_"+IdGen.getRandomNumberString() + " ");
                    } else {
                        dto.setJoin("object-group " + description + " ");
                    }
                }else {
                    if (StringUtils.isEmpty(description)) {
                        dto.setJoin("object-group " + ipSystem+ " ");
                    } else {
                        dto.setJoin("object-group " + ipSystem + "_" + description + " ");
                    }
                }

                dto.setGroup(true);
                sb.substring(0,sb.length()-1);
                sb.append("exit\n");
                createObjectGroupName.add(ticket);
            }

            dto.setCommandLine(sb.toString());
            dto.setName(ticket);
            if (StringUtils.isEmpty(ipSystem)) {
                if (StringUtils.isEmpty(description)) {
                    sb.append(String.format("object-group network %s \n", LocalDate.now()+"_"+IdGen.getRandomNumberString()));
                } else {
                    sb.append(String.format("object-group network %s \n", description));
                }
            }else {
                if (StringUtils.isEmpty(description)) {
                    sb.append(String.format("object-group network %s \n", ipSystem));
                } else {
                    sb.append(String.format("object-group network %s \n", ipSystem + "_"+description));
                }
            }

            dto.setObjectFlag(true);
        } else {
            //直接显示内容
            for (String ip : arr) {
                getAddressObject(ip, "", list, "");
            }
            dto.setCommandLineList(list);
            dto.setObjectFlag(false);
        }
        dto.setCreateObjectName(createObjectName);
        dto.setCreateGroupObjectName(createObjectGroupName);
        return dto;
    }

    /**
     * 根据自定义名称和ips或ip地址的list形式
     * @param ipAddress ip地址的String形式
     * @param name      自定义名称
     * @param list      ip地址的List形式
     * @return
     */
    public String getAddressObject(String ipAddress, String name, List<String> list, String ipNumType) {
        StringBuilder sb = new StringBuilder();

        String newName = "";
        if (IpUtils.isIP(ipAddress)) {
            newName = ipAddress;
        } else if (IpUtils.isIPRange(ipAddress)) {
            String startIpFromRange = IpUtils.getStartIpFromRange(ipAddress);
            String endIpFromRange = IpUtils.getEndIpFromRange(ipAddress);
            StringBuilder rangeSB = new StringBuilder();
            rangeSB.append(startIpFromRange);
            rangeSB.append("-");
            String[] startIpFromRangeArr = startIpFromRange.split("[.]");
            String[] endIpFromRangeArr = endIpFromRange.split("[.]");
            boolean flag = false;
            for (int i = 0; i < 3; i++) {
                if (!startIpFromRangeArr[i].equals(endIpFromRangeArr[i])) {
                    flag = true;
                    break;
                }
            }
            if (flag) {
                rangeSB.append(endIpFromRange);
            } else {
                rangeSB.append(endIpFromRange.substring(endIpFromRange.lastIndexOf(".")+1));
            }
            newName = rangeSB.toString();
        } else if (IpUtils.isIPSegment(ipAddress)) {
            newName = ipAddress.replace("/", "_");
        } else {
            newName = name;
        }
        if ("multiple".equals(ipNumType)) {
            sb.append(String.format("object network %s\n", newName));
        }else {
            if (StringUtils.isNotBlank(name)) {
                sb.append(String.format("object network %s\n", name+"_"+ newName));
            } else {
                sb.append(String.format("object network %s\n", newName));
            }
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
        String name = generateServiceName(serviceDTOList);
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

    public String generateServiceName(List<ServiceDTO> serviceDTOList) {
        StringBuilder nameSb = new StringBuilder();
        int number = 0;
        for (ServiceDTO dto : serviceDTOList) {
            if (number != 0) {
                nameSb.append("_");
            }
            nameSb.append(generateServiceName(dto));
            number++;
        }

        String name = nameSb.toString();
        if(name.length() > getMaxNameLength()) {
            String shortName = name.substring(0, getMaxNameLength()-4);
            name = String.format("%s_etc", shortName.substring(0, shortName.lastIndexOf("_")));
        }
        return name;
    }

    private String generateServiceName(ServiceDTO dto) {
        StringBuilder sb = new StringBuilder();
        String protocolString = ProtocolUtils.getProtocolByValue(Integer.valueOf(dto.getProtocol()));
        sb.append(protocolString.toLowerCase());
        if (StringUtils.isBlank(dto.getDstPorts())) {
            return sb.toString();
        }
        if(protocolString.equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_ICMP)){
            return sb.toString();
        }
        if(dto.getDstPorts().equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_ANY) || dto.getDstPorts().equals(PolicyConstants.PORT_ANY)){
            return sb.toString();
        }
        String[] dstPorts = dto.getDstPorts().split(",");
        for (String dstPort : dstPorts) {

            sb.append(String.format("_%s", dstPort));
        }
        return sb.toString().toLowerCase();
    }

    @Override
    public int getMaxNameLength() {
        return MAX_NAME_LENGTH;
    }

    public static void main(String[] args) {
        SecurityCiscoASA86ForZZYH ciscoASA = new SecurityCiscoASA86ForZZYH();
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
