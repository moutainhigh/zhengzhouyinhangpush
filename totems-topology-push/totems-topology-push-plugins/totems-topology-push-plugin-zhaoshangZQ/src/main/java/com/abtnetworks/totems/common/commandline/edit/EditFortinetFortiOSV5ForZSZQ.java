package com.abtnetworks.totems.common.commandline.edit;

import com.abtnetworks.totems.common.annotation.CustomCli;
import com.abtnetworks.totems.common.commandline.EditPolicyGenerator;
import com.abtnetworks.totems.common.commandline.PolicyGenerator;
import com.abtnetworks.totems.common.commandline.security.SecurityFortinetFortiOSForZSZQ;
import com.abtnetworks.totems.common.commandline.security.SecurityFortinetFortiOSV5ForZSZQ;
import com.abtnetworks.totems.common.dto.CmdDTO;
import com.abtnetworks.totems.common.dto.commandline.CommandlineDTO;
import com.abtnetworks.totems.common.dto.commandline.EditCommandlineDTO;
import com.abtnetworks.totems.common.enums.DeviceModelNumberEnum;
import com.abtnetworks.totems.common.enums.PolicyEnum;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

/**
 * @author lifei
 * @desc 飞塔V5.0设备 编辑策略定制化命令行
 * @date 2021/10/15 13:44
 */

@Service
@CustomCli(value = DeviceModelNumberEnum.FORTINET_V5, type = PolicyEnum.EDIT_SECURITY)
public class EditFortinetFortiOSV5ForZSZQ extends EditPolicyGenerator implements PolicyGenerator {

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
        SecurityFortinetFortiOSV5ForZSZQ securityFortinetFortiOSV5 = new SecurityFortinetFortiOSV5ForZSZQ();
        CommandlineDTO dto1 = new CommandlineDTO();
        BeanUtils.copyProperties(dto, dto1);
        return  securityFortinetFortiOSV5.createCommandLine(dto1,dto.getMergeProperty());
    }

    public String editCommandLine(EditCommandlineDTO dto) {
        return null;
    }
}
