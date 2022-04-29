package com.abtnetworks.totems.disposal.entity;

import com.abtnetworks.totems.disposal.BaseEntity;
import java.io.Serializable;
import java.util.Date;

/**
 * @Author hw
 * @Description
 * @Date 17:18 2019/11/15
 */
public class DisposalRollbackEntity extends BaseEntity {

    private static final long serialVersionUID = 587152724974170553L;

    /**
     * 主键id，自增
     */
    private Long id;

    /**
     * 父类工单UUID
     */
    private String pCenterUuid;

    /**
     * 工单内容UUID
     */
    private String centerUuid;

    /**
     * 备注
     */
    private String remarks;

    /**
     * 状态
     */
    private Integer status;

    /**
     * 错误异常信息
     */
    private String errorMessage;

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

    public String getPCenterUuid() {
        return pCenterUuid;
    }

    public void setPCenterUuid(String pCenterUuid) {
        this.pCenterUuid = pCenterUuid;
    }

    public String getCenterUuid() {
        return centerUuid;
    }

    public void setCenterUuid(String centerUuid) {
        this.centerUuid = centerUuid;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
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
