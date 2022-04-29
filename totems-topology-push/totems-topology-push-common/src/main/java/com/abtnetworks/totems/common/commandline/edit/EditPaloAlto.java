package com.abtnetworks.totems.common.commandline.edit;

import com.abtnetworks.totems.common.commandline.EditPolicyGenerator;
import com.abtnetworks.totems.common.commandline.PolicyGenerator;
import com.abtnetworks.totems.common.commandline.security.SecurityPaloAlto;
import com.abtnetworks.totems.common.dto.CmdDTO;
import com.abtnetworks.totems.common.dto.commandline.CommandlineDTO;
import com.abtnetworks.totems.common.dto.commandline.EditCommandlineDTO;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

@Service
public class EditPaloAlto  extends EditPolicyGenerator implements PolicyGenerator {

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

    public String createCommandLine(EditCommandlineDTO dto){
        SecurityPaloAlto securityPaloAlto = new SecurityPaloAlto();
        CommandlineDTO dto1 = new CommandlineDTO();
        BeanUtils.copyProperties(dto, dto1);
        return  securityPaloAlto.generatePreCommandline(dto1)
                +securityPaloAlto.createMergeCommandLine(dto1,dto.getMergeProperty())
                +securityPaloAlto.generatePostCommandline(dto1);
    }

    public String editCommandLine(EditCommandlineDTO dto){
        return  null;
    }
}
