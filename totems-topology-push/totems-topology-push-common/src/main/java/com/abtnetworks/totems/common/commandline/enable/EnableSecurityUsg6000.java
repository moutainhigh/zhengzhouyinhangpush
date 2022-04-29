package com.abtnetworks.totems.common.commandline.enable;

import com.abtnetworks.totems.common.commandline.PolicyGenerator;
import com.abtnetworks.totems.common.dto.CmdDTO;
import com.abtnetworks.totems.common.dto.DeviceDTO;
import com.abtnetworks.totems.common.dto.GeneratedObjectDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * @author luwei
 * @date 2020/9/11
 */
@Slf4j
@Service
public class EnableSecurityUsg6000 implements PolicyGenerator {

    @Override
    public String generate(CmdDTO cmdDTO) {
        StringBuilder sb = new StringBuilder();
        DeviceDTO device = cmdDTO.getDevice();

        // 华为虚墙加命令行
        if(device.isVsys()) {
            sb.append("system-view\n");
            sb.append("switch vsys ").append(device.getVsysName()).append("\n");
        }

        sb.append("system-view\n");
        sb.append("security-policy\n");

        GeneratedObjectDTO generatedObjectDTO = cmdDTO.getGeneratedObject();

        //进入策略
        sb.append(String.format("rule name %s \n", generatedObjectDTO.getPolicyName()));
        //禁用
        sb.append("enable \n");
        sb.append("return \n");

        return sb.toString();
    }
}
