package com.abtnetworks.totems.common.commandline.dnat;

import com.abtnetworks.totems.common.commandline.PolicyGenerator;
import com.abtnetworks.totems.common.commandline.nat.LegendSecNat;
import com.abtnetworks.totems.common.dto.CmdDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class DnatLegendSec implements PolicyGenerator {
    @Override
    public String generate(CmdDTO cmdDTO) {
        LegendSecNat legendSecNat = new LegendSecNat();
        return legendSecNat.generate(cmdDTO);
    }
}
