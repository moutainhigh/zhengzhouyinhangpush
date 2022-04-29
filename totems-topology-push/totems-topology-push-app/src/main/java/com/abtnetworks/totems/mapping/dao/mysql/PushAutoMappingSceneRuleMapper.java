package com.abtnetworks.totems.mapping.dao.mysql;

import com.abtnetworks.totems.mapping.entity.PushAutoMappingSceneRuleEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Mapper
@Repository
public interface PushAutoMappingSceneRuleMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(PushAutoMappingSceneRuleEntity record);

    int insertSelective(PushAutoMappingSceneRuleEntity record);

    PushAutoMappingSceneRuleEntity selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(PushAutoMappingSceneRuleEntity record);

    int updateByPrimaryKey(PushAutoMappingSceneRuleEntity record);

    List<PushAutoMappingSceneRuleEntity> selectByEntity(PushAutoMappingSceneRuleEntity record);

    /**
     * 查询所有的场景规则
     * @return
     */
    List<PushAutoMappingSceneRuleEntity> selectAll();

    int deleteIdList(@Param("idList") List<Integer> idList);
}