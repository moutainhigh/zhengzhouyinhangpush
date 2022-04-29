package com.abtnetworks.totems.common.commandline.security;

import com.abtnetworks.totems.command.line.dto.AbsoluteTimeParamDTO;
import com.abtnetworks.totems.command.line.dto.IpAddressParamDTO;
import com.abtnetworks.totems.command.line.dto.ServiceParamDTO;
import com.abtnetworks.totems.command.line.enums.RuleIPTypeEnum;
import com.abtnetworks.totems.command.line.enums.StatusTypeEnum;
import com.abtnetworks.totems.common.commandline.PolicyGenerator;
import com.abtnetworks.totems.common.commandline.SecurityPolicyGenerator;
import com.abtnetworks.totems.common.constants.PolicyConstants;
import com.abtnetworks.totems.common.dto.*;
import com.abtnetworks.totems.common.dto.commandline.CommandlineDTO;
import com.abtnetworks.totems.common.dto.commandline.ServiceDTO;
import com.abtnetworks.totems.common.enums.ActionEnum;
import com.abtnetworks.totems.common.utils.DateUtils;
import com.abtnetworks.totems.common.utils.IP6Utils;
import com.abtnetworks.totems.common.utils.Param4CommandLineUtils;
import com.abtnetworks.totems.vender.h3c.acl.AclH3cSecPathV7Impl;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @Author: WangCan
 * @Description
 * @Date: 2021/6/22
 */
@Service
public class SecurityH3cSecPathV7ZonePair extends SecurityPolicyGenerator implements PolicyGenerator {
    private AclH3cSecPathV7Impl aclH3cSecPathV7;

    public SecurityH3cSecPathV7ZonePair() {
        aclH3cSecPathV7 = new AclH3cSecPathV7Impl();
    }

    private final static Logger LOGGER = LoggerFactory.getLogger(SecurityH3cSecPathV7ZonePair.class);

    @Override
    public String generate(CmdDTO cmdDTO) {
        LOGGER.info("cmdDTO is " + JSONObject.toJSONString(cmdDTO, true));
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

        dto.setCreateObjFlag(settingDTO.isCreateObject());

        ExistObjectDTO existObjectDTO = cmdDTO.getExistObject();
        dto.setSrcAddressName(existObjectDTO.getSrcAddressObjectName());
        dto.setDstAddressName(existObjectDTO.getDstAddressObjectName());
        dto.setServiceName(existObjectDTO.getServiceObjectName());
        dto.setExistSrcAddressList(existObjectDTO.getExistSrcAddressList());
        dto.setExistDstAddressList(existObjectDTO.getExistDstAddressList());
        dto.setExistServiceNameList(existObjectDTO.getExistServiceNameList());
        dto.setRestServiceList(existObjectDTO.getRestServiceList());
        dto.setRestSrcAddressList(existObjectDTO.getRestSrcAddressList());
        dto.setRestDstAddressList(existObjectDTO.getRestDstAddressList());
        LOGGER.info("dto is" + JSONObject.toJSONString(dto, true));
        GeneratedObjectDTO generatedDto = cmdDTO.getGeneratedObject();
        String commandLine = composite(dto);
        generatedDto.setPolicyName(dto.getName());
        generatedDto.setAclPolicyCommand(dto.getAclPolicyCommand());
        return commandLine;
    }

    @Override
    public String generatePreCommandline(CommandlineDTO dto) {
        return aclH3cSecPathV7.generatePreCommandline(dto.isVsys(),dto.getVsysName(),null,null);
    }

    @Override
    public String generateCommandline(CommandlineDTO dto) {
        StringBuffer sb = new StringBuffer();
        RuleIPTypeEnum ruleIPTypeEnum = RuleIPTypeEnum.IP4;
        if(IP6Utils.isIPv6(dto.getSrcIp()) || IP6Utils.isIPv6(dto.getDstIp())){
            ruleIPTypeEnum = RuleIPTypeEnum.IP6;
        }
        String srcIpName = null;
        String srcIpObjectCommandLine = null;
        // 地址复用和srcIp 不能同时使用
        if(StringUtils.isNotBlank(dto.getSrcAddressName())) {
            srcIpName = dto.getSrcAddressName();   //地址复用
        } else if(StringUtils.isNotBlank(dto.getSrcIp())){
            IpAddressParamDTO srcParamDTO = Param4CommandLineUtils.buildIpAddressParamDTO(dto.getSrcIp());
            if(StringUtils.isNotBlank(dto.getSrcIpSystem())){
                srcIpName = buildIpSystemName(dto.getSrcIpSystem());
            } else {
                srcIpName = aclH3cSecPathV7.createIpAddressObjectNameByParamDTO(srcParamDTO.getSingleIpArray(), srcParamDTO.getRangIpArray(), srcParamDTO.getSubnetIntIpArray(), srcParamDTO.getSubnetStrIpArray(),srcParamDTO.getHosts(), null,null, null);
            }
            srcParamDTO = Param4CommandLineUtils.buildIpAddressParamDTO(dto.getSrcIp());
            if(dto.isCreateObjFlag() || RuleIPTypeEnum.IP6.name().equalsIgnoreCase(ruleIPTypeEnum.name())){
                try {
                    srcIpObjectCommandLine = aclH3cSecPathV7.generateIpAddressObjectCommandLine(StatusTypeEnum.ADD,ruleIPTypeEnum,srcIpName,null,srcParamDTO.getSingleIpArray(), srcParamDTO.getRangIpArray(), srcParamDTO.getSubnetIntIpArray()
                            ,srcParamDTO.getSubnetStrIpArray(),null,srcParamDTO.getHosts(), null,null,null,null,null,null);
                } catch (Exception e) {
                    LOGGER.error("原子化命令行生成地址对象异常",e);
                }
            }
        } else {
            srcIpName = "any";
        }
        String dstIpName = null;
        String dstIpObjectCommandLine = null;
        if(StringUtils.isNotBlank(dto.getDstAddressName())) {
            dstIpName = dto.getDstAddressName();
        } else if (StringUtils.isNotBlank(dto.getDstIp())){
            IpAddressParamDTO dstParamDTO = Param4CommandLineUtils.buildIpAddressParamDTO(dto.getDstIp());
            if(StringUtils.isNotBlank(dto.getDstIpSystem())){
                dstIpName = buildIpSystemName(dto.getDstIpSystem());
            } else {
                dstIpName = aclH3cSecPathV7.createIpAddressObjectNameByParamDTO(dstParamDTO.getSingleIpArray(), dstParamDTO.getRangIpArray(), dstParamDTO.getSubnetIntIpArray(), dstParamDTO.getSubnetStrIpArray(),dstParamDTO.getHosts(), null,null, null);
            }
            if(dto.isCreateObjFlag() || RuleIPTypeEnum.IP6.name().equalsIgnoreCase(ruleIPTypeEnum.name())){
                try {
                    dstIpObjectCommandLine = aclH3cSecPathV7.generateIpAddressObjectCommandLine(StatusTypeEnum.ADD,ruleIPTypeEnum,dstIpName,null,dstParamDTO.getSingleIpArray(), dstParamDTO.getRangIpArray(), dstParamDTO.getSubnetIntIpArray()
                            ,dstParamDTO.getSubnetStrIpArray(),null,dstParamDTO.getHosts(), null,null,null,null,null,null);
                } catch (Exception e) {
                    LOGGER.error("原子化命令行生成地址对象异常",e);
                }
            }
        } else {
            dstIpName = "any";
        }
        String timeName = null;
        String timeObjectCommandLine = null;
        if(StringUtils.isNotBlank(dto.getStartTime()) && StringUtils.isNotBlank(dto.getEndTime())){
            AbsoluteTimeParamDTO absoluteTimeParamDTO = new AbsoluteTimeParamDTO(dto.getStartTime(), dto.getEndTime());
            timeName = aclH3cSecPathV7.createTimeObjectNameByAbsolute(absoluteTimeParamDTO, null, null);
            try {
                timeObjectCommandLine = aclH3cSecPathV7.generateAbsoluteTimeCommandLine(timeName, null, absoluteTimeParamDTO, null, null);
            } catch (Exception e) {
                LOGGER.error("原子化命令行生成时间对象异常",e);
            }
        }

        StringBuffer aclRollbackPolicyCommand = new StringBuffer();
        List<ServiceDTO> serviceList = dto.getServiceList();
        String srcZone = dto.getSrcZone();
        String dstZone = dto.getDstZone();
        if(StringUtils.isBlank(srcZone)){
            srcZone = "any";
        }
        if(StringUtils.isBlank(dstZone)){
            dstZone = "any";
        }
        String preCommandline = this.generatePreCommandline(dto);
        aclRollbackPolicyCommand.append(preCommandline);
        if(StringUtils.isNotBlank(srcIpObjectCommandLine)){
            sb.append(srcIpObjectCommandLine);
        }
        if(StringUtils.isNotBlank(dstIpObjectCommandLine)){
            sb.append(dstIpObjectCommandLine);
        }
        if(StringUtils.isNotBlank(timeObjectCommandLine)){
            sb.append(timeObjectCommandLine);
        }
        String searchZonePair = aclH3cSecPathV7.getDomainAcl(srcZone, dstZone);
        sb.append(searchZonePair);
        aclRollbackPolicyCommand.append(searchZonePair);
        String aclName = aclH3cSecPathV7.generateAclName("%s", "%s",ruleIPTypeEnum);
        String description = aclH3cSecPathV7.generateAclDescription(dto.getDescription());
        sb.append(aclName);
        sb.append(description);
        aclRollbackPolicyCommand.append(aclName);
        if (CollectionUtils.isNotEmpty(serviceList)) {
            for (ServiceDTO serviceDTO : serviceList) {
                List<ServiceParamDTO> serviceParamDTOS = Param4CommandLineUtils.buildServiceParamDTO(serviceDTO,true);
                for (ServiceParamDTO serviceParamDTO : serviceParamDTOS) {
                    try {
                        String commandLine = aclH3cSecPathV7.generateAclPolicyCommandLine(StatusTypeEnum.ADD, "%s","%s", ruleIPTypeEnum, dto.getAction(), null, null, null, serviceParamDTO,null,null, new String[]{srcIpName}, null, new String[]{dstIpName}, null, StringUtils.isBlank(timeName)?null:new String[]{timeName}, null,null);
                        String deleteCommandLine = aclH3cSecPathV7.generateAclPolicyCommandLine(StatusTypeEnum.DELETE, "%s","%s", ruleIPTypeEnum, dto.getAction(), null, null, null, serviceParamDTO,null,null, new String[]{srcIpName}, null, new String[]{dstIpName}, null, StringUtils.isBlank(timeName)?null:new String[]{timeName},null, null);
                        sb.append(commandLine);
                        aclRollbackPolicyCommand.append(deleteCommandLine);
                    } catch (Exception e) {
                        LOGGER.error("原子化命令行生成ACL策略异常",e);
                    }
                }
            }
        } else {
            try {
                String commandLine = aclH3cSecPathV7.generateAclPolicyCommandLine(StatusTypeEnum.ADD, "%s","%s", ruleIPTypeEnum, dto.getAction(), null, null, null, null,null,null, new String[]{srcIpName}, null,  new String[]{dstIpName}, null, StringUtils.isBlank(timeName)?null:new String[]{timeName},null, null);
                String deleteCommandLine = aclH3cSecPathV7.generateAclPolicyCommandLine(StatusTypeEnum.DELETE, "%s","%s", ruleIPTypeEnum, dto.getAction(), null, null, null, null,null,null,  new String[]{srcIpName}, null, new String[]{dstIpName}, null, StringUtils.isBlank(timeName)?null:new String[]{timeName},null, null);
                sb.append(commandLine);
                aclRollbackPolicyCommand.append(deleteCommandLine);
            } catch (Exception e) {
                LOGGER.error("原子化命令行生成ACL策略异常",e);
            }
        }
        String aclPostCommandline = aclH3cSecPathV7.generateAclPost();
        sb.append(aclPostCommandline);
        aclRollbackPolicyCommand.append(aclPostCommandline);
        String postCommandline = this.generatePostCommandline(dto);
        aclRollbackPolicyCommand.append(postCommandline);
        dto.setAclPolicyCommand(aclRollbackPolicyCommand.toString());
        return sb.toString();
    }

    @Override
    public String generatePostCommandline(CommandlineDTO dto) {
        return aclH3cSecPathV7.generatePostCommandline(null,null);
    }

    public String buildIpSystemName(String ipSystem){
        String name = StringUtils.EMPTY;
        if(StringUtils.isNotEmpty(ipSystem)){
            name = ipSystem;
            // 对象名称长度限制，一个中文2个字符
            name = strSub(name, 31,"GB2312");
            // 对象名称长度限制
            int len = 0;
            try{
                len = name.getBytes("GB2312").length;
            }catch (Exception e) {
                LOGGER.error("字符串长度计算异常");
            }
            if(len > 31 -7 ) {
                name = strSub(name, 31 -7, "GB2312");
            }
            name = String.format("%s_%s", name, DateUtils.getDate().replace("-","").substring(2));
        }
        return name;
    }
}
