package com.abtnetworks.totems.disposal.request;

import java.util.List;

/**
 * @Author hw
 * @Description
 * @Date 18:06 2020/1/7
 */
public class OrderSendCommandQueryRequest {

    /**
     * 工单内容UUID 集合
     */
    private List<String> centerUuidList;

    public List<String> getCenterUuidList() {
        return centerUuidList;
    }

    public void setCenterUuidList(List<String> centerUuidList) {
        this.centerUuidList = centerUuidList;
    }
}
