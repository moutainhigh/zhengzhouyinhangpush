package com.abtnetworks.totems.common.commandline.editforbid;

import com.abtnetworks.totems.common.commandline.PolicyGenerator;
import com.abtnetworks.totems.common.commandline.security.SecurityUsg6000;
import com.abtnetworks.totems.common.dto.*;
import com.abtnetworks.totems.common.dto.commandline.CommandlineDTO;
import com.abtnetworks.totems.common.dto.commandline.PolicyObjectDTO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

/**
 * @author luwei
 * @date 2020/9/11
 */
@Slf4j
@Service
public class EditForbidUsg6000 implements PolicyGenerator {

    @Override
    public String generate(CmdDTO cmdDTO) {

        PolicyDTO policyDTO = cmdDTO.getPolicy();

        TaskDTO taskDTO = cmdDTO.getTask();

        SettingDTO settingDTO = cmdDTO.getSetting();

        DeviceDTO deviceDTO = cmdDTO.getDevice();
        GeneratedObjectDTO generatedObjectDTO = cmdDTO.getGeneratedObject();


        //转CommandlineDTO
        CommandlineDTO commandDTO = new CommandlineDTO();
        BeanUtils.copyProperties(deviceDTO, commandDTO);


        SecurityUsg6000 securityUsg6000 = new SecurityUsg6000();

        //建立新对象
        PolicyObjectDTO srcAddressObject = securityUsg6000.generateAddressObject(policyDTO.getSrcIp(), taskDTO.getTheme(), "source-address", settingDTO.isCreateObject(), null, null,0);

        StringBuilder sb = new StringBuilder();
        //进入命令行，前缀
        String preCmd = securityUsg6000.generatePreCommandline(commandDTO);
        sb.append(preCmd);

        //定义对象
        if (srcAddressObject.isObjectFlag() && StringUtils.isNotBlank(srcAddressObject.getCommandLine())) {
            sb.append(String.format("%s\n", srcAddressObject.getCommandLine()));
        }

        sb.append("security-policy\n");
        //修改策略中，引用的源地址对象
        sb.append(String.format("rule name %s \n", generatedObjectDTO.getPolicyName()));
        sb.append(srcAddressObject.getJoin());
        sb.append(String.format("undo source-address address-set %s \n", generatedObjectDTO.getSrcObjectName()));
        sb.append("quit \n");
        sb.append("quit \n");

        //删除旧的对象
        sb.append(String.format("undo ip address-set %s \n", generatedObjectDTO.getSrcObjectName()));
        sb.append("return\n");
        //命令行生成完成之后，再将新对象名返回
        generatedObjectDTO.setSrcObjectName(srcAddressObject.getName());

        return sb.toString();
    }
}
