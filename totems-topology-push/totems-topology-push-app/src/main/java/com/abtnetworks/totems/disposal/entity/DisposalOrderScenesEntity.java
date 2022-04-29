package com.abtnetworks.totems.disposal.entity;

import com.abtnetworks.totems.disposal.BaseEntity;
import java.io.Serializable;

/**
 * @Author hw
 * @Description
 * @Date 10:34 2019/11/12
 */
public class DisposalOrderScenesEntity extends BaseEntity {

    private static final long serialVersionUID = 2460385577255517977L;

    /**
     * 主键id，自增
     */
    private Long id;

    /**
     * 工单uuid
     */
    private String centerUuid;

    /**
     * 场景uuid
     */
    private String scenesUuid;


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCenterUuid() {
        return centerUuid;
    }

    public void setCenterUuid(String centerUuid) {
        this.centerUuid = centerUuid;
    }

    public String getScenesUuid() {
        return scenesUuid;
    }

    public void setScenesUuid(String scenesUuid) {
        this.scenesUuid = scenesUuid;
    }

}
