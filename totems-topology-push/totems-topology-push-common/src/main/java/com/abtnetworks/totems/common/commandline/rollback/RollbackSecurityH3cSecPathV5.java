package com.abtnetworks.totems.common.commandline.rollback;

import com.abtnetworks.totems.common.commandline.PolicyGenerator;
import com.abtnetworks.totems.common.dto.CmdDTO;
import com.abtnetworks.totems.common.dto.PolicyDTO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;


@Slf4j
@Service
public class RollbackSecurityH3cSecPathV5 implements PolicyGenerator {

    @Override
    public String generate(CmdDTO cmdDTO) {
        PolicyDTO policy = cmdDTO.getPolicy();
        String srcZone = policy.getSrcZone();
        String dstZone = policy.getDstZone();
        if (StringUtils.isEmpty(srcZone)) {
            srcZone = "Any";
        } else {
            if(srcZone.contains("_")){
                srcZone = StringUtils.substringAfterLast(srcZone, "_");
            }else if(srcZone.contains("-")){
                srcZone = StringUtils.substringAfterLast(srcZone, "-");
            }

        }
        if (StringUtils.isEmpty(dstZone)) {
            dstZone = "Any";
        } else {
            if(dstZone.contains("_")) {
                dstZone = StringUtils.substringAfterLast(dstZone, "_");
            }else if(dstZone.contains("-")){
                dstZone = StringUtils.substringAfterLast(dstZone, "-");
            }
        }
        StringBuilder sb = new StringBuilder();
        sb.append("system-view\n");
        sb.append("interzone source ").append(srcZone).append(" destination ").append(dstZone).append(StringUtils.LF);
        sb.append("undo rule <policyId>\n");
        sb.append("quit\n");
        sb.append("return\n");
        return sb.toString();
    }

}
