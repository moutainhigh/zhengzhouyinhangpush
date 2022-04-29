package com.abtnetworks.totems.recommend.dao.mysql;

import com.abtnetworks.totems.recommend.entity.RecommendRelevanceSceneEntity;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

/**
* 仿真关联场景实体Dao
*
* @author Administrator
* @since 2021年12月24日
*/
@Repository
public interface RecommendRelevanceSceneMapper {

    /**
     * 插入关联场景数据
     * @param entity 路径信息数据
     * @return
     */
    int add(RecommendRelevanceSceneEntity entity);

    /**
     * 更新关联场景数据
     * @param entity
     * @return
     */
    int update(RecommendRelevanceSceneEntity entity);

    /**
     * 根据任务id获取关联场景数据
     * @param params 参数map
     * @return
     */
    List<RecommendRelevanceSceneEntity> selectScene(Map<String, Object> params);

    /**
     * 查询列表
     * 
     * @param entity
     * @return
     */
    List<RecommendRelevanceSceneEntity> queryList(RecommendRelevanceSceneEntity entity);

    /**
     * 根据id查询关联关系
     * @param id
     * @return
     */
    RecommendRelevanceSceneEntity selectSceneById(@Param("id") Integer id);

    /**
     * 根据id删除数据
     *
     * @param ids
     * @return
     */
    int deleteSceneById(@Param("ids") String ids);

    /**
     * 根据ids查询关联场景
     * @param ids
     * @return
     */
    List<RecommendRelevanceSceneEntity> selectSceneByIds(@Param("ids") String ids);
}