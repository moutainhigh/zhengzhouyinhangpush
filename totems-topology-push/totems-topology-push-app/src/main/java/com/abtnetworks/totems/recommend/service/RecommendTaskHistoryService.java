package com.abtnetworks.totems.recommend.service;

import com.abtnetworks.totems.disposal.ReturnT;
import com.abtnetworks.totems.push.entity.PushForbidIpEntity;
import com.abtnetworks.totems.recommend.entity.PushRecommendTaskHistoryEntity;
import com.github.pagehelper.PageInfo;
import org.springframework.stereotype.Service;

/**
 * @desc    仿真导入历史接口
 * @author liuchanghao
 * @date 2020-09-27 17:32
 */
@Service
public interface RecommendTaskHistoryService {

    /**
     * 分页查询
     * @param entity
     * @param pageNum
     * @param pageSize
     * @return
     */
    PageInfo<PushRecommendTaskHistoryEntity> findList(PushRecommendTaskHistoryEntity entity, int pageNum, int pageSize);


}
