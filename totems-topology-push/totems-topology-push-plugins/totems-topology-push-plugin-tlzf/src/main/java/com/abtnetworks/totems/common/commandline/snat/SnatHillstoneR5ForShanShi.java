package com.abtnetworks.totems.common.commandline.snat;

import com.abtnetworks.totems.common.annotation.CustomCli;
import com.abtnetworks.totems.common.commandline.PolicyGenerator;

import com.abtnetworks.totems.common.commandline.nat.HillstoneForShanShi;
import com.abtnetworks.totems.common.dto.CmdDTO;
import com.abtnetworks.totems.common.enums.DeviceModelNumberEnum;
import com.abtnetworks.totems.common.enums.PolicyEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@CustomCli(value = DeviceModelNumberEnum.HILLSTONE, type = PolicyEnum.SNAT)
public class SnatHillstoneR5ForShanShi implements PolicyGenerator {

    @Override
    public String generate(CmdDTO cmdDTO) {
        HillstoneForShanShi hillstoneForShanShi = new HillstoneForShanShi();
        return hillstoneForShanShi.generate(cmdDTO);
    }
}
