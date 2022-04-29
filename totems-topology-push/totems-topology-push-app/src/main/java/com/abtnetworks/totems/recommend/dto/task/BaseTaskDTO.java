package com.abtnetworks.totems.recommend.dto.task;

import com.abtnetworks.totems.common.constants.PolicyConstants;

/**
 * @Author: wenjiachang
 * @Date: 2018/12/19 9:34
 */
public abstract class BaseTaskDTO {

    /**
     * 是否单步进行
     */
    boolean automatic = true;

    /**
     * 当前进行步骤
     */
    int current = PolicyConstants.POLICY_INT_TASK_NO_TASK;

    public boolean isAutomatic() {
        return automatic;
    }

    public void setAutomatic(boolean automatic) {
        this.automatic = automatic;
    }

    public int getCurrent() {
        return current;
    }

    public void setCurrent(int current) {
        this.current = current;
    }
}
