package com.abtnetworks.totems.recommend.service;

import com.abtnetworks.totems.recommend.dto.task.*;
import org.springframework.stereotype.Component;

/**
 * @Author: wenjiachang
 * @Date: 2019/3/13 17:13
 */
@Component
public interface AnalyzeService {


    /**
     * 路径分析
     * @param task 策略开通任务
     * @param simulationTaskDTO
     * @return 路径分析结果结果
     */
    int analyzePathByPathInfo(PathInfoTaskDTO task, SimulationTaskDTO simulationTaskDTO);
}
