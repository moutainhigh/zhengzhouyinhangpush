package com.abtnetworks.totems.common.commandline.snat;

import com.abtnetworks.totems.common.commandline.PolicyGenerator;
import com.abtnetworks.totems.common.commandline.nat.TopsecTOS010020Nat;
import com.abtnetworks.totems.common.dto.CmdDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class SnatTopsecTOS010020 implements PolicyGenerator {
    @Override
    public String generate(CmdDTO cmdDTO) {
        TopsecTOS010020Nat topsecNat = new TopsecTOS010020Nat();
        return topsecNat.generate(cmdDTO);
    }
}
