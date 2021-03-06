package com.abtnetworks.totems.common.commandline.rollback;

import com.abtnetworks.totems.command.line.abs.OverAllGeneratorAbstractBean;
import com.abtnetworks.totems.command.line.enums.RuleIPTypeEnum;
import com.abtnetworks.totems.common.commandline.PolicyGenerator;
import com.abtnetworks.totems.common.commandline.common.dto.PolicyGeneratorDTO;
import com.abtnetworks.totems.common.dto.CmdDTO;
import com.abtnetworks.totems.common.dto.GeneratedObjectDTO;
import com.abtnetworks.totems.common.enums.IpTypeEnum;
import com.abtnetworks.totems.vender.fortinet.nat.NatFortinetForIosImpl;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author lifei
 * @desc 回滚飞塔bothnat命令行
 * @date 2021/12/3 14:26
 */
@Service
@Log4j2
public class RollbackBothFortineForisOS implements PolicyGenerator {

    private OverAllGeneratorAbstractBean generatorBean;


    public RollbackBothFortineForisOS(){
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
        sb.append("config firewall policy\n");
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

            RuleIPTypeEnum ipTypeEnum = null;
            if (IpTypeEnum.IPV6.getCode().equals(generatedObject.getIpType())) {
                ipTypeEnum = RuleIPTypeEnum.IP6;
            } else {
                ipTypeEnum = RuleIPTypeEnum.IP4;
            }
            // 地址对象拼接对象回滚命令行
            if (CollectionUtils.isNotEmpty(addressNames)) {
                for (String addressName : addressNames) {
                    // 拼接对象回滚cmd
                    if (StringUtils.isBlank(addressName)) {
                        continue;
                    }
                    objectRollbackCommandLine.append(
                            generatorBean.deleteIpAddressObjectCommandLine(ipTypeEnum, addressName, addressName, null, null));
                    objectRollbackCommandLine.append("end\n");
                }
            }
            // 服务对象拼接对象回滚命令行
            if (CollectionUtils.isNotEmpty(serviceNames)) {
                objectRollbackCommandLine.append("config firewall service custom\n");
                for (String serviceName : serviceNames) {
                    // 拼接对象回滚cmd
                    if (StringUtils.isBlank(serviceName)) {
                        continue;
                    }
                    objectRollbackCommandLine
                            .append(generatorBean.deleteServiceObjectCommandLine(null, null, serviceName, null, null));
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
