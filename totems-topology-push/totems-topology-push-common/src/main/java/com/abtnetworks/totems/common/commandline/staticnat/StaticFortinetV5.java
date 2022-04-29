package com.abtnetworks.totems.common.commandline.staticnat;

import com.abtnetworks.totems.common.commandline.PolicyGenerator;
import com.abtnetworks.totems.common.commandline.nat.FortinetNatV5;
import com.abtnetworks.totems.common.dto.CmdDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @desc    飞塔 v5.2静态NAT
 * @author liuchanghao
 * @date 2020-11-30 9:26
 */
@Slf4j
@Service
public class StaticFortinetV5 implements PolicyGenerator {

    @Autowired
    FortinetNatV5 fortinetNat;

    @Override
    public String generate(CmdDTO cmdDTO) {
        return fortinetNat.generate(cmdDTO);
    }
}
