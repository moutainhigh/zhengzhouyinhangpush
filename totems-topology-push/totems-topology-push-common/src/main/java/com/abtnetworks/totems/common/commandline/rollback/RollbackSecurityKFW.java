package com.abtnetworks.totems.common.commandline.rollback;


import com.abtnetworks.totems.common.commandline.PolicyGenerator;

import com.abtnetworks.totems.common.dto.CmdDTO;

import com.abtnetworks.totems.common.dto.commandline.CommandlineDTO;

import org.springframework.stereotype.Service;



@Service
public class RollbackSecurityKFW implements PolicyGenerator {
    @Override
    public String generate(CmdDTO cmdDTO) {

        CommandlineDTO commandlineDTO = new CommandlineDTO();
        commandlineDTO.setCurrentId(cmdDTO.getSetting().getPolicyId());

        StringBuilder sb = new StringBuilder();

        sb.append("define firewall policy\n");
        sb.append(String.format("delete %s\n",commandlineDTO.getCurrentId()));
        sb.append("end");
        return sb.toString();
    }
}
