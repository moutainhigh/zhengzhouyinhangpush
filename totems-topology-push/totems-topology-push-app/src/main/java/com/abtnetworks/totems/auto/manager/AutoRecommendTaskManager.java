package com.abtnetworks.totems.auto.manager;

import com.abtnetworks.totems.auto.entity.AutoRecommendTaskEntity;

import java.util.List;

/**
 * @desc    自动开通任务相关
 * @author liuchanghao
 * @date 2021-06-11 13:59
 */
public interface AutoRecommendTaskManager {

    /**
     * 添加工单任务
     * @param record
     * @return
     */
    int insert(AutoRecommendTaskEntity record);

    /**
     * 根据uuid获取自动开通工单任务
     * @param uuid
     * @return
     */
    AutoRecommendTaskEntity getByUuid(String uuid);

    /**
     * 添加自动开通工单任务
     * @param id
     * @return
     * @throws Exception
     */
    int delete(Integer id) throws Exception;

    /**
     * 查询防护网段列表
     * @param record
     * @return
     */
    List<AutoRecommendTaskEntity> findList(AutoRecommendTaskEntity record);

}
