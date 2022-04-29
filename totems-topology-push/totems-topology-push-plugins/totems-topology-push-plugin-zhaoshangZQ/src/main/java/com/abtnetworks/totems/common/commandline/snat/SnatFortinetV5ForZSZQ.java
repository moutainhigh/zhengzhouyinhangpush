package com.abtnetworks.totems.common.commandline.snat;

import com.abtnetworks.totems.common.annotation.CustomCli;
import com.abtnetworks.totems.common.commandline.PolicyGenerator;
import com.abtnetworks.totems.common.commandline.nat.FortinetNat;
import com.abtnetworks.totems.common.commandline.nat.FortinetNatV5ForZSZQ;
import com.abtnetworks.totems.common.dto.CmdDTO;
import com.abtnetworks.totems.common.enums.DeviceModelNumberEnum;
import com.abtnetworks.totems.common.enums.PolicyEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Slf4j
@Service
@CustomCli(value = DeviceModelNumberEnum.FORTINET_V5, type = PolicyEnum.SNAT)
public class SnatFortinetV5ForZSZQ implements PolicyGenerator {

    @Autowired
    FortinetNatV5ForZSZQ fortinetNatV5ForZSZQ;

    @Override
    public String generate(CmdDTO cmdDTO) {
        return fortinetNatV5ForZSZQ.generate(cmdDTO);
    }
}
