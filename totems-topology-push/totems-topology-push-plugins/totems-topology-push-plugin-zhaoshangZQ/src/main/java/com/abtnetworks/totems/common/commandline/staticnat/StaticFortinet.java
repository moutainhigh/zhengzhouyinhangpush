package com.abtnetworks.totems.common.commandline.staticnat;

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

/**
 * @desc    飞塔静态NAT
 * @author liuchanghao
 * @date 2020-11-30 9:26
 */
@Slf4j
@Service
@CustomCli(value = DeviceModelNumberEnum.FORTINET_V5, type = PolicyEnum.STATIC)
public class StaticFortinet implements PolicyGenerator {

    @Autowired
    FortinetNatV5ForZSZQ fortinetNatV5ForZSZQ;

    @Override
    public String generate(CmdDTO cmdDTO) {
        return fortinetNatV5ForZSZQ.generate(cmdDTO);
    }
}
