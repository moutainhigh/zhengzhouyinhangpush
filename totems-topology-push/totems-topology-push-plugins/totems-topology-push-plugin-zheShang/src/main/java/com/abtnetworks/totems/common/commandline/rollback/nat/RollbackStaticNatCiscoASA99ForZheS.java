package com.abtnetworks.totems.common.commandline.rollback.nat;

import com.abtnetworks.totems.common.annotation.CustomCli;
import com.abtnetworks.totems.common.commandline.PolicyGenerator;
import com.abtnetworks.totems.common.commandline.nat.NatCiscoASA99ForzheShang;
import com.abtnetworks.totems.common.commandline.rollback.RollbackStaticNatCiscoASA99;
import com.abtnetworks.totems.common.dto.CmdDTO;
import com.abtnetworks.totems.common.dto.commandline.StaticNatTaskDTO;
import com.abtnetworks.totems.common.enums.DeviceModelNumberEnum;
import com.abtnetworks.totems.common.enums.PolicyEnum;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

@Service
@Log4j2
@CustomCli(value = DeviceModelNumberEnum.CISCO_ASA_99, type = PolicyEnum.STATIC, classPoxy = RollbackStaticNatCiscoASA99.class)
public class RollbackStaticNatCiscoASA99ForZheS implements PolicyGenerator {
    @Override
    public String generate(CmdDTO cmdDTO) {
        log.info("进行思科99 StaticNat回滚执行器");

        NatCiscoASA99ForzheShang natPolicyGenerator = new NatCiscoASA99ForzheShang();

        StaticNatTaskDTO policyDTO = natPolicyGenerator.getStaticNatDTO(cmdDTO);
        policyDTO.setRollback(true);

        String rollbackCommendLine = natPolicyGenerator.generateStaticNatCommandLine(policyDTO);

        return rollbackCommendLine;
    }
}
