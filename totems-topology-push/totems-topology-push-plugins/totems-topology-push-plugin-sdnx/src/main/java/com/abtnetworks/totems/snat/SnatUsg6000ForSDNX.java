package com.abtnetworks.totems.snat;

import com.abtnetworks.totems.common.annotation.CustomCli;
import com.abtnetworks.totems.common.commandline.PolicyGenerator;
import com.abtnetworks.totems.common.dto.CmdDTO;
import com.abtnetworks.totems.common.enums.DeviceModelNumberEnum;
import com.abtnetworks.totems.common.enums.PolicyEnum;
import com.abtnetworks.totems.nat.U6000ForSDNX;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@CustomCli(value = DeviceModelNumberEnum.USG6000, type = PolicyEnum.SNAT)
public class SnatUsg6000ForSDNX implements PolicyGenerator {
    @Override
    public String generate(CmdDTO cmdDTO) {
        U6000ForSDNX u6000ForSDNX = new U6000ForSDNX();
        return u6000ForSDNX.generate(cmdDTO);
    }
}
