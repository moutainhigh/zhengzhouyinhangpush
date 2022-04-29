package com.abtnetworks.totems.recommend.dao.mysql;

import com.abtnetworks.totems.recommend.entity.RecommendPolicyEntity;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Mapper
@Repository
public interface MergedPolicyMapper {
    /**
     * 根据任务id删除策略建议数据
     * @param taskId
     * @return
     */
    int deleteByTaskId(Integer taskId);

    /**
     * 根据任务idList删除策略检查结果
     * @param cond
     * @return
     */
    int deleteByTaskList(Map<String, Object> cond);

    /**
     * 插入策略建议数据
     * @param record 策略建议数据
     * @return
     */
    int insert(RecommendPolicyEntity record);

    /**
     * 根据路径信息id获取策略建议数据列表
     * @param taskId 任务id
     * @return
     */
    List<RecommendPolicyEntity> selectByTaskId(Integer taskId);

//selectByTaskId

    /**
     * 插入策略建议数据列表
     * @param list 策略建议数据列表
     * @return
     */
    int insertRecommendPolicyList(List<RecommendPolicyEntity> list);
}