package com.abtnetworks.totems.recommend.service;

public interface ExceptionService {

    /**
     * 处理任务异常
     * @param taskId 任务id
     * @param statusType 状态类型：是那种状态中执行出现异常
     */
    void handleException(int taskId, int statusType);

    void handleException(int taskId);
}
