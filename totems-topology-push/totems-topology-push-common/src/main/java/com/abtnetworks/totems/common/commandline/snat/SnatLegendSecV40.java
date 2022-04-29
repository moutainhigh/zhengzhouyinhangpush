package com.abtnetworks.totems.common.commandline.snat;

import com.abtnetworks.totems.common.commandline.PolicyGenerator;
import com.abtnetworks.totems.common.commandline.nat.LegendSecNatV40;
import com.abtnetworks.totems.common.dto.CmdDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class SnatLegendSecV40 implements PolicyGenerator {
    @Override
    public String generate(CmdDTO cmdDTO) {
        LegendSecNatV40 legendSecNatV40 = new LegendSecNatV40();
        return legendSecNatV40.generate(cmdDTO);
    }
}
