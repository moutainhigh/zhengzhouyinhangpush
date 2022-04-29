package com.abtnetworks.totems.common.commandline.rollback.nat;

import com.abtnetworks.totems.common.annotation.CustomCli;
import com.abtnetworks.totems.common.commandline.PolicyGenerator;
import com.abtnetworks.totems.common.commandline.nat.NatCiscoASA99ForzheShang;
import com.abtnetworks.totems.common.commandline.rollback.RollbackBothNatCiscoASA99;
import com.abtnetworks.totems.common.dto.CmdDTO;
import com.abtnetworks.totems.common.dto.commandline.NatPolicyDTO;
import com.abtnetworks.totems.common.enums.DeviceModelNumberEnum;
import com.abtnetworks.totems.common.enums.PolicyEnum;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

@Service
@CustomCli(value = DeviceModelNumberEnum.CISCO_ASA_99, type = PolicyEnum.BOTH, classPoxy = RollbackBothNatCiscoASA99.class)
@Log4j2
public class RollbackBothNatCiscoASA99ForZheS implements PolicyGenerator {
    @Override
    public String generate(CmdDTO cmdDTO) {
        log.info("进行思科99bothNat回滚执行器");
        NatCiscoASA99ForzheShang natPolicyGenerator = new NatCiscoASA99ForzheShang();

        NatPolicyDTO policyDTO = natPolicyGenerator.getBothNatDTO(cmdDTO);
        policyDTO.setRollback(true);

        String rollbackCommendLine = natPolicyGenerator.generateBothNatCommandLine(policyDTO);

        return rollbackCommendLine;
    }
}
