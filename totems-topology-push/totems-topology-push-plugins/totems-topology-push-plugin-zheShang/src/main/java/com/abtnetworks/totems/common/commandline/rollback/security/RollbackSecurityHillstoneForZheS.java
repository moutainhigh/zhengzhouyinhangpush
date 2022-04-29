package com.abtnetworks.totems.common.commandline.rollback.security;

import com.abtnetworks.totems.common.annotation.CustomCli;
import com.abtnetworks.totems.common.commandline.PolicyGenerator;
import com.abtnetworks.totems.common.commandline.rollback.RollbackSecurityHillstoneR5;
import com.abtnetworks.totems.common.dto.CmdDTO;
import com.abtnetworks.totems.common.dto.DeviceDTO;
import com.abtnetworks.totems.common.dto.GeneratedObjectDTO;
import com.abtnetworks.totems.common.enums.DeviceModelNumberEnum;
import com.abtnetworks.totems.common.enums.PolicyEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * @author luwei
 * @date 2020/7/18
 */
@Slf4j
@CustomCli(value = DeviceModelNumberEnum.HILLSTONE, type = PolicyEnum.SECURITY, classPoxy = RollbackSecurityHillstoneR5.class)
@Service
public class RollbackSecurityHillstoneForZheS implements PolicyGenerator {

    @Override
    public String generate(CmdDTO cmdDTO) {
        GeneratedObjectDTO generatedObjectDTO = cmdDTO.getGeneratedObject();

        StringBuilder sb = new StringBuilder();
        DeviceDTO deviceDTO =  cmdDTO.getDevice();
        if (deviceDTO.isVsys()) {
            sb.append("enter-vsys " + deviceDTO.getVsysName() + "\n");
        }
        sb.append("configure\n");
        sb.append("policy-global\n");
        sb.append(String.format("no rule name %s \n", generatedObjectDTO.getPolicyName()));

        sb.append("end\n");
        return sb.toString();
    }
}
