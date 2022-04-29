package com.abtnetworks.totems.common.commandline.rollback;

import com.abtnetworks.totems.common.commandline.PolicyGenerator;
import com.abtnetworks.totems.common.commandline.common.dto.PolicyGeneratorDTO;
import com.abtnetworks.totems.common.dto.CmdDTO;
import com.abtnetworks.totems.common.dto.DeviceDTO;
import com.abtnetworks.totems.common.dto.GeneratedObjectDTO;
import org.springframework.stereotype.Service;

@Service
public class RollbackDnatHillstone implements PolicyGenerator {
    @Override
    public String generate(CmdDTO cmdDTO) {
        StringBuilder sb = new StringBuilder();

        DeviceDTO deviceDTO =  cmdDTO.getDevice();
        if (deviceDTO.isVsys()) {

            sb.append("enter-vsys " + deviceDTO.getVsysName() + "\n");
        }
        sb.append("configure\n");
        sb.append("nat\n");
        sb.append(String.format("no dnatrule id #1 \n"));
        sb.append("end\n");
        return sb.toString();
    }

}
