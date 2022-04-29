package com.abtnetworks.totems.common.commandline.rollback;

import com.abtnetworks.totems.common.commandline.PolicyGenerator;
import com.abtnetworks.totems.common.commandline.common.dto.PolicyGeneratorDTO;
import com.abtnetworks.totems.common.dto.CmdDTO;
import com.abtnetworks.totems.common.dto.DeviceDTO;
import com.abtnetworks.totems.common.dto.GeneratedObjectDTO;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

/**
 * @author lifei
 * @desc 思科ASA和ASA8.4 支持snat策略回滚
 * @date 2021/7/27 16:41
 */
@Service
public class RollbackSnatCiscoASA implements PolicyGenerator {
    @Override
    public String generate(CmdDTO cmdDTO) {
        StringBuilder sb = new StringBuilder();
        DeviceDTO device = cmdDTO.getDevice();
        GeneratedObjectDTO generatedObject = cmdDTO.getGeneratedObject();
        if (device.isVsys()) {
            sb.append("changeto context system \n");
            sb.append("changeto context " + device.getVsysName() + "\n");
        }
        sb.append("configure terminal").append(StringUtils.LF);

        String rollbackShowCmd = generatedObject.getRollbackShowCmd();
        if (StringUtils.isNotEmpty(rollbackShowCmd)){
            String[] strings = rollbackShowCmd.split(StringUtils.LF);
            for (String string : strings) {
                sb.append("no ").append(string).append(StringUtils.LF);
            }
        }

        sb.append("\nend\nwrite\n");
        sb.append("\n");
        return sb.toString();
    }

    @Override
    public PolicyGeneratorDTO generateV2(CmdDTO cmdDTO) {
        // 这里 cisco ASA老的设备 snat创建的时候没有地址对象和服务对象的创建
        PolicyGeneratorDTO policyGeneratorDTO = new PolicyGeneratorDTO();
        StringBuilder sb = new StringBuilder();
        DeviceDTO device = cmdDTO.getDevice();
        GeneratedObjectDTO generatedObject = cmdDTO.getGeneratedObject();
        if (device.isVsys()) {
            sb.append("changeto context system \n");
            sb.append("changeto context " + device.getVsysName() + "\n");
        }
        sb.append("configure terminal").append(StringUtils.LF);

        String rollbackShowCmd = generatedObject.getRollbackShowCmd();
        if (StringUtils.isNotEmpty(rollbackShowCmd)){
            String[] strings = rollbackShowCmd.split(StringUtils.LF);
            for (String string : strings) {
                sb.append("no ").append(string).append(StringUtils.LF);
            }
        }

        sb.append("\nend\nwrite\n");
        sb.append("\n");
        policyGeneratorDTO.setPolicyRollbackCommandLine(sb.toString());
        return policyGeneratorDTO;
    }
}
