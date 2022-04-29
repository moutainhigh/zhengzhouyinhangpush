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
public class RollbackSecuritySangfor implements PolicyGenerator {
    @Override
    public String generate(CmdDTO cmdDTO) {
        StringBuilder sb = new StringBuilder();
        judgeIsVsy( cmdDTO,  sb);

        GeneratedObjectDTO generatedObjectDTO = cmdDTO.getGeneratedObject();
        sb.append(String.format("no acl-policy %s\n", generatedObjectDTO.getPolicyName()));
        sb.append("end\n");

        return sb.toString();
    }

    /**
     * 判断虚墙
     * @param cmdDTO
     * @param sb
     */
    private void judgeIsVsy(CmdDTO cmdDTO, StringBuilder sb){
        sb.append("config\n");
        DeviceDTO deviceDTO = cmdDTO.getDevice();
        if(deviceDTO.isVsys()) {
            sb.append("vsys change " + deviceDTO.getVsysName() + "\n");
            sb.append("config\n");
        }
    }

}
