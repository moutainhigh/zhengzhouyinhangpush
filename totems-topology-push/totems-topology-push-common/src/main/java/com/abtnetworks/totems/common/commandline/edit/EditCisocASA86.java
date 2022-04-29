package com.abtnetworks.totems.common.commandline.edit;

import com.abtnetworks.totems.common.commandline.EditPolicyGenerator;
import com.abtnetworks.totems.common.commandline.PolicyGenerator;
import com.abtnetworks.totems.common.commandline.security.SecurityCiscoASA86;
import com.abtnetworks.totems.common.dto.CmdDTO;
import com.abtnetworks.totems.common.dto.commandline.CommandlineDTO;
import com.abtnetworks.totems.common.dto.commandline.EditCommandlineDTO;
import org.apache.log4j.Logger;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

@Service
public class EditCisocASA86 extends EditPolicyGenerator implements PolicyGenerator {

    private static Logger logger = Logger.getLogger(EditCisocASA86.class);

    @Override
    public String generate(CmdDTO cmdDTO) {
        EditCommandlineDTO allEditDTO = createAllEditDTO(cmdDTO);
        return composite(allEditDTO);
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
        SecurityCiscoASA86 securityCiscoASA86 = new SecurityCiscoASA86();
        CommandlineDTO dto1 = new CommandlineDTO();
        BeanUtils.copyProperties(dto,dto1);
        return securityCiscoASA86.generatePreCommandline(dto1) + securityCiscoASA86.createMergeCommandLine(dto1,dto.getMergeProperty());
    }

    public String editCommandLine(EditCommandlineDTO dto){
        return  null;
    }

}
