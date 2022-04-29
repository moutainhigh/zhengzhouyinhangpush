package com.abtnetworks.totems.common.commandline.security;

import com.abtnetworks.totems.common.commandline.PolicyGenerator;
import com.abtnetworks.totems.common.commandline.SecurityPolicyGenerator;
import com.abtnetworks.totems.common.constants.PolicyConstants;
import com.abtnetworks.totems.common.dto.*;
import com.abtnetworks.totems.common.dto.commandline.CommandlineDTO;
import com.abtnetworks.totems.common.dto.commandline.PolicyObjectDTO;
import com.abtnetworks.totems.common.dto.commandline.ServiceDTO;
import com.abtnetworks.totems.common.enums.ActionEnum;
import com.abtnetworks.totems.common.enums.IpTypeEnum;
import com.abtnetworks.totems.common.enums.MoveSeatEnum;
import com.abtnetworks.totems.common.utils.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.*;

/**
 * @Description
 * @Version
 * @Created by hw on '2020/8/11'.
 */
@Slf4j
@Service
public class SecuritySangfor extends SecurityPolicyGenerator implements PolicyGenerator {

    private static Logger logger = LoggerFactory.getLogger(SecuritySangfor.class);

    @Override
    public String generate(CmdDTO cmdDTO) {
        CommandlineDTO dto = new CommandlineDTO();
        PolicyDTO policyDTO = cmdDTO.getPolicy();
        BeanUtils.copyProperties(policyDTO, dto);
        DeviceDTO deviceDTO = cmdDTO.getDevice();
        BeanUtils.copyProperties(deviceDTO, dto);
        SettingDTO settingDTO = cmdDTO.getSetting();
        BeanUtils.copyProperties(settingDTO, dto);
        dto.setSwapRuleNameId(settingDTO.getSwapNameId());
        if (policyDTO.getAction().equals(ActionEnum.PERMIT)) {
            dto.setAction(PolicyConstants.POLICY_STR_PERMISSION_PERMIT.toLowerCase());
        } else {
            dto.setAction(PolicyConstants.POLICY_STR_PERMISSION_DENY.toLowerCase());
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


        dto.setHasVsys(deviceDTO.isHasVsys());
        dto.setExistSrcAddressList(existObjectDTO.getExistSrcAddressList());
        dto.setRestSrcAddressList(existObjectDTO.getRestSrcAddressList());
        dto.setExistDstAddressList(existObjectDTO.getExistDstAddressList());
        dto.setRestDstAddressList(existObjectDTO.getRestDstAddressList());
        GeneratedObjectDTO generatedDto = cmdDTO.getGeneratedObject();
        generatedDto.setPolicyName(dto.getName());
        return composite(dto);
    }

    @Override
    public String generatePreCommandline(CommandlineDTO dto) {
        StringBuilder sb = new StringBuilder();
        sb.append("config\n");
        if (dto.isVsys()) {
            sb.append("vsys change " + dto.getVsysName() + "\n");
            sb.append("config\n");
        }
        return sb.toString();
    }

    @Override
    public String generateCommandline(CommandlineDTO dto) {
        return createCommandLine(dto);
    }

    @Override
    public String generatePostCommandline(CommandlineDTO dto) {
        return "end";
    }

    public String createCommandLine(CommandlineDTO dto) {

        boolean createObjFlag = dto.isCreateObjFlag();
        String srcIp = dto.getSrcIp();
        String dstIp = dto.getDstIp();
        List<ServiceDTO> serviceList = dto.getServiceList();
        List<String> srcAddrList = dto.getRestSrcAddressList();
        List<String> dstAddrList = dto.getRestDstAddressList();
        List<String> srcExitNameList = dto.getExistSrcAddressList();
        List<String> dstExitNameList = dto.getExistDstAddressList();
        List<ServiceDTO> serviceDTOList = dto.getRestServiceList();
        List<String> existServiceNameList = dto.getExistServiceNameList();

        String srcAddressName = dto.getSrcAddressName();
        String dstAddressName = dto.getDstAddressName();
        String serviceName = dto.getServiceName();

        boolean domain = false;

        if (StringUtils.isNotEmpty(dstIp)){
            String[] strings = dstIp.split(",");
            for (String string : strings) {
                if (TotemsIp6Utils.isIp6(string) || TotemsIp6Utils.isIp6Mask(string) || TotemsIp6Utils.isIp6Range(string) ||
                        TotemsIp4Utils.isIp4(string) || TotemsIp4Utils.isIp4Mask(string) || TotemsIp4Utils.isIp4Range(string)){
                }else {
                    domain = true;
                    break;
                }
            }
        }

        String ticket = dto.getName();
        StringBuilder sb = new StringBuilder();
        String srcIpCreate = generateAddressObject(srcIp, ticket, srcAddressName, srcExitNameList, srcAddrList, dto.getIpType(), dto.getUrlType());
        sb.append(srcIpCreate);
        if (!domain){
            String dstIpCreate = generateAddressObject(dstIp, ticket, dstAddressName, dstExitNameList, dstAddrList, dto.getIpType(), dto.getUrlType());
            sb.append(dstIpCreate);
        }

        String serviceCreate = generateServiceObject(serviceList, serviceName, serviceDTOList, existServiceNameList);
        sb.append(serviceCreate);

        PolicyObjectDTO timeObject = generateTimeObject(dto.getStartTime(), dto.getEndTime(), dto.getName());


        if (timeObject != null) {
            sb.append(timeObject.getCommandLine());
        }

        String setName = String.format("%s", dto.getName());
        sb.append(String.format("acl-policy %s\n", setName));

        String swapRuleNameId = StringUtils.isNotBlank(dto.getSwapRuleNameId()) ? dto.getSwapRuleNameId() : "";
        int moveSeatCode = dto.getMoveSeatEnum().getCode();
        if (moveSeatCode == MoveSeatEnum.BEFORE.getCode()) {
            if (StringUtils.isNotBlank(swapRuleNameId)) {
                sb.append(String.format("move before %s\n", swapRuleNameId));
            }
        } else if (moveSeatCode == MoveSeatEnum.AFTER.getCode()) {
            if (StringUtils.isNotBlank(swapRuleNameId)) {
                sb.append(String.format("move after %s\n", swapRuleNameId));
            }
        } else if (moveSeatCode == MoveSeatEnum.FIRST.getCode()) {
            sb.append("move top\n");
        } else {

        }

        if (StringUtils.isNotBlank(dto.getDescription())) {
            sb.append(String.format("description \"%s\"\n", dto.getDescription()));
        }

        if (StringUtils.isNotBlank(dto.getAction())) {
            sb.append(String.format("action %s\n", dto.getAction()));
        }

        if (StringUtils.isNotBlank(dto.getSrcZone())) {
            sb.append(String.format("src-zone %s\n", dto.getSrcZone()));
        }

        if (!CollectionUtils.isEmpty(srcExitNameList)) {
            for (String srcAddress : srcExitNameList) {
                sb.append(String.format("src-ipgroup %s \n", srcAddress));
            }

        }

        if (StringUtils.isNotBlank(dto.getDstZone())) {
            sb.append(String.format("dst-zone %s \n", dto.getDstZone()));
        }

        if (!CollectionUtils.isEmpty(dstExitNameList)) {
            for (String dstAddress : dstExitNameList) {
                sb.append(String.format("dst-ipgroup %s\n", dstAddress));
            }
        }else {
            if (domain){
                String[] strings = dstIp.split(",");
                for (String string : strings) {
                    if (TotemsIp6Utils.isIp6(string) || TotemsIp6Utils.isIp6Mask(string) || TotemsIp6Utils.isIp6Range(string) ||
                            TotemsIp4Utils.isIp4(string) || TotemsIp4Utils.isIp4Mask(string) || TotemsIp4Utils.isIp4Range(string)){
                    }else {
                        sb.append(String.format("dst-domain %s\n", string));
                    }
                }
                sb.append("enable").append(StringUtils.LF);
            }
        }

        if (serviceList.get(0).getProtocol().equals(PolicyConstants.POLICY_NUM_VALUE_ANY)) {
            sb.append("service any").append(StringUtils.LF);
        } else {
            if (!CollectionUtils.isEmpty(existServiceNameList)) {
                for (String serviceObject : existServiceNameList) {
                    sb.append(String.format("service %s\n", serviceObject));
                }
            }
        }


        if (StringUtils.isEmpty(srcIp)) {
            sb.append("src-ipgroup 全部").append(StringUtils.LF);
        }
        if (StringUtils.isEmpty(dstIp)) {
            sb.append("dst-ipgroup 全部").append(StringUtils.LF);
        }

        if (timeObject != null) {
            sb.append(String.format("schedule %s\n", timeObject.getName()));
        } else {
            sb.append(String.format("schedule 全天\n"));
        }
        sb.append("enable\n");


        return sb.toString();
    }

    /**
     * 生成地址对象
     *
     * @param ipAddress ip地址
     * @param ticket 主题名称
     * @param existsAddressName 已存在的地址对象
     * @return
     */

    private final static String SYMBOL1 = ",";
    private final static String SYMBOL2 = "-";
    private final static String SYMBOL3 = "/";

    /**
     * 生成地址对象
     *
     * @param ipAddress ip地址
     * @param ticket    主题名称
     * @param
     * @return 地址对象
     */
    public String generateAddressObject(String ipAddress, String ticket, String exitAddressName, List<String> exitAddressNameList, List<String> restAddressList, Integer ipType, Integer urlType) {
        StringBuilder sb = new StringBuilder();
        if (StringUtils.isNotEmpty(exitAddressName) && CollectionUtils.isEmpty(exitAddressNameList)) {
            exitAddressNameList.add(exitAddressName);
        } else {
            StringBuffer stringBuffer = new StringBuffer();
            for (String srcAddress : restAddressList) {
                stringBuffer.append(srcAddress).append(SYMBOL1);
            }
            if (stringBuffer.indexOf(SYMBOL1) >= 0) {
                stringBuffer = stringBuffer.deleteCharAt(stringBuffer.lastIndexOf(SYMBOL1));
            }
            sb.append(generateAddressObject(stringBuffer.toString(), ticket, exitAddressNameList, ipType, urlType));
        }

        return sb.toString();

    }

    /**
     * 生成地址对象
     *
     * @param ipAddress         ip地址
     * @param ticket            主题名称
     * @param existsAddressName 已存在的地址对象
     * @return 地址对象
     */
    public String generateAddressObject(String ipAddress, String ticket, List<String> existsAddressName, Integer ipType, Integer urlType) {
        StringBuilder sb = new StringBuilder();

        if (StringUtils.isEmpty(ipAddress)) {
            return "";
        }
        //判断 已存在的地址对象 是否为空
        String setName = String.format("%s_AO_%s", ticket, IdGen.getRandomNumberString());

        if ((ipType != null && ipType == 1) || (urlType != null && urlType == 1)) {
            sb.append(String.format("ipgroup %s ipv6\n", setName));
        } else {
            sb.append(String.format("ipgroup %s\n", setName));

        }
        sb.append("type ip\n");

        //循环获取ip
        String[] ipArray = StringUtils.split(ipAddress, SYMBOL1);
        for (String ip : ipArray) {
            if (IPUtil.isIPRange(ip) || IPUtil.isIPv6Range(ip)) {
                //判断 ip段
                sb.append(String.format("ipentry %s\n", ip));
            } else if (IPUtil.isSubnetMask(ip) || IP6Utils.isIPv6Subnet(ip)) {
                //判断是 子网
                sb.append(String.format("ipentry %s\n", ip));
            } else if (IPUtil.isIP(ip) || IPUtil.isIPv6(ip)) {
                //单个ip
                sb.append(String.format("ipentry %s\n", ip));
            }
        }
        sb.append("exit\n");

        existsAddressName.add(setName);
        return sb.toString();
}

    /**
     * 生成地址对象
     *
     * @param ipAddress         ip地址
     * @param ticket            主题名称
     * @param existsAddressName 已存在的地址对象
     * @return 地址对象
     */
    public PolicyObjectDTO generateAddressObjectForNat(String ipAddress, String ticket, String existsAddressName, Integer ipTpye) {
        //判断 已存在的地址对象 是否为空
        if (StringUtils.isNotBlank(existsAddressName)) {
            PolicyObjectDTO dto = new PolicyObjectDTO();
            dto.setJoin(existsAddressName);
            dto.setName(existsAddressName);
            dto.setObjectFlag(true);
            return dto;
        }

        //判断ip地址 是否为空
        if (StringUtils.isBlank(ipAddress)) {
            PolicyObjectDTO dto = new PolicyObjectDTO();
            dto.setJoin("");
            return dto;
        }

        PolicyObjectDTO dto = new PolicyObjectDTO();
        dto.setObjectFlag(true);

        StringBuilder sb = new StringBuilder();
        List<String> existsAddressNames = new LinkedList<>();

        String natIpCmd = generateAddressObject(ipAddress, ticket, existsAddressNames, ipTpye, null);

        dto.setObjectFlag(true);
        dto.setCommandLine(natIpCmd);
        dto.setName(existsAddressNames.get(0));
        dto.setJoin(existsAddressNames.get(0));
        return dto;
    }

    /**
     * 整体到离散复用
     *
     * @param serviceDTOList
     * @param serviceName
     * @param restServiceList
     * @param existsServiceName
     * @return
     */
    public String generateServiceObject(List<ServiceDTO> serviceDTOList, String serviceName, List<ServiceDTO> restServiceList, List<String> existsServiceName) {
        StringBuilder sb = new StringBuilder();
        if (CollectionUtils.isEmpty(serviceDTOList)) {
            return "";
        } else {
            if (PolicyConstants.POLICY_STR_VALUE_ANY.equalsIgnoreCase(ProtocolUtils.getProtocolByString(serviceDTOList.get(0).getProtocol()))) {
                return "";
            }
        }
        if (StringUtils.isNotEmpty(serviceName) && CollectionUtils.isEmpty(existsServiceName)) {
            existsServiceName.add(serviceName);
        } else {
            sb.append(generateServiceObject(restServiceList, existsServiceName));

        }
        return sb.toString();
    }

    /**
     * 生成服务对象
     *
     * @param serviceList       服务列表
     * @param existsServiceName 已存在的服务对象
     * @return 服务对象
     */
    public String generateServiceObject(List<ServiceDTO> serviceList, List<String> existsServiceName) {


        StringBuilder sb = new StringBuilder();
        if (CollectionUtils.isEmpty(serviceList)) {
            return "";
        }
        Map<String, String> mergeServiceDTOMap = new HashMap<>();

        for (ServiceDTO service : serviceList) {
            String protocol = service.getProtocol();
            String port = service.getDstPorts();
            if (PolicyConstants.POLICY_STR_VALUE_ANY.equalsIgnoreCase(ProtocolUtils.getProtocolByString(protocol))) {
                return "";
            }
            boolean portNotAny = !PolicyConstants.POLICY_STR_VALUE_ANY.equalsIgnoreCase(port) && StringUtils.isNotEmpty(port);
            if (portNotAny) {
                if (mergeServiceDTOMap.containsKey(protocol)) {
                    String newPort = mergeServiceDTOMap.get(protocol) + SYMBOL1 + port;
                    mergeServiceDTOMap.put(protocol, newPort);
                } else {
                    mergeServiceDTOMap.put(protocol, port);
                }
            } else {
                existsServiceName.add(ProtocolUtils.getProtocolByString(service.getProtocol()).toLowerCase());
            }
        }
        List<ServiceDTO> newServiceList = new LinkedList();
        for (String protocol : mergeServiceDTOMap.keySet()) {
            ServiceDTO service = new ServiceDTO();
            String port = mergeServiceDTOMap.get(protocol);
            service.setProtocol(protocol);
            service.setDstPorts(port);
            newServiceList.add(service);
        }
        if (CollectionUtils.isEmpty(newServiceList)) {
            return "";
        }


        for (ServiceDTO service : newServiceList) {
            int protocolNum = Integer.valueOf(service.getProtocol());

            String protocolString = ProtocolUtils.getProtocolByValue(protocolNum).toLowerCase();
            if (protocolString.equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_ANY)) {
                return "";
            }

            //建对象
            if (protocolString.equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_ICMP)) {

                //icmp协议没有端口
                sb.append("icmp type 255 code 255\n");
                sb.append("exit\n");
                existsServiceName.add(protocolString);
            } else {
                String[] dstPorts = StringUtils.split(service.getDstPorts(), SYMBOL1);
                //协议相同，端口逗号分开

                if (dstPorts != null && dstPorts.length > 0) {
                    for (String dstPort : dstPorts) {
                        String name = protocolString + dstPort;
                        ;
                        if (dstPort.equals(PolicyConstants.POLICY_STR_VALUE_ANY)) {
                            name = protocolString + "0-65535";
                            sb.append(String.format("service %s\n", name));

                            sb.append(String.format("%s dst-port 0-65535", protocolString)).append("\n");
                            sb.append("exit\n");
                            break;
                        } else if (PortUtils.isPortRange(dstPort)) {
                            String startPort = PortUtils.getStartPort(dstPort);
                            String endPort = PortUtils.getEndPort(dstPort);
                            sb.append(String.format("service %s\n", name));
                            sb.append(String.format("%s dst-port %s-%s", protocolString, startPort, endPort)).append("\n");
                            sb.append("exit\n");
                        } else {
                            sb.append(String.format("service %s\n", name));
                            sb.append(String.format("%s dst-port %s", protocolString, dstPort)).append("\n");
                            sb.append("exit\n");
                        }

                        existsServiceName.add(name);
                    }

                    if (sb.lastIndexOf(",") > 0) {
                        sb = sb.deleteCharAt(sb.lastIndexOf(","));
                    }
                } else {
                    String name = protocolString + "0-65535";
                    sb.append(String.format("service %s\n", name));
                    sb.append(String.format("%s dst-port 0-65535\n", protocolString));
                    sb.append("exit\n");
                    existsServiceName.add(name);
                }

            }
        }


        return sb.toString();
    }


    /**
     * 生成时间对象
     *
     * @param startTimeString 开始时间字符串
     * @param endTimeString   结束时间字符串
     * @return 时间对象
     */
    public PolicyObjectDTO generateTimeObject(String startTimeString, String endTimeString, String ticket) {
        if (StringUtils.isNoneBlank(startTimeString, endTimeString)) {
            String startTime = startTimeString;
            String endTime = endTimeString;

            String setName = String.format("%s_time_%s", ticket, IdGen.getRandomNumberString());

            StringBuilder sb = new StringBuilder();
            sb.append(String.format("single-schedule %s\n", setName));
            sb.append(String.format("absolute start \"%s\" end \"%s\"\n", startTime, endTime));
            sb.append("exit\n");
            PolicyObjectDTO dto = new PolicyObjectDTO();
            dto.setName(setName);
            dto.setJoin(setName);
            dto.setCommandLine(sb.toString());
            return dto;
        } else {
            return null;
        }
    }


//    /**
//     * 生成服务对象
//     * @param serviceList 服务列表
//     * @param existsServiceName 已存在的服务对象
//     * @return 服务对象
//     */
//    public PolicyObjectDTO generateServiceObject(List<ServiceDTO> serviceList, String existsServiceName) {
//
//        if (StringUtils.isNotBlank(existsServiceName)) {
//            PolicyObjectDTO dto = new PolicyObjectDTO();
//            dto.setName(existsServiceName);
//            dto.setObjectFlag(true);
//            dto.setJoin(existsServiceName);
//            return dto;
//        }
//
//        if (serviceList == null || serviceList.size() == 0) {
//            PolicyObjectDTO dto = new PolicyObjectDTO();
//
//            return dto;
//        }
//
//        PolicyObjectDTO dto = new PolicyObjectDTO();
//        StringBuilder sb = new StringBuilder();
//
//
//        for (ServiceDTO service : serviceList) {
//
//            int protocolNum = Integer.valueOf(service.getProtocol());
//            String protocolString = ProtocolUtils.getProtocolByValue(protocolNum).toLowerCase();
//            if(protocolString.equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_ANY)) {
//                return dto;
//            }
//
//            //建对象
//            if(protocolString.equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_ICMP)) {
//                String setName = getServiceName(service);
//                sb.append(String.format("service %s\n", setName));
//                //icmp协议没有端口
//                sb.append("icmp type 255 code 255\n");
//                sb.append("exit\n");
//                existsServiceName.
//            }else{
//                String[] dstPorts = StringUtils.split(service.getDstPorts(), SYMBOL1);
//                for(String dstPort: dstPorts) {
//                    String setName = getServiceName(service);
//                    sb.append(String.format("service %s\n", setName));
//                    sb.append(String.format("service %s protocol %s ", protocolString));
//
//                    if (dstPort.equals(PolicyConstants.POLICY_STR_VALUE_ANY)) {
//                        sb.append(String.format("%s dst-port 0-65535\n", protocolString));
//                    } else if (PortUtils.isPortRange(dstPort)) {
//                        String startPort = PortUtils.getStartPort(dstPort);
//                        String endPort = PortUtils.getEndPort(dstPort);
//                        sb.append(String.format("%s dst-port %s-%s\n", protocolString, startPort, endPort));
//                    } else {
//                        sb.append(String.format("%s dst-port %s\n", protocolString, dstPort));
//                    }
//
//                    sb.append("exit\n");
//                }
//            }
//        }
//
//
//
//        dto.setName(setName);
//        dto.setJoin(setName);
//        dto.setCommandLine(sb.toString());
//        dto.setObjectFlag(true);
//        return dto;
//    }

    public static void main(String[] args) {
        CommandlineDTO dto = CommandlineDTO.getInstanceDemo();

        SecuritySangfor sangfor = new SecuritySangfor();
        dto.setIpType(1);

        List<String> restDstAddressList = new ArrayList<>();
        restDstAddressList.add("2.2.2.2");
        dto.setRestDstAddressList(restDstAddressList);
        List<String> restSrcAddressList = new ArrayList<>();
        restSrcAddressList.add("4.5.4.2");
        dto.setRestSrcAddressList(restSrcAddressList);

        List<ServiceDTO> serviceDTOS = new ArrayList<>();
        ServiceDTO serviceDTO = new ServiceDTO();
        serviceDTO.setProtocol("6");
        serviceDTO.setDstPorts("42,45-47");
        ServiceDTO serviceDTO2 = new ServiceDTO();
        serviceDTO2.setProtocol("1");
        serviceDTO2.setDstPorts("any");
        serviceDTOS.add(serviceDTO);
        serviceDTOS.add(serviceDTO2);
        dto.setServiceList(serviceDTOS);
        dto.setRestServiceList(serviceDTOS);
        dto.setIdleTimeout(7834);
        dto.setStartTime("2008-1-3 10:33:13");
        dto.setEndTime("2008-1-3 10:33:16");
        String commandLine = sangfor.composite(dto);

        System.out.println("commandline:\n" + commandLine);

    }

    public String createMergeCommandLine(CommandlineDTO dto, Integer mergeProperty) {

        boolean createObjFlag = dto.isCreateObjFlag();
        String srcIp = dto.getSrcIp();
        String dstIp = dto.getDstIp();
        List<ServiceDTO> serviceList = dto.getServiceList();
        List<String> srcAddrList = dto.getRestSrcAddressList();
        List<String> dstAddrList = dto.getRestDstAddressList();
        List<String> srcExitNameList = dto.getExistSrcAddressList();
        List<String> dstExitNameList = dto.getExistDstAddressList();
        List<ServiceDTO> serviceDTOList = dto.getRestServiceList();
        List<String> existServiceNameList = dto.getExistServiceNameList();

        String srcAddressName = dto.getSrcAddressName();
        String dstAddressName = dto.getDstAddressName();
        String serviceName = dto.getServiceName();

        String ticket = dto.getName();
        StringBuilder sb = new StringBuilder();
        sb.append(generatePreCommandline(dto));

        String srcIpCreate = generateAddressObject(srcIp, ticket, srcAddressName, srcExitNameList, srcAddrList, dto.getIpType(), null);
        sb.append(srcIpCreate);
        String dstIpCreate = generateAddressObject(dstIp, ticket, dstAddressName, dstExitNameList, dstAddrList, dto.getIpType(), null);
        sb.append(dstIpCreate);

        String serviceCreate = generateServiceObject(serviceList, serviceName, serviceDTOList, existServiceNameList);
        sb.append(serviceCreate);

        String setName = String.format("%s", dto.getName());
        sb.append(String.format("acl-policy %s\n", setName));

        if (StringUtils.isNotBlank(dto.getDescription())) {
            sb.append(String.format("description \"%s\"\n", dto.getDescription()));
        }

        if (!CollectionUtils.isEmpty(srcExitNameList) && mergeProperty == 0) {
            for (String srcAddress : srcExitNameList) {
                sb.append(String.format("src-ipgroup %s \n", srcAddress));
            }

        }

        if (!CollectionUtils.isEmpty(dstExitNameList) && mergeProperty == 1) {
            for (String dstAddress : dstExitNameList) {
                sb.append(String.format("dst-ipgroup %s\n", dstAddress));
            }
        }

        if (serviceList.get(0).getProtocol().equals(PolicyConstants.POLICY_NUM_VALUE_ANY) && mergeProperty == 2) {
            sb.append("service any").append(StringUtils.LF);
        } else {
            if (!CollectionUtils.isEmpty(existServiceNameList) && mergeProperty == 2) {
                for (String serviceObject : existServiceNameList) {
                    sb.append(String.format("service %s\n", serviceObject));
                }
            }
        }

        if (StringUtils.isEmpty(srcIp) && mergeProperty == 0) {
            sb.append("src-ipgroup 全部").append(StringUtils.LF);
        }
        if (StringUtils.isEmpty(dstIp) && mergeProperty == 1) {
            sb.append("dst-ipgroup 全部").append(StringUtils.LF);
        }

        sb.append("enable\n");

        sb.append(generatePostCommandline(dto));
        return sb.toString();
    }


}
