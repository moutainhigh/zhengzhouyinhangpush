package com.abtnetworks.totems.common.commandline.rollback;

import com.abtnetworks.totems.command.line.abs.OverAllGeneratorAbstractBean;
import com.abtnetworks.totems.command.line.enums.RuleIPTypeEnum;
import com.abtnetworks.totems.common.commandline.PolicyGenerator;
import com.abtnetworks.totems.common.commandline.common.dto.PolicyGeneratorDTO;
import com.abtnetworks.totems.common.dto.CmdDTO;
import com.abtnetworks.totems.common.dto.GeneratedObjectDTO;
import com.abtnetworks.totems.common.dto.PolicyDTO;
import com.abtnetworks.totems.common.enums.IpTypeEnum;
import com.abtnetworks.totems.common.enums.UrlTypeEnum;
import com.abtnetworks.totems.vender.h3c.security.SecurityH3cSecPathV7Impl;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Log4j2
public class RollbackSecurityH3cSecPathV7 implements PolicyGenerator {

    private OverAllGeneratorAbstractBean generatorBean;

    public RollbackSecurityH3cSecPathV7(){
        generatorBean = new SecurityH3cSecPathV7Impl();
    }

    @Override
    public String generate(CmdDTO cmdDTO) {
        StringBuilder sb = new StringBuilder();
        PolicyDTO policyDTO = cmdDTO.getPolicy();
        GeneratedObjectDTO generatedObjectDTO = cmdDTO.getGeneratedObject();
        boolean isIPv6 = false;
        if (cmdDTO.getPolicy().getUrlType() != null && cmdDTO.getPolicy().getIpType() != null &&
                cmdDTO.getPolicy().getIpType().intValue() == IpTypeEnum.URL.getCode() && cmdDTO.getPolicy().getUrlType().intValue() == UrlTypeEnum.IPV6.getCode()){
            isIPv6 = true;
        }
        if (cmdDTO.getPolicy().getIpType() != null && cmdDTO.getPolicy().getIpType().intValue() == IpTypeEnum.IPV6.getCode()){
            isIPv6 = true;
        }
        if(StringUtils.isNotEmpty(policyDTO.getSrcIp()) && policyDTO.getSrcIp().contains(":")) {
            isIPv6 = true;
        }
        if (cmdDTO.getDevice().isVsys()) {
            sb.append("system-view\n");
            sb.append("switchto context " + cmdDTO.getDevice().getVsysName() + "\n");
        }
        return sb.toString() + "system-view\n" +
                (isIPv6 ? "security-policy ipv6\n" : "security-policy ip\n") +
                "undo rule name " + cmdDTO.getSetting().getRandomNumberString() + "\n" +
                "quit\nreturn\n";

    }

    @Override
    public PolicyGeneratorDTO generateV2(CmdDTO cmdDTO) {
        PolicyGeneratorDTO policyGeneratorDTO = new PolicyGeneratorDTO();

        StringBuilder sb = new StringBuilder();
        PolicyDTO policyDTO = cmdDTO.getPolicy();
        GeneratedObjectDTO generatedObjectDTO = cmdDTO.getGeneratedObject();
        boolean isIPv6 = false;
        if (cmdDTO.getPolicy().getUrlType() != null && cmdDTO.getPolicy().getIpType() != null &&
                cmdDTO.getPolicy().getIpType().intValue() == IpTypeEnum.URL.getCode() && cmdDTO.getPolicy().getUrlType().intValue() == UrlTypeEnum.IPV6.getCode()){
            isIPv6 = true;
        }
        if (cmdDTO.getPolicy().getIpType() != null && cmdDTO.getPolicy().getIpType().intValue() == IpTypeEnum.IPV6.getCode()){
            isIPv6 = true;
        }
        if(StringUtils.isNotEmpty(policyDTO.getSrcIp()) && policyDTO.getSrcIp().contains(":")) {
            isIPv6 = true;
        }
        if (cmdDTO.getDevice().isVsys()) {
            sb.append("system-view\n");
            sb.append("switchto context " + cmdDTO.getDevice().getVsysName() + "\n");
        }
        
        String commandLine = sb.toString() + "system-view\n" +
                (isIPv6 ? "security-policy ipv6\n" : "security-policy ip\n") +
                "undo rule name " + cmdDTO.getSetting().getRandomNumberString() + "\n" +"quit\nreturn\n";



        StringBuffer objectRollbackCommandLine = new StringBuffer();
        try {
            log.info("生成对象回滚命令行 参数为:{}", JSONObject.toJSONString(generatedObjectDTO));
            List<String> addressGroupNames = generatedObjectDTO.getAddressObjectGroupNameList();
            List<String> addressNames = generatedObjectDTO.getAddressObjectNameList();
            List<String> serviceGroupNames = generatedObjectDTO.getServiceObjectGroupNameList();
            List<String> serviceNames = generatedObjectDTO.getServiceObjectNameList();
            List<String> timeNames = generatedObjectDTO.getTimeObjectNameList();

            if(CollectionUtils.isEmpty(addressGroupNames) && CollectionUtils.isEmpty(addressNames) &&
                    CollectionUtils.isEmpty(serviceGroupNames) && CollectionUtils.isEmpty(serviceNames) && CollectionUtils.isEmpty(timeNames)){
                policyGeneratorDTO.setPolicyRollbackCommandLine(commandLine);
                return policyGeneratorDTO;
            }


            if (cmdDTO.getDevice().isVsys()) {
                objectRollbackCommandLine.append("system-view\n");
                objectRollbackCommandLine.append("switchto context " + cmdDTO.getDevice().getVsysName() + "\n");
            }else{
                objectRollbackCommandLine.append("system-view\n");
            }
            RuleIPTypeEnum ipTypeEnum = null;
            if (isIPv6) {
                ipTypeEnum = RuleIPTypeEnum.IP6;
            } else {
                ipTypeEnum = RuleIPTypeEnum.IP4;
            }
            objectRollbackCommandLine.append(StringUtils.LF);


            // 地址组对象拼接对象回滚命令行
            if (CollectionUtils.isNotEmpty(addressGroupNames)) {
                for (String addressName : addressGroupNames) {
                    // 拼接对象回滚cmd
                    if (StringUtils.isBlank(addressName)) {
                        continue;
                    }

                    objectRollbackCommandLine.append(
                        generatorBean.deleteIpAddressObjectGroupCommandLine(ipTypeEnum, null, addressName, null, null));
                }
            }

            // 地址对象拼接对象回滚命令行
            if (CollectionUtils.isNotEmpty(addressNames)) {
                for (String addressName : addressNames) {
                    // 拼接对象回滚cmd
                    if (StringUtils.isBlank(addressName)) {
                        continue;
                    }
                    objectRollbackCommandLine.append(
                            generatorBean.deleteIpAddressObjectCommandLine(ipTypeEnum, null, addressName, null, null));
                }
            }

            // 服务组对象拼接对象回滚命令行
            if (CollectionUtils.isNotEmpty(serviceGroupNames)) {
                for (String serviceName : serviceGroupNames) {
                    // 拼接对象回滚cmd
                    if (StringUtils.isBlank(serviceName)) {
                        continue;
                    }
                    objectRollbackCommandLine
                        .append(generatorBean.deleteServiceObjectGroupCommandLine(null, null, serviceName, null, null));
                }
            }

            // 服务对象拼接对象回滚命令行
            if (CollectionUtils.isNotEmpty(serviceNames)) {
                for (String serviceName : serviceNames) {
                    // 拼接对象回滚cmd
                    if (StringUtils.isBlank(serviceName)) {
                        continue;
                    }
                    objectRollbackCommandLine
                        .append(generatorBean.deleteServiceObjectCommandLine(null, null, serviceName, null, null));
                }
            }

            // 时间对象拼接对象回滚命令行
            if (CollectionUtils.isNotEmpty(timeNames)) {
                for (String timeName : timeNames) {
                    // 拼接对象回滚cmd
                    if (StringUtils.isBlank(timeName)) {
                        continue;
                    }
                    objectRollbackCommandLine
                            .append(generatorBean.deleteAbsoluteTimeCommandLine(timeName, null, null));
                }
            }

            objectRollbackCommandLine.append("quit\n");

        } catch (Exception e) {
            log.error("调用原子化命令行拼接命令行失败,失败原因:{}", e);
        }
        policyGeneratorDTO.setPolicyRollbackCommandLine(commandLine);
        policyGeneratorDTO.setObjectRollbackCommandLine(objectRollbackCommandLine.toString());
        return policyGeneratorDTO;
    }
}
