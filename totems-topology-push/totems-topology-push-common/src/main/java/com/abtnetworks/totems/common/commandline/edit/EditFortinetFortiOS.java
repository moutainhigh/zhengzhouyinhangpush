package com.abtnetworks.totems.common.commandline.edit;

import com.abtnetworks.totems.common.commandline.EditPolicyGenerator;
import com.abtnetworks.totems.common.commandline.PolicyGenerator;
import com.abtnetworks.totems.common.commandline.security.SecurityFortinetFortiOS;
import com.abtnetworks.totems.common.dto.CmdDTO;
import com.abtnetworks.totems.common.dto.commandline.CommandlineDTO;
import com.abtnetworks.totems.common.dto.commandline.EditCommandlineDTO;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

@Service
public class EditFortinetFortiOS extends EditPolicyGenerator implements PolicyGenerator {

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
        SecurityFortinetFortiOS securityFortinetFortiOS = new SecurityFortinetFortiOS();
        CommandlineDTO dto1 = new CommandlineDTO();
        BeanUtils.copyProperties(dto, dto1);
        return  securityFortinetFortiOS.createMergeCommandLine(dto1,dto.getMergeProperty());
    }

    public String editCommandLine(EditCommandlineDTO dto) {
        return null;
    }
}
