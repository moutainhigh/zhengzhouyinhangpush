package com.abtnetworks.totems.common.commandline.dnat;

import com.abtnetworks.totems.common.commandline.PolicyGenerator;
import com.abtnetworks.totems.common.commandline.nat.FortinetNat;
import com.abtnetworks.totems.common.dto.CmdDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class DnatFortinet implements PolicyGenerator {
    @Autowired
    FortinetNat fortinetNat;

    @Override
    public String generate(CmdDTO cmdDTO) {
        return fortinetNat.generate(cmdDTO);
    }
}
