package com.abtnetworks.totems.common.commandline.edit;

import com.abtnetworks.totems.common.commandline.EditPolicyGenerator;
import com.abtnetworks.totems.common.commandline.PolicyGenerator;
import com.abtnetworks.totems.common.commandline.security.SecurityCiscoASA;
import com.abtnetworks.totems.common.dto.CmdDTO;
import com.abtnetworks.totems.common.dto.commandline.CommandlineDTO;
import com.abtnetworks.totems.common.dto.commandline.EditCommandlineDTO;
import org.apache.log4j.Logger;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

@Service
public class EditCisocASA extends EditPolicyGenerator implements PolicyGenerator {

    private static Logger logger = Logger.getLogger(EditCisocASA.class);

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
        SecurityCiscoASA securityCiscoASA = new SecurityCiscoASA();
        CommandlineDTO dto1 = new CommandlineDTO();
        BeanUtils.copyProperties(dto,dto1);
        return securityCiscoASA.generatePreCommandline(dto1) + securityCiscoASA.createMergeCommandLine(dto1,dto.getMergeProperty());
    }


    public String editCommandLine(EditCommandlineDTO dto){
        return  null;
    }

}
