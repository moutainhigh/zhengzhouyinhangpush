package com.abtnetworks.totems.mapping.rule.snatmany2one;

import com.abtnetworks.totems.mapping.common.AutoMappingExecutor;
import com.abtnetworks.totems.mapping.common.CommonExecutor;
import com.abtnetworks.totems.mapping.dto.AutoMappingTaskResultDTO;
import com.abtnetworks.totems.mapping.dto.CommonExecuteResultDTO;
import com.abtnetworks.totems.mapping.dto.RuleProcessDTO;
import org.springframework.stereotype.Service;

/**
 * @desc    Snat 多对一V2版本，当前不实现，仅做示例
 * @author liuchanghao
 * @date 2022-01-20 17:06
 */
@Service
public class SnatManyToOneV2 extends CommonExecutor implements AutoMappingExecutor {
    @Override
    public AutoMappingTaskResultDTO matchAndGenerateAutoMappingTask(RuleProcessDTO dto) {
        return null;
    }

    @Override
    public boolean validateOrderAndAddressPool(RuleProcessDTO dto, CommonExecuteResultDTO commonExecuteResultDTO) {
        return false;
    }

    @Override
    public String  getNextAvailableIp(RuleProcessDTO dto, CommonExecuteResultDTO commonExecuteResultDTO) {
        return null;
    }

    @Override
    public void insertIntoIpMatchTable(RuleProcessDTO dto, CommonExecuteResultDTO commonExecuteResultDTO) {
        return ;
    }

    @Override
    public void mergeAndGenerateNatScene(RuleProcessDTO dto, CommonExecuteResultDTO commonExecuteResultDTO) {
        return ;
    }
}
