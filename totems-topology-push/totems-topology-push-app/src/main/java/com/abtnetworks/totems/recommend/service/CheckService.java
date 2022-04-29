package com.abtnetworks.totems.recommend.service;

import com.abtnetworks.totems.recommend.dto.task.PathInfoTaskDTO;
import com.abtnetworks.totems.recommend.dto.task.RecommendTaskDTO;
import com.abtnetworks.totems.recommend.dto.task.SimulationTaskDTO;
import org.springframework.stereotype.Component;

/**
 * @Author: wenjiachang
 * @Date: 2019/3/13 17:35
 */
@Component
public interface CheckService {

    /**
     * 策略检查
     * @param task 策略仿真任务
     * @param simulationTaskDTO
     * @return 策略检查结果
     */
    int checkPolicyByPathInfo(PathInfoTaskDTO task, SimulationTaskDTO simulationTaskDTO);


    /**
     * 策略检查
     * @param simulationTaskDTO 策略仿真任务
     * @return 策略检查结果
     */
    int checkPolicyByPolicyTask(SimulationTaskDTO simulationTaskDTO);
}
