package com.abtnetworks.totems.recommend.dao.mysql;

import com.abtnetworks.totems.recommend.entity.RecommendTaskCheckEntity;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Mapper
@Repository
public interface RecommendTaskCheckMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(RecommendTaskCheckEntity record);

    int insertSelective(RecommendTaskCheckEntity record);

    RecommendTaskCheckEntity selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(RecommendTaskCheckEntity record);

    int updateByPrimaryKey(RecommendTaskCheckEntity record);

    List<RecommendTaskCheckEntity> selectByStatus(Integer status);

    List<RecommendTaskCheckEntity> selectByOrder(String orderNumber);

    List<RecommendTaskCheckEntity> selectByStatusAndBatchType(Map<String, String> params);

    List<RecommendTaskCheckEntity> searchTask(Map<String, Object> params);
}