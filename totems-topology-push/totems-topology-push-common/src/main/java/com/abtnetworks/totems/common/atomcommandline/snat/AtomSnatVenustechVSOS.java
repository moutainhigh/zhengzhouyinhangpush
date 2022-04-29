package com.abtnetworks.totems.common.atomcommandline.snat;

import com.abtnetworks.totems.common.atomcommandline.nat.AtomVenustechVSOSNat;
import com.abtnetworks.totems.common.commandline.PolicyGenerator;
import com.abtnetworks.totems.common.dto.CmdDTO;
import org.springframework.stereotype.Service;

@Service
public class AtomSnatVenustechVSOS implements PolicyGenerator {

    @Override
    public String generate(CmdDTO cmdDTO) {
        AtomVenustechVSOSNat atomVenustechVSOSNat = new AtomVenustechVSOSNat();
        return atomVenustechVSOSNat.generate(cmdDTO);
    }
}
