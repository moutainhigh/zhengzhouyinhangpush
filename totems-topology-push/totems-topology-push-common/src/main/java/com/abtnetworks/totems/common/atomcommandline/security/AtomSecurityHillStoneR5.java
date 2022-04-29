package com.abtnetworks.totems.common.atomcommandline.security;

import com.abtnetworks.totems.command.line.abs.OverAllGeneratorAbstractBean;
import com.abtnetworks.totems.command.line.dto.ZoneParamDTO;
import com.abtnetworks.totems.command.line.enums.StatusTypeEnum;
import com.abtnetworks.totems.common.commandline.PolicyGenerator;
import com.abtnetworks.totems.common.commandline.SecurityPolicyGenerator;
import com.abtnetworks.totems.common.constants.PolicyConstants;
import com.abtnetworks.totems.common.dto.*;
import com.abtnetworks.totems.common.dto.commandline.CommandlineDTO;
import com.abtnetworks.totems.common.dto.commandline.PolicyMergeDTO;
import com.abtnetworks.totems.common.dto.commandline.PolicyObjectDTO;
import com.abtnetworks.totems.common.dto.commandline.ServiceDTO;
import com.abtnetworks.totems.common.enums.ActionEnum;
import com.abtnetworks.totems.common.enums.IpTypeEnum;
import com.abtnetworks.totems.common.utils.*;
import com.abtnetworks.totems.vender.hillstone.security.SecurityHillStoneR5Impl;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * @Author: wenjiachang
 * @Date: 2019/1/29 9:41
 */
@Service
public class AtomSecurityHillStoneR5 extends SecurityPolicyGenerator implements PolicyGenerator {

    private SecurityHillStoneR5Impl generatorBean;

    private static Logger logger = Logger.getLogger(AtomSecurityHillStoneR5.class);

    private static Set<Integer> allowType = new HashSet<>();

    private final int MAX_NAME_LENGTH = 95;

    private final int DAY_SECOND = 24 * 60 * 60;

    public AtomSecurityHillStoneR5() {
        generatorBean = new SecurityHillStoneR5Impl();
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
        logger.info("cmdDTO is " + cmdDTO);
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
        dto.setSwapRuleNameId(settingDTO.getSwapNameId());
        dto.setIdleTimeout(policyDTO.getIdleTimeout());

        // ip类型默认为ipv4
        if (ObjectUtils.isEmpty(dto.getIpType())) {
            dto.setIpType(IpTypeEnum.IPV4.getCode());
        }

        GeneratedObjectDTO generatedDto = cmdDTO.getGeneratedObject();
        generatedDto.setPolicyName(dto.getName());
        generatedDto.setVsys(dto.isVsys());
        generatedDto.setVsysName(dto.getVsysName());
        generatedDto.setHasVsys(dto.isHasVsys());
        String commandLine = composite(dto);
        generatedDto.setAddressObjectNameList(dto.getAddressObjectNameList());
        generatedDto.setAddressObjectGroupNameList(dto.getAddressObjectGroupNameList());
        generatedDto.setServiceObjectNameList(dto.getServiceObjectNameList());
        generatedDto.setServiceObjectGroupNameList(dto.getServiceObjectGroupNameList());
        generatedDto.setTimeObjectNameList(dto.getTimeObjectNameList());
        return commandLine;
    }

    @Override
    public String generatePreCommandline(CommandlineDTO dto) {
        return generatorBean.generatePreCommandline(dto.isVsys(),dto.getVsysName(),null,null);
    }

    @Override
    public String generateCommandline(CommandlineDTO dto) {

        if (dto.getMergeDTO() == null || dto.isMustCreateFlag()) {
            return createCommandLine(dto);
        } else {
            return StringUtils.EMPTY;
        }
    }


    public String createCommandLine(CommandlineDTO dto) {
        StringBuilder sb = new StringBuilder();
        String startTime = dto.getStartTime();
        String endTime = dto.getEndTime();
        boolean createObjFlag = dto.isCreateObjFlag();  //true:需要生成地址对象然后引用  false:直接使用IP地址
        List<String> createAddressObjectNames = new ArrayList<>();
        List<String> createServiceObjectNames = new ArrayList<>();
        List<String> createServiceGroupObjectNames = new ArrayList<>();
        List<String> createTimeObjectNames = new ArrayList<>();
        List<String> srcRefIpAddressNames = Param4CommandLineUtils.getRefIpAddressNames(dto.getSrcAddressName(), dto.getExistSrcAddressList(), dto.getRestSrcAddressList(), dto.getSrcIpSystem(), sb, createObjFlag,generatorBean);
        createAddressObjectNames.addAll(srcRefIpAddressNames);
        List<String> dstRefIpAddressNames = Param4CommandLineUtils.getRefIpAddressNames(dto.getDstAddressName(), dto.getExistDstAddressList(), dto.getRestDstAddressList(), dto.getDstIpSystem(), sb, createObjFlag,generatorBean);
        createAddressObjectNames.addAll(dstRefIpAddressNames);
        List<String> refServiceNames = Param4CommandLineUtils.getRefServiceNames(dto.getServiceName(), dto.getExistServiceNameList(), dto.getRestServiceList(),sb,generatorBean);
        createServiceObjectNames.addAll(refServiceNames);
        String refTimeName = "";
        if(StringUtils.isNotBlank(endTime)){
            // 结束时间不能为空
            refTimeName = Param4CommandLineUtils.getRefTimeName(startTime, endTime, sb,generatorBean);
            createTimeObjectNames.add(refTimeName);
        }
        // 统计创建地址名称
        dto.setAddressObjectNameList(createAddressObjectNames);
        dto.setServiceObjectNameList(createServiceObjectNames);
        dto.setServiceObjectGroupNameList(createServiceGroupObjectNames);
        dto.setTimeObjectNameList(createTimeObjectNames);
        String securityCl = null;
        try {
            securityCl = generatorBean.generateSecurityPolicyCommandLine(StatusTypeEnum.ADD, null,dto.getBusinessName(), null, dto.getAction().toLowerCase(), dto.getDescription(), null, null, null, com.abtnetworks.totems.command.line.enums.MoveSeatEnum.getByCode(dto.getMoveSeatEnum().getCode()), dto.getSwapRuleNameId(),
                    null, null, null, null, null, new ZoneParamDTO(dto.getSrcZone()), new ZoneParamDTO(dto.getDstZone()), null, null, srcRefIpAddressNames.toArray(new String[0]), null, dstRefIpAddressNames.toArray(new String[0]),null, refServiceNames.toArray(new String[0]), null, new String[]{refTimeName}, null, null);
        } catch (Exception e) {
            logger.error("原子化命令行创建安全策略异常",e);
        }
        sb.append(securityCl);
        return sb.toString();
    }

    @Override
    public String generatePostCommandline(CommandlineDTO dto) {
        return generatorBean.generatePostCommandline(null,null);
    }

    public PolicyObjectDTO generateAddressObject(List<String> existAddressList, List<String> restAddressList, String ticket,
                                                 String ipPrefix, boolean createObjFlag, String ipSystem, Integer ipType) {
        PolicyObjectDTO policyObjectDTO = new PolicyObjectDTO();
        policyObjectDTO.setName("any");
        if (ipType.intValue() == IpTypeEnum.IPV4.getCode()) {
            policyObjectDTO.setJoin(ipPrefix + "-addr any\n");
        } else {
            policyObjectDTO.setJoin(ipPrefix + "-addr IPv6-any\n");
        }
        policyObjectDTO.setObjectFlag(true);

        if (restAddressList.size() > 0) {
            StringBuilder sb = new StringBuilder();
            for (String addr : restAddressList) {
                sb.append(",");
                sb.append(addr);
            }
            if (sb.length() > 0) {
                sb.deleteCharAt(0);
            }
            policyObjectDTO = generateAddressObject(sb.toString(), ticket, ipPrefix, createObjFlag, "", ipSystem, ipType);
        }

        if (existAddressList.size() > 0) {
            for (String existName : existAddressList) {
                policyObjectDTO.setJoin((policyObjectDTO.getJoin().contains("any") ? "" : policyObjectDTO.getJoin()) + ipPrefix + "-addr " + existName + "\n");
            }
        }

        logger.info("policyObjectDTO is " + JSONObject.toJSONString(policyObjectDTO));
        return policyObjectDTO;
    }

    public PolicyObjectDTO generateAddressObject(String ipAddress, String ticket, String ipPrefix, boolean createObjFlag, String existsAddressName, String ipSystem, Integer ipType) {
        PolicyObjectDTO dto = new PolicyObjectDTO();

        if (AliStringUtils.isEmpty(ipAddress) || ipAddress.equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_ANY)) {
            if (ipType.intValue() == IpTypeEnum.IPV4.getCode()) {
                dto.setJoin(ipPrefix + "-addr any\n");
                dto.setName("any");
                dto.setObjectFlag(true);
                return dto;
            } else {
                dto.setJoin(ipPrefix + "-addr IPv6-any\n");
                dto.setName("any");
                dto.setObjectFlag(true);
                return dto;
            }
        }

        if (StringUtils.isNotBlank(existsAddressName)) {
            dto.setObjectFlag(true);
            if (existsAddressName.indexOf("\"") < 0) {
                existsAddressName = "\"" + existsAddressName + "\"";
            }
            dto.setJoin(ipPrefix + "-addr " + existsAddressName + "\n");
            dto.setName(existsAddressName);
            return dto;
        }

        dto.setObjectFlag(createObjFlag);

        String[] arr = ipAddress.split(",");
        StringBuilder sb = new StringBuilder();
        int index = 1;
        for (String address : arr) {
            // 是创建对象
            if (createObjFlag) {
                formatFullAddress(address, sb, ipPrefix, createObjFlag, dto, ticket, ipSystem, arr.length, ipType, index);
                sb.append("exit\n");
                dto.setCommandLine(sb.toString());
            } else {
                //直接显示内容
                formatFullAddress(address, sb, ipPrefix, createObjFlag, dto, ticket, ipSystem, arr.length, ipType, index);
                dto.setCommandLine(sb.toString());
                dto.setName(sb.toString());
            }
            index++;
        }
        return dto;
    }

    public PolicyObjectDTO generateAddressObjectForNat(String ipAddress, String ticket, String ipPrefix, boolean createObjFlag, String existsAddressName, String ipSystem) {
        PolicyObjectDTO dto = new PolicyObjectDTO();

        if (AliStringUtils.isEmpty(ipAddress) || ipAddress.equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_ANY)) {
            dto.setJoin(ipPrefix + "-addr any\n");
            dto.setName("any");
            dto.setObjectFlag(true);
            return dto;
        }

        if (StringUtils.isNotBlank(existsAddressName)) {
            dto.setObjectFlag(true);
            if (existsAddressName.indexOf("\"") < 0) {
                existsAddressName = "\"" + existsAddressName + "\"";
            }
            dto.setJoin(ipPrefix + "-addr " + existsAddressName + "\n");
            dto.setName(existsAddressName);
            return dto;
        }

        dto.setObjectFlag(createObjFlag);

        String[] arr = ipAddress.split(",");
        StringBuilder sb = new StringBuilder();

        String name;
        if (StringUtils.isNotEmpty(ipSystem)) {
            name = ipSystem;
        } else {
            name = String.format("%s_AO_%s", ticket, IdGen.getRandomNumberString());
        }

        sb.append(String.format("address %s\n", name));
        if (arr.length > 1) {

            for (String address : arr) {
                String fullStr = "";
                if (IpUtils.isIPSegment(address)) {
                    fullStr = String.format("ip %s\n", address);
                } else if (IpUtils.isIPRange(address)) {
                    String startIp = IpUtils.getStartIpFromRange(address);
                    String endIp = IpUtils.getEndIpFromRange(address);
                    fullStr = String.format("range %s %s\n", startIp, endIp);
                } else {
                    fullStr = String.format("ip %s/32\n", address);
                }
                sb.append(fullStr);
            }

            sb.append("exit\n");
            dto.setName(name);
            dto.setCommandLine(sb.toString());
        } else {
            if (arr.length == 1) {
                //直接显示内容
                formatFullAddress(arr[0], sb, ipPrefix, false, dto, ticket, ipSystem, arr.length, 0, 0);
                dto.setCommandLine(sb.toString());
                dto.setName(sb.toString());
            }

        }
        return dto;
    }

    private void formatFullAddress(String address, StringBuilder sb, String ipPrefix, boolean createObjFlag, PolicyObjectDTO dto, String ticket, String ipSystem, int length,
                                   Integer ipType, int index) {
        String name;
        StringBuilder serialNum = new StringBuilder();
        if (StringUtils.isNotEmpty(ipSystem)) {
            name = ipSystem;
            if (length > 1) {
                serialNum.append("_").append(index);
            }
        } else {
            name = String.format("%s_AO_%s", ticket, IdGen.getRandomNumberString());
        }

        if (StringUtils.isNotEmpty(ipSystem)) {
            // 对象名称长度限制，一个中文2个字符
            name = strSub(name, getMaxNameLength(), "GB2312");
            // 对象名称长度限制
            int len = 0;
            try {
                len = name.getBytes("GB2312").length;
            } catch (Exception e) {
                logger.error("字符串长度计算异常");
            }
            // 序列号长度
            int serialLengh = serialNum.length();
            if (len > getMaxNameLength() - (7 + serialLengh)) {
                name = strSub(name, getMaxNameLength() - (7 + serialLengh), "GB2312");
            }
            name = String.format("%s_%s", name, DateUtils.getDate().replace("-", "").substring(2));
            if (USE_RAW_NAME) {
                name = ipSystem;
            }
            name = name + serialNum.toString();
        }

        String fullStr = "";
        if (ipType.intValue() == IpTypeEnum.IPV4.getCode()) {
            if (IpUtils.isIPSegment(address)) {
                fullStr = String.format("ip %s\n", address);
            } else if (IpUtils.isIPRange(address)) {
                String startIp = IpUtils.getStartIpFromRange(address);
                String endIp = IpUtils.getEndIpFromRange(address);
                fullStr = String.format("range %s %s\n", startIp, endIp);
            } else {
                fullStr = String.format("ip %s/32\n", address);
            }
            name = String.format("\"%s\"", name);
            dto.setName(name);
            sb.append(String.format("address %s\n", name));
            if (dto.getJoin() != null) {
                dto.setJoin(dto.getJoin() + ipPrefix + "-addr " + name + "\n");
            } else {
                dto.setJoin(ipPrefix + "-addr " + dto.getName() + "\n");
            }
        } else if (ipType.intValue() == IpTypeEnum.IPV6.getCode()) {
            // ipv6
            if (address.contains("/")) {
                fullStr = String.format("ip %s\n", address);
            } else if (address.contains("-")) {
                // 范围
                String startIp = IpUtils.getRangeStartIPv6(address);
                String endIp = IpUtils.getRangeEndIPv6(address);
                fullStr = String.format("range %s %s\n", startIp, endIp);
            } else {
                fullStr = String.format("ip %s/128\n", address);
            }
            name = String.format("\"%s\"", name);
            dto.setName(name);
            sb.append(String.format("address %s  ipv6\n", name));
            if (dto.getJoin() != null) {
                dto.setJoin(dto.getJoin() + ipPrefix + "-addr " + name + "\n");
            } else {
                dto.setJoin(ipPrefix + "-addr " + dto.getName() + "\n");
            }
        } else {
            // 目的地址是URL类型
            // ipv4
            if (IpUtils.isIPSegment(address)) {
                fullStr = String.format("ip %s\n", address);
            } else if (IpUtils.isIPRange(address)) {
                String startIp = IpUtils.getStartIpFromRange(address);
                String endIp = IpUtils.getEndIpFromRange(address);
                fullStr = String.format("range %s %s\n", startIp, endIp);
            } else if (IpUtils.isIPv6(address)) {
                // ipv6
                if (address.contains("/")) {
                    fullStr = String.format("ip %s\n", address);
                } else if (address.contains("-")) {
                    // 范围
                    String startIp = IpUtils.getRangeStartIPv6(address);
                    String endIp = IpUtils.getRangeEndIPv6(address);
                    fullStr = String.format("range %s %s\n", startIp, endIp);
                } else {
                    fullStr = String.format("ip %s/128\n", address);
                }
            } else if (IpUtils.isIP(address)) {
                fullStr = String.format("ip %s/32\n", address);
            } else {
                // 域名
                fullStr = String.format("host %s\n", address);
            }
            name = String.format("\"%s\"", address);
            dto.setName(name);
            sb.append(String.format("address %s\n", name));
            if (dto.getJoin() != null) {
                dto.setJoin(dto.getJoin() + ipPrefix + "-addr " + name + "\n");
            } else {
                dto.setJoin(ipPrefix + "-addr " + dto.getName() + "\n");
            }
        }

        if (createObjFlag) {
            sb.append(fullStr);
        } else {
            sb.append(ipPrefix + "-" + fullStr);
        }
    }


    public static void main(String[] args) {
        CommandlineDTO dto = CommandlineDTO.getInstanceDemo();
        AtomSecurityHillStoneR5 hillStoneR5 = new AtomSecurityHillStoneR5();
        String commandLine = hillStoneR5.composite(dto);
        System.out.println("commandline:\n" + commandLine);
    }


}
