package com.abtnetworks.totems.common.commandline.rollback;

import com.abtnetworks.totems.command.line.abs.OverAllGeneratorAbstractBean;
import com.abtnetworks.totems.common.atomcommandline.base.BaseHillStoneCommonBuss;
import com.abtnetworks.totems.common.commandline.PolicyGenerator;
import com.abtnetworks.totems.common.commandline.common.dto.PolicyGeneratorDTO;
import com.abtnetworks.totems.common.dto.CmdDTO;
import com.abtnetworks.totems.common.dto.DeviceDTO;
import com.abtnetworks.totems.common.dto.GeneratedObjectDTO;
import com.abtnetworks.totems.vender.Usg.security.SecurityUsg6000Impl;
import com.abtnetworks.totems.vender.hillstone.security.SecurityHillStoneR5Impl;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author luwei
 * @date 2020/7/18
 */
@Service
@Log4j2
public class RollbackSecurityHillstoneR5 extends BaseHillStoneCommonBuss implements PolicyGenerator {

    protected OverAllGeneratorAbstractBean generatorBean;


    public RollbackSecurityHillstoneR5(){
        generatorBean = new SecurityHillStoneR5Impl();
    }

    @Override
    public String generate(CmdDTO cmdDTO) {
        GeneratedObjectDTO generatedObjectDTO = cmdDTO.getGeneratedObject();

        StringBuilder sb = new StringBuilder();
        DeviceDTO deviceDTO =  cmdDTO.getDevice();
        if (deviceDTO.isVsys()) {
            sb.append("enter-vsys " + deviceDTO.getVsysName() + "\n");
        }
        sb.append("configure\n");
        sb.append("policy-global\n");
        if (cmdDTO.getSetting().getRollbackType()){
            sb.append(String.format("no rule name %s \n", generatedObjectDTO.getPolicyName()));
        }else {
            sb.append(String.format("no rule id #1 \n"));
        }

        sb.append("end\n");
        return sb.toString();
    }

    @Override
    public PolicyGeneratorDTO generateV2(CmdDTO cmdDTO) {
        PolicyGeneratorDTO policyGeneratorDTO = new PolicyGeneratorDTO();

        GeneratedObjectDTO generatedObjectDTO = cmdDTO.getGeneratedObject();

        StringBuilder sb = new StringBuilder();
        DeviceDTO deviceDTO =  cmdDTO.getDevice();
        if (deviceDTO.isVsys()) {
            sb.append("enter-vsys " + deviceDTO.getVsysName() + "\n");
        }
        sb.append("configure\n");
        sb.append("policy-global\n");
        if (cmdDTO.getSetting().getRollbackType()){
            sb.append(String.format("no rule name %s \n", generatedObjectDTO.getPolicyName()));
        }else {
            sb.append(String.format("no rule id #1 \n"));
        }
        sb.append("end\n");


        StringBuilder objRollbackCommandLine = new StringBuilder();
        log.info("生成对象回滚命令行的参数为:{}", JSONObject.toJSONString(generatedObjectDTO));
        List<String> addressNames = generatedObjectDTO.getAddressObjectNameList();
        List<String> serviceNames = generatedObjectDTO.getServiceObjectNameList();
        List<String> serviceObjectGroupNameList = generatedObjectDTO.getServiceObjectGroupNameList();
        List<String> timeNames = generatedObjectDTO.getTimeObjectNameList();
        if (CollectionUtils.isEmpty(addressNames) && CollectionUtils.isEmpty(serviceNames) && CollectionUtils.isEmpty(timeNames)
                && CollectionUtils.isEmpty(serviceObjectGroupNameList)) {
            policyGeneratorDTO.setPolicyRollbackCommandLine(sb.toString());
            return policyGeneratorDTO;
        }
        super.rollbackHillStoneObject(objRollbackCommandLine,generatorBean,cmdDTO);

        policyGeneratorDTO.setPolicyRollbackCommandLine(sb.toString());
        policyGeneratorDTO.setObjectRollbackCommandLine(objRollbackCommandLine.toString());
        return policyGeneratorDTO;
    }
}
