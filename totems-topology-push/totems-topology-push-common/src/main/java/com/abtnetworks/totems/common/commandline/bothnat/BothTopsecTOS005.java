package com.abtnetworks.totems.common.commandline.bothnat;

import com.abtnetworks.totems.common.commandline.PolicyGenerator;
import com.abtnetworks.totems.common.commandline.nat.TopsecTOS005Nat;
import com.abtnetworks.totems.common.dto.CmdDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * @author luwei
 * @date 2020/6/17
 */
@Slf4j
@Service
public class BothTopsecTOS005 implements PolicyGenerator {

    @Override
    public String generate(CmdDTO cmdDTO) {
        TopsecTOS005Nat topsec = new TopsecTOS005Nat();
        return topsec.generate(cmdDTO);
    }


}
