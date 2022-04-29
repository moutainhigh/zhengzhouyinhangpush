package com.abtnetworks.totems.common.commandline.edit;

import com.abtnetworks.totems.common.commandline.EditPolicyGenerator;
import com.abtnetworks.totems.common.commandline.PolicyGenerator;
import com.abtnetworks.totems.common.commandline.security.SecurityJuniperSsg;
import com.abtnetworks.totems.common.dto.*;
import com.abtnetworks.totems.common.dto.commandline.*;
import org.apache.log4j.Logger;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

@Service
public class EditJuniperSsg extends EditPolicyGenerator implements PolicyGenerator {

    private static Logger logger = Logger.getLogger(EditJuniperSsg.class);

    private int MAX_NAME_LENGTH = 24;

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
        SecurityJuniperSsg securityJuniperSsg = new SecurityJuniperSsg();
        CommandlineDTO dto1 = new CommandlineDTO();
        BeanUtils.copyProperties(dto,dto1);
        return securityJuniperSsg.createMergeCommandLine(dto1,dto.getMergeProperty());
    }


    public String editCommandLine(EditCommandlineDTO dto){
        return  null;
    }
}
