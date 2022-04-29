package com.abtnetworks.totems.recommend.service;

import com.abtnetworks.totems.recommend.entity.RecommendTaskEntity;

import java.util.List;

public interface VerifyService {

    int verifyTask(List<RecommendTaskEntity> list);

    /**
     * 获取采集失败的策略
     * @param deviceUuidList
     * @return
     */
    List<String> getGatherFailedDevices(List<String> deviceUuidList);

    /**
     * 是否存在采集中的设备
     * @param deviceUuidList
     * @return
     */
    boolean hasGatheringDevices(List<String> deviceUuidList);
}
