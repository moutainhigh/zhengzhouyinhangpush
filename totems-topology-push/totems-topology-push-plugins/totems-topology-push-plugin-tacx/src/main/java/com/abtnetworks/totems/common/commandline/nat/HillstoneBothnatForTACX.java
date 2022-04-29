package com.abtnetworks.totems.common.commandline.nat;


import com.abtnetworks.totems.common.annotation.CustomCli;
import com.abtnetworks.totems.common.commandline.PolicyGenerator;
import com.abtnetworks.totems.common.dto.CmdDTO;
import com.abtnetworks.totems.common.enums.DeviceModelNumberEnum;
import com.abtnetworks.totems.common.enums.PolicyEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@CustomCli(value = DeviceModelNumberEnum.HILLSTONE, type = PolicyEnum.BOTH)
public class HillstoneBothnatForTACX implements PolicyGenerator {
    @Autowired
    HillStoneNatForTACX hillStoneNatForTACX;
    @Override
    public String generate(CmdDTO cmdDTO) {

        return hillStoneNatForTACX.generate(cmdDTO);
    }
}
