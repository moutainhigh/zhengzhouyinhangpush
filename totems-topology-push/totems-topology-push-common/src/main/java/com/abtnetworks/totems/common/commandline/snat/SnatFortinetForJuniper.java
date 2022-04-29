package com.abtnetworks.totems.common.commandline.snat;

import com.abtnetworks.totems.common.commandline.PolicyGenerator;
import com.abtnetworks.totems.common.commandline.nat.FortinetNat;
import com.abtnetworks.totems.common.commandline.nat.JuniperNat;
import com.abtnetworks.totems.common.dto.CmdDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class SnatFortinetForJuniper implements PolicyGenerator {

    @Autowired
    JuniperNat juniperNat;


    @Override
    public String generate(CmdDTO cmdDTO) {
        return juniperNat.generate(cmdDTO);
    }

}
