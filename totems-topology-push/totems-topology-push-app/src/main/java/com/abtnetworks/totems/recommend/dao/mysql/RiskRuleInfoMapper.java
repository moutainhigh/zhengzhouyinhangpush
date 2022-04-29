package com.abtnetworks.totems.recommend.dao.mysql;

import com.abtnetworks.totems.recommend.entity.RiskRuleDetailEntity;
import com.abtnetworks.totems.recommend.entity.RiskRuleInfoEntity;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

import java.util.List;


@Mapper
@Repository
public interface RiskRuleInfoMapper {
    /**
     * 获取策略风险规则信息
     * @param ruleId
     * @return
     */
    List<RiskRuleInfoEntity> getRiskInfoByRuleId(String ruleId);

    /**
     * 根据二级分类ID获取风险信息列表
     * @param secondSortId 二级分类ID
     * @return 风险信息列表
     */
    List<RiskRuleInfoEntity> getRiskInfoBySecondSortId(int secondSortId);

    /**
     * 获取风险分析策略详情
     * @param ruleId 策略id
     * @return 策略详情
     */
    List<RiskRuleDetailEntity> getRiskDetailByRuleId(String ruleId);
}
