package com.abtnetworks.totems.disposal.dto;

import com.abtnetworks.totems.disposal.BaseEntity;

/**
 * @Author hw
 * @Description
 * @Date 10:02 2019/11/12
 */
public class DisposalTeamBranchDTO extends BaseEntity {

    private static final long serialVersionUID = -1604487506329531117L;

    /**
     * 工单uuid
     */
    private String centerUuid;

    /**
     * 下级单位uuid
     */
    private String[] branchUuidArray;

    /**
     * 是否需要审核
     */
    private Boolean needAuditFlag;

    /**
     * 下级单位编号
     */
    private String[] branchCodeArray;

    public DisposalTeamBranchDTO() {
    }

    public String getCenterUuid() {
        return centerUuid;
    }

    public void setCenterUuid(String centerUuid) {
        this.centerUuid = centerUuid;
    }

    public String[] getBranchUuidArray() {
        return branchUuidArray;
    }

    public void setBranchUuidArray(String[] branchUuidArray) {
        this.branchUuidArray = branchUuidArray;
    }

    public Boolean getNeedAuditFlag() {
        return needAuditFlag;
    }

    public void setNeedAuditFlag(Boolean needAuditFlag) {
        this.needAuditFlag = needAuditFlag;
    }

    public String[] getBranchCodeArray() {
        return branchCodeArray;
    }

    public void setBranchCodeArray(String[] branchCodeArray) {
        this.branchCodeArray = branchCodeArray;
    }
}
