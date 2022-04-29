package com.abtnetworks.totems.common.atomcommandline.rollback;

import com.abtnetworks.totems.common.commandline.PolicyGenerator;
import com.abtnetworks.totems.common.constants.CommonConstants;
import com.abtnetworks.totems.common.dto.CmdDTO;
import com.abtnetworks.totems.common.dto.DeviceDTO;
import com.abtnetworks.totems.common.dto.GeneratedObjectDTO;
import com.abtnetworks.totems.vender.ssg.security.SecurityH3cv7OpImpl;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

/**
 * @author lifei
 * @desc 原子化安全策略 ssg型号设备回滚执行类
 * @date 2021/11/17 18:37
 */
@Service
public class AtomRollbackSecurityH3cV7Op implements PolicyGenerator {

    private SecurityH3cv7OpImpl generatorBean;

    public AtomRollbackSecurityH3cV7Op() {
        generatorBean = new SecurityH3cv7OpImpl();
    }

    @Override
    public String generate(CmdDTO cmdDTO) {
        GeneratedObjectDTO generatedObjectDTO = cmdDTO.getGeneratedObject();
        StringBuilder sb = new StringBuilder();
        DeviceDTO deviceDTO =  cmdDTO.getDevice();
        sb.append(generatorBean.generatePreCommandline(deviceDTO.isVsys(),deviceDTO.getVsysName(),null,null));
        sb.append(generatedObjectDTO.getRollbackShowCmd()).append(StringUtils.LF);
        sb.append(generatorBean.deleteSecurityPolicyByIdOrName(null, CommonConstants.POLICY_ID,CommonConstants.OBJECT_POLICY,null,null));
        sb.append(generatorBean.generatePostCommandline(null,null));
        return sb.toString();
    }
}
