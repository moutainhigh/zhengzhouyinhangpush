package com.abtnetworks.totems.common.commandline.rollback;

import com.abtnetworks.totems.command.line.enums.RuleIPTypeEnum;
import com.abtnetworks.totems.common.commandline.PolicyGenerator;
import com.abtnetworks.totems.common.dto.CmdDTO;
import com.abtnetworks.totems.common.dto.GeneratedObjectDTO;
import com.abtnetworks.totems.common.dto.PolicyDTO;
import com.abtnetworks.totems.vender.h3c.security.SecurityH3cSecPathV7Impl;
import org.springframework.stereotype.Service;

@Service
public class RollbackSecurityH3cSecPathV7ZonePair implements PolicyGenerator {

    private SecurityH3cSecPathV7Impl securityH3cSecPathV7;

    public RollbackSecurityH3cSecPathV7ZonePair() {
        securityH3cSecPathV7 = new SecurityH3cSecPathV7Impl();
    }

    @Override
    public String generate(CmdDTO cmdDTO) {
        GeneratedObjectDTO generatedObjectDTO = cmdDTO.getGeneratedObject();
        String aclPolicyCommand = generatedObjectDTO.getAclPolicyCommand();
        return aclPolicyCommand;
    }
}
