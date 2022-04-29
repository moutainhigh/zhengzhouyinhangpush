package com.abtnetworks.totems.recommend.dao.mysql;

import com.abtnetworks.totems.recommend.entity.PolicyRiskEntity;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Mapper
@Repository
public interface PolicyRiskMapper {
    /**
     * 根据路径信息id删除风险分析数据
     * @param pathInfoId 路径信息id
     * @return
     */
    int deleteByPathInfoId(Integer pathInfoId);

    /**
     * 根据任务idList删除策略检查结果
     * @param cond
     * @return
     */
    int deleteByTaskList(Map<String, Object> cond);

    /**
     * 掺入风险分析数据
     * @param record 风险分析数据
     * @return
     */
    int insert(PolicyRiskEntity record);

    /**
     * 根据路径信息id获取风险结果列表
     * @param pathInfoId 路径信息id
     * @return
     */
    List<PolicyRiskEntity> selectByPathInfoId(Integer pathInfoId);
}