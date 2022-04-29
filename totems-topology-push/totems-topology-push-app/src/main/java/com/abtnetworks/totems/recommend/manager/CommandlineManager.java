package com.abtnetworks.totems.recommend.manager;

import com.abtnetworks.totems.common.dto.CmdDTO;

public interface CommandlineManager {

    String generate(CmdDTO cmdDTO);

    String generateRollback(CmdDTO cmdDTO);
}
