package com.abtnetworks.totems.common.commandline.staticnat;

import com.abtnetworks.totems.common.commandline.PolicyGenerator;
import com.abtnetworks.totems.common.commandline.nat.LeadSecPowerNat;
import com.abtnetworks.totems.common.commandline.nat.VenustechPowerNat;
import com.abtnetworks.totems.common.dto.CmdDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class StaticVenustechPowerV implements PolicyGenerator {
    @Override
    public String generate(CmdDTO cmdDTO) {
        VenustechPowerNat venustechPowerNat = new VenustechPowerNat();
        return venustechPowerNat.generate(cmdDTO);
    }
}
