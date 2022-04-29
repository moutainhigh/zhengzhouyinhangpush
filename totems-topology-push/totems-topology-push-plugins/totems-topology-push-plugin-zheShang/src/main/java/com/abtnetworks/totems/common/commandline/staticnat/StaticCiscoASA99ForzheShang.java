package com.abtnetworks.totems.common.commandline.staticnat;

import com.abtnetworks.totems.common.annotation.CustomCli;
import com.abtnetworks.totems.common.commandline.PolicyGenerator;
import com.abtnetworks.totems.common.commandline.nat.NatCiscoASA99ForzheShang;
import com.abtnetworks.totems.common.dto.CmdDTO;
import com.abtnetworks.totems.common.enums.DeviceModelNumberEnum;
import com.abtnetworks.totems.common.enums.PolicyEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * @desc
 * @author lifei
 * @date 2021-7-27 9:26
 */
@Slf4j
@Service
@CustomCli(value = DeviceModelNumberEnum.CISCO_ASA_99, type = PolicyEnum.STATIC)
public class StaticCiscoASA99ForzheShang implements PolicyGenerator {
    
    @Override
    public String generate(CmdDTO cmdDTO) {
        NatCiscoASA99ForzheShang ciscoASA99Nat = new NatCiscoASA99ForzheShang();
        return ciscoASA99Nat.generate(cmdDTO);
    }
}