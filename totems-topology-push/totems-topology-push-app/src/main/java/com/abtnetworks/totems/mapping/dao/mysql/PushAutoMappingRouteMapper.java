package com.abtnetworks.totems.mapping.dao.mysql;

import com.abtnetworks.totems.mapping.entity.PushAutoMappingRouteEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Mapper
@Repository
public interface PushAutoMappingRouteMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(PushAutoMappingRouteEntity record);

    int insertSelective(PushAutoMappingRouteEntity record);

    PushAutoMappingRouteEntity selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(PushAutoMappingRouteEntity record);

    int updateByPrimaryKey(PushAutoMappingRouteEntity record);

    List<PushAutoMappingRouteEntity> selectByEntity(PushAutoMappingRouteEntity record);

    int deleteIdList(@Param("idList") List<Integer> idList);
}