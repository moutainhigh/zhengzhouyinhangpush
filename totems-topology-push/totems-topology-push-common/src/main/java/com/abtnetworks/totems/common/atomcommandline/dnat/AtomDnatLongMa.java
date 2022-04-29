package com.abtnetworks.totems.common.atomcommandline.dnat;

import com.abtnetworks.totems.common.atomcommandline.nat.AtomLongMaNat;
import com.abtnetworks.totems.common.commandline.PolicyGenerator;
import com.abtnetworks.totems.common.dto.CmdDTO;
import org.springframework.stereotype.Service;

@Service
public class AtomDnatLongMa  implements PolicyGenerator {
    @Override
    public String generate(CmdDTO cmdDTO) {
        AtomLongMaNat atomLongMaNat = new AtomLongMaNat();
        return atomLongMaNat.generate(cmdDTO);
    }
}
