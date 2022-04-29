package com.abtnetworks.totems.common.atomcommandline.edit;

import com.abtnetworks.totems.common.commandline.EditPolicyGenerator;
import com.abtnetworks.totems.common.commandline.PolicyGenerator;
import com.abtnetworks.totems.common.atomcommandline.edit.common.EditH3cSecPathCommon;
import com.abtnetworks.totems.common.dto.CmdDTO;
import com.abtnetworks.totems.common.dto.commandline.EditCommandlineDTO;
import com.abtnetworks.totems.vender.h3c.security.SecurityH3cSecPathV7Impl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * @author liushaohua
 * @version 0.1
 * @description: TODO
 * @date 2021/7/8 9:36
 */
@Service
public class EditH3cSecPathV7 extends EditPolicyGenerator implements PolicyGenerator {

    Logger logger = LoggerFactory.getLogger(EditH3cSecPathV7.class);


    @Override
    public String generateCommandline(EditCommandlineDTO dto) {
        logger.info("EditH3cSecPathV7.generateCommandline...");
        return EditH3cSecPathCommon.generateCommandline(dto,new SecurityH3cSecPathV7Impl());
    }

    @Override
    public String generate(CmdDTO cmdDTO) {
        EditCommandlineDTO partEditDTO = createPartEditDTO(cmdDTO);
        return composite(partEditDTO);
    }

}
