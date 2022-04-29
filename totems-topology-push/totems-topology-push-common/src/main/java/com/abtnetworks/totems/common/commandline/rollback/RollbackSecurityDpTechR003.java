package com.abtnetworks.totems.common.commandline.rollback;

import com.abtnetworks.totems.common.commandline.PolicyGenerator;
import com.abtnetworks.totems.common.dto.CmdDTO;
import com.abtnetworks.totems.common.dto.GeneratedObjectDTO;
import org.springframework.stereotype.Service;

@Service
public class RollbackSecurityDpTechR003 implements PolicyGenerator {
    @Override
    public String generate(CmdDTO cmdDTO) {

        GeneratedObjectDTO generatedObjectDTO = cmdDTO.getGeneratedObject();

        return "conf-mode\n" +
                "no pf-policy " + generatedObjectDTO.getPolicyName() + "\n" +
                "exit\n";
    }
}
