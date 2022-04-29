package com.abtnetworks.totems.common.commandline.rollback;

import com.abtnetworks.totems.command.line.abs.OverAllGeneratorAbstractBean;
import com.abtnetworks.totems.common.atomcommandline.base.BaseTopsecCommonBuss;
import com.abtnetworks.totems.common.commandline.PolicyGenerator;
import com.abtnetworks.totems.common.commandline.common.dto.PolicyGeneratorDTO;
import com.abtnetworks.totems.common.constants.CommonConstants;
import com.abtnetworks.totems.common.dto.CmdDTO;
import com.abtnetworks.totems.common.dto.GeneratedObjectDTO;
import com.abtnetworks.totems.vender.topsec.TOS_010.SecurityTopsec010Impl;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

import static com.abtnetworks.totems.common.commandline.NatPolicyGenerator.DO_NOT_SUPPORT;

/**
 * @author lifei
 * @desc 天融信dnat策略回滚命令行执行器
 * @date 2021/10/19 10:34
 */
@Slf4j
@Service
public class RollbackDnatTopsecTos010020  extends BaseTopsecCommonBuss implements PolicyGenerator {

    private OverAllGeneratorAbstractBean generatorBean;

    public RollbackDnatTopsecTos010020() {
        generatorBean = new SecurityTopsec010Impl();
    }

    @Override
    public String generate(CmdDTO cmdDTO) {
        log.info("天融信dnat 开始回滚---");
        GeneratedObjectDTO generatedObject = cmdDTO.getGeneratedObject();
        String rollbackShowCmd = generatedObject.getRollbackShowCmd();
        if (StringUtils.isBlank(rollbackShowCmd)) {
            return DO_NOT_SUPPORT;
        } else {
            StringBuffer stringBuffer = new StringBuffer();
            String policyId = cmdDTO.getSetting().getPolicyId();
            if (StringUtils.isBlank(policyId)) {
                stringBuffer.append("nat ").append(CommonConstants.POLICY_SHOW_TOP_SEC).append(" ")
                        .append(rollbackShowCmd).append("\n");
                stringBuffer.append("nat policy delete id ").append(CommonConstants.POLICY_ID).append("\n");
            } else {
                stringBuffer.append("nat policy delete id ").append(policyId).append("\n");
            }

            stringBuffer.append("end\n");
            return stringBuffer.toString();
        }
    }

    @Override
    public PolicyGeneratorDTO generateV2(CmdDTO cmdDTO) {
        PolicyGeneratorDTO policyGeneratorDTO = new PolicyGeneratorDTO();

        log.info("天融信dnat 开始回滚---");
        GeneratedObjectDTO generatedObject = cmdDTO.getGeneratedObject();
        String rollbackShowCmd = generatedObject.getRollbackShowCmd();
        StringBuffer stringBuffer = new StringBuffer();
        if (StringUtils.isBlank(rollbackShowCmd)) {
            policyGeneratorDTO.setPolicyRollbackCommandLine(DO_NOT_SUPPORT);
            return policyGeneratorDTO;
        }else {
            String policyId = cmdDTO.getSetting().getPolicyId();
            if (StringUtils.isBlank(policyId)) {
                stringBuffer.append("nat ").append(CommonConstants.POLICY_SHOW_TOP_SEC).append(" ")
                        .append(rollbackShowCmd).append("\n");
                stringBuffer.append("nat policy delete id ").append(CommonConstants.POLICY_ID).append("\n");
            } else {
                stringBuffer.append("nat policy delete id ").append(policyId).append("\n");
            }
            stringBuffer.append("end\n");
        }

        StringBuilder objRollbackCommandLine = new StringBuilder();
        log.info("生成对象回滚命令行的参数为:{}", JSONObject.toJSONString(generatedObject));
        Map<String,String> addressMap = generatedObject.getAddressTypeMap();
        List<String> serviceNames = generatedObject.getServiceObjectNameList();
        List<String> serviceObjectGroupNameList = generatedObject.getServiceObjectGroupNameList();
        List<String> timeNames = generatedObject.getTimeObjectNameList();
        if (addressMap.isEmpty() && CollectionUtils.isEmpty(serviceNames) && CollectionUtils.isEmpty(timeNames)
                && CollectionUtils.isEmpty(serviceObjectGroupNameList)) {
            policyGeneratorDTO.setPolicyRollbackCommandLine(stringBuffer.toString());
            return policyGeneratorDTO;
        }
        super.rollbackTopsecObject(objRollbackCommandLine,generatorBean,cmdDTO);

        policyGeneratorDTO.setPolicyRollbackCommandLine(stringBuffer.toString());
        policyGeneratorDTO.setObjectRollbackCommandLine(objRollbackCommandLine.toString());
        return policyGeneratorDTO;
    }
}
