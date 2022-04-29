package com.abtnetworks.totems.common.commandline.bothnat;

import com.abtnetworks.totems.common.commandline.PolicyGenerator;
import com.abtnetworks.totems.common.commandline.nat.U6000;
import com.abtnetworks.totems.common.dto.CmdDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class BothUsg6000 implements PolicyGenerator {

    @Override
    public String generate(CmdDTO cmdDTO) {
        U6000 u6000 = new U6000();
        return u6000.generate(cmdDTO);
    }
}
