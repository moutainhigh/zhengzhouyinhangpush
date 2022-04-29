package com.abtnetworks.totems.common.atomcommandline.rollback;

import com.abtnetworks.totems.common.atomcommandline.base.BaseHillStoneCommonBuss;
import com.abtnetworks.totems.common.commandline.PolicyGenerator;
import com.abtnetworks.totems.common.dto.CmdDTO;
import com.abtnetworks.totems.common.dto.DeviceDTO;
import com.abtnetworks.totems.common.dto.GeneratedObjectDTO;
import com.abtnetworks.totems.vender.hillstone.security.SecurityHillStoneR5Impl;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * @author luwei
 * @date 2020/7/18
 */
@Service
@Log4j2
public class AtomRollbackSecurityHillstoneR5 extends BaseHillStoneCommonBuss implements PolicyGenerator {

    private SecurityHillStoneR5Impl generatorBean;

    public AtomRollbackSecurityHillstoneR5() {
        generatorBean = new SecurityHillStoneR5Impl();
    }

    @Override
    public String generate(CmdDTO cmdDTO) {
        GeneratedObjectDTO generatedObjectDTO = cmdDTO.getGeneratedObject();
        StringBuilder sb = new StringBuilder();
        DeviceDTO deviceDTO =  cmdDTO.getDevice();
        sb.append(generatorBean.generatePreCommandline(deviceDTO.isVsys(),deviceDTO.getVsysName(),null,null));
        Map<String, Object> map = new HashMap<>();
        map.put("rollbackType",cmdDTO.getSetting().getRollbackType());
        sb.append(generatorBean.deleteSecurityPolicyByIdOrName(null,null,generatedObjectDTO.getPolicyName(),map,null));
        sb.append(generatorBean.generatePostCommandline(null,null));
        super.rollbackHillStoneObject(sb,generatorBean,cmdDTO);

        return sb.toString();
    }
}
