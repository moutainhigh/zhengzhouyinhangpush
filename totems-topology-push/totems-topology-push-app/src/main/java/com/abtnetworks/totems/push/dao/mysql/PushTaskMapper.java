package com.abtnetworks.totems.push.dao.mysql;

import com.abtnetworks.totems.push.entity.PushTaskEntity;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

/**
 * @Author: wenjiachang
 * @Date: 2019/3/4 14:53
 */
@Mapper
@Repository
public interface PushTaskMapper {

    /**
     * 获取策略下发任务列表
     * @return 任务列表
     */
    List<PushTaskEntity> getPushTaskList();

    /**
     * 根据push任务id获取push任务数据对象
     * @param taskId 任务id
     * @return 数据对象
     */
    PushTaskEntity getPushTaskById(int taskId);

    /**
     * 根据工单号获取策略下发任务列表
     * @param orderNo 工单号
     * @return 任务列表
     */
    List<PushTaskEntity> getPushTaskListByOrderNo(String orderNo);

    /**
     * 根据工单号搜索策略下发任务列表
     * @param params 查询参数
     * @return 任务列表
     */
    List<PushTaskEntity> searchPushTaskListByOrderNo(Map<String, String> params);

    /**
     * 根据任务状态获取策略下发任务列表
     * @param status 任务状态
     * @return 任务列表
     */
    List<PushTaskEntity> getPushTaskListByStatus(int status);

    /**
     * 根据任务状态和任务类型获取策略下发任务列表
     * @param params 任务状态和类型参数
     * @return 任务列表
     */
    List<PushTaskEntity> getPushTaskListByOrderTypeAndStatus(Map<String, String> params);

    /**
     * 更新下发时间
     * @param params 参数列表
     */
    void updatePushTime(Map<String, String> params);

    /**
     * 更新下发结果
     * @param params 参数列表
     */
    void updatePushResult(Map<String, String> params);

    /**
     * 更新下发状态
     * @param params 参数列表
     */
    void updatePushStatus(Map<String, String> params);

    /**
     * 添加策略下发任务
     * @param entity 策略下发任务对象
     */
    void addPushTask(PushTaskEntity entity);

    /**
     * 删除策略下发任务
     * @param id 任务id
     */
    void deletePushTask(int id);

    /**
     * 更新下发时间
     * @param entity 任务对象
     */
    void updatePushTime(PushTaskEntity entity);
}
