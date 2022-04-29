package com.abtnetworks.totems.recommend.service;

import com.abtnetworks.totems.recommend.dto.task.PathInfoTaskDTO;
import com.abtnetworks.totems.recommend.dto.task.RecommendTaskDTO;

public interface RiskService {

    /**
     * 检查策略风险
     * @param task
     * @return
     */
    int checkPolicyRecommendRiskByPathInfo(PathInfoTaskDTO task);
}
