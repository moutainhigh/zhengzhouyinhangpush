package com.abtnetworks.totems.common.commandline.rollback;

import com.abtnetworks.totems.command.line.abs.OverAllGeneratorAbstractBean;
import com.abtnetworks.totems.common.commandline.PolicyGenerator;
import com.abtnetworks.totems.common.commandline.common.dto.PolicyGeneratorDTO;
import com.abtnetworks.totems.common.commandline.nat.FortinetNat;
import com.abtnetworks.totems.common.dto.CmdDTO;
import com.abtnetworks.totems.common.dto.GeneratedObjectDTO;
import com.abtnetworks.totems.common.dto.commandline.SNatPolicyDTO;
import com.abtnetworks.totems.vender.fortinet.nat.NatFortinetForIosImpl;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Log4j2
public class RollbackSnatFotineForisOS implements PolicyGenerator {

    private OverAllGeneratorAbstractBean generatorBean;


    public RollbackSnatFotineForisOS(){
        generatorBean = new NatFortinetForIosImpl();
    }

    @Override
    public String generate(CmdDTO cmdDTO) {

        StringBuilder sb = new StringBuilder();
        if (cmdDTO.getDevice().isVsys()) {
            sb.append("config vdom\n");
            sb.append("edit " + cmdDTO.getDevice().getVsysName() + "\n");
        } else {
            if (cmdDTO.getDevice().isHasVsys()) {
                sb.append("config vdom\n");
                sb.append("edit root\n");
            }
        }
        sb.append(String.format("show firewall policy | grep -f %s %s",cmdDTO.getGeneratedObject().getPolicyName(),StringUtils.LF));
        sb.append("config firewall policy\n");
        sb.append("delete <policyId>\n");
        sb.append("end");
        return sb.toString();
    }


    @Override
    public PolicyGeneratorDTO generateV2(CmdDTO cmdDTO) {
        PolicyGeneratorDTO policyGeneratorDTO = new PolicyGeneratorDTO();

        StringBuilder sb = new StringBuilder();
        if (cmdDTO.getDevice().isVsys()) {
            sb.append("config vdom\n");
            sb.append("edit " + cmdDTO.getDevice().getVsysName() + "\n");
        } else {
            if (cmdDTO.getDevice().isHasVsys()) {
                sb.append("config vdom\n");
                sb.append("edit root\n");
            }
        }
        sb.append(String.format("show firewall policy | grep -f %s %s",cmdDTO.getGeneratedObject().getPolicyName(),StringUtils.LF));
        sb.append("config firewall policy\n");
        sb.append("delete <policyId>\n");
        sb.append("end");



        GeneratedObjectDTO generatedObject = cmdDTO.getGeneratedObject();

        StringBuffer objectRollbackCommandLine = new StringBuffer();
        try {
            log.info("??????????????????????????? ?????????:{}", JSONObject.toJSONString(generatedObject));
            List<String> addressNames = generatedObject.getAddressObjectNameList();
            List<String> serviceNames = generatedObject.getServiceObjectNameList();
            if (CollectionUtils.isEmpty(addressNames) && CollectionUtils.isEmpty(serviceNames)) {
                policyGeneratorDTO.setPolicyRollbackCommandLine(sb.toString());
                return policyGeneratorDTO;
            }

            objectRollbackCommandLine.append(StringUtils.LF);
            // ???????????????????????????????????????
            if (CollectionUtils.isNotEmpty(addressNames)) {
                objectRollbackCommandLine.append("config firewall address\n");
                for (String addressName : addressNames) {
                    // ??????????????????cmd
                    if (StringUtils.isBlank(addressName)) {
                        continue;
                    }
                    objectRollbackCommandLine.append(
                            generatorBean.deleteIpAddressObjectCommandLine(null, null, addressName, null, null));
                }
                objectRollbackCommandLine.append("end\n");
            }
            // ???????????????????????????????????????
            if (CollectionUtils.isNotEmpty(serviceNames)) {
                objectRollbackCommandLine.append("config firewall service custom\n");
                for (String serviceName : serviceNames) {
                    // ??????????????????cmd
                    if (StringUtils.isBlank(serviceName)) {
                        continue;
                    }
                    objectRollbackCommandLine
                            .append(generatorBean.deleteServiceObjectCommandLine(null, null, serviceName, null, null));
                }
                objectRollbackCommandLine.append("end\n");
            }

        } catch (Exception e) {
            log.error("?????????????????????????????????????????????,????????????:{}", e);
        }

        policyGeneratorDTO.setPolicyRollbackCommandLine(sb.toString());
        policyGeneratorDTO.setObjectRollbackCommandLine(objectRollbackCommandLine.toString());
        return policyGeneratorDTO;
    }
}
