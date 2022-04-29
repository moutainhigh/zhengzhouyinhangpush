package com.abtnetworks.totems.translation.dao;

import com.abtnetworks.totems.disposal.ReturnT;
import com.abtnetworks.totems.translation.entity.TranslationTaskMappingEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @Description 策略迁移信息表
 * @Version --
 * @Created by hw on '2021-01-12 10:38:35'.
 */
@Mapper
@Repository
public interface TranslationTaskMappingMapper {

    /**
     * 新增
     */
    public int insert(TranslationTaskMappingEntity entity);

    /**
     * 删除
     */
    public int delete(@Param("id") int id);

    /**
     * get查询 By Id
     */
    public TranslationTaskMappingEntity getById(@Param("id") int id);

    /**
     * 查询Count
     */
    public int count();

    int deleteByTaskId(String taskUuid);

    void insertList(List<TranslationTaskMappingEntity> mappingVOList);

    List<TranslationTaskMappingEntity> findVOListByTaskUuid(String taskUuid);
}
