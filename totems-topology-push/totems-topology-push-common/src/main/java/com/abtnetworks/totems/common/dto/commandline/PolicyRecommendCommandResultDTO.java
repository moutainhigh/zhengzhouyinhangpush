package com.abtnetworks.totems.common.dto.commandline;

import java.util.List;

/**
 * @Author: wenjiachang
 * @Date: 2018/12/17 12:34
 */
public class PolicyRecommendCommandResultDTO {

    /**
     * 任务id
     */
    int taskId;

    /**
     * 消息
     */
    String msg;

    /**
     * 结果
     */
    String result;

    /**
     * 命令行数量
     */
    int total;

    /**
     * 生成命令行数据
     */
    List<PolicyRecommendCommandResultDataDTO> data;

    public int getTaskId() {
        return taskId;
    }

    public void setTaskId(int taskId) {
        this.taskId = taskId;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public List<PolicyRecommendCommandResultDataDTO> getData() {
        return data;
    }

    public void setData(List<PolicyRecommendCommandResultDataDTO> data) {
        this.data = data;
    }
}
