package com.abtnetworks.totems.common.commandline.rollback;

import com.abtnetworks.totems.command.line.abs.OverAllGeneratorAbstractBean;
import com.abtnetworks.totems.common.commandline.PolicyGenerator;
import com.abtnetworks.totems.common.commandline.common.dto.PolicyGeneratorDTO;
import com.abtnetworks.totems.common.commandline.nat.CiscoASA99Nat;
import com.abtnetworks.totems.common.dto.CmdDTO;
import com.abtnetworks.totems.common.dto.GeneratedObjectDTO;
import com.abtnetworks.totems.common.dto.commandline.NatPolicyDTO;
import com.abtnetworks.totems.vender.cisco.nat.NatCiscoASA99Impl;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Log4j2
public class RollbackBothNatCiscoASA99 implements PolicyGenerator {

    private OverAllGeneratorAbstractBean generatorBean;

    public RollbackBothNatCiscoASA99() {
        generatorBean = new NatCiscoASA99Impl();
    }

    @Override
    public String generate(CmdDTO cmdDTO) {
        GeneratedObjectDTO generatedObjectDTO = cmdDTO.getGeneratedObject();
        String rollbackCommendLine = generatedObjectDTO.getRollbackCommandLine();
        return rollbackCommendLine;
    }

    @Override
    public PolicyGeneratorDTO generateV2(CmdDTO cmdDTO) {
        PolicyGeneratorDTO policyGeneratorDTO = new PolicyGeneratorDTO();

        GeneratedObjectDTO generatedObjectDTO = cmdDTO.getGeneratedObject();
        String rollbackCommendLine = generatedObjectDTO.getRollbackCommandLine();

        // 如果是生成回滚命令行
        StringBuffer objectRolllbackCommandLineSb = new StringBuffer();
        try {
            log.info("生成对象回滚命令行 参数为:{}", JSONObject.toJSONString(generatedObjectDTO));
            List<String> addressGroupNames = generatedObjectDTO.getAddressObjectGroupNameList();
            List<String> addressNames = generatedObjectDTO.getAddressObjectNameList();
            List<String> serviceGroupNames = generatedObjectDTO.getServiceObjectGroupNameList();
            List<String> serviceNames = generatedObjectDTO.getServiceObjectNameList();
            if (CollectionUtils.isEmpty(addressGroupNames) && CollectionUtils.isEmpty(addressNames)
                    && CollectionUtils.isEmpty(serviceGroupNames) && CollectionUtils.isEmpty(serviceNames)) {
                policyGeneratorDTO.setPolicyRollbackCommandLine(rollbackCommendLine);
                return policyGeneratorDTO;
            }

            objectRolllbackCommandLineSb.append(StringUtils.LF);

            // 前面回滚策略命令行已经有end和write了 本着不被坏之前的命令行的原则，这个地方重新进入视图
            objectRolllbackCommandLineSb.append(generatorBean.generatePreCommandline(
                    cmdDTO.getDevice().isVsys(), cmdDTO.getDevice().getVsysName(), null, null));

            // 地址组对象拼接命令行
            if (CollectionUtils.isNotEmpty(addressGroupNames)) {
                objectRolllbackCommandLineSb.append(StringUtils.LF);
                for (String addressName : addressGroupNames) {
                    // 拼接对象回滚cmd
                    if (StringUtils.isBlank(addressName)) {
                        continue;
                    }
                    objectRolllbackCommandLineSb.append(
                            generatorBean.deleteIpAddressObjectGroupCommandLine(null, null, addressName, null, null));
                }
            }

            // 地址对象拼接命令行
            if (CollectionUtils.isNotEmpty(addressNames)) {
                objectRolllbackCommandLineSb.append(StringUtils.LF);
                for (String addressName : addressNames) {
                    // 拼接对象回滚cmd
                    if (StringUtils.isBlank(addressName)) {
                        continue;
                    }
                    objectRolllbackCommandLineSb.append(
                            generatorBean.deleteIpAddressObjectCommandLine(null, null, addressName, null, null));
                }
            }

            // 服务组对象拼接命令行
            if (CollectionUtils.isNotEmpty(serviceGroupNames)) {
                objectRolllbackCommandLineSb.append(StringUtils.LF);
                for (String serviceGroupName : serviceGroupNames) {
                    // 拼接对象回滚cmd
                    if (StringUtils.isBlank(serviceGroupName)) {
                        continue;
                    }
                    objectRolllbackCommandLineSb.append(generatorBean.deleteServiceObjectGroupCommandLine(null,
                            null, serviceGroupName, null, null));
                }
            }

            // 服务对象拼接命令行
            if (CollectionUtils.isNotEmpty(serviceNames)) {
                objectRolllbackCommandLineSb.append(StringUtils.LF);
                for (String serviceName : serviceNames) {
                    // 拼接对象回滚cmd
                    if (StringUtils.isBlank(serviceName)) {
                        continue;
                    }
                    objectRolllbackCommandLineSb
                            .append(generatorBean.deleteServiceObjectCommandLine(null, null, serviceName, null, null));
                }
            }
            objectRolllbackCommandLineSb.append("\nend\nwrite\n");

        } catch (Exception e) {
            log.error("调用原子化命令行拼接命令行失败,失败原因:{}", e);
        }
        policyGeneratorDTO.setPolicyRollbackCommandLine(rollbackCommendLine);
        policyGeneratorDTO.setObjectRollbackCommandLine(objectRolllbackCommandLineSb.toString());
        return policyGeneratorDTO;
    }
}
