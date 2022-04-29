package com.abtnetworks.totems.common.commandline.edit;

import com.abtnetworks.totems.common.annotation.CustomCli;
import com.abtnetworks.totems.common.commandline.EditPolicyGenerator;
import com.abtnetworks.totems.common.commandline.PolicyGenerator;
import com.abtnetworks.totems.common.commandline.security.SecurityCiscoASA86ForZZYH;
import com.abtnetworks.totems.common.dto.CmdDTO;
import com.abtnetworks.totems.common.dto.commandline.CommandlineDTO;
import com.abtnetworks.totems.common.dto.commandline.EditCommandlineDTO;
import com.abtnetworks.totems.common.enums.DeviceModelNumberEnum;
import com.abtnetworks.totems.common.enums.PolicyEnum;
import org.apache.log4j.Logger;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

@Service
@CustomCli(value = DeviceModelNumberEnum.CISCO_ASA_86, type = PolicyEnum.EDIT_SECURITY)
public class EditCisocASA86ForZZYH extends EditPolicyGenerator implements PolicyGenerator {

    private static Logger logger = Logger.getLogger(EditCisocASA86ForZZYH.class);

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
        SecurityCiscoASA86ForZZYH securityCiscoASA86 = new SecurityCiscoASA86ForZZYH();
        CommandlineDTO dto1 = new CommandlineDTO();
        BeanUtils.copyProperties(dto,dto1);
        return securityCiscoASA86.generatePreCommandline(dto1) + securityCiscoASA86.createMergeCommandLine(dto1,dto.getMergeProperty());
    }

    public String editCommandLine(EditCommandlineDTO dto){
        return  null;
    }

}
