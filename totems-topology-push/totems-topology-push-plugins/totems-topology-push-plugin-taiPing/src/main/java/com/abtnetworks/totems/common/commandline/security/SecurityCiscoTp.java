package com.abtnetworks.totems.common.commandline.security;

import com.abtnetworks.totems.common.annotation.CustomCli;
import com.abtnetworks.totems.common.commandline.PolicyGenerator;
import com.abtnetworks.totems.common.commandline.SecurityPolicyGenerator;
import com.abtnetworks.totems.common.constants.PolicyConstants;
import com.abtnetworks.totems.common.dto.CmdDTO;
import com.abtnetworks.totems.common.dto.DeviceDTO;
import com.abtnetworks.totems.common.dto.ExistObjectDTO;
import com.abtnetworks.totems.common.dto.ExistObjectRefDTO;
import com.abtnetworks.totems.common.dto.GeneratedObjectDTO;
import com.abtnetworks.totems.common.dto.PolicyDTO;
import com.abtnetworks.totems.common.dto.RefObjectDTO;
import com.abtnetworks.totems.common.dto.SettingDTO;
import com.abtnetworks.totems.common.dto.TaskDTO;
import com.abtnetworks.totems.common.dto.commandline.CommandlineDTO;
import com.abtnetworks.totems.common.dto.commandline.PolicyObjectDTO;
import com.abtnetworks.totems.common.dto.commandline.ServiceDTO;
import com.abtnetworks.totems.common.enums.ActionEnum;
import com.abtnetworks.totems.common.enums.DeviceModelNumberEnum;
import com.abtnetworks.totems.common.enums.DeviceObjectTypeEnum;
import com.abtnetworks.totems.common.enums.IpTypeEnum;
import com.abtnetworks.totems.common.enums.PolicyEnum;
import com.abtnetworks.totems.common.utils.AliStringUtils;
import com.abtnetworks.totems.common.utils.IdGen;
import com.abtnetworks.totems.common.utils.IpUtils;
import com.abtnetworks.totems.common.utils.PortUtils;
import com.abtnetworks.totems.common.utils.ProtocolUtils;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author Administrator
 * @Title:
 * @Description: 思科策略命令行生成
 * @date 2020/7/2
 */
@Slf4j
@Service
@CustomCli(value = DeviceModelNumberEnum.CISCO, type = PolicyEnum.SECURITY)
public class SecurityCiscoTp extends SecurityPolicyGenerator implements PolicyGenerator {
    private static Logger logger = Logger.getLogger(SecurityCiscoTp.class);

    private static Set<Integer> allowType = new HashSet<>();

    private final String IN_KEY = "in";

    private final int MAX_NAME_LENGTH = 65;

    private final int DAY_SECOND = 24 * 60 * 60;

    //private static CommandTaskEdiableMapper commandTaskEdiableMapper = SpringContextUtils.getBean(CommandTaskEdiableMapper.class);

    public SecurityCiscoTp() {
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
        log.info("cmdDTO is " + JSONObject.toJSONString(cmdDTO, true));
        log.info("成功啦成功啦成功啦成功啦成功啦成功啦成功啦！！！！！！");
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
        // 默认设置IPV4
        if(ObjectUtils.isEmpty(dto.getIpType())){
            dto.setIpType(IpTypeEnum.IPV4.getCode());
        }

        ExistObjectRefDTO specialObject = dto.getSpecialExistObject();

        SecurityCiscoTp securityCiscoTp = new SecurityCiscoTp();
        SecurityCiscoASA99ForTp securityCiscoASA99ForTp = new SecurityCiscoASA99ForTp();

        PolicyObjectDTO srcAddress = securityCiscoASA99ForTp.generateAddressObject(dto.getSrcIp(), ticket, createObjFlag, specialObject.getSrcAddressObjectName(), dto.getIpType(), dto.getSrcIpSystem());
        PolicyObjectDTO dstAddress = securityCiscoASA99ForTp.generateAddressObject(dto.getDstIp(), ticket, createObjFlag, specialObject.getDstAddressObjectName(), dto.getIpType(), dto.getDstIpSystem());
        PolicyObjectDTO serviceObject = securityCiscoTp.generateServiceObject(dto,createObjFlag, null);

//        String commandLine = securityCiscoASA.commonLine(srcAddress, dstAddress, serviceObject, dto);
        String commandLine = securityCiscoASA99ForTp.commonLine(srcAddress, dstAddress, serviceObject, dto);

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

        dto.setObjectFlag(createObjFlag);
        String[] arr = ipAddress.split(",");

        StringBuilder sb = new StringBuilder();
        List<String> list = new ArrayList<>();
        String objName = String.format("%s_AO_%s", ticket, IdGen.getRandomNumberString());

        Integer number = 1;

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
                    if(ip.split("-").length > 1){
                        //证明是多个IP混合(范围IP和单个IP)
                        number ++;
                    }else {
                        //多个地址混合时，仅子网和范围建对象，单IP不建
                        /*if (IpUtils.isIPSegment(ip) || IpUtils.isIPRange(ip)) {
                            String refObjName = String.format("%s_AG_%s", ticket, IdGen.getRandomNumberString());
                            sb.append(getAddressObject(ip, refObjName, null));
                            objectJoinList.add("network object " + refObjName);
                        } else {
                            objectJoinList.add("network host " + ip);
                        }*/
                    }
                }
                if(number >= 2){
                    sb.append(getAddressObject(ipAddress,objName,null));
                }

                //建地址组，去引用哪些对象
               /* sb.append(String.format("object-group network %s\n", objName));
                for (String joinStr : objectJoinList) {
                    sb.append(joinStr + "\n");
                }*/

                dto.setJoin("object-group " + objName + " ");
            }
           // sb.substring(0,sb.length()-1);
            sb.append("exit\n");
            dto.setCommandLine(sb.toString());
            dto.setName(objName);
            //sb.append(String.format("object-group network %s \n", objName));
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

        if(ipAddress.split(",").length > 1){
            //证明是多个IP混合(范围IP和单个IP)

           /* List<CommandTaskEditableEntity> commandTaskEditableEntities = commandTaskEdiableMapper.selectTaskNumberByUUid(ipAddress);
            Integer size = new Integer(commandTaskEditableEntities.size());*/

            Integer size = 5;
            //10000为写死的规则
            size += 10000;
            sb.append(String.format("object-group network DM_INLINE_NETWORK_%s\n", size));
        }else {
            //单个IP默认加H
            String splicingStr = "H";

            if(ipAddress.split("-").length > 1){
                //范围IP默认加R
                splicingStr = "R";
            }
            if(ipAddress.split("/").length > 1){
                //子网默认加N
                splicingStr = "N";
            }

            if (StringUtils.isNotBlank(name)) {
                sb.append(String.format("object network %s%s\n", splicingStr,ipAddress));
                if(ipAddress.split("-").length == 1){
                    sb.append(String.format("host %s\n",ipAddress));
                }

            }
        }

        String fullStr = "";
        String identification = "";
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
            String[] arr = ipAddress.split(",");
            if(arr.length > 1){
                for (String str : arr){
                    if(str.split("-").length > 1){
                        //ip范围默认R开头
                        identification = "R";
                    }else {
                        //单个IP默认H开通
                        identification = "H";
                    }
                    fullStr += String.format("host %s%s\n",identification,str);
                }
            }
        }

        sb.append(fullStr + "\n\n");

        if (list != null) {
            list.add(fullStr);
        }

        return sb.toString();
    }


    public PolicyObjectDTO generateServiceObject(CommandlineDTO commandlineDTO, boolean createObjFlag, RefObjectDTO refObjectDTO ){
        PolicyObjectDTO dto = new PolicyObjectDTO();
        List<ServiceDTO> serviceDTOList = commandlineDTO.getServiceList();

//        if (refObjectDTO != null) {
//            dto.setObjectFlag(true);
//            if (refObjectDTO.getObjectTypeEnum().equals(DeviceObjectTypeEnum.SERVICE_GROUP_OBJECT)) {
//                dto.setJoin(String.format("object-group %s ", refObjectDTO.getRefName()));
//            } else {
//                dto.setJoin(String.format("object %s ", refObjectDTO.getRefName()));
//            }
//            return dto;
//        }

//        if(!createObjFlag){
//            return dto;
//        }
        if(CollectionUtils.isEmpty(serviceDTOList)){
            dto.setObjectFlag(true);
            dto.setJoin("any");
            return dto;
        }
        if(serviceDTOList.size() == 1 && serviceDTOList.get(0).getDstPorts().equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_ANY)) {
            dto.setObjectFlag(false);
            String str = ProtocolUtils.getProtocolByValue(Integer.valueOf(serviceDTOList.get(0).getProtocol()));
            dto.setJoin(str.equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_ANY)?"ip":str);
            return dto;
        }

        StringBuilder sb = new StringBuilder();

//        String name = getServiceName2(commandlineDTO);
//        dto.setName(name);
//        dto.setJoin(String.format("object %s ", name));

        List<String> groupServiceName = new ArrayList<>();
        for(ServiceDTO service : serviceDTOList){
            int protocolNum = Integer.valueOf(service.getProtocol());
            String protocolString = ProtocolUtils.getProtocolByValue(protocolNum).toLowerCase();


            if(protocolString.equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_ICMP)) {

//                sb.append(String.format("service %s ", protocolString));
//                if(StringUtils.isNotBlank(service.getType())){
//                    sb.append(String.format("%d ", Integer.valueOf(service.getType())));
//                }
//                sb.append("\n");
                groupServiceName.add(protocolString);
            }else{

                if(StringUtils.isBlank(service.getDstPorts()) || service.getDstPorts().equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_ANY)){
//                    sb.append(String.format("service %s\n", protocolString));
                    groupServiceName.add(protocolString);
                    continue;
                }

                String[] ports = service.getDstPorts().split(",");
                for(String port : ports){

                    if(PortUtils.isPortRange(port)) {
                        String name = protocolString.toUpperCase()+":"+port;
                        groupServiceName.add(name);
                        sb.append(String.format("object service %s\n",name));
                        sb.append(String.format("service %s destination range %s \n", protocolString, PortUtils.getPortString(port, PortUtils.BLANK_FORMAT)));
                    } else if(port.equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_ANY)){
                        //2019-03-18 修改：如果端口为any，显示保持为空
                        sb.append(" ");
                    } else {
                        String name = protocolString.toUpperCase()+":"+port;
                        groupServiceName.add(name);
                        sb.append(String.format("object service %s\n",name));
                        sb.append(String.format("service %s destination eq %s \n", protocolString, PortUtils.getPortString(port, PortUtils.BLANK_FORMAT)));
                    }
                    sb.append("exit\n");
                }
            }
        }
        if(CollectionUtils.isNotEmpty(groupServiceName) && groupServiceName.size()>1){
            String groupName = String.format("DM_INLINE_SERVICE_%s", getDMINLINESERVICE());
            if(groupName.length() > getMaxNameLength()) {
                String shortName = groupName.substring(0, getMaxNameLength()-4);
                groupName = String.format("%s_etc", shortName.substring(0, shortName.lastIndexOf("_")));
            }
            sb.append(String.format("object-group service %s\n", groupName));

            for (String name : groupServiceName) {
                sb.append(String.format("service-object object %s\n", name));
            }
            sb.append("exit\n");
            dto.setJoin(String.format("object-group %s ", groupName));
            dto.setName(groupName);
        }else if(CollectionUtils.isNotEmpty(groupServiceName) && groupServiceName.size() == 1){
            dto.setJoin(String.format("object %s ", groupServiceName.get(0)));
            dto.setName(groupServiceName.get(0));
        }
        dto.setCommandLine(sb.toString());
        dto.setObjectFlag(true);
        return dto;
    }
    public String getDMINLINESERVICE(){
        //获取当前毫秒时间戳
        Long timeStamp = System.currentTimeMillis();
        //将十进制毫秒时间戳转换为十六进制
        return Long.toHexString(timeStamp);
    }

    /*获取服务名称（定制版）*/
    public String getServiceName2(CommandlineDTO dto){
        StringBuilder nameSb = new StringBuilder();
        String deviceUuid = dto.getDeviceUuid();
        List<ServiceDTO> serviceDTOList = dto.getServiceList();
        if(serviceDTOList.size() > 1){


            Integer size = 5;
            //10000为写死的规则
            size += 10000;

            nameSb.append(String.format("DM_INLINE_SERVICE_%s", size));
        }else {
            nameSb.append(getServiceName(serviceDTOList.get(0)));
        }

        String name = nameSb.toString();
        if(name.length() > getMaxNameLength()) {
            String shortName = name.substring(0, getMaxNameLength()-4);
            name = String.format("%s_etc", shortName.substring(0, shortName.lastIndexOf("_")));
        }
        return name;
    }

    /**获取服务名称***/
    public String getServiceName(List<ServiceDTO> serviceDTOList){
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
        if(name.length() > getMaxNameLength()) {
            String shortName = name.substring(0, getMaxNameLength()-4);
            name = String.format("%s_etc", shortName.substring(0, shortName.lastIndexOf("_")));
        }
        return name;
    }
    public String getServiceName(ServiceDTO dto) {
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
            if (PortUtils.isPortRange(dstPort)) {
                if (dstPorts.length==1 && getPortNumber(dstPort)){
                    String[] rangePorts = getRangePorts(dstPort);
                    sb.append(String.format(":%s-%s", rangePorts[0], rangePorts[1]));
                }/*else {
                    String startPort = PortUtils.getStartPort(dstPort);
                    String endPort = PortUtils.getEndPort(dstPort);
                    sb.append(String.format(":%s,.%s", startPort, endPort));
                }*/
            }else {
                sb.append(String.format(":%s", dstPort));
            }
        }
        return sb.toString().toUpperCase();
    }
    public static boolean getPortNumber(String port){
        String[] arr = port.split("-");
        if(arr.length > 1){
            return true;
        }
        return false;
    }
    public static String[] getRangePorts(String port){
        return port.split("-");
    }

    @Override
    public int getMaxNameLength() {
        return MAX_NAME_LENGTH;
    }

    public static void main(String[] args) {
        CommandlineDTO dto = CommandlineDTO.getInstanceDemo();
        /*List<String> restSrc = new ArrayList<>();
        restSrc.add("1.3.2.5");
        //restSrc.add("1.3.2.55");
        dto.setRestSrcAddressList(restSrc);
        List<String> restDst = new ArrayList<>();
        restDst.add("1.3.2.56");
//        restDst.add("10.80.32.14-10.80.32.23");
        dto.setRestDstAddressList(restDst);
        List<ServiceDTO> serviceList = new ArrayList<>();
        *//*ServiceDTO serviceDTO = new ServiceDTO();
        serviceDTO.setProtocol("6");
        serviceDTO.setDstPorts("8888-8890");
        serviceDTO.setSrcPorts("");
        serviceList.add(serviceDTO);*//*
        ServiceDTO serviceDTO2 = new ServiceDTO();
        serviceDTO2.setProtocol("6");
        serviceDTO2.setDstPorts("768");
        serviceDTO2.setSrcPorts("");
        serviceList.add(serviceDTO2);
        dto.setServiceList(serviceList);*/
        List<String> restSrc = new ArrayList<>();
        //restSrc.add("10.12.12.3");
        restSrc.add("10.12.12.3-10.12.12.30");
        dto.setSrcIp(StringUtils.join(restSrc.toArray(), ","));
//
        List<String> restDst = new ArrayList<>();
        //restDst.add("11.12.12.3");
        restDst.add("12.12.12.3-1.1.1.1");
//        restDst.add("12.12.12.3/24");
        dto.setDstIp(StringUtils.join(restDst.toArray(), ","));

        List<ServiceDTO> serviceList = new ArrayList<>();
        ServiceDTO serviceDTO = new ServiceDTO();
        serviceDTO.setProtocol("17");
        serviceDTO.setDstPorts("768");
        serviceList.add(serviceDTO);
        ServiceDTO serviceDTO2 = new ServiceDTO();
        serviceDTO2.setProtocol("17");
        serviceDTO2.setDstPorts("890-901,");
        serviceDTO2.setSrcPorts("");
        serviceList.add(serviceDTO2);
        dto.setServiceList(serviceList);
//        dto.setIdleTimeout(2);
        dto.setIpType(0);
        dto.setDescription("aaaaa");
        dto.setAction("PERMIT");

        SecurityCiscoTp hillStoneR5 = new SecurityCiscoTp();
        String commandLine = hillStoneR5.composite(dto);
        System.out.println("commandline:\n" + commandLine);
    }
}
