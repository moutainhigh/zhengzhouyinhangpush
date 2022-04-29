package com.abtnetworks.totems.common.commandline.bothnat;

import com.abtnetworks.totems.common.commandline.PolicyGenerator;
import com.abtnetworks.totems.common.commandline.nat.DpTechR004;
import com.abtnetworks.totems.common.dto.CmdDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * @author luwei
 * @date 2020/6/9
 */
@Slf4j
@Service
public class BothDpTechR004 implements PolicyGenerator {

    @Override
    public String generate(CmdDTO cmdDTO) {
        DpTechR004 dpTechR004 = new DpTechR004();
        return dpTechR004.generate(cmdDTO);
    }
}
