package com.abtnetworks.totems.common.commandline.bothnat;

import com.abtnetworks.totems.common.commandline.PolicyGenerator;
import com.abtnetworks.totems.common.commandline.nat.TopsecNG2Nat;
import com.abtnetworks.totems.common.commandline.nat.TopsecNGNat;
import com.abtnetworks.totems.common.dto.CmdDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * @author luwei
 * @date 2020/6/17
 */
@Slf4j
@Service
public class BothTopsecNG2 implements PolicyGenerator {

    @Override
    public String generate(CmdDTO cmdDTO) {
        TopsecNG2Nat topsec = new TopsecNG2Nat();
        return topsec.generate(cmdDTO);
    }


}
