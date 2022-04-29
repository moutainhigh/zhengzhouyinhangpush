package com.abtnetworks.totems.common.commandline.rollback;

import com.abtnetworks.totems.common.commandline.PolicyGenerator;
import com.abtnetworks.totems.common.dto.CmdDTO;
import com.abtnetworks.totems.common.dto.DeviceDTO;
import com.abtnetworks.totems.common.dto.GeneratedObjectDTO;
import org.springframework.stereotype.Service;

/**
 * @author luwei
 * @date 2020/7/18
 */
@Service
public class RollbackStaticSangfor implements PolicyGenerator {

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

        sb.append(String.format("no snat-rule %s\n", generatedObjectDTO.getPolicyName()));
        sb.append("end\n");
        return sb.toString();
    }
}
