package com.abtnetworks.totems.common.commandline.editforbid;

import com.abtnetworks.totems.common.commandline.PolicyGenerator;
import com.abtnetworks.totems.common.commandline.forbid.ForbidHillStoneR5;
import com.abtnetworks.totems.common.constants.CommonConstants;
import com.abtnetworks.totems.common.dto.*;
import com.abtnetworks.totems.common.dto.commandline.PolicyObjectDTO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

/**
 * @author luwei
 * @date 2020/9/11
 */
@Slf4j
@Service
public class EditForbidHillStoneR5 implements PolicyGenerator {

    @Override
    public String generate(CmdDTO cmdDTO) {

        PolicyDTO policyDTO = cmdDTO.getPolicy();

        TaskDTO taskDTO = cmdDTO.getTask();

        SettingDTO settingDTO = cmdDTO.getSetting();

        DeviceDTO deviceDTO = cmdDTO.getDevice();
        GeneratedObjectDTO generatedObjectDTO = cmdDTO.getGeneratedObject();

        ForbidHillStoneR5 forbidHillStoneR5 = new ForbidHillStoneR5();

        StringBuilder sb = new StringBuilder();
        PolicyObjectDTO srcAddress = forbidHillStoneR5.generateAddressObject(policyDTO.getSrcIp(), taskDTO.getTheme(), "src");

        sb.append("configure\n");
        //定义对象
        if (srcAddress.isObjectFlag() && StringUtils.isNotBlank(srcAddress.getCommandLine())) {
            sb.append(String.format("%s\n", srcAddress.getCommandLine()));
        }

        sb.append(String.format("rule id %s \n", CommonConstants.LABEL_POLICY_ID));
        //修改源对象引用
        sb.append(srcAddress.getJoin());
        //去掉旧对象的引用
        sb.append(String.format("no src-addr %s \n", generatedObjectDTO.getSrcObjectName()));

        sb.append("exit \n");

        //删除旧对象
        sb.append(String.format("no address %s \n", generatedObjectDTO.getSrcObjectName()));
        sb.append("end\n");
        //命令行生成完成之后，再将新对象名返回
        generatedObjectDTO.setSrcObjectName(srcAddress.getName());

        return sb.toString();
    }
}
