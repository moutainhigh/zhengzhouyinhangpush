package com.abtnetworks.totems.common.commandline.nat;

import com.abtnetworks.totems.common.commandline.NatPolicyGenerator;
import com.abtnetworks.totems.common.dto.CmdDTO;
import com.abtnetworks.totems.common.dto.commandline.*;
import org.springframework.stereotype.Service;

@Service("defaultNat")
@Deprecated
public class Default implements NatPolicyGenerator {

    @Override
    public String generate(CmdDTO cmdDTO) {
        return DO_NOT_SUPPORT;
    }

    @Override
    public String generateStaticNatCommandLine(StaticNatTaskDTO policyDTO){
        return DO_NOT_SUPPORT;
    }

    @Override
    public String generateSNatCommandLine(SNatPolicyDTO policyDTO){
        return DO_NOT_SUPPORT;
    }

    @Override
    public String generateDNatCommandLine(DNatPolicyDTO policyDTO){
        return DO_NOT_SUPPORT;
    }

    @Override
    public String generateBothNatCommandLine(NatPolicyDTO policyDTO) {
        return DO_NOT_SUPPORT;
    }
}
