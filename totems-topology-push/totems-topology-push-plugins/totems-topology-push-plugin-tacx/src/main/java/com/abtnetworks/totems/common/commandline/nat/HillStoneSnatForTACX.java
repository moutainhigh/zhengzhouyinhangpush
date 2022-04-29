package com.abtnetworks.totems.common.commandline.nat;

import com.abtnetworks.totems.common.annotation.CustomCli;
import com.abtnetworks.totems.common.commandline.PolicyGenerator;
import com.abtnetworks.totems.common.dto.CmdDTO;
import com.abtnetworks.totems.common.enums.DeviceModelNumberEnum;
import com.abtnetworks.totems.common.enums.PolicyEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author Administrator
 * @Title:
 * @Description: 请写注释类
 * @date 2020/8/13
 */
@Slf4j
@Service
@CustomCli(value = DeviceModelNumberEnum.HILLSTONE, type = PolicyEnum.SNAT)
public class HillStoneSnatForTACX implements PolicyGenerator {

    @Autowired
    HillStoneNatForTACX hillStoneNatForTACX;
    @Override
    public String generate(CmdDTO cmdDTO) {
        log.info("天安财险山石nat命令生成");
        return hillStoneNatForTACX.generate(cmdDTO);
    }
}
