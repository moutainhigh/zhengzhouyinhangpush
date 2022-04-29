package com.abtnetworks.totems.common.commandline.staticnat;

import com.abtnetworks.totems.common.commandline.PolicyGenerator;
import com.abtnetworks.totems.common.commandline.nat.LeadSecPowerNat;
import com.abtnetworks.totems.common.dto.CmdDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class StaticLeadSecPowerV implements PolicyGenerator {
    @Override
    public String generate(CmdDTO cmdDTO) {
        LeadSecPowerNat legendSecNat = new LeadSecPowerNat();
        return legendSecNat.generate(cmdDTO);
    }
}
