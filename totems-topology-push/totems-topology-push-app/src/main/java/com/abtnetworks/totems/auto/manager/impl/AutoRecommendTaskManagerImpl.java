package com.abtnetworks.totems.auto.manager.impl;

import com.abtnetworks.totems.auto.dao.mysql.AutoRecommendTaskMapper;
import com.abtnetworks.totems.auto.entity.AutoRecommendTaskEntity;
import com.abtnetworks.totems.auto.manager.AutoRecommendTaskManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @desc    自动开通任务相关
 * @author liuchanghao
 * @date 2021-06-11 14:09
 */
@Service
public class AutoRecommendTaskManagerImpl implements AutoRecommendTaskManager {

    private static Logger logger = LoggerFactory.getLogger(AutoRecommendTaskManagerImpl.class);


    @Autowired
    private AutoRecommendTaskMapper autoRecommendTaskMapper;


    @Override
    public int insert(AutoRecommendTaskEntity record) {
        return autoRecommendTaskMapper.insert(record);
    }

    @Override
    public AutoRecommendTaskEntity getByUuid(String uuid) {
        return autoRecommendTaskMapper.getByUuid(uuid);
    }

    @Override
    public int delete(Integer id) throws Exception {
        return autoRecommendTaskMapper.deleteByPrimaryKey(id);
    }

    @Override
    public List<AutoRecommendTaskEntity> findList(AutoRecommendTaskEntity record) {
        return autoRecommendTaskMapper.findList(record);
    }

}
