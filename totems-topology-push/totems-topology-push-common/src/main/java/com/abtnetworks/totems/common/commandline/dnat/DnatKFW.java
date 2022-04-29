package com.abtnetworks.totems.common.commandline.dnat;

import com.abtnetworks.totems.common.commandline.PolicyGenerator;

import com.abtnetworks.totems.common.commandline.nat.KFWNat;
import com.abtnetworks.totems.common.dto.CmdDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class DnatKFW implements PolicyGenerator {
    @Autowired
    KFWNat kfwNat;

    @Override
    public String generate(CmdDTO cmdDTO) {
        return kfwNat.generate(cmdDTO);
    }
}