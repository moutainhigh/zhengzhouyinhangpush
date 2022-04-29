package com.abtnetworks.totems.recommend.dao.mysql;

import com.abtnetworks.totems.recommend.entity.DeviceDimension;
import com.abtnetworks.totems.recommend.entity.RecommendPolicyEntity;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Mapper
@Repository
public interface RecommendPolicyMapper {
    /**
     * 根据路径信息id删除策略建议数据
     * @param pathInfoId
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
     * 插入策略建议数据
     * @param record 策略建议数据
     * @return
     */
    int insert(RecommendPolicyEntity record);

    /**
     * 根据路径信息id获取策略建议数据列表
     * @param pathInfoId 路径信息id
     * @return
     */
    List<RecommendPolicyEntity> selectByPathInfoId(Integer pathInfoId);

    /**
     * 根据任务选取所有生成策略
     * @param taskId
     * @return
     */
    List<RecommendPolicyEntity> selectByTaskId(Integer taskId);

    /**
     * 更新策略信息数据
     * @param record 策略信息数据
     * @return
     */
    int updateMergeRuleName(RecommendPolicyEntity record);

    /**
     * 插入策略建议数据列表
     * @param entityList 策略建议数据列表
     * @return
     */
    int insertRecommendPolicyList(List<RecommendPolicyEntity> entityList);

    /**
     * 根据策略id获取策略数据
     * @param id
     * @return
     */
    List<RecommendPolicyEntity> selectById(Integer id);


    List<DeviceDimension> selectDeviceDimensionByTaskId(Integer taskId);

    List<RecommendPolicyEntity> selectByDeviceDimension(Map<String, String> params);
}