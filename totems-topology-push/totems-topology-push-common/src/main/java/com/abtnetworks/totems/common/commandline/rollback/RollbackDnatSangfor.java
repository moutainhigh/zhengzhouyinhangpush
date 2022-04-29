package com.abtnetworks.totems.common.commandline.rollback;

import com.abtnetworks.totems.common.commandline.PolicyGenerator;
import com.abtnetworks.totems.common.dto.CmdDTO;
import com.abtnetworks.totems.common.dto.DeviceDTO;
import com.abtnetworks.totems.common.dto.GeneratedObjectDTO;
import org.springframework.stereotype.Service;

/**
 * @Description
 * @Version
 * @Created by hw on '2020/8/13'.
 */
@Service
public class RollbackDnatSangfor implements PolicyGenerator {

    @Override
    public String generate(CmdDTO cmdDTO) {
        GeneratedObjectDTO generatedObjectDTO = cmdDTO.getGeneratedObject();
        DeviceDTO deviceDTO = cmdDTO.getDevice();

        StringBuilder sb = new StringBuilder();
        sb.append("config\n");
        if (deviceDTO.isVsys()) {
            sb.append("vsys change " + deviceDTO.getVsysName() + "\n");
            sb.append("config\n");
        }

        sb.append(String.format("no dnat-rule %s\n", generatedObjectDTO.getPolicyName()));
        sb.append("end\n");
        return sb.toString();
    }
}
