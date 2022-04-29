package com.abtnetworks.totems.common.commandline.snat;

import com.abtnetworks.totems.common.commandline.PolicyGenerator;
import com.abtnetworks.totems.common.commandline.nat.TopsecNG2Nat;
import com.abtnetworks.totems.common.commandline.nat.TopsecNGNat;
import com.abtnetworks.totems.common.dto.CmdDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class SnatTopsecNG2 implements PolicyGenerator {
    @Override
    public String generate(CmdDTO cmdDTO) {
        TopsecNG2Nat topsecNat = new TopsecNG2Nat();
        return topsecNat.generate(cmdDTO);
    }
}
