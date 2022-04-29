package com.abtnetworks.totems.push.dao.mysql;

import com.abtnetworks.totems.push.entity.PushRecommendTaskExpandEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
* F5策略生成表Dao
*
* @author lifei
* @since 2021年08月02日
*/
@Repository
@Mapper
public interface PushRecommendTaskExpandMapper {
    /**
     * 查询列表
     *
     * @param entity
     * @return
     */
    List<PushRecommendTaskExpandEntity> queryList(PushRecommendTaskExpandEntity entity);

    /**
     * 根据id删除数据
     *
     * @param ids
     * @return
     */
    int delete(@Param("ids") String ids);

    /**
     * 根据taskId删除数据
     *
     * @param ids
     * @return
     */
    int deleteByTaskId(@Param("ids") String ids);

    /**
     * 插入数据
     *
     * @param entity
     */
    int add(PushRecommendTaskExpandEntity entity);

    /**
     * 更新数据
     *
     * @param entity
     * @return
     */
    int update(PushRecommendTaskExpandEntity entity);

    /**
     * 根据场景实体查询实体
     *
     * @param entity
     * @return
     */
    PushRecommendTaskExpandEntity getByEntity(PushRecommendTaskExpandEntity entity);

    /**
     * 根据场景实体查询实体
     *
     * @param taskId
     * @return
     */
    PushRecommendTaskExpandEntity getByTaskId(@Param("taskId") Integer taskId);

    /**
     * 根据id获取taskId集合
     * @param ids
     * @return
     */
    List<Integer>  getTaskIdByIds(@Param("ids") String ids);
}