package com.abtnetworks.totems.disposal;

import java.io.Serializable;

/**
 * @Author hw
 * @Description
 * @Date 16:51 2018/11/20
 */
public class PublicParameter implements Serializable {

    /**
     * 开始时间戳
     */
    private Long startTime;
    /**
     * 结束时间戳
     */
    private Long endTime;

    /**
     * 页数
     */
    private Integer page = 1;

    /**
     * 每页条数
     */
    private Integer limit = 20;

    public Long getStartTime() {
        return startTime;
    }

    public void setStartTime(Long startTime) {
        this.startTime = startTime;
    }

    public Long getEndTime() {
        return endTime;
    }

    public void setEndTime(Long endTime) {
        this.endTime = endTime;
    }

    public Integer getPage() {
        return page;
    }

    public void setPage(Integer page) {
        this.page = page;
    }

    public Integer getLimit() {
        return limit;
    }

    public void setLimit(Integer limit) {
        this.limit = limit;
    }
}
