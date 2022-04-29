package com.abtnetworks.totems.common.commandline.staticnat;

import com.abtnetworks.totems.common.commandline.PolicyGenerator;
import com.abtnetworks.totems.common.commandline.nat.Cisco;
import com.abtnetworks.totems.common.dto.CmdDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class StaticCiscoASA implements PolicyGenerator {

    @Override
    public String generate(CmdDTO cmdDTO) {
        Cisco cisco = new Cisco();
        return cisco.generate(cmdDTO);
    }
}
