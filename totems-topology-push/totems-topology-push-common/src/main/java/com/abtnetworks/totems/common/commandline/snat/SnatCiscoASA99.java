package com.abtnetworks.totems.common.commandline.snat;


import com.abtnetworks.totems.common.commandline.PolicyGenerator;
import com.abtnetworks.totems.common.commandline.nat.CiscoASA99Nat;
import com.abtnetworks.totems.common.dto.CmdDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Slf4j
@Service
public class SnatCiscoASA99 implements PolicyGenerator {

    @Autowired
    CiscoASA99Nat ciscoASA99Nat;

    @Override
    public String generate(CmdDTO cmdDTO) {
        return ciscoASA99Nat.generate(cmdDTO);
    }
}
