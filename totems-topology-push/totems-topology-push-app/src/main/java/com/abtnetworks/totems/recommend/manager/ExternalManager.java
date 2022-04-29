package com.abtnetworks.totems.recommend.manager;

/**
 * @Author: wenjiachang
 * @Date: 2019/3/15 11:48
 */

import com.abtnetworks.totems.recommend.dto.risk.PolicyRecommendRiskDTO;
import com.abtnetworks.totems.recommend.dto.risk.PolicyRecommendRiskResultDTO;
import com.abtnetworks.totems.whale.policy.dto.PathAnalyzeDTO;

/**
 * 调用外部模块服务
 */
public interface ExternalManager {

    /**
     * 进行设备采集
     * @param gatherId 设备采集id
     * @return 采集开始结果
     */
    int doGather(String gatherId);

    /**
     * 从Layer模块获取路径详细数据供前端显示
     * @param pathAnalyzeDTO 路径数据信息
     * @return 路径详细数据字符串, 若获取失败，则返回为空
     */
    String getDetailPath(PathAnalyzeDTO pathAnalyzeDTO) throws Exception;

    /**
     * 进行风险分析
     * @param checkRiskDTO
     * @return
     */
    PolicyRecommendRiskResultDTO checkRisk(PolicyRecommendRiskDTO checkRiskDTO);
}
