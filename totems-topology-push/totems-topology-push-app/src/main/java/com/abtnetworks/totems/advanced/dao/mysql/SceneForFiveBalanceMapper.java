package com.abtnetworks.totems.advanced.dao.mysql;

import com.abtnetworks.totems.advanced.entity.SceneForFiveBalanceEntity;
import org.apache.ibatis.annotations.Param;
import org.mapstruct.Mapper;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 策略生成F5负载场景实体类Dao
 *
 * @author lifei
 * @since 2021年07月30日
 */
@Mapper
@Repository
public interface SceneForFiveBalanceMapper {
    /**
     * 查询列表
     *
     * @param entity
     * @return
     */
    List<SceneForFiveBalanceEntity> queryList(SceneForFiveBalanceEntity entity);

    /**
     * 根据id删除数据
     *
     * @param ids
     * @return
     */
    int delete(@Param("ids") String ids);

    /**
     * 插入数据
     *
     * @param entity
     */
    int add(SceneForFiveBalanceEntity entity);

    /**
     * 更新数据
     *
     * @param entity
     * @return
     */
    int update(SceneForFiveBalanceEntity entity);

    /**
     * 根据场景实体查询实体
     *
     * @param entity
     * @return
     */
    SceneForFiveBalanceEntity getByEntity(SceneForFiveBalanceEntity entity);
}