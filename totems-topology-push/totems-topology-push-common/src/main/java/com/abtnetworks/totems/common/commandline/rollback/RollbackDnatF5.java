package com.abtnetworks.totems.common.commandline.rollback;

import com.abtnetworks.totems.common.commandline.PolicyGenerator;
import com.abtnetworks.totems.common.dto.CmdDTO;
import com.abtnetworks.totems.common.dto.DeviceDTO;
import com.abtnetworks.totems.common.dto.TaskDTO;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

@Service
public class RollbackDnatF5 implements PolicyGenerator {
    @Override
    public String generate(CmdDTO cmdDTO) {
        StringBuilder sb = new StringBuilder();
        TaskDTO taskDTO = cmdDTO.getTask();
        DeviceDTO device = cmdDTO.getDevice();
        String virtualName = taskDTO.getTheme();
        sb.append(String.format("tmsh%s", StringUtils.LF));
        if (device.isVsys()){
            sb.append(String.format("cd /%s",device.getVsysName())).append(StringUtils.LF);
        }
        sb.append(String.format("delete ltm virtual %s%s", virtualName,StringUtils.LF));
        sb.append(String.format("save sys config%s", StringUtils.LF));
        sb.append(String.format("quit%s", StringUtils.LF));
        return sb.toString();
    }
}
