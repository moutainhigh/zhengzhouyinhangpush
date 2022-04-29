package com.abtnetworks.totems.common.commandline.rollback;

import com.abtnetworks.totems.common.annotation.CustomCli;
import com.abtnetworks.totems.common.commandline.PolicyGenerator;
import com.abtnetworks.totems.common.commandline.rollback.security.RollbackTopsecTos010020;
import com.abtnetworks.totems.common.constants.CommonConstants;
import com.abtnetworks.totems.common.dto.CmdDTO;
import com.abtnetworks.totems.common.dto.GeneratedObjectDTO;
import com.abtnetworks.totems.common.enums.DeviceModelNumberEnum;
import com.abtnetworks.totems.common.enums.PolicyEnum;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import static com.abtnetworks.totems.common.commandline.NatPolicyGenerator.DO_NOT_SUPPORT;

/**
 * @author luwei
 * @date 2020/7/18
 */
@Slf4j
@CustomCli(value = DeviceModelNumberEnum.TOPSEC_TOS_010_020, type = PolicyEnum.SECURITY, classPoxy = RollbackTopsecTos010020.class)
@Service
public class RollbackTopsecTos010020AHNX implements PolicyGenerator {

    @Override
    public String generate(CmdDTO cmdDTO) {
      log.info("山石插件开始回滚---");
        GeneratedObjectDTO generatedObject = cmdDTO.getGeneratedObject();
        String rollbackShowCmd = generatedObject.getRollbackShowCmd();
        Integer ipType = generatedObject.getIpType();
        if (StringUtils.isBlank(rollbackShowCmd)) {
            return DO_NOT_SUPPORT;
        }else{
            StringBuffer stringBuffer = new StringBuffer().append("define\n");
            //            ipv6
            if(ipType!=null&&ipType==1){
                stringBuffer.append("firewall6\n");
            }else {
                stringBuffer.append("firewall\n");
            }
            stringBuffer.append(CommonConstants.POLICY_SHOW_TOP_SEC) .append(" ")
                    .append(rollbackShowCmd).append("\n");
            stringBuffer.append("policy delete id ").append(CommonConstants.POLICY_ID).append("\n");
            stringBuffer.append("end\n");
            return stringBuffer.toString();
        }

    }
}
