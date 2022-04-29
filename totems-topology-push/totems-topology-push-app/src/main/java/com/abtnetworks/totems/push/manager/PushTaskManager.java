package com.abtnetworks.totems.push.manager;

import com.abtnetworks.totems.credential.entity.CredentialEntity;
import com.abtnetworks.totems.push.entity.PushTaskEntity;
import com.abtnetworks.totems.push.vo.PushTaskPageVO;
import com.abtnetworks.totems.push.vo.PushTaskVO;
import com.github.pagehelper.PageInfo;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @Author: wenjiachang
 * @Date: 2019/3/5 12:05
 */
public interface PushTaskManager {
    /**
     * 获取策略下发任务列表分页数据
     * @param orderNo 工单号
     * @param page 页数
     * @param psize 页面数据大小
     * @return 页面数据
     */
    PushTaskPageVO getPushTaskList(String orderNo, String type, String status, int page, int psize);

    /**
     * 根据任务id获取策略下发任务数据
     * @param taskId 策略下发任务Id
     * @return 策略下发任务数据
     */
    PushTaskEntity getPushTaskById(int taskId);

    /**
     * 添加策略下发任务
     * @param entity
     */
    void addPushTask(PushTaskEntity entity);

    /**
     * 根据工单号获取策略下发任务列表
     * @param orderNo
     * @return
     */
    List<PushTaskEntity> getPolicyRecommendTaskListByOrderNo(String orderNo);

    /**
     * 更新策略下发结果
     * @param id 策略下发任务id
     * @param pushResult 下发结果
     */
    void updatePushResult(int id, int status, String pushResult);

    /**
     * 更新策略下发任务状态
     * @param id 策略下发任务id
     * @param status 任务状态：0，未下发；1，下发完成
     */
    void updatePushStatus(int id, int status);

    /**
     * 删除策略下发任务
     * @param id 策略下发任务id
     */
    void deletePushTask(int id);

    /**
     * 获取设备加密信息
     * @param uuid 设备UUID
     * @return 设备加密信息
     */
    CredentialEntity getCredentialEntity(String uuid);

    /**
     * 获取设备型号
     * @param uuid 设备uuid
     * @return 设备型号字符串
     */
    String getDeviceModelNumber(String uuid);

    /**
     * 通过设备UUID获取凭证UUID
     * @param uuid 设备UUID
     * @return 凭证UUID
     */
    String getCredentialUuid(String uuid);

    /**
     * 更新策略下发时间
     * @param entity
     */
    void updatePushTime(PushTaskEntity entity);
}
