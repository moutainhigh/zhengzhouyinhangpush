package com.abtnetworks.totems.common.commandline.dnat;

import com.abtnetworks.totems.common.commandline.PolicyGenerator;
import com.abtnetworks.totems.common.commandline.nat.H3cSecPathV7;
import com.abtnetworks.totems.common.dto.CmdDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @desc 华3V7 NAT策略
 * @author zhoumuhua
 * @date 2021-6-17
 */
@Slf4j
@Service
public class DnatH3cSecPathV7 implements PolicyGenerator {

    @Autowired
    H3cSecPathV7 h3cSecPathV7;

    @Override
    public String generate(CmdDTO cmdDTO) {
        return h3cSecPathV7.generate(cmdDTO);
    }
}
