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
@CustomCli(value = DeviceModelNumberEnum.HILLSTONE, type = PolicyEnum.DNAT)
public class HillStoneDnatForHUARUI implements PolicyGenerator {

    @Autowired
    HillstoneNatForHUARUI hillstoneForHuaRui;
    @Override
    public String generate(CmdDTO cmdDTO) {
        log.info("华瑞银行Dnat命令生成");
        return hillstoneForHuaRui.generate(cmdDTO);
    }
}
