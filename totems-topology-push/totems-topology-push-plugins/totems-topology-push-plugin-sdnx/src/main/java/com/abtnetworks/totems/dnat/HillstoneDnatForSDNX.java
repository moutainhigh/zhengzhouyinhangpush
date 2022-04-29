package com.abtnetworks.totems.dnat;

import com.abtnetworks.totems.common.annotation.CustomCli;
import com.abtnetworks.totems.common.commandline.PolicyGenerator;
import com.abtnetworks.totems.common.dto.CmdDTO;
import com.abtnetworks.totems.common.enums.DeviceModelNumberEnum;
import com.abtnetworks.totems.common.enums.PolicyEnum;
import com.abtnetworks.totems.nat.HillStoneNatForSDNX;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @desc    山东农信定制nat命令行
 * @author liuchanghao
 * @date 2020-11-19 15:13
 */
@Service
@Slf4j
@CustomCli(value = DeviceModelNumberEnum.HILLSTONE, type = PolicyEnum.DNAT)
public class HillstoneDnatForSDNX implements PolicyGenerator {

    @Autowired
    HillStoneNatForSDNX hillStoneNatForSDNX;

    @Override
    public String generate(CmdDTO cmdDTO) {
        log.info("山东农信山石nat命令生成");
        return hillStoneNatForSDNX.generate(cmdDTO);
    }

}
