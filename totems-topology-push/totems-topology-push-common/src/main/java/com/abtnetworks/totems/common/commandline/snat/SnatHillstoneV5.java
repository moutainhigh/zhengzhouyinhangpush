package com.abtnetworks.totems.common.commandline.snat;

import com.abtnetworks.totems.common.annotation.CustomCli;
import com.abtnetworks.totems.common.commandline.PolicyGenerator;
import com.abtnetworks.totems.common.commandline.nat.HillstoneV5;
import com.abtnetworks.totems.common.dto.CmdDTO;
import com.abtnetworks.totems.common.enums.DeviceModelNumberEnum;
import com.abtnetworks.totems.common.enums.PolicyEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
public class SnatHillstoneV5 implements PolicyGenerator {

    @Override
    public String generate(CmdDTO cmdDTO) {
        HillstoneV5 hillstone = new HillstoneV5();
        return hillstone.generate(cmdDTO);
    }
}
