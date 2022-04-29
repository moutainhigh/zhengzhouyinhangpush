package com.abtnetworks.totems.disposal.entity;

import com.abtnetworks.totems.disposal.BaseEntity;
import java.io.Serializable;
import java.util.Date;

/**
 * @Author hw
 * @Description
 * @Date 10:14 2019/11/12
 */
public class DisposalHandleEntity extends BaseEntity {

    private static final long serialVersionUID = -3029722841831521688L;

    /**
     * 主键id，自增
     */
    private Long id;

    /**
     * 工单uuid
     */
    private String centerUuid;

    /**
     * 创建人员、派发人员
     */
    private String createUser;

    /**
     * 创建时间、派发时间
     */
    private Date createTime;

    /**
     * 处置单状态
     */
    private Integer status;

    /**
     * 是否需要审核
     */
    private Boolean needAuditFlag;

    /**
     * 审核人员
     */
    private String auditUser;

    /**
     * 是否回滚标记
     */
    private Boolean callbackFlag;

    /**
     * 审核时间
     */
    private Date auditTime;


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

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Boolean getNeedAuditFlag() {
        return needAuditFlag;
    }

    public void setNeedAuditFlag(Boolean needAuditFlag) {
        this.needAuditFlag = needAuditFlag;
    }

    public String getAuditUser() {
        return auditUser;
    }

    public void setAuditUser(String auditUser) {
        this.auditUser = auditUser;
    }

    public Boolean getCallbackFlag() {
        return callbackFlag;
    }

    public void setCallbackFlag(Boolean callbackFlag) {
        this.callbackFlag = callbackFlag;
    }

    public Date getAuditTime() {
        return auditTime;
    }

    public void setAuditTime(Date auditTime) {
        this.auditTime = auditTime;
    }

}
