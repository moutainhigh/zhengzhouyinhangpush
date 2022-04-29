package com.abtnetworks.totems.common.atomcommandline.dnat;

import com.abtnetworks.totems.common.atomcommandline.nat.AtomFortinet;
import com.abtnetworks.totems.common.commandline.PolicyGenerator;
import com.abtnetworks.totems.common.dto.CmdDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class AtomDnatFortinetiOS implements PolicyGenerator {

    @Override
    public String generate(CmdDTO cmdDTO) {
        AtomFortinet fortinet = new AtomFortinet();
        return fortinet.generate(cmdDTO);
    }
}
