package com.abtnetworks.totems.common.commandline.snat;

import com.abtnetworks.totems.common.commandline.PolicyGenerator;
import com.abtnetworks.totems.common.commandline.nat.Hillstone;
import com.abtnetworks.totems.common.dto.CmdDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class SnatHillstoneR5 implements PolicyGenerator {

    @Override
    public String generate(CmdDTO cmdDTO) {
        Hillstone hillstone = new Hillstone();
        return hillstone.generate(cmdDTO);
    }
}
