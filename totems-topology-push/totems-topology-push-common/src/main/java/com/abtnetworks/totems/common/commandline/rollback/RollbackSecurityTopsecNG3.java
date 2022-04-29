package com.abtnetworks.totems.common.commandline.rollback;

import com.abtnetworks.totems.common.commandline.PolicyGenerator;
import com.abtnetworks.totems.common.dto.CmdDTO;
import com.abtnetworks.totems.common.dto.GeneratedObjectDTO;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

/**
 * @author lifei
 * @desc 天融信NG3 安全策略回滚命令行
 * @date 2021/9/13 11:24
 */
@Service
public class RollbackSecurityTopsecNG3 implements PolicyGenerator {
    @Override
    public String generate(CmdDTO cmdDTO) {
        StringBuilder sb = new StringBuilder();

        GeneratedObjectDTO generatedObjectDTO = cmdDTO.getGeneratedObject();
        sb.append("firewalll").append(StringUtils.LF);
        sb.append(String.format("policy delete name %s",generatedObjectDTO.getPolicyName()));
        sb.append(StringUtils.LF);
        sb.append("end\n");

        return sb.toString();
    }
}
