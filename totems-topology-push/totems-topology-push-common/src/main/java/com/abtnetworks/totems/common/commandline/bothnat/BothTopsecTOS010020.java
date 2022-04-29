package com.abtnetworks.totems.common.commandline.bothnat;

import com.abtnetworks.totems.common.commandline.PolicyGenerator;
import com.abtnetworks.totems.common.commandline.nat.TopsecNGNat;
import com.abtnetworks.totems.common.commandline.nat.TopsecTOS010020Nat;
import com.abtnetworks.totems.common.dto.CmdDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * @author luwei
 * @date 2020/6/17
 */
@Slf4j
@Service
public class BothTopsecTOS010020 implements PolicyGenerator {

    @Override
    public String generate(CmdDTO cmdDTO) {
        TopsecTOS010020Nat topsec = new TopsecTOS010020Nat();
        return topsec.generate(cmdDTO);
    }


}
