package com.abtnetworks.totems.common.atomcommandline.edit;

import com.abtnetworks.totems.common.atomcommandline.edit.common.EditTopsecCommon;
import com.abtnetworks.totems.common.commandline.EditPolicyGenerator;
import com.abtnetworks.totems.common.commandline.PolicyGenerator;
import com.abtnetworks.totems.common.dto.CmdDTO;
import com.abtnetworks.totems.common.dto.commandline.EditCommandlineDTO;
import com.abtnetworks.totems.vender.topsec.TOS_NG4.SecurityTopsecNG4Impl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * @author lifei
 * @desc 天融信NG4编辑策略
 * @date 2022/1/13 14:54
 */
@Service
public class EditTopsecNG4 extends EditPolicyGenerator implements PolicyGenerator {

    private final static Logger logger = LoggerFactory.getLogger(EditTopsecNG4.class);

    @Override
    public String generateCommandline(EditCommandlineDTO dto) {
        logger.info("EditTopsecNg4.generateCommandline...");
        return EditTopsecCommon.generateCommandline(dto,new SecurityTopsecNG4Impl());
    }

    @Override
    public String generate(CmdDTO cmdDTO) {
        EditCommandlineDTO partEditDTO = createPartEditDTO(cmdDTO);
        return composite(partEditDTO);
    }
}
