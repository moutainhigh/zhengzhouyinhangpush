package com.abtnetworks.totems.common.commandline.rollback;

import com.abtnetworks.totems.common.commandline.PolicyGenerator;
import com.abtnetworks.totems.common.commandline.common.dto.PolicyGeneratorDTO;
import com.abtnetworks.totems.common.dto.CmdDTO;
import com.abtnetworks.totems.common.dto.SettingDTO;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

@Service
public class RollbackDnatLongMa implements PolicyGenerator {
    @Override
    public String generate(CmdDTO cmdDTO) {
        return null;
    }

    /**
     * @return no ip nat source 策略id号
     */
    @Override
    public PolicyGeneratorDTO generateV2(CmdDTO cmdDTO) {
        PolicyGeneratorDTO policyGeneratorDTO = new PolicyGeneratorDTO();
        StringBuilder sb = new StringBuilder();
        SettingDTO setting = cmdDTO.getSetting();
        String policyId = setting.getPolicyId();
        if(!StringUtils.isEmpty(policyId)){
            sb.append("enable\nconfigure terminal\n");
            sb.append("no ip nat destination  ").append(policyId).append(StringUtils.LF);
            sb.append("end\nsave config\n");
        }
        policyGeneratorDTO.setPolicyRollbackCommandLine(sb.toString());
        return policyGeneratorDTO;
    }
}
