package com.abtnetworks.totems.common.commandline.edit;

import com.abtnetworks.totems.common.commandline.EditPolicyGenerator;
import com.abtnetworks.totems.common.commandline.PolicyGenerator;
import com.abtnetworks.totems.common.commandline.security.SecurityDpTechR004ForHunanDL;
import com.abtnetworks.totems.common.dto.CmdDTO;
import com.abtnetworks.totems.common.dto.commandline.CommandlineDTO;
import com.abtnetworks.totems.common.dto.commandline.EditCommandlineDTO;
import org.apache.log4j.Logger;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

@Service
public class EditDptechr004ForHunanDL extends EditPolicyGenerator implements PolicyGenerator {

    private static Logger logger = Logger.getLogger(EditDptechr004ForHunanDL.class);

    @Override
    public String generate(CmdDTO cmdDTO) {
        EditCommandlineDTO partEditDTO = createPartEditDTO(cmdDTO);
        return composite(partEditDTO);
    }

    @Override
    public String generateCommandline(EditCommandlineDTO dto) {
        if (dto.getMergeDTO() == null || dto.isMustCreateFlag()) {
            return createCommandLine(dto);
        } else {
            return editCommandLine(dto);
        }
    }

    public String createCommandLine(EditCommandlineDTO dto) {
        SecurityDpTechR004ForHunanDL securityDpTechR004 = new SecurityDpTechR004ForHunanDL();
        CommandlineDTO dto1 = new CommandlineDTO();
        BeanUtils.copyProperties(dto, dto1);
        return securityDpTechR004.generatePreCommandline(dto1) + securityDpTechR004.createCommandLine(dto1,dto.getMergeProperty());
    }

    public String editCommandLine(EditCommandlineDTO dto) {
        return null;
    }
}

