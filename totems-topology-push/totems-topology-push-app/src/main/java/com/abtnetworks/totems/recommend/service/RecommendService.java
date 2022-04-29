package com.abtnetworks.totems.recommend.service;

import com.abtnetworks.totems.push.dto.policy.PolicyInfoDTO;
import com.abtnetworks.totems.recommend.dto.task.PathInfoTaskDTO;
import com.abtnetworks.totems.recommend.dto.task.SimulationTaskDTO;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 策略开通服务
 */
@Component
public interface RecommendService {

    /**
     * 策略生成
     * @param task 策略仿真任务
     * @param simulationTaskDTO
     * @return 策略生成结果
     */
    int recommendPolicyByPathInfo(PathInfoTaskDTO task, SimulationTaskDTO simulationTaskDTO);

    /**
     * 合并
     * @param policyInfoList
     * @return
     */
    List<PolicyInfoDTO> mergePolicyInfoList(List<PolicyInfoDTO> policyInfoList);

}
