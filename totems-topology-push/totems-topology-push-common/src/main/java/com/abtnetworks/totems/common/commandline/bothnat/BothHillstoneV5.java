package com.abtnetworks.totems.common.commandline.bothnat;

import com.abtnetworks.totems.common.commandline.PolicyGenerator;
import com.abtnetworks.totems.common.commandline.nat.HillstoneV5;
import com.abtnetworks.totems.common.dto.CmdDTO;
import org.springframework.stereotype.Service;

@Service
public class BothHillstoneV5 implements PolicyGenerator {

    @Override
    public String generate(CmdDTO cmdDTO) {
        HillstoneV5 hillstone = new HillstoneV5();
        return hillstone.generate(cmdDTO);
    }
}
