package com.abtnetworks.totems.common.commandline.rollback.acl;

import com.abtnetworks.totems.common.commandline.PolicyGenerator;
import com.abtnetworks.totems.common.dto.CmdDTO;
import com.abtnetworks.totems.common.dto.GeneratedObjectDTO;
import com.alibaba.fastjson.JSON;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;


/**
 * juniper路由的回滚命令行
 */
@Service
@Log4j2
public class RollbackAclJuniper implements PolicyGenerator {
    @Override
    public String generate(CmdDTO cmdDTO) {

        GeneratedObjectDTO generatedObjectDTO = cmdDTO.getGeneratedObject();
        StringBuffer sb = new StringBuffer();
        sb.append("configure\n");
        log.info("Juniper Acl回滚命令行为：{}", JSON.toJSONString(generatedObjectDTO.getRollbackCommandLine()));
        sb.append(String.format("%s", generatedObjectDTO.getRollbackCommandLine()));

        sb.append("commit\n");
        return sb.toString();
    }
}
