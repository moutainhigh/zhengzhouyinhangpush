package com.abtnetworks.totems.common.commandline.rollback;

import com.abtnetworks.totems.command.line.abs.OverAllGeneratorAbstractBean;
import com.abtnetworks.totems.common.commandline.PolicyGenerator;
import com.abtnetworks.totems.common.commandline.common.dto.PolicyGeneratorDTO;
import com.abtnetworks.totems.common.dto.CmdDTO;
import com.abtnetworks.totems.common.dto.GeneratedObjectDTO;
import com.abtnetworks.totems.common.dto.commandline.CommandlineDTO;
import com.abtnetworks.totems.common.enums.IpTypeEnum;
import com.abtnetworks.totems.common.enums.UrlTypeEnum;
import com.abtnetworks.totems.vender.fortinet.security.SecurityFortinetImpl;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Log4j2
public class RollbackSecurityFotinetForiOS implements PolicyGenerator {

    private OverAllGeneratorAbstractBean generatorBean;


    public RollbackSecurityFotinetForiOS(){
        generatorBean = new SecurityFortinetImpl();
    }

    @Override
    public String generate(CmdDTO cmdDTO) {
        CommandlineDTO commandlineDTO = new CommandlineDTO();
        commandlineDTO.setCurrentId(cmdDTO.getSetting().getPolicyId());

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
        boolean isIpv6 = false;
        if (cmdDTO.getPolicy().getUrlType() != null && cmdDTO.getPolicy().getIpType() != null &&
                cmdDTO.getPolicy().getIpType().intValue() == IpTypeEnum.URL.getCode() && cmdDTO.getPolicy().getUrlType().intValue() == UrlTypeEnum.IPV6.getCode()){
            isIpv6 = true;
        }
        if ((cmdDTO.getPolicy().getIpType() != null && cmdDTO.getPolicy().getIpType().intValue() == IpTypeEnum.IPV6.getCode()) || isIpv6){
            sb.append("config firewall policy6\n");
        }else {
            sb.append("config firewall policy\n");
        }
        sb.append("delete <policyId>\n");
        sb.append("end");
        return sb.toString();
    }

    @Override
    public PolicyGeneratorDTO generateV2(CmdDTO cmdDTO) {
        PolicyGeneratorDTO policyGeneratorDTO = new PolicyGeneratorDTO();

        CommandlineDTO commandlineDTO = new CommandlineDTO();
        commandlineDTO.setCurrentId(cmdDTO.getSetting().getPolicyId());

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
        boolean isIpv6 = false;
        if (cmdDTO.getPolicy().getUrlType() != null && cmdDTO.getPolicy().getIpType() != null &&
                cmdDTO.getPolicy().getIpType().intValue() == IpTypeEnum.URL.getCode() && cmdDTO.getPolicy().getUrlType().intValue() == UrlTypeEnum.IPV6.getCode()){
            isIpv6 = true;
        }
        if ((cmdDTO.getPolicy().getIpType() != null && cmdDTO.getPolicy().getIpType().intValue() == IpTypeEnum.IPV6.getCode()) || isIpv6){
            sb.append("config firewall policy6\n");
        }else {
            sb.append("config firewall policy\n");
        }
        sb.append("delete <policyId>\n");
        sb.append("end");

        GeneratedObjectDTO generatedObject = cmdDTO.getGeneratedObject();
        StringBuffer objectRollbackCommandLine = new StringBuffer();
        try {
            log.info("生成对象回滚命令行 参数为:{}", JSONObject.toJSONString(generatedObject));
            List<String> addressNames = generatedObject.getAddressObjectNameList();
            List<String> serviceNames = generatedObject.getServiceObjectNameList();
            if (CollectionUtils.isEmpty(addressNames) && CollectionUtils.isEmpty(serviceNames)) {
                policyGeneratorDTO.setPolicyRollbackCommandLine(sb.toString());
                return policyGeneratorDTO;
            }

            objectRollbackCommandLine.append(StringUtils.LF);
            // 地址对象拼接对象回滚命令行
            if (CollectionUtils.isNotEmpty(addressNames)) {
                objectRollbackCommandLine.append("config firewall address\n");
                for (String addressName : addressNames) {
                    // 拼接对象回滚cmd
                    if (StringUtils.isBlank(addressName)) {
                        continue;
                    }
                    objectRollbackCommandLine.append(
                            generatorBean.deleteIpAddressObjectCommandLine(null, null, addressName, null, null));
                }
                objectRollbackCommandLine.append("end\n");
            }

            // 服务对象拼接对象回滚命令行
            if (CollectionUtils.isNotEmpty(serviceNames)) {
                objectRollbackCommandLine.append("config firewall service custom\n");
                for (String serviceName : serviceNames) {
                    // 拼接对象回滚cmd
                    if (StringUtils.isBlank(serviceName)) {
                        continue;
                    }
                    objectRollbackCommandLine.append(String.format("delete %s", serviceName));
                    objectRollbackCommandLine.append(StringUtils.LF);
                    // 这里不用原子化命令行的原因是 多个服务对象的时候原子化命令行多次进入目录，导致导致下发报错,也就是多次执行config firewall service custom
//                    objectRollbackCommandLine
//                            .append(generatorBean.deleteServiceObjectCommandLine(null, null, serviceName, null, null));
                }
                objectRollbackCommandLine.append("end\n");
            }

        } catch (Exception e) {
            log.error("调用原子化命令行拼接命令行失败,失败原因:{}", e);
        }

        policyGeneratorDTO.setPolicyRollbackCommandLine(sb.toString());
        policyGeneratorDTO.setObjectRollbackCommandLine(objectRollbackCommandLine.toString());
        return policyGeneratorDTO;
    }
}
