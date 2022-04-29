package com.abtnetworks.totems.common.commandline.sant;

import com.abtnetworks.totems.common.annotation.CustomCli;
import com.abtnetworks.totems.common.commandline.PolicyGenerator;
import com.abtnetworks.totems.common.commandline.nat.NatCiscoASA99ForzheShang;
import com.abtnetworks.totems.common.dto.CmdDTO;
import com.abtnetworks.totems.common.enums.DeviceModelNumberEnum;
import com.abtnetworks.totems.common.enums.PolicyEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * @author lifei
 * @desc XXXX
 * @date 2021/6/24 15:43
 */
@Slf4j
@Service
@CustomCli(value = DeviceModelNumberEnum.CISCO_ASA_99, type = PolicyEnum.SNAT)
public class SnatCiscoASA99Forzheshang implements PolicyGenerator {
    @Override
    public String generate(CmdDTO cmdDTO) {
        NatCiscoASA99ForzheShang ciscoASA99ForzheShang = new NatCiscoASA99ForzheShang();
        return ciscoASA99ForzheShang.generate(cmdDTO);
    }
}
