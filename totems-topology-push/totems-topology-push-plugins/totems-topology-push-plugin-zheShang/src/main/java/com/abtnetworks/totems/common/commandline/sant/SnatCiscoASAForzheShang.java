package com.abtnetworks.totems.common.commandline.sant;

import com.abtnetworks.totems.common.annotation.CustomCli;
import com.abtnetworks.totems.common.commandline.PolicyGenerator;
import com.abtnetworks.totems.common.commandline.nat.Cisco;
import com.abtnetworks.totems.common.commandline.nat.NatCiscoASAForzheShang;
import com.abtnetworks.totems.common.dto.CmdDTO;
import com.abtnetworks.totems.common.enums.DeviceModelNumberEnum;
import com.abtnetworks.totems.common.enums.PolicyEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;


@Slf4j
@Service
@CustomCli(value = DeviceModelNumberEnum.CISCO, type = PolicyEnum.SNAT)
public class SnatCiscoASAForzheShang implements PolicyGenerator {

    @Override
    public String generate(CmdDTO cmdDTO) {
        NatCiscoASAForzheShang cisco = new NatCiscoASAForzheShang();
        return cisco.generate(cmdDTO);
    }
}
