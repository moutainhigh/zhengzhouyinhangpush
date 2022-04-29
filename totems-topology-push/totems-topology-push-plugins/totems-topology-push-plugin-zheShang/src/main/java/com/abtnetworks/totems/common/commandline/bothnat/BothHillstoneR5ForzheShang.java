package com.abtnetworks.totems.common.commandline.bothnat;

import com.abtnetworks.totems.common.annotation.CustomCli;
import com.abtnetworks.totems.common.commandline.PolicyGenerator;
import com.abtnetworks.totems.common.commandline.nat.NatHillstoneForzheShang;
import com.abtnetworks.totems.common.dto.CmdDTO;
import com.abtnetworks.totems.common.enums.DeviceModelNumberEnum;
import com.abtnetworks.totems.common.enums.PolicyEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@CustomCli(value = DeviceModelNumberEnum.HILLSTONE, type = PolicyEnum.BOTH)
public class BothHillstoneR5ForzheShang implements PolicyGenerator {

    @Override
    public String generate(CmdDTO cmdDTO) {
        NatHillstoneForzheShang hillstone = new NatHillstoneForzheShang();
        return hillstone.generate(cmdDTO);
    }
}
