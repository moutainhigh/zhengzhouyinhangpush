package com.abtnetworks.totems.common.commandline.dnat;

import com.abtnetworks.totems.common.annotation.CustomCli;
import com.abtnetworks.totems.common.commandline.PolicyGenerator;
import com.abtnetworks.totems.common.commandline.nat.JuniperNatForBeiNongShang;
import com.abtnetworks.totems.common.dto.CmdDTO;
import com.abtnetworks.totems.common.enums.DeviceModelNumberEnum;
import com.abtnetworks.totems.common.enums.PolicyEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * @Description
 * @Author guanduo.su
 * @Date: 2021/8/31 16:40
 **/
@Slf4j
@Service
@CustomCli(value = DeviceModelNumberEnum.SRX, type = PolicyEnum.DNAT)
public class DnatJuniperForBeiNongShang implements PolicyGenerator {

    @Override
    public String generate(CmdDTO cmdDTO) {
        JuniperNatForBeiNongShang juniperNatForBeiNongShang = new JuniperNatForBeiNongShang();
        return juniperNatForBeiNongShang.generate(cmdDTO);
    }
}
