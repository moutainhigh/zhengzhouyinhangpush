package com.abtnetworks.totems.common.atomcommandline.snat;

import com.abtnetworks.totems.common.atomcommandline.nat.AtomFortinet;
import com.abtnetworks.totems.common.commandline.PolicyGenerator;
import com.abtnetworks.totems.common.dto.CmdDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * @Author: WangCan
 * @Description 山石原子化命令行方式生成nat策略
 * @Date: 2021/6/23
 */
@Slf4j
@Service
public class AtomSnatFortinetiOS implements PolicyGenerator {

    @Override
    public String generate(CmdDTO cmdDTO) {
        AtomFortinet fortinet = new AtomFortinet();
        return fortinet.generate(cmdDTO);
    }
}
