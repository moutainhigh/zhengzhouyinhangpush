package com.abtnetworks.totems.common.commandline.snat;

import com.abtnetworks.totems.common.annotation.CustomCli;
import com.abtnetworks.totems.common.commandline.PolicyGenerator;
import com.abtnetworks.totems.common.commandline.nat.FortinetNatForZXTX;
import com.abtnetworks.totems.common.dto.CmdDTO;
import com.abtnetworks.totems.common.enums.DeviceModelNumberEnum;
import com.abtnetworks.totems.common.enums.PolicyEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Slf4j
@Service
@CustomCli(value = DeviceModelNumberEnum.FORTINET, type = PolicyEnum.SNAT)
public class SnatFortinetForZXTX implements PolicyGenerator {

    @Autowired
    FortinetNatForZXTX fortinetNatForZXTX;

    @Override
    public String generate(CmdDTO cmdDTO) {
        return fortinetNatForZXTX.generate(cmdDTO);
    }
}
