package com.abtnetworks.totems.auto.service;

import com.abtnetworks.totems.auto.entity.AutoRecommendTaskEntity;
import com.abtnetworks.totems.recommend.entity.CommandTaskEditableEntity;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @desc 自动开通下发邮件告警相关接口
 * @author zhoumuhua
 * @date 2021-07-06
 */
public interface PushAutoTaskEmailService {

    /**
     * 自动开通下发邮件告警
     * @param taskEditableEntityList
     * @param autoTaskEntity
     */
    void startAutoRecommendPushEmail(List<CommandTaskEditableEntity> taskEditableEntityList, AutoRecommendTaskEntity autoTaskEntity);

    /**
     * 即将过期策略邮件告警
     * @param autoTaskEntity
     */
    void startWillExpirePolicyEmail(AutoRecommendTaskEntity autoTaskEntity);

    /**
     * 自动开通下发邮件告警-相似工单发送一封邮件 工单号分组 -前面的相同的为一组
     * @param taskMap
     * @param entityList
     */
    void startAutoRecommendPushBatchTaskEmail(Map<String, List<CommandTaskEditableEntity>> taskMap, List<AutoRecommendTaskEntity> entityList);

    /**
     * 根据命令行对象获取策略名称
     * @param commandlime
     * @param modelNumber
     * @return
     */
    Set<String> getPolicyName(String commandlime, String modelNumber);
}
