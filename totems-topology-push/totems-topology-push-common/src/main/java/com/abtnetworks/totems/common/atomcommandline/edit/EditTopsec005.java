package com.abtnetworks.totems.common.atomcommandline.edit;

import com.abtnetworks.totems.common.atomcommandline.edit.common.EditTopsecCommon;
import com.abtnetworks.totems.common.commandline.EditPolicyGenerator;
import com.abtnetworks.totems.common.commandline.PolicyGenerator;
import com.abtnetworks.totems.common.dto.CmdDTO;
import com.abtnetworks.totems.common.dto.commandline.EditCommandlineDTO;
import com.abtnetworks.totems.vender.topsec.TOS_005.SecurityTopsec005Impl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * @author liushaohua
 * @version 0.1
 * @description: TODO
 * @date 2021/7/9 17:39
 */
@Service
public class EditTopsec005 extends EditPolicyGenerator implements PolicyGenerator {
    Logger logger = LoggerFactory.getLogger(EditTopsec005.class);


    @Override
    public String generateCommandline(EditCommandlineDTO dto) {
        logger.info("EditTopsec005.generateCommandline...");
        return EditTopsecCommon.generateCommandline(dto,new SecurityTopsec005Impl());
    }

    @Override
    public String generate(CmdDTO cmdDTO) {
        EditCommandlineDTO partEditDTO = createPartEditDTO(cmdDTO);
        return composite(partEditDTO);
    }
}
