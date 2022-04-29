package com.abtnetworks.totems.common.commandline.edit;

import com.abtnetworks.totems.common.annotation.CustomCli;
import com.abtnetworks.totems.common.commandline.EditPolicyGenerator;
import com.abtnetworks.totems.common.commandline.PolicyGenerator;
import com.abtnetworks.totems.common.commandline.security.SecurityHillStoneR5;
import com.abtnetworks.totems.common.commandline.security.SecurityHillStoneR5ForZSZQ;
import com.abtnetworks.totems.common.dto.CmdDTO;
import com.abtnetworks.totems.common.dto.commandline.CommandlineDTO;
import com.abtnetworks.totems.common.dto.commandline.EditCommandlineDTO;
import com.abtnetworks.totems.common.enums.DeviceModelNumberEnum;
import com.abtnetworks.totems.common.enums.PolicyEnum;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

@Service
@CustomCli(value = DeviceModelNumberEnum.HILLSTONE, type = PolicyEnum.EDIT_SECURITY)
public class EditHillStoneForZSZQ extends EditPolicyGenerator implements PolicyGenerator {

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
        SecurityHillStoneR5ForZSZQ securityHillStoneR5 = new SecurityHillStoneR5ForZSZQ();
        CommandlineDTO dto1 = new CommandlineDTO();
        BeanUtils.copyProperties(dto, dto1);
        return  securityHillStoneR5.generatePreCommandline(dto1) + securityHillStoneR5.createCommandLine(dto1,dto.getMergeProperty());
    }

    public String editCommandLine(EditCommandlineDTO dto) {
        return null;
    }
}
