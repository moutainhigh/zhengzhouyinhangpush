package com.abtnetworks.totems.common.commandline.dnat;

import com.abtnetworks.totems.common.annotation.CustomCli;
import com.abtnetworks.totems.common.commandline.PolicyGenerator;
import com.abtnetworks.totems.common.commandline.nat.DpTechR004;
import com.abtnetworks.totems.common.commandline.nat.NatDpTechR004ForzheShang;
import com.abtnetworks.totems.common.dto.CmdDTO;
import com.abtnetworks.totems.common.enums.DeviceModelNumberEnum;
import com.abtnetworks.totems.common.enums.PolicyEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 迪普 目的nat
 *
 * @author lifei
 * @date 2021/6/24 15:47
 */
@Slf4j
@Service
@CustomCli(value = DeviceModelNumberEnum.DPTECHR004, type = PolicyEnum.DNAT)
public class DnatDpTechR004ForzheShang implements PolicyGenerator {

    @Override
    public String generate(CmdDTO cmdDTO) {
        NatDpTechR004ForzheShang dpTechR004 = new NatDpTechR004ForzheShang();
        return dpTechR004.generate(cmdDTO);
    }
}
