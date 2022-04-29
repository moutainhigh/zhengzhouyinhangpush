package com.abtnetworks.totems.common.commandline.rollback.acl;

import com.abtnetworks.totems.common.commandline.PolicyGenerator;
import com.abtnetworks.totems.common.dto.CmdDTO;
import org.springframework.stereotype.Service;

import static com.abtnetworks.totems.common.commandline.NatPolicyGenerator.DO_NOT_SUPPORT;

/**
 * @author Administrator
 */
@Service("rollbackAclH3cV7-v1")
public class RollbackAclH3cV7 implements PolicyGenerator {

    @Override
    public String generate(CmdDTO cmdDTO) {

        return DO_NOT_SUPPORT;
    }
}
