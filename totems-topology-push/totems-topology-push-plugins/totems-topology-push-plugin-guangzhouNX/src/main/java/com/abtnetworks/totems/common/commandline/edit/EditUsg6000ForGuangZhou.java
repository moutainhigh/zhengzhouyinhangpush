package com.abtnetworks.totems.common.commandline.edit;

import com.abtnetworks.totems.common.annotation.CustomCli;
import com.abtnetworks.totems.common.commandline.EditPolicyGenerator;
import com.abtnetworks.totems.common.commandline.PolicyGenerator;
import com.abtnetworks.totems.common.commandline.security.SecurityUsg6000;
import com.abtnetworks.totems.common.commandline.security.SecurityUsg6000ForGuangZhou;
import com.abtnetworks.totems.common.dto.CmdDTO;
import com.abtnetworks.totems.common.dto.commandline.CommandlineDTO;
import com.abtnetworks.totems.common.dto.commandline.EditCommandlineDTO;
import com.abtnetworks.totems.common.enums.DeviceModelNumberEnum;
import com.abtnetworks.totems.common.enums.PolicyEnum;
import org.apache.log4j.Logger;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

/**
 * @author lps
 */
@Service
@CustomCli(value = DeviceModelNumberEnum.USG6000, type = PolicyEnum.EDIT_SECURITY)
public class EditUsg6000ForGuangZhou extends EditPolicyGenerator implements PolicyGenerator {

    private static Logger logger = Logger.getLogger(EditUsg6000ForGuangZhou.class);



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
        SecurityUsg6000ForGuangZhou securityUsg6000 = new SecurityUsg6000ForGuangZhou();
        CommandlineDTO dto1 = new CommandlineDTO();
        BeanUtils.copyProperties(dto, dto1);
        return  securityUsg6000.generatePreCommandline(dto1)+securityUsg6000.createCommandLine(dto1,dto.getMergeProperty());
    }

    public String editCommandLine(EditCommandlineDTO dto){
        return  null;
    }
}
