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
import com.abtnetworks.totems.common.enums.*;
import com.abtnetworks.totems.common.utils.AliStringUtils;
import com.abtnetworks.totems.common.utils.IdGen;
import com.abtnetworks.totems.common.utils.IpUtils;
import com.abtnetworks.totems.common.utils.PortUtils;
import com.abtnetworks.totems.common.utils.ProtocolUtils;
import com.abtnetworks.totems.common.utils.TimeUtils;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * @author dyj
 * @Title:
 * @Description: 思科策略命令行生成
 * @date 2021/3/18
 */
@Slf4j
@Service
@CustomCli(value = DeviceModelNumberEnum.CISCO_ASA_86, type = PolicyEnum.SECURITY)
public class SecurityCiscoASAForTp extends SecurityPolicyGenerator implements PolicyGenerator {

    private static Logger logger = LoggerFactory.getLogger(SecurityCiscoASA99ForTp.class);

    private final int MAX_NAME_LENGTH = 65;
    private String prefixService = "DM_INLINE_SERVICE";
    private String prefixIp = "DM_INLINE_NETWORK";

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
        //对象（单个ip）/对象组名称（多个ip）
        String objName = "";
        if(StringUtils.isNotEmpty(ipSystem)){
            objName = ipSystem;
        } /*else {
//            objName = String.format("%s_AO_%s", ticket, IdGen.getRandomNumberString());
        }*/

        // 是创建对象
        if (createObjFlag) {
            //只有一个，直接创建对象，引用即可
            if (arr.length == 1) {
                if (StringUtils.isBlank(objName)) {
                    objName = getUrlAddressPrefix(arr[0]);
                }
                sb.append(getAddressObject(arr[0], objName, null,ipType));
                dto.setJoin("object " + objName);
            } else {
                //创建对象、对象组
                boolean containsUrl = false;
                List<String> objectJoinList = new ArrayList<>();
                List<String> urlJoinList = new ArrayList<>();
                for (String ip : arr) {
                    //多个地址混合时，仅子网和范围建对象，单IP不建
                    if (IpUtils.isIPSegment(ip) || IpUtils.isIPRange(ip) || IpUtils.isIP(ip)) {
//                        String refObjName = String.format("%s_AG_%s", ticket, IdGen.getRandomNumberString());
                        String refObjName = getUrlAddressPrefix(ip);
                        sb.append(getAddressObject(ip, refObjName, null,ipType));
                        objectJoinList.add("network-object object " + refObjName);
                    } /*else if(IpUtils.isIP(ip)){
                        String refObjName = getUrlAddressPrefix(ip);
                        sb.append(getAddressObject(ip, refObjName, null,ipType));
                        objectJoinList.add("network-object host " + refObjName);
                    } */else {
                        containsUrl = true;
                        sb.append(getURLAddressObject(ip, ticket,urlJoinList));
                    }
                }

                //建地址组，去引用哪些对象
                /*if(containsUrl){
                    sb.append(String.format("object-group network %s\n", objName));
                } else {
                    sb.append(String.format("object-group network %s\n", objName));
                }*/
                if (StringUtils.isBlank(objName)) {
                    objName = prefixIp+"_"+getDMINLINESERVICE();
                }
                sb.append(String.format("object-group network %s \n", objName));
                for (String joinStr : objectJoinList) {
                    sb.append(joinStr + "\n");
                }
                for (String joinStr : urlJoinList) {
                    sb.append(joinStr + "\n");
                }
                sb.append("exit\n");
                dto.setJoin(String.format("object-group %s ",objName));
            }

            dto.setCommandLine(sb.toString());
            dto.setName(objName);
            sb.append(String.format("object-group network %s\n", objName));
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
        String groupStr = String.format("network-object object %s", getUrlAddressPrefix(ipAddress));
        sb.append(fullStr + "\n");
        sb.append("exit\n");
        if (urlJoinList != null) {
            urlJoinList.add(groupStr);
        }
        return sb.toString();
    }

    public String getUrlAddressPrefix(String ip){
        /*根据IP获取前缀：
         * 1、单个IP前缀默认为 “H” 10.10.1.1
         * 2、IP范围前缀默认为 “R” 10.10.1.1-10.10.1.4
         * 3、IP子网前缀默认为 “N” 10.10.1.1/32
         * */
        StringBuilder name = new StringBuilder();

        if(ip.split(",").length > 1){
            //多个ipv4
            name.append(String.format("%s_%s",prefixIp,getDMINLINESERVICE()));
        }else {
            if(StringUtils.isNotBlank(ip)){
                String[] rangeIp = ip.split("-");
                String[] subnetIp = ip.split("/");
                String prefix = "";
                if(rangeIp.length > 1){
                    //根据 "-" 分割，如果长度大于1表示传递IP为范围IP
                    prefix = "R";
                    name.append(String.format("%s%s",prefix,ip));
                } else if(subnetIp.length > 1){
                    //根据 “/” 分割，如果长度大于1表示传递IP为IP子网
                    prefix = "N";
                    name.append(String.format("%s%s_%s",prefix,subnetIp[0],subnetIp[1]));
                } else {
                    prefix = "H";
                    name.append(String.format("%s%s",prefix,ip));
                }
            }
        }
        return name.toString();
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
        String dminlineservice = getDMINLINESERVICE();
        dto.setName(name);
        //这里加name.split(":").length > 2判断是为了防止一个service中穿过来多个IP，比如name:"TCP:8080-8888,8889"这种情况
        if(serviceDTOList.size() > 1 || name.split(":").length > 2){
            dto.setJoin(String.format("object-group %s_%s ",  prefixService,dminlineservice));
        }else {
            dto.setJoin(String.format("object %s ",  name));
        }

        boolean isRangeIP = false;
        if(serviceDTOList.size() > 1){
            isRangeIP = true;
        }else {
            for (ServiceDTO serviceDTO : serviceDTOList){
                if(serviceDTO.getDstPorts().split(",").length > 1){
                    isRangeIP = true;
                }
            }
        }


        if(isRangeIP){
            sb.append(String.format("object-group service %s_%s \n", prefixService,dminlineservice));
        }else{
            sb.append(String.format("object service %s \n", name));
        }
        for(ServiceDTO service : serviceDTOList){
            int protocolNum = Integer.valueOf(service.getProtocol());
            String protocolString = ProtocolUtils.getProtocolByValue(protocolNum);
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
                        if(ports.length > 1){
                            sb.append(String.format("service-object %s destination eq %s \n", protocolString, PortUtils.getPortString(port, PortUtils.BLANK_FORMAT)));
                        }else {
                            sb.append(String.format("service %s destination eq %s \n", protocolString, PortUtils.getPortString(port, PortUtils.BLANK_FORMAT)));
                        }
                    }
                }
            }
        }
        sb.append("exit\n");
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
        sb.append(protocolString);
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
                String startPort = PortUtils.getStartPort(dstPort);
                String endPort = PortUtils.getEndPort(dstPort);
                sb.append(String.format(":%s-%s", startPort, endPort));
            } else {
                sb.append(String.format(":%s", dstPort));
            }
        }
        return sb.toString().toUpperCase();
    }

    private String formatTimeString(String timeString) {
        return TimeUtils.transformDateFormat(timeString, TimeUtils.EUROPEAN_TIME_FORMAT, TimeUtils.CISCO_ASA_TIME_FORMAT);
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

        sb.append(String.format("time-range %s \n", name));
        sb.append(String.format("absolute start %s end %s \n", formatTimeString(startTime), formatTimeString(endTime)));
        sb.append("exit\n");
        dto.setName(name);
        dto.setCommandLine(sb.toString());
        dto.setJoin(String.format(" time-range %s", name));
        return dto;
    }

    private List<String> formatFullPort(String[] arr) {
        List<String> list = new ArrayList<>();
        for (String srcPortString : arr) {
            if (PortUtils.isPortRange(srcPortString)) {
                list.add(String.format("range %s", PortUtils.getPortString(srcPortString, PortUtils.BLANK_FORMAT)));
            } else if (srcPortString.equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_ANY)) {
                //2019-03-18 修改：如果端口为any，显示保持为空
                list.add(" ");
            } else {
                list.add(String.format("eq %s", PortUtils.getPortString(srcPortString, PortUtils.BLANK_FORMAT)));
            }
        }
        return list;
    }

    //提取公共, 供思科 ASA 2个版本使用
    public String commonLine(PolicyObjectDTO srcAddress, PolicyObjectDTO dstAddress, PolicyObjectDTO serviceObject,
                             CommandlineDTO dto) {

        List<String> srcAddressList = srcAddress.getCommandLineList();
        List<String> dstAddressList = dstAddress.getCommandLineList();

        String ticket = dto.getName();
        String srcItf = dto.getSrcItf();
        String srcItfAlias = dto.getSrcItfAlias();
        String dstItfAlias = dto.getDstItfAlias();
        String startTime = dto.getStartTime();
        String endTime = dto.getEndTime();

        boolean createObjFlag = dto.isCreateObjFlag();
        PolicyObjectDTO time = generateTimeObject(startTime, endTime, ticket);

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
        if (time != null) {
            sb.append(String.format("%s\n", time.getCommandLine()));
        }

        String interfaceName = dto.getCiscoInterfacePolicyName();

        String businessName = String.format("%s", dto.getBusinessName());

        //思科新建策略，默认是置顶的、最前，不分前后
        String line1 = "";
        String description = dto.getDescription();
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
            }
        }

        if(!AliStringUtils.isEmpty(description) && MoveSeatEnum.FIRST.equals(dto.getMoveSeatEnum()) && StringUtils.isBlank(line1)){
            sb.append(String.format("access-list %s %s remark %s\n", businessName, line1,description));
        }

        //对象式命令行 或，服务、源、目的地址都是建对象或复用的
        if (createObjFlag || (serviceObject.isObjectFlag() && srcAddress.isObjectFlag() && dstAddress.isObjectFlag())) {
            sb.append(String.format("access-list %s %s extended %s %s %s %s log disable", businessName, line1, dto.getAction().toLowerCase(), serviceObject.getJoin(), srcAddress.getJoin(), dstAddress.getJoin()));
            if (time != null) {
                sb.append(time.getJoin());
            }
            sb.append("\n");
            if(!AliStringUtils.isEmpty(description) && StringUtils.isNotBlank(line1)){
                sb.append(String.format("access-list %s %s remark %s\n", businessName, line1,description));
            }
        } else if (!createObjFlag && serviceObject.isObjectFlag() && StringUtils.isNotBlank(serviceObject.getJoin())
                && !srcAddress.isObjectFlag() && !dstAddress.isObjectFlag()) {
            //不建对象，但服务对象是复用的，源、目的地址都是直接写内容
            for (int i = 0; i < srcAddressList.size(); i++) {
                for (int j = 0; j < dstAddressList.size(); j++) {
                    sb.append(String.format("access-list %s %s extended %s %s %s %s log disable", businessName, line1, dto.getAction().toLowerCase(), serviceObject.getJoin(), srcAddressList.get(i), dstAddressList.get(j)));
                    if (time != null) {
                        sb.append(time.getJoin());
                    }
                    sb.append("\n");
                    if(!AliStringUtils.isEmpty(description)  && StringUtils.isNotBlank(line1)){
                        sb.append(String.format("access-list %s %s remark %s\n", businessName, line1,description));
                    }
                }
            }
        } else if (!createObjFlag && serviceObject.isObjectFlag() && StringUtils.isNotBlank(serviceObject.getJoin())
                && srcAddress.isObjectFlag() && StringUtils.isNotBlank(srcAddress.getJoin())) {
            //不建对象，服务、源地址是复用的，目的地址直接写内容
            for (int j = 0; j < dstAddressList.size(); j++) {
                sb.append(String.format("access-list %s %s extended %s %s %s %s log disable", businessName, line1, dto.getAction().toLowerCase(), serviceObject.getJoin(), srcAddress.getJoin(), dstAddressList.get(j)));
                if (time != null) {
                    sb.append(time.getJoin());
                }
                sb.append("\n");
                if(!AliStringUtils.isEmpty(description)  && StringUtils.isNotBlank(line1)){
                    sb.append(String.format("access-list %s %s remark %s\n", businessName, line1,description));
                }
            }
        } else if (!createObjFlag && serviceObject.isObjectFlag() && StringUtils.isNotBlank(serviceObject.getJoin())
                && dstAddress.isObjectFlag() && StringUtils.isNotBlank(dstAddress.getJoin())) {
            //不建对象，服务、目的地址是复用，源地址直接写内容
            for (int j = 0; j < srcAddressList.size(); j++) {
                sb.append(String.format("access-list %s %s extended %s %s %s %s log disable", businessName, line1, dto.getAction().toLowerCase(), serviceObject.getJoin(), srcAddressList.get(j), dstAddress.getJoin()));
                if (time != null) {
                    sb.append(time.getJoin());
                }
                sb.append("\n");
                if(!AliStringUtils.isEmpty(description)  && StringUtils.isNotBlank(line1)){
                    sb.append(String.format("access-list %s %s remark %s\n", businessName, line1,description));
                }
            }
        } else if (!createObjFlag && srcAddress.isObjectFlag() && StringUtils.isNotBlank(srcAddress.getJoin())
                && dstAddress.isObjectFlag() && StringUtils.isNotBlank(dstAddress.getJoin())) {
            //不建对象，但源、目的地址复用，服务直接写内容
            for (ServiceDTO service : dto.getServiceList()) {
                int protocolNum = Integer.valueOf(service.getProtocol());
                String protocolString = ProtocolUtils.getProtocolByValue(protocolNum);
                if (protocolString.equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_ANY)) {
                    protocolString = "ip";
                }

                //服务的端口是any
                if (service.getDstPorts().equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_ANY)) {
                    sb.append(String.format("access-list %s %s extended %s %s %s %s log disable", businessName, line1, dto.getAction().toLowerCase(), protocolString, srcAddress.getJoin(), dstAddress.getJoin()));
                    if (time != null) {
                        sb.append(time.getJoin());
                    }
                    sb.append("\n");
                    if(!AliStringUtils.isEmpty(description) && StringUtils.isNotBlank(line1)){
                        sb.append(String.format("access-list %s %s remark %s\n", businessName, line1,description));
                    }
                    continue;
                }

                String[] dstPorts = service.getDstPorts().split(",");
                List<String> dstPortList = formatFullPort(dstPorts);
                for (int j = 0; j < dstPortList.size(); j++) {
                    sb.append(String.format("access-list %s %s extended %s %s %s %s %s log disable", businessName, line1, dto.getAction().toLowerCase(), protocolString, srcAddress.getJoin(), dstAddress.getJoin(), dstPortList.get(j)));
                }
                if (time != null) {
                    sb.append(time.getJoin());
                }
                sb.append("\n");
                if(!AliStringUtils.isEmpty(description) && StringUtils.isNotBlank(line1)){
                    sb.append(String.format("access-list %s %s remark %s\n", businessName, line1,description));
                }
            }

        } else {
            //不建对象，做笛卡尔积处理， 下面的循环保留srcPort，以防止后期出现源端口
            for (ServiceDTO service : dto.getServiceList()) {
                //根据协议Id 取协议名称
                int protocolNum = Integer.valueOf(service.getProtocol());
                String protocolString = ProtocolUtils.getProtocolByValue(protocolNum);
                if (protocolString.equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_ANY)) {
                    protocolString = "ip";
                }

                List<String> srcPortList = new ArrayList<>();
                List<String> dstPortList = new ArrayList<>();

                // 多个端口
                if (!protocolString.equals(PolicyConstants.POLICY_STR_VALUE_ICMP) && !"ip".equals(protocolString)) {
                    String[] srcPorts = service.getSrcPorts().split(",");
                    String[] dstPorts = service.getDstPorts().split(",");
                    srcPortList = formatFullPort(srcPorts);
                    dstPortList = formatFullPort(dstPorts);
                }
                //dstPortList 非空，则表示是：tcp、udp协议
                if (dstPortList != null && dstPortList.size() > 0) {
                    //非对象笛卡尔积
                    for (int i = 0; i < srcAddressList.size(); i++) {
                        for (int j = 0; j < srcPortList.size(); j++) {
                            for (int m = 0; m < dstAddressList.size(); m++) {
                                for (int n = 0; n < dstPortList.size(); n++) {
                                    sb.append(String.format("access-list %s %s extended %s %s %s %s %s %s log disable", businessName, line1, dto.getAction().toLowerCase(), protocolString, srcAddressList.get(i), srcPortList.get(j), dstAddressList.get(m), dstPortList.get(n)));
                                    if (time != null) {
                                        sb.append(time.getJoin());
                                    }
                                    sb.append("\n");
                                    if(!AliStringUtils.isEmpty(description)  && StringUtils.isNotBlank(line1)){
                                        sb.append(String.format("access-list %s %s remark %s\n", businessName, line1,description));
                                    }
                                }
                            }
                        }
                    }
                } else {
                    for (int i = 0; i < srcAddressList.size(); i++) {
                        for (int m = 0; m < dstAddressList.size(); m++) {
                            sb.append(String.format("access-list %s %s extended %s %s %s %s log disable",
                                    businessName, line1, dto.getAction().toLowerCase(), protocolString, srcAddressList.get(i), dstAddressList.get(m)));

                            //icmp协议需要增加协议类型
                            if (protocolString.equals(PolicyConstants.POLICY_STR_VALUE_ICMP) && StringUtils.isNotBlank(service.getType())) {
                                sb.append(String.format(" %d", Integer.valueOf(service.getType())));
                            }

                            if (time != null) {
                                sb.append(time.getJoin());
                            }
                            sb.append("\n");
                            if(!AliStringUtils.isEmpty(description)  && StringUtils.isNotBlank(line1)){
                                sb.append(String.format("access-list %s %s remark %s\n", businessName, line1,description));
                            }
                        }
                    }
                }
            }
        }
        //长链接命令行生成
        String idleTimeoutCmd = getIdleTimeoutCmd(dto,businessName);
        sb.append(idleTimeoutCmd);
        //接口为空时，需要新建
        if (dto.isCiscoInterfaceCreate()) {
            if (dto.isOutBound()) {
                sb.append(String.format("access-group %s out interface %s\n", businessName, dstItfAlias));
            } else {
                sb.append(String.format("access-group %s in interface %s\n", businessName, srcItfAlias));
            }
        }

        sb.append("end\nwrite\n");
        sb.append("\n");

        return sb.toString();
    }

    public String commonLine2(PolicyObjectDTO srcAddress, PolicyObjectDTO dstAddress, PolicyObjectDTO serviceObject,
                              CommandlineDTO dto) {

        List<String> srcAddressList = srcAddress.getCommandLineList();
        List<String> dstAddressList = dstAddress.getCommandLineList();

        String ticket = dto.getName();
        String srcItf = dto.getSrcItf();
        String srcItfAlias = dto.getSrcItfAlias();
        String dstItfAlias = dto.getDstItfAlias();
        String startTime = dto.getStartTime();
        String endTime = dto.getEndTime();

        boolean createObjFlag = dto.isCreateObjFlag();
        PolicyObjectDTO time = generateTimeObject(startTime, endTime, ticket);

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
        if (time != null) {
            sb.append(String.format("%s\n", time.getCommandLine()));
        }

        String interfaceName = dto.getCiscoInterfacePolicyName();

        String businessName = String.format("%s", dto.getBusinessName());

        //思科新建策略，默认是置顶的、最前，不分前后
        String line1 = "";

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
            }
        }


        //对象式命令行 或，服务、源、目的地址都是建对象或复用的
        if (createObjFlag || (serviceObject.isObjectFlag() && srcAddress.isObjectFlag() && dstAddress.isObjectFlag())) {
            sb.append(String.format("access-list %s %s extended %s %s %s %s", businessName, line1, dto.getAction().toLowerCase(), serviceObject.getJoin(), srcAddress.getJoin(), dstAddress.getJoin()));
            if (time != null) {
                sb.append(time.getJoin());
            }
            sb.append("\n");
        } else if (!createObjFlag && serviceObject.isObjectFlag() && StringUtils.isNotBlank(serviceObject.getJoin())
                && !srcAddress.isObjectFlag() && !dstAddress.isObjectFlag()) {
            //不建对象，但服务对象是复用的，源、目的地址都是直接写内容
            for (int i = 0; i < srcAddressList.size(); i++) {
                for (int j = 0; j < dstAddressList.size(); j++) {
                    sb.append(String.format("access-list %s %s extended %s %s %s %s log disable", businessName, line1, dto.getAction().toLowerCase(), serviceObject.getJoin(), srcAddressList.get(i), dstAddressList.get(j)));
                    if (time != null) {
                        sb.append(time.getJoin());
                    }
                    sb.append("\n");
                }
            }
        } else if (!createObjFlag && serviceObject.isObjectFlag() && StringUtils.isNotBlank(serviceObject.getJoin())
                && srcAddress.isObjectFlag() && StringUtils.isNotBlank(srcAddress.getJoin())) {
            //不建对象，服务、源地址是复用的，目的地址直接写内容
            for (int j = 0; j < dstAddressList.size(); j++) {
                sb.append(String.format("access-list %s %s extended %s %s %s %s log disable", businessName, line1, dto.getAction().toLowerCase(), serviceObject.getJoin(), srcAddress.getJoin(), dstAddressList.get(j)));
                if (time != null) {
                    sb.append(time.getJoin());
                }
                sb.append("\n");
            }
        } else if (!createObjFlag && serviceObject.isObjectFlag() && StringUtils.isNotBlank(serviceObject.getJoin())
                && dstAddress.isObjectFlag() && StringUtils.isNotBlank(dstAddress.getJoin())) {
            //不建对象，服务、目的地址是复用，源地址直接写内容
            for (int j = 0; j < srcAddressList.size(); j++) {
                sb.append(String.format("access-list %s %s extended %s %s %s %s log disable", businessName, line1, dto.getAction().toLowerCase(), serviceObject.getJoin(), srcAddressList.get(j), dstAddress.getJoin()));
                if (time != null) {
                    sb.append(time.getJoin());
                }
                sb.append("\n");
            }
        } else if (!createObjFlag && srcAddress.isObjectFlag() && StringUtils.isNotBlank(srcAddress.getJoin())
                && dstAddress.isObjectFlag() && StringUtils.isNotBlank(dstAddress.getJoin())) {
            //不建对象，但源、目的地址复用，服务直接写内容
            for (ServiceDTO service : dto.getServiceList()) {
                int protocolNum = Integer.valueOf(service.getProtocol());
                String protocolString = ProtocolUtils.getProtocolByValue(protocolNum).toLowerCase();
                if (protocolString.equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_ANY)) {
                    protocolString = "ip";
                }

                //服务的端口是any
                if (service.getDstPorts().equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_ANY)) {
                    sb.append(String.format("access-list %s %s extended %s %s %s %s log disable", businessName, line1, dto.getAction().toLowerCase(), protocolString, srcAddress.getJoin(), dstAddress.getJoin()));
                    if (time != null) {
                        sb.append(time.getJoin());
                    }
                    sb.append("\n");
                    continue;
                }

                String[] dstPorts = service.getDstPorts().split(",");
                List<String> dstPortList = formatFullPort(dstPorts);
                for (int j = 0; j < dstPortList.size(); j++) {
                    sb.append(String.format("access-list %s %s extended %s %s %s %s %s log disable", businessName, line1, dto.getAction().toLowerCase(), protocolString, srcAddress.getJoin(), dstAddress.getJoin(), dstPortList.get(j)));
                }
                if (time != null) {
                    sb.append(time.getJoin());
                }
                sb.append("\n");
            }

        } else {
            //不建对象，做笛卡尔积处理， 下面的循环保留srcPort，以防止后期出现源端口
            for (ServiceDTO service : dto.getServiceList()) {
                //根据协议Id 取协议名称
                int protocolNum = Integer.valueOf(service.getProtocol());
                String protocolString = ProtocolUtils.getProtocolByValue(protocolNum).toLowerCase();
                if (protocolString.equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_ANY)) {
                    protocolString = "ip";
                }

                List<String> srcPortList = new ArrayList<>();
                List<String> dstPortList = new ArrayList<>();

                // 多个端口
                if (!protocolString.equals(PolicyConstants.POLICY_STR_VALUE_ICMP) && !"ip".equals(protocolString)) {
                    String[] srcPorts = service.getSrcPorts().split(",");
                    String[] dstPorts = service.getDstPorts().split(",");
                    srcPortList = formatFullPort(srcPorts);
                    dstPortList = formatFullPort(dstPorts);
                }
                //dstPortList 非空，则表示是：tcp、udp协议
                if (dstPortList != null && dstPortList.size() > 0) {
                    //非对象笛卡尔积
                    for (int i = 0; i < srcAddressList.size(); i++) {
                        for (int j = 0; j < srcPortList.size(); j++) {
                            for (int m = 0; m < dstAddressList.size(); m++) {
                                for (int n = 0; n < dstPortList.size(); n++) {
                                    sb.append(String.format("access-list %s %s extended %s %s %s %s %s %s log disable", businessName, line1, dto.getAction().toLowerCase(), protocolString, srcAddressList.get(i), srcPortList.get(j), dstAddressList.get(m), dstPortList.get(n)));
                                    if (time != null) {
                                        sb.append(time.getJoin());
                                    }
                                    sb.append("\n");
                                }
                            }
                        }
                    }
                } else {
                    for (int i = 0; i < srcAddressList.size(); i++) {
                        for (int m = 0; m < dstAddressList.size(); m++) {
                            sb.append(String.format("access-list %s %s extended %s %s %s %s log disable",
                                    businessName, line1, dto.getAction().toLowerCase(), protocolString, srcAddressList.get(i), dstAddressList.get(m)));

                            //icmp协议需要增加协议类型
                            if (protocolString.equals(PolicyConstants.POLICY_STR_VALUE_ICMP) && StringUtils.isNotBlank(service.getType())) {
                                sb.append(String.format(" %d", Integer.valueOf(service.getType())));
                            }

                            if (time != null) {
                                sb.append(time.getJoin());
                            }
                            sb.append("\n");
                        }
                    }
                }
            }
        }
        //长链接命令行生成
        String idleTimeoutCmd = getIdleTimeoutCmd(dto, businessName);
        sb.append(idleTimeoutCmd);
        //接口为空时，需要新建
        if (dto.isCiscoInterfaceCreate()) {
            if (dto.isOutBound()) {
                sb.append(String.format("access-group %s out interface %s\n", businessName, dstItfAlias));
            } else {
                sb.append(String.format("access-group %s in interface %s\n", businessName, srcItfAlias));
            }
        }

        sb.append("end\nwrite\n");
        sb.append("\n");

        return sb.toString();
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
        String businessName = String.format("%s", dto.getBusinessName());
        //思科新建策略，默认是置顶的、最前，不分前后
        sb.append(String.format("access-list %s extended %s %s %s %s log disable\n", businessName, dto.getAction().toLowerCase(), serviceObject.getJoin(), srcAddress.getJoin(), dstAddress.getJoin()));
        sb.append("end\nwrite\n");
        sb.append("\n");

        return sb.toString();
    }

    public String getIdleTimeoutCmd(CommandlineDTO dto, String businessName) {
        if (dto.getIdleTimeout() != null) {
            StringBuffer stringBuffer = new StringBuffer();
            String ticket = dto.getName();

            String objName1 = String.format("%s_T_%s", ticket, IdGen.getRandomNumberString());
            stringBuffer.append(String.format("class-map %s \nmatch access-list %s\nexit\n", objName1, businessName));

            int timeStamp = dto.getIdleTimeout();
            int second = timeStamp % 60;
            int minuteTemp = timeStamp / 60;
            String object ;
            if (minuteTemp > 0) {
                int minute = minuteTemp % 60;
                int hour = minuteTemp / 60;
                if (hour > 0) {
                    object = (hour >= 10 ? (hour + "") : (hour)) + ":" + (minute >= 10 ? (minute + "") : (minute))
                            + ":" + (second >= 10 ? (second + "") : (second));
                } else {
                    object = "0:" + (minute >= 10 ? (minute + "") : (minute)) + ":"
                            + (second >= 10 ? (second + "") : (second));
                }
            } else {
                object = "0:0:" + (second >= 10 ? (second + "") : (second));
            }
            String objName2 = String.format("%s_AO_%s", ticket, IdGen.getRandomNumberString());
            stringBuffer.append(String.format("policy-map %s \nclass %s \nset connection timeout idle %s\nexit\nexit\n", objName2, objName1, object));
            return stringBuffer.toString();
        } else {
            return "";
        }
    }

    @Override
    public int getMaxNameLength() {
        return MAX_NAME_LENGTH;
    }

    public static void main(String[] args) {
        CommandlineDTO dto = CommandlineDTO.getInstanceDemo();
        SecurityCiscoASA99ForTp cisco = new SecurityCiscoASA99ForTp();
        List<String> restSrc = new ArrayList<>();
        //源地址
        restSrc.add("10.12.12.3-10.12.12.5");
        restSrc.add("10.12.12.3");
        dto.setSrcIp(StringUtils.join(restSrc.toArray(), ","));
//
        //目的地址
        List<String> restDst = new ArrayList<>();
        restDst.add("11.12.12.3");
        restDst.add("12.12.12.3-12.1.1.1,12.1.1.1");
//        restDst.add("12.12.12.3/24");
        dto.setDstIp(StringUtils.join(restDst.toArray(), ","));
        dto.setSrcIpSystem("");
        dto.setDstIpSystem("");

        List<ServiceDTO> serviceList = new ArrayList<>();
//        ServiceDTO serviceDTO = new ServiceDTO();
//        serviceDTO.setProtocol("6");
//        serviceDTO.setDstPorts("");
//        serviceDTO.setDstPorts("8000");
//        serviceList.add(serviceDTO);
        ServiceDTO serviceDTO2 = new ServiceDTO();
        serviceDTO2.setProtocol("6");
        serviceDTO2.setDstPorts("8080");
        serviceDTO2.setSrcPorts("");
        ServiceDTO serviceDTO1 = new ServiceDTO();
        serviceDTO2.setProtocol("17");
        serviceDTO2.setDstPorts("8080");
        serviceDTO2.setSrcPorts("");
        serviceList.add(serviceDTO1);
        serviceList.add(serviceDTO2);
        dto.setServiceList(serviceList);
//        dto.setIdleTimeout(2);
        dto.setIpType(0);
        dto.setDescription("aaaaa");

        dto.setAction("PERMIT");
        String commandLine = cisco.composite(dto);
        System.out.println("commandline:\n" + commandLine);
    }
}
