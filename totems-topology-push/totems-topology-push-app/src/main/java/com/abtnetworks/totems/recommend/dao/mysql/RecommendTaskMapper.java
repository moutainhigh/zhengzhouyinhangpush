package com.abtnetworks.totems.recommend.dao.mysql;

import com.abtnetworks.totems.recommend.entity.RecommendTaskEntity;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Mapper
@Repository
public interface RecommendTaskMapper {
    /**
     * 根据任务id删除任务
     * @param id 任务id
     * @return
     */
    int deleteByTaskId(Integer id);

    /**
     * 根据任务idList删除策略检查结果
     * @param cond
     * @return
     */
    int deleteByTaskList(Map<String, Object> cond);

    /**
     * 插入策略开通任务数据
     * @param record
     * @return
     */
    int insert(RecommendTaskEntity record);


    /**
     * 根据任务id获取策略开通任务数据
     * @param id 任务id
     * @return 任务数据
     */
    List<RecommendTaskEntity> selectByTaskId(Integer id);



    /**
     * 更新策略开通任务数据
     * @param record 任务数据
     * @return
     */
    int updateByPrimaryKeySelective(RecommendTaskEntity record);

    int updateByTaskList(Map<String, Object> cond);

    /**
     * 更新策略开通任务数据
     * @param record 任务数据
     * @return
     */
    int updateByPrimaryKey(RecommendTaskEntity record);

    /**
     * 查找策略开通任务数据
     * @param params 查询参数
     * @return 查找结果列表
     */
    List<RecommendTaskEntity> searchTask(Map<String, Object> params);

    /**
     * 添加策略开通任务列表
     * @param list 策略开通任务列表
     * @return 添加结果
     */
    int addRecommendTaskList(List<RecommendTaskEntity> list);

    /**
     * 查找策略开通任务数据
     * @param params 查询参数
     * @return 查找结果列表
     */
    List<RecommendTaskEntity> searchRecommendTask(Map<String, Object> params);

    /**
     * 查找互联网开通任务数据
     * @param params 查询参数
     * @return 查找结果列表
     */
    List<RecommendTaskEntity> searchRecommendTaskWithServiceAny(Map<String, Object> params);

    /**
     * 查找策略开通任务数据
     * @param params 查询参数
     * @return 查找结果列表
     */
    List<RecommendTaskEntity> searchNatTask(Map<String, Object> params);

    /**
     * 查找策略开通任务数据
     * @param params 带时间查询参数
     * @return 查找结果列表
     */
    List<RecommendTaskEntity> searchPolicyTask(Map<String, Object> params);

    /**
     * 查找策略开通带时间任务数据
     * @param params 带时间查询参数
     * @return 查找结果列表
     */
    List<RecommendTaskEntity> searchNatPolicyTask(Map<String, Object> params);

    /**
     * 查找策略开通执行中任务
     * @return 查找结果列表
     */
    List<RecommendTaskEntity> selectExecuteRecommendTask();

    /**
     * 根据主键ID查询
     * @param id
     * @return
     */
    RecommendTaskEntity getById(Integer id);

    void updateWeTaskId(RecommendTaskEntity entity);
}