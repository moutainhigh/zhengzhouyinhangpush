package com.abtnetworks.totems.recommend.dao.mysql;

import com.abtnetworks.totems.recommend.entity.PushRecommendTaskHistoryEntity;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @desc
 * @author liuchanghao
 * @date 2020-10-27 9:47
 */
@Repository
public interface PushRecommendTaskHistoryMapper {

    int deleteByPrimaryKey(Integer id);

    int insert(PushRecommendTaskHistoryEntity record);

    int insertSelective(PushRecommendTaskHistoryEntity record);

    PushRecommendTaskHistoryEntity selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(PushRecommendTaskHistoryEntity record);

    int updateByPrimaryKeyWithBLOBs(PushRecommendTaskHistoryEntity record);

    int updateByPrimaryKey(PushRecommendTaskHistoryEntity record);

    /**
     * 分页查询仿真历史数据
     * @param record
     * @return
     */
    List<PushRecommendTaskHistoryEntity> findList(PushRecommendTaskHistoryEntity record);
}