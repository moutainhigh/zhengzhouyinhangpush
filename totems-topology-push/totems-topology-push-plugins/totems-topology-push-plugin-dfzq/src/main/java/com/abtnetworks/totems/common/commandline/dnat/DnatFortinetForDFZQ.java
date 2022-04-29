package com.abtnetworks.totems.common.commandline.dnat;

import com.abtnetworks.totems.common.annotation.CustomCli;
import com.abtnetworks.totems.common.commandline.PolicyGenerator;
import com.abtnetworks.totems.common.commandline.nat.FortinetNatForDFZQ;
import com.abtnetworks.totems.common.dto.CmdDTO;
import com.abtnetworks.totems.common.enums.DeviceModelNumberEnum;
import com.abtnetworks.totems.common.enums.PolicyEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@CustomCli(value = DeviceModelNumberEnum.FORTINET, type = PolicyEnum.DNAT)
public class DnatFortinetForDFZQ implements PolicyGenerator {
    @Autowired
    FortinetNatForDFZQ fortinetNatForDFZQ;

    @Override
    public String generate(CmdDTO cmdDTO) {
        return fortinetNatForDFZQ.generate(cmdDTO);
    }
}
