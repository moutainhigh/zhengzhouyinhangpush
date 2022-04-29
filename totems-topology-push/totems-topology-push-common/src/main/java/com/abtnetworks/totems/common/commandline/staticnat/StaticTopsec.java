package com.abtnetworks.totems.common.commandline.staticnat;

import com.abtnetworks.totems.common.commandline.PolicyGenerator;
import com.abtnetworks.totems.common.commandline.nat.TopsecTOS005Nat;
import com.abtnetworks.totems.common.dto.CmdDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class StaticTopsec implements PolicyGenerator {
    @Override
    public String generate(CmdDTO cmdDTO) {
        TopsecTOS005Nat topsetNat = new TopsecTOS005Nat();
        return topsetNat.generate(cmdDTO);
    }
}
