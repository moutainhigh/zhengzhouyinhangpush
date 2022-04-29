package com.abtnetworks.totems.common.commandline.dnat;


import com.abtnetworks.totems.common.annotation.CustomCli;
import com.abtnetworks.totems.common.commandline.PolicyGenerator;
import com.abtnetworks.totems.common.commandline.nat.HillstoneForDFZQ;
import com.abtnetworks.totems.common.dto.CmdDTO;
import com.abtnetworks.totems.common.enums.DeviceModelNumberEnum;
import com.abtnetworks.totems.common.enums.PolicyEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@CustomCli(value = DeviceModelNumberEnum.HILLSTONE, type = PolicyEnum.DNAT)
public class DnatHillstoneR5ForDFZQ implements PolicyGenerator {

    @Override
    public String generate(CmdDTO cmdDTO) {
        HillstoneForDFZQ hillstoneForDFZQ = new HillstoneForDFZQ();
        return hillstoneForDFZQ.generate(cmdDTO);
    }
}
