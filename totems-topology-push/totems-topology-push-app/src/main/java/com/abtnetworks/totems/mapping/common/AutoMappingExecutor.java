package com.abtnetworks.totems.mapping.common;

import com.abtnetworks.totems.mapping.dto.AutoMappingTaskResultDTO;
import com.abtnetworks.totems.mapping.dto.RuleProcessDTO;

/**
 * @desc    规则自动匹配执行器
 * @author liuchanghao
 * @date 2022-01-20 17:22
 */
public interface AutoMappingExecutor {

    /**
     * 匹配规则 —> 执行流程 —> 生成工单检测任务单数据
     * @param dto
     * @return
     */
    AutoMappingTaskResultDTO matchAndGenerateAutoMappingTask(RuleProcessDTO dto) throws Exception;
}
