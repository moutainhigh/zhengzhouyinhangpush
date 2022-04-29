package com.abtnetworks.totems.common.commandline.rollback;

import com.abtnetworks.totems.common.commandline.PolicyGenerator;
import com.abtnetworks.totems.common.dto.CmdDTO;
import com.abtnetworks.totems.common.dto.SettingDTO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class RollbackDNatVenustech implements PolicyGenerator {
    @Override
    public String generate(CmdDTO cmdDTO) {
        StringBuilder sb = new StringBuilder();
        SettingDTO setting = cmdDTO.getSetting();
        String policyId = setting.getPolicyId();
        if(!StringUtils.isEmpty(policyId)){
            sb.append("enable\nconfigure terminal\n");
            sb.append("no ip nat ").append(policyId).append(StringUtils.LF);
            sb.append("exit\n");
        }
        return sb.toString();
    }
}