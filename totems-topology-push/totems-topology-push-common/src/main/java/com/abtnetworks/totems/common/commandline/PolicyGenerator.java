package com.abtnetworks.totems.common.commandline;

import com.abtnetworks.totems.common.commandline.common.dto.PolicyGeneratorDTO;
import com.abtnetworks.totems.common.dto.CmdDTO;

public interface PolicyGenerator {
    String generate(CmdDTO cmdDTO);

    default PolicyGeneratorDTO generateV2(CmdDTO cmdDTO){
        PolicyGeneratorDTO policyGeneratorDTO = new PolicyGeneratorDTO();
        policyGeneratorDTO.setPolicyRollbackCommandLine(generate(cmdDTO));
        return policyGeneratorDTO;
    }
}
