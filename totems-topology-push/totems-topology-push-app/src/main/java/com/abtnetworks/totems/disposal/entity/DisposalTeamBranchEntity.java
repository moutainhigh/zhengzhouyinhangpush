package com.abtnetworks.totems.disposal.entity;

import com.abtnetworks.totems.disposal.BaseEntity;
import java.io.Serializable;
import java.util.Date;

/**
 * @Author hw
 * @Description
 * @Date 10:02 2019/11/12
 */
public class DisposalTeamBranchEntity extends BaseEntity {

    private static final long serialVersionUID = 4484729548188662801L;

    /**
     * 主键id，自增
     */
    private Long id;

    /**
     * 工单uuid
     */
    private String centerUuid;

    /**
     * 下级单位uuid
     */
    private String branchUuid;

    /**
     * 是否需要审核
     */
    private Boolean needAuditFlag;

    /**
     * 下级单位编号
     */
    private String branchCode;

    /**
     * 下级单位名称
     */
    private String branchName;

    /**
     * 下级单位主机IP
     */
    private String branchIp;

    /**
     * 下级单位备注
     */
    private String branchRemarks;

    /**
     * 处置单状态
     */
    private Integer handleStatus;

    /**
     * 回滚处置单状态
     */
    private Integer callbackHandleStatus;

    /**
     * 下级单位处置时间
     */
    private Date handleTime;


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

    public String getBranchUuid() {
        return branchUuid;
    }

    public void setBranchUuid(String branchUuid) {
        this.branchUuid = branchUuid;
    }

    public Boolean getNeedAuditFlag() {
        return needAuditFlag;
    }

    public void setNeedAuditFlag(Boolean needAuditFlag) {
        this.needAuditFlag = needAuditFlag;
    }

    public String getBranchCode() {
        return branchCode;
    }

    public void setBranchCode(String branchCode) {
        this.branchCode = branchCode;
    }

    public String getBranchName() {
        return branchName;
    }

    public void setBranchName(String branchName) {
        this.branchName = branchName;
    }

    public String getBranchIp() {
        return branchIp;
    }

    public void setBranchIp(String branchIp) {
        this.branchIp = branchIp;
    }

    public String getBranchRemarks() {
        return branchRemarks;
    }

    public void setBranchRemarks(String branchRemarks) {
        this.branchRemarks = branchRemarks;
    }

    public Integer getHandleStatus() {
        return handleStatus;
    }

    public void setHandleStatus(Integer handleStatus) {
        this.handleStatus = handleStatus;
    }

    public Integer getCallbackHandleStatus() {
        return callbackHandleStatus;
    }

    public void setCallbackHandleStatus(Integer callbackHandleStatus) {
        this.callbackHandleStatus = callbackHandleStatus;
    }

    public Date getHandleTime() {
        return handleTime;
    }

    public void setHandleTime(Date handleTime) {
        this.handleTime = handleTime;
    }

}
