package com.abtnetworks.totems.common.atomcommandline.staticnat;

import com.abtnetworks.totems.common.atomcommandline.nat.AtomFortinet;
import com.abtnetworks.totems.common.commandline.PolicyGenerator;
import com.abtnetworks.totems.common.dto.CmdDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class AtomStaticnatFortinetiOS implements PolicyGenerator {

    @Override
    public String generate(CmdDTO cmdDTO) {
        AtomFortinet fortinet = new AtomFortinet();
        return fortinet.generate(cmdDTO);
    }
}
