package com.abtnetworks.totems.common.commandline.rollback;

import com.abtnetworks.totems.common.annotation.CustomCli;
import com.abtnetworks.totems.common.commandline.PolicyGenerator;
import com.abtnetworks.totems.common.commandline.rollback.acl.RollbackAclH3cV7;
import com.abtnetworks.totems.common.dto.CmdDTO;
import com.abtnetworks.totems.common.dto.DeviceDTO;
import com.abtnetworks.totems.common.dto.GeneratedObjectDTO;
import com.abtnetworks.totems.common.enums.DeviceModelNumberEnum;
import com.abtnetworks.totems.common.enums.PolicyEnum;
import org.springframework.stereotype.Service;

/**
 * @author Administrator
 */
@CustomCli(value = DeviceModelNumberEnum.H3CV7, type = PolicyEnum.ACL,classPoxy = RollbackAclH3cV7.class)
@Service
public class RollbackAclH3cV7AnHui implements PolicyGenerator {

    @Override
    public String generate(CmdDTO cmdDTO) {

        GeneratedObjectDTO generatedObjectDTO = cmdDTO.getGeneratedObject();
        DeviceDTO deviceDTO = cmdDTO.getDevice();

        StringBuilder sb = new StringBuilder();
        sb.append("system-view\n");
        if (deviceDTO.isVsys()) {
            sb.append("switchto context " + deviceDTO.getVsysName() + "\n");
        }

        String aclPolicyCommand = generatedObjectDTO.getAclPolicyCommand();
        sb.append(aclPolicyCommand);
        sb.append("quit\n");
        return sb.toString();
    }
}
