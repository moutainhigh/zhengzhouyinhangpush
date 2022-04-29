package com.abtnetworks.totems.common.atomcommandline.bothnat;

import com.abtnetworks.totems.common.atomcommandline.nat.AtomHillstone;
import com.abtnetworks.totems.common.commandline.PolicyGenerator;
import com.abtnetworks.totems.common.dto.CmdDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class AtomBothHillstoneR5 implements PolicyGenerator {

    @Override
    public String generate(CmdDTO cmdDTO) {
        AtomHillstone hillStone = new AtomHillstone();
        return hillStone.generate(cmdDTO);
    }
}
