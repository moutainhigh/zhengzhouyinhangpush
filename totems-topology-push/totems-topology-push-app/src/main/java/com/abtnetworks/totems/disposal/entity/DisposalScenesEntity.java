package com.abtnetworks.totems.disposal.entity;

import com.abtnetworks.totems.disposal.BaseEntity;
import java.io.Serializable;
import java.util.Date;

/**
 * @Author hw
 * @Description
 * @Date 17:03 2019/11/11
 */
public class DisposalScenesEntity extends BaseEntity {

    private static final long serialVersionUID = -1938086281548176134L;

    /**
     * 主键id，自增
     */
    private Long id;

    /**
     * UUID
     */
    private String uuid;

    /**
     * 场景名称
     */
    private String name;

    /**
     * 备注
     */
    private String remarks;

    /**
     * 设备JSON串
     */
    private String deviceJson;

    /**
     * 创建人员
     */
    private String createUser;

    /**
     * 创建时间
     */
    private Date createTime;


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public String getDeviceJson() {
        return deviceJson;
    }

    public void setDeviceJson(String deviceJson) {
        this.deviceJson = deviceJson;
    }

    public String getCreateUser() {
        return createUser;
    }

    public void setCreateUser(String createUser) {
        this.createUser = createUser;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

}
