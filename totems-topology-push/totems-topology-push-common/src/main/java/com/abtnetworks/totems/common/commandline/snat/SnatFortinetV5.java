package com.abtnetworks.totems.common.commandline.snat;

import com.abtnetworks.totems.common.commandline.PolicyGenerator;
import com.abtnetworks.totems.common.commandline.nat.FortinetNatV5;
import com.abtnetworks.totems.common.dto.CmdDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author lifei
 * @desc 飞塔NAT V5.2版本
 * @date 2022/2/25 14:03
 */
@Service
public class SnatFortinetV5 implements PolicyGenerator {
    @Autowired
    FortinetNatV5 fortinetNat;

    @Override
    public String generate(CmdDTO cmdDTO) {
        return fortinetNat.generate(cmdDTO);
    }
}
