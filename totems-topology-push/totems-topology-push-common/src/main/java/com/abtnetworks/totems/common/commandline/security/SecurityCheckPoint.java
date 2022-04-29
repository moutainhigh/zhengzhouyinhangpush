package com.abtnetworks.totems.common.commandline.security;

import com.abtnetworks.totems.command.line.dto.IpAddressRangeDTO;
import com.abtnetworks.totems.command.line.dto.IpAddressSubnetIntDTO;
import com.abtnetworks.totems.command.line.enums.StatusTypeEnum;
import com.abtnetworks.totems.common.commandline.PolicyGenerator;
import com.abtnetworks.totems.common.commandline.SecurityPolicyGenerator;
import com.abtnetworks.totems.common.commandline.common.service.AddressObjectCommonService;
import com.abtnetworks.totems.common.constants.CommonConstants;
import com.abtnetworks.totems.common.constants.PolicyConstants;
import com.abtnetworks.totems.common.dto.*;
import com.abtnetworks.totems.common.dto.commandline.CommandlineDTO;
import com.abtnetworks.totems.common.dto.commandline.PolicyObjectDTO;
import com.abtnetworks.totems.common.dto.commandline.ServiceDTO;
import com.abtnetworks.totems.common.enums.ActionEnum;
import com.abtnetworks.totems.common.enums.MoveSeatEnum;
import com.abtnetworks.totems.common.utils.*;
import com.abtnetworks.totems.vender.checkpoint.security.SecurityCheckpointImpl;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.*;

/**
 * @Description
 * @Version
 * @Created by hw on '2020/8/12 14:29'.
 */
@Slf4j
@Service
public class SecurityCheckPoint extends SecurityPolicyGenerator implements PolicyGenerator {

    private static Logger logger = LoggerFactory.getLogger(SecurityCheckPoint.class);

    @Autowired
    AddressObjectCommonService addressObjectCommonService;

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


        return "";
    }

    @Override
    public String generateCommandline(CommandlineDTO dto) {
        return createCommandLine(dto);
    }

    public String createCommandLine(CommandlineDTO dto) {
        boolean createObjFlag = dto.isCreateObjFlag();
        PolicyObjectDTO srcAddressObject = generateAddressObject(dto.getSrcIp(), dto.getName(), dto.getSrcAddressName(), dto.getExistSrcAddressList(), dto.getRestSrcAddressList(),dto.getSrcIpSystem());
        PolicyObjectDTO dstAddressObject = generateAddressObject(dto.getDstIp(), dto.getName(), dto.getDstAddressName(), dto.getExistDstAddressList(), dto.getRestDstAddressList(),dto.getDstIpSystem());
        PolicyObjectDTO serviceObject = generateServiceObject( dto.getServiceName(), dto.getRestServiceList(), dto.getExistServiceNameList());
        PolicyObjectDTO timeObject = generateTimeObject(dto.getStartTime(), dto.getEndTime(), dto.getName());

        StringBuilder sb = new StringBuilder();

        //先定义对象
        if (srcAddressObject.isObjectFlag() && StringUtils.isNotBlank(srcAddressObject.getCommandLine())) {
            sb.append(String.format("%s", srcAddressObject.getCommandLine()));
        }

        if (dstAddressObject.isObjectFlag() && StringUtils.isNotBlank(dstAddressObject.getCommandLine())) {
            sb.append(String.format("%s", dstAddressObject.getCommandLine()));
        }

        if (serviceObject.isObjectFlag() && StringUtils.isNotBlank(serviceObject.getCommandLine())) {
            sb.append(String.format("%s", serviceObject.getCommandLine()));
        }
        if (timeObject != null) {
            sb.append(timeObject.getCommandLine());
        }

        String layerName = dto.getLayerName();
        sb.append(String.format("mgmt add access-rule layer \"%s\" ", layerName));

        String swapRuleNameId = StringUtils.isNotBlank(dto.getSwapRuleNameId()) ? dto.getSwapRuleNameId() : "";
        int moveSeatCode = dto.getMoveSeatEnum().getCode();
        if (moveSeatCode == MoveSeatEnum.BEFORE.getCode()) {
            if (StringUtils.isNotBlank(swapRuleNameId)) {
                sb.append(String.format("position.above \"%s\" ", swapRuleNameId));
            }
        } else if (moveSeatCode == MoveSeatEnum.AFTER.getCode()) {
            //暂无
        } else if (MoveSeatEnum.FIRST.getCode() == moveSeatCode) {
            sb.append("position \"top\" ");
        } else {
            sb.append("position \"bottom\" ");
        }

        String setName = String.format("%s", dto.getName());

        sb.append(String.format("name \"%s\" ", setName));


        if (!CollectionUtils.isEmpty(srcAddressObject.getExistAddressNameList())) {
            int i = 1;
            for (String srcName : srcAddressObject.getExistAddressNameList()) {
                sb.append(String.format("source.%s \"%s\" ", String.valueOf(i), srcName));
                i++;

            }

        }

        if (!CollectionUtils.isEmpty(dstAddressObject.getExistAddressNameList())) {
            int i = 1;
            for (String dstName : dstAddressObject.getExistAddressNameList()) {
                sb.append(String.format("destination.%s \"%s\" ", String.valueOf(i), dstName));
                i++;

            }

        }

        if (!CollectionUtils.isEmpty(serviceObject.getExistServiceNameList())) {
            int i = 1;
            for (String serviceName : serviceObject.getExistServiceNameList()) {
                String[] array = StringUtils.split(serviceName, SYMBOL1);
                for (String serviceArr: array) {
                    sb.append(String.format("service.%s \"%s\" ", String.valueOf(i), serviceArr));
                    i++;
                }


            }
        }

        if (timeObject != null && StringUtils.isNotBlank(timeObject.getJoin())) {
            sb.append(String.format("time \"%s\" ", timeObject.getJoin()));
        }

        if (StringUtils.isNotBlank(dto.getAction())) {
            if (PolicyConstants.POLICY_STR_PERMISSION_PERMIT.equalsIgnoreCase(dto.getAction())) {
                sb.append(String.format("action \"Accept\""));
            } else {
                sb.append(String.format("action \"Drop\""));
            }

        }

//        sb.append(String.format(" track.type \"log\" \n"));
        sb.append(String.format(" track.type \"log\" %s\n", CommonConstants.INSTALL_LAYER));
//        sb.append(String.format(" track.type \"log\" \n"));   2021 0113 oppo实施时需要

        return sb.toString();
    }

    @Override
    public String generatePostCommandline(CommandlineDTO dto) {
        return "";
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
     * 地址整体和离散复用前置判读
     *
     * @param ipAddress
     * @param ticket
     * @param existsAddressName
     * @return
     */
    public PolicyObjectDTO generateAddressObject(String ipAddress, String ticket, String existsAddressName, List<String> exitAddressNameList, List<String> restAddressList,String ipSystem) {
        PolicyObjectDTO policyObjectDTO = new PolicyObjectDTO();

        StringBuilder sb = new StringBuilder();
        if (StringUtils.isNotBlank(existsAddressName)) {
            List<String> existAddressNameList = new ArrayList<>();
            existAddressNameList.add(existsAddressName);
            policyObjectDTO.setExistAddressNameList(existAddressNameList);
        } else {
            sb.append(generateAddressObject(restAddressList, ticket, exitAddressNameList, ipSystem));
            policyObjectDTO.setExistAddressNameList(exitAddressNameList);
            policyObjectDTO.setCommandLine(sb.toString());
        }

        policyObjectDTO.setObjectFlag(true);
        return policyObjectDTO;
    }

    /**
     * 生成地址对象
     *
     * @param restAddressList         ip地址
     * @param ticket            主题名称
     * @param existsAddressName 已存在的地址对象
     * @return 地址对象
     */
    private String generateAddressObject(List<String> restAddressList, String ticket, List<String> existsAddressName,String ipSystem) {
        StringBuilder sb = new StringBuilder();
        StringBuilder nameSB = new StringBuilder();
        try {
            //原子化命令行工具类
            SecurityCheckpointImpl securityCheckpoint = new SecurityCheckpointImpl();
            //判断 已存在的地址对象 是否为空
            int index = 1;
            for (String ip : restAddressList) {
                StringBuilder serialNum = new StringBuilder();
                String name = null;
                // 确认地址对象名称
                if(StringUtils.isNotEmpty(ipSystem)){
                    name = ipSystem;
                    if(restAddressList.size() > 1){
                        serialNum.append("_").append(index);
                    }
                    name = name + serialNum;
                }


                String ipName = ip;
                if (ip.contains(SYMBOL2)) {
                    //判断 ip段
                    String[] array = StringUtils.split(ip, SYMBOL2);
                    if(IpUtils.isIPv6Range(ip)){
                        ipName = String.format("%s_%s",array[0],array[1].substring(array[1].lastIndexOf(":")));
                    }
                    String line = String.format("mgmt add address-range name \"%s\" ip-address-first \"%s\" ip-address-last \"%s\"\n", StringUtils.isNotBlank(name) ? name : ipName, array[0], array[1]);
                    sb.append(line + "\n");
                } else if (ip.contains(SYMBOL3)) {
                    //判断是 子网
                    ipName = ip.replace("/","_");
                    String[] array = StringUtils.split(ip, SYMBOL3);
                    String endIp = IPUtil.getMaskMap(array[1]);
                    if(IpUtils.isIPv6Subnet(ip)){
                        // mgmt add network name "Subnet1" subnet "ff00::ff00" mask-length "120"
                        IpAddressSubnetIntDTO ipAddressSubnetIntDTO = new IpAddressSubnetIntDTO();
                        ipAddressSubnetIntDTO.setIp(array[0]);
                        ipAddressSubnetIntDTO.setMask(Integer.parseInt(array[1]));
                        sb.append(securityCheckpoint.generateSubnetIntIpV6CommandLine(StatusTypeEnum.ADD,new IpAddressSubnetIntDTO[]{ipAddressSubnetIntDTO},null,null,new String[]{StringUtils.isNotBlank(name) ? name : ipName}));
                    }else {
                        sb.append(String.format("mgmt add network name \"%s\" subnet \"%s\" subnet-mask \"%s\"\n", StringUtils.isNotBlank(name) ? name : ipName, array[0], endIp));
                    }
                } else if(IPUtil.isIP(ip) || IpUtils.isIPv6(ip)){
                    //单个ip
                    sb.append(String.format("mgmt add host name \"%s\" ip-address \"%s\"\n", StringUtils.isNotBlank(name) ? name : ipName, ip));
                } else {
                    //域名
                    ipName='.'+ip;
                    sb.append(securityCheckpoint.generateHostCommandLine(StatusTypeEnum.ADD,new String[]{ip},null,null));
                }
                if (StringUtils.isBlank(nameSB.toString())) {
                    nameSB.append(String.format("%s", StringUtils.isNotBlank(name) ? name : ipName));
                } else {
                    nameSB.append(String.format(",%s", StringUtils.isNotBlank(name) ? name : ipName));
                }
                existsAddressName.add(StringUtils.isNotBlank(name) ? name : ipName);

                index++;
            }
        }catch (Exception e){
            logger.info("",e);
        }
        return sb.toString();
    }

    /**
     * 整体到离散复用
     *
     * @param serviceName
     * @param restServiceList
     * @param existsServiceName
     * @return
     */
    public PolicyObjectDTO generateServiceObject(String serviceName, List<ServiceDTO> restServiceList, List<String> existsServiceName) {
        StringBuilder sb = new StringBuilder();
        PolicyObjectDTO dto = new PolicyObjectDTO();
        if (StringUtils.isNotEmpty(serviceName)) {
            List<String> existsServiceName1 = new ArrayList<>();
            existsServiceName1.add(serviceName);
            dto.setExistServiceNameList(existsServiceName1);
        } else {
            sb.append(generateServiceObject(restServiceList, existsServiceName));
            dto.setExistServiceNameList(existsServiceName);

        }

        dto.setCommandLine(sb.toString());
        dto.setObjectFlag(true);

        return dto;
    }

    /**
     * 生成服务对象
     *
     * @param serviceList       服务列表
     * @param existsServiceName 已存在的服务对象
     * @return 服务对象
     */
    private String generateServiceObject(List<ServiceDTO> serviceList, List<String> existsServiceName) {
        Map<String, String> mergeServiceDTOMap = new HashMap<>();
        if (CollectionUtils.isEmpty(serviceList)) {
            return "";
        }
        for (ServiceDTO service : serviceList) {
            String protocol = service.getProtocol();
            String port = service.getDstPorts();
            if (mergeServiceDTOMap.containsKey(protocol)) {
                String newPort = mergeServiceDTOMap.get(protocol) + "," + port;
                mergeServiceDTOMap.put(protocol, newPort);
            } else {
                mergeServiceDTOMap.put(protocol, port);
            }
        }


        StringBuilder sb = new StringBuilder();
        StringBuilder nameSB = new StringBuilder();


        for (String protocol : mergeServiceDTOMap.keySet()) {
            int protocolNum = Integer.valueOf(protocol);
            ServiceDTO service = new ServiceDTO();
            String port = mergeServiceDTOMap.get(protocol);
            service.setProtocol(protocol);
            service.setDstPorts(port);
            String protocolString = ProtocolUtils.getProtocolByValue(protocolNum).toLowerCase();
            if (protocolString.equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_ANY)) {
                return "";
            }

            //建对象
            if (protocolString.equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_ICMP)) {
                //icmp协议没有端口
                nameSB.append(protocolString);
            } else {
                String[] dstPorts = StringUtils.split(service.getDstPorts(), SYMBOL1);
                for (String dstPort : dstPorts) {
                    String serviceName;
                    if (dstPort.equals(PolicyConstants.POLICY_STR_VALUE_ANY)) {
                        serviceName = protocolString;
                        sb.append(String.format("mgmt add service-%s name \"%s\" port 1-65535\n", protocolString, serviceName));
                    } else if (PortUtils.isPortRange(dstPort)) {
                        String startPort = PortUtils.getStartPort(dstPort);
                        String endPort = PortUtils.getEndPort(dstPort);
                        serviceName = String.format("%s%s-%s", protocolString, startPort, endPort);
                        sb.append(String.format("mgmt add service-%s name \"%s\" port %s-%s\n", protocolString, serviceName, startPort, endPort));
                    } else {
                        serviceName = String.format("%s%s", protocolString, dstPort);
                        sb.append(String.format("mgmt add service-%s name \"%s\" port %s\n", protocolString, serviceName, dstPort));
                    }

                    if (StringUtils.isBlank(nameSB.toString())) {
                        nameSB.append(String.format("%s,", serviceName));
                    } else {
                        nameSB.append(String.format("%s,", serviceName));
                    }

                }
            }
        }
        if (nameSB.indexOf(",") > 0) {
            nameSB = nameSB.deleteCharAt(nameSB.lastIndexOf(","));
        }

        existsServiceName.add(nameSB.toString());
        return sb.toString();
    }

    /**
     * 生成时间对象
     *
     * @param startTimeString 开始时间字符串
     * @param endTimeString   结束时间字符串
     * @return 时间对象
     */
    private PolicyObjectDTO generateTimeObject(String startTimeString, String endTimeString, String ticket) {
        if (StringUtils.isNoneBlank(startTimeString, endTimeString)) {

            Date startDate = DateUtils.parseDate(startTimeString);
            Date endDate = DateUtils.parseDate(endTimeString);

            String startY = String.format(Locale.US, "%1$td-%1$tb-%1$tY", startDate);
            String startM = String.format(Locale.US, "%tR", startDate);

            String endY = String.format(Locale.US, "%1$td-%1$tb-%1$tY", endDate);
            String endM = String.format(Locale.US, "%tR", endDate);

            String setName = String.format("%s_T_%s", ticket, IdGen.getRandomNumberString(3));
            if(setName.length()>11){
                setName  =  setName.substring(setName.length()-11);
            }
            StringBuilder sb = new StringBuilder();

            sb.append(String.format("mgmt add time name \"%s\" start-now \"false\" start.date \"%s\" start.time \"%s\" end.date \"%s\" end.time \"%s\" end-never \"false\"\n", setName, startY, startM, endY, endM));

            PolicyObjectDTO dto = new PolicyObjectDTO();
            dto.setName(setName);
            dto.setJoin(setName);
            dto.setCommandLine(sb.toString());
            return dto;
        } else {
            return null;
        }
    }

    public static void main(String[] args) {
        CommandlineDTO dto = CommandlineDTO.getInstanceDemo();

        SecurityCheckPoint checkPoint = new SecurityCheckPoint();
        String commandLine = checkPoint.composite(dto);
        System.out.println("commandline:\n" + commandLine);

    }
}
