package com.abtnetworks.totems.common.commandline.rollback.security;

import com.abtnetworks.totems.common.annotation.CustomCli;
import com.abtnetworks.totems.common.commandline.PolicyGenerator;
import com.abtnetworks.totems.common.commandline.rollback.RollbackSecurityDpTechR004;
import com.abtnetworks.totems.common.dto.CmdDTO;
import com.abtnetworks.totems.common.dto.GeneratedObjectDTO;
import com.abtnetworks.totems.common.enums.DeviceModelNumberEnum;
import com.abtnetworks.totems.common.enums.PolicyEnum;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@CustomCli(value = DeviceModelNumberEnum.DPTECHR004, type = PolicyEnum.SECURITY, classPoxy = RollbackSecurityDpTechR004.class)
public class RollbackSecurityDpTechR004ForZheS implements PolicyGenerator {
    @Override
    public String generate(CmdDTO cmdDTO) {
        GeneratedObjectDTO generatedObjectDTO = cmdDTO.getGeneratedObject();
        Integer ipType = cmdDTO.getPolicy().getIpType();
        StringBuffer sb = new StringBuffer();
        sb.append("conf-mode\n");
        if(null == ipType || 0 == ipType || 2 == ipType){
            //ipv4的回滚策略
            sb.append(String.format("no security-policy %s\n",generatedObjectDTO.getPolicyName()));
        }else{
            //ipv6的回滚策略
            sb.append(String.format("no ipv6 security-policy %s\n",generatedObjectDTO.getPolicyName()));
        }
        sb.append("exit\n");
        sb.append(StringUtils.LF);
        sb.append("end").append(StringUtils.LF);
        sb.append("write file").append(StringUtils.LF);
        return sb.toString();
    }
}
