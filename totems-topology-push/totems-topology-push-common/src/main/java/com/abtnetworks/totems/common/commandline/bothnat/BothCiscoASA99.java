package com.abtnetworks.totems.common.commandline.bothnat;

import com.abtnetworks.totems.common.commandline.PolicyGenerator;
import com.abtnetworks.totems.common.commandline.nat.CiscoASA99Nat;
import com.abtnetworks.totems.common.dto.CmdDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @desc
 * @author liuchanghao
 * @date 2020-11-30 9:26
 */
@Slf4j
@Service
public class BothCiscoASA99 implements PolicyGenerator {

    @Override
    public String generate(CmdDTO cmdDTO) {
        CiscoASA99Nat ciscoASA99Nat = new CiscoASA99Nat();
        return ciscoASA99Nat.generate(cmdDTO);
    }
}
