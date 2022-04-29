package com.abtnetworks.totems.mapping.dao.mysql;

import com.abtnetworks.totems.mapping.entity.PushAutoMappingIpEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Mapper
@Repository
public interface PushAutoMappingIpMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(PushAutoMappingIpEntity record);

    int insertSelective(PushAutoMappingIpEntity record);

    PushAutoMappingIpEntity selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(PushAutoMappingIpEntity record);

    int updateByPrimaryKey(PushAutoMappingIpEntity record);

    List<PushAutoMappingIpEntity> selectByEntity(PushAutoMappingIpEntity record);


    List<PushAutoMappingIpEntity> findIpMappingOneToOneByIp(PushAutoMappingIpEntity record);

    List<PushAutoMappingIpEntity> findIpMappingByDeviceUuid(PushAutoMappingIpEntity record);

    List<PushAutoMappingIpEntity> findIpMappingByEntity(PushAutoMappingIpEntity record);

    int batchInsert(@Param("pushAutoMappingIpEntitys") List<PushAutoMappingIpEntity> pushAutoMappingIpEntitys);

    int deleteIdList(@Param("idList") List<Integer> idList);
}