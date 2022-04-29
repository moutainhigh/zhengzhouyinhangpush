package com.abtnetworks.totems.disposal.request;

import java.util.List;

/**
 * @Author hw
 * @Description
 * @Date 10:23 2020/1/8
 */
public class RollbackSaveQueryRequest {

    /**
     * 父类工单UUID
     */
    private List<String> pCenterUuidList;

    public List<String> getpCenterUuidList() {
        return pCenterUuidList;
    }

    public void setpCenterUuidList(List<String> pCenterUuidList) {
        this.pCenterUuidList = pCenterUuidList;
    }
}
