package com.abtnetworks.totems.common.commandline.snat;

import com.abtnetworks.totems.common.commandline.PolicyGenerator;
import com.abtnetworks.totems.common.commandline.nat.FortinetNat;
import com.abtnetworks.totems.common.commandline.nat.KFWNat;
import com.abtnetworks.totems.common.dto.CmdDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class SnatKFW implements PolicyGenerator {

    @Autowired
    KFWNat kfwNat;

    @Override
    public String generate(CmdDTO cmdDTO) {
        return kfwNat.generate(cmdDTO);
    }
}
