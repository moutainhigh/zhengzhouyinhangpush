package com.abtnetworks.totems.mapping.dao.mysql;

import com.abtnetworks.totems.mapping.entity.PushAutoMappingTaskEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Mapper
@Repository
public interface PushAutoMappingTaskMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(PushAutoMappingTaskEntity record);

    int insertSelective(PushAutoMappingTaskEntity record);

    PushAutoMappingTaskEntity selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(PushAutoMappingTaskEntity record);

    int updateByPrimaryKey(PushAutoMappingTaskEntity record);

    List<PushAutoMappingTaskEntity> selectByEntity(PushAutoMappingTaskEntity record);

    int deleteIdList(@Param("idList") List<Integer> idList);

    PushAutoMappingTaskEntity getByUuid(String uuid);
}