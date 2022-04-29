package com.abtnetworks.totems.common.commandline.bothnat;

import com.abtnetworks.totems.common.commandline.PolicyGenerator;
import com.abtnetworks.totems.common.commandline.nat.SangforNat;
import com.abtnetworks.totems.common.dto.CmdDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author Administrator
 * @Title:
 * @Description: 请写注释类
 * @date 2020/8/21
 */
@Service
public class BothSangFor implements PolicyGenerator {
    @Autowired
    SangforNat sangforNat;

    @Override
    public String generate(CmdDTO cmdDTO) {
        return sangforNat.generate(cmdDTO);
    }
}
