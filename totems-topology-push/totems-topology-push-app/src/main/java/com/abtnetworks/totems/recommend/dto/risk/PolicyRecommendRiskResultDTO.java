package com.abtnetworks.totems.recommend.dto.risk;

import java.util.List;

/**
 * @Author: wenjiachang
 * @Date: 2018/12/14 15:18
 */
public class PolicyRecommendRiskResultDTO {

    /**
     * 任务id
     */
    int taskId;

    /**
     * 结果，true表示分析成功，false表示分析失败
     */
    String result;

    /**
     * 出错信息
     */
    String msg;

    /**
     * 风险ID列表
     */
    List<String> riskList;

    public int getTaskId() {
        return taskId;
    }

    public void setTaskId(int taskId) {
        this.taskId = taskId;
    }

    public List<String> getRiskList() {
        return riskList;
    }

    public void setRiskList(List<String> riskList) {
        this.riskList = riskList;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }
}
