package com.abtnetworks.totems.common.dto.commandline;

import java.util.List;

/**
 * @Author: wenjiachang
 * @Date: 2018/12/17 12:29
 */
public class PolicyRecommendCommandDTO {
    /**
     * 任务id
     */
    int taskId;

    /**
     * 任务名称
     */
    String taskName;

    /**
     * 任务描述
     */
    String taskDesc;

    /**
     * 策略列表
     */
    List<PolicyRecommendCommandPolicyDTO> policyList;

    public int getTaskId() {
        return taskId;
    }

    public void setTaskId(int taskId) {
        this.taskId = taskId;
    }

    public String getTaskName() {
        return taskName;
    }

    public void setTaskName(String taskName) {
        this.taskName = taskName;
    }

    public String getTaskDesc() {
        return taskDesc;
    }

    public void setTaskDesc(String taskDesc) {
        this.taskDesc = taskDesc;
    }

    public List<PolicyRecommendCommandPolicyDTO> getPolicyList() {
        return policyList;
    }

    public void setPolicyList(List<PolicyRecommendCommandPolicyDTO> policyList) {
        this.policyList = policyList;
    }
}
