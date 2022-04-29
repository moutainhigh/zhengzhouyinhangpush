package com.abtnetworks.totems.common.commandline.enable;

import com.abtnetworks.totems.common.commandline.PolicyGenerator;
import com.abtnetworks.totems.common.constants.CommonConstants;
import com.abtnetworks.totems.common.dto.CmdDTO;
import com.abtnetworks.totems.common.dto.GeneratedObjectDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * @author luwei
 * @date 2020/9/11
 */
@Slf4j
@Service
public class EnableSecurityHillStoneR5 implements PolicyGenerator {

    @Override
    public String generate(CmdDTO cmdDTO) {
        StringBuilder sb = new StringBuilder();
        sb.append("configure \n");

        //进入策略
        sb.append(String.format("rule id %s \n", CommonConstants.LABEL_POLICY_ID));
        //禁用
        sb.append("enable \n");
        sb.append("exit \n");
        sb.append("end\n");
        return sb.toString();
    }
}
