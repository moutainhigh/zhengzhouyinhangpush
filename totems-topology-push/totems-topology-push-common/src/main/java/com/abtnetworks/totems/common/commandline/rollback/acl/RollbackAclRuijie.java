package com.abtnetworks.totems.common.commandline.rollback.acl;

import com.abtnetworks.totems.common.commandline.PolicyGenerator;
import com.abtnetworks.totems.common.dto.*;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * @author lifei
 * @desc 锐捷acl策略回滚命令行
 * @date 2021/9/1 19:52
 */
@Service
@Log4j2
public class RollbackAclRuijie implements PolicyGenerator {

    @Override
    public String generate(CmdDTO cmdDTO) {

        SettingDTO settingDTO = cmdDTO.getSetting();
        DeviceDTO deviceDTO = cmdDTO.getDevice();
        TaskDTO task = cmdDTO.getTask();
        List<Integer> actualUserableRuleId = settingDTO.getUsableRuleList();

        // 截取
        StringBuffer sb = new StringBuffer();
        sb.append("configure terminal\n\n");
        // 如果策略集名称为空 则就是新建策略集 新建的策略集名称为工单的名称。
        if (StringUtils.isBlank(deviceDTO.getRuleListName())) {
            sb.append(String.format("ip access-list extended %s", task.getTheme()));
        } else {
            sb.append(String.format("ip access-list extended %s", deviceDTO.getRuleListName()));
        }
        sb.append("\n");

        // 针对于ruleId不够的情况，也去生成不带ruleId的回滚命令行
        if (CollectionUtils.isEmpty(actualUserableRuleId)) {
            log.info("工单:{}经过设备:[{}]需要生成的策略ruleId不够,生成默认回滚命令行", task.getTaskId(), deviceDTO.getDeviceUuid());
            CommandLineBusinessInfoDTO commandLineBusinessInfoDTO = cmdDTO.getBusinessInfoDTO();
            Integer policyNums = commandLineBusinessInfoDTO.getPolicyNums();
            for (int i = 0; i < policyNums; i++) {
                sb.append("no ");
                sb.append("\n");
            }
        } else {
            for (Integer ruleId : actualUserableRuleId) {
                sb.append(String.format("no %d", ruleId));
                sb.append("\n");
            }
        }
        sb.append("exit\n\n");
        sb.append("end\n");
        sb.append("write\n");
        return sb.toString();
    }
}
