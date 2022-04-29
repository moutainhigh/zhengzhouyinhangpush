package com.abtnetworks.totems.common.commandline.bothnat;

import com.abtnetworks.totems.common.commandline.PolicyGenerator;
import com.abtnetworks.totems.common.commandline.nat.FortinetNat;
import com.abtnetworks.totems.common.dto.CmdDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author lifei
 * @desc 飞塔bothNat策略命令行
 * @date 2021/12/1 19:13
 */
@Slf4j
@Service
public class BothFortinet implements PolicyGenerator {
    @Autowired
    FortinetNat fortinetNat;

    @Override
    public String generate(CmdDTO cmdDTO) {
        return fortinetNat.generate(cmdDTO);
    }
}
