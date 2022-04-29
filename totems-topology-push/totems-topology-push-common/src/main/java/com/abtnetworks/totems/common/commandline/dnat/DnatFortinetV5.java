package com.abtnetworks.totems.common.commandline.dnat;

import com.abtnetworks.totems.common.commandline.PolicyGenerator;
import com.abtnetworks.totems.common.commandline.nat.FortinetNat;
import com.abtnetworks.totems.common.commandline.nat.FortinetNatV5;
import com.abtnetworks.totems.common.dto.CmdDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author lifei
 * @desc 飞塔dnat v5.2版本
 * @date 2022/2/25 16:02
 */
@Service
public class DnatFortinetV5 implements PolicyGenerator {
    @Autowired
    FortinetNatV5 fortinetNat;

    @Override
    public String generate(CmdDTO cmdDTO) {
        return fortinetNat.generate(cmdDTO);
    }
}
