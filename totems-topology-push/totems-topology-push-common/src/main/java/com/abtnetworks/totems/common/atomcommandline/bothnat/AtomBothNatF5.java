package com.abtnetworks.totems.common.atomcommandline.bothnat;

import com.abtnetworks.totems.common.atomcommandline.nat.AtomF5;
import com.abtnetworks.totems.common.commandline.PolicyGenerator;
import com.abtnetworks.totems.common.dto.CmdDTO;
import org.springframework.stereotype.Service;

/**
 * @author lifei
 * @desc F5 bothNat 命令行生成
 * @date 2021/8/3 15:08
 */
@Service
public class AtomBothNatF5 implements PolicyGenerator {

    @Override
    public String generate(CmdDTO cmdDTO) {
        AtomF5 atomF5 = new AtomF5();
        return atomF5.generate(cmdDTO);
    }
}
