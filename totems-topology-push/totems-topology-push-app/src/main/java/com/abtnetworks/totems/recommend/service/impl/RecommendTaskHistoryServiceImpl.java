package com.abtnetworks.totems.recommend.service.impl;

import com.abtnetworks.totems.recommend.dao.mysql.PushRecommendTaskHistoryMapper;
import com.abtnetworks.totems.recommend.entity.PushRecommendTaskHistoryEntity;
import com.abtnetworks.totems.recommend.service.RecommendTaskHistoryService;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @desc
 * @author liuchanghao
 * @date 2020-09-27 18:52
 */
@Slf4j
@Service
public class RecommendTaskHistoryServiceImpl implements RecommendTaskHistoryService {

    private static Logger logger = LoggerFactory.getLogger(RecommendTaskHistoryServiceImpl.class);

    @Autowired
    private PushRecommendTaskHistoryMapper pushRecommendTaskHistoryMapper;

    @Override
    public PageInfo<PushRecommendTaskHistoryEntity> findList(PushRecommendTaskHistoryEntity entity, int pageNum, int pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        List<PushRecommendTaskHistoryEntity> list = pushRecommendTaskHistoryMapper.findList(entity);
        PageInfo<PushRecommendTaskHistoryEntity> pageInfo = new PageInfo<>(list);
        return pageInfo;
    }


}
