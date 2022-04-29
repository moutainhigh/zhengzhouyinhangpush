package com.abtnetworks.totems.common.atomcommandline.staticnat;

import com.abtnetworks.totems.common.atomcommandline.nat.AtomLeadSecPowerV30Nat;
import com.abtnetworks.totems.common.commandline.PolicyGenerator;
import com.abtnetworks.totems.common.dto.CmdDTO;
import org.springframework.stereotype.Service;

@Service
public class AtomStaticnatLeadSecPowerV30 implements PolicyGenerator {
    @Override
    public String generate(CmdDTO cmdDTO) {
        AtomLeadSecPowerV30Nat leadSecPowerV30Nat = new AtomLeadSecPowerV30Nat();
        return leadSecPowerV30Nat.generate(cmdDTO);
    }
}
