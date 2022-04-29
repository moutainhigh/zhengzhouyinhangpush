package com.abtnetworks.totems.common.commandline.rollback;

import com.abtnetworks.totems.common.commandline.PolicyGenerator;
import com.abtnetworks.totems.common.dto.CmdDTO;
import com.abtnetworks.totems.common.dto.GeneratedObjectDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class RollbackSecurityDpTechR004 implements PolicyGenerator {
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
        return sb.toString();
    }
}
