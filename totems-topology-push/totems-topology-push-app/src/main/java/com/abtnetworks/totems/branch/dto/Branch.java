package com.abtnetworks.totems.branch.dto;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;
import java.util.Date;

/**
 * @ Author     ：muyuanling.
 * @ Date       ：Created in 9:54 2019/5/10
 */
@Entity
@Table(name = "topo_branch")
public class Branch implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    private int id;

    /**
     * 等级，总部为00，总部之下每一级加两位数字，如湖北总部是00，武汉分部为0001，襄阳分部为0002
     */
    @Column
    private String level;

    /**
     * 上级等级
     */
    @Column
    private String parentLevel;

    /**
     * 机构名称
     */
    @Column
    private String branchName;

    /**
     * 机构描述
     */
    @Column
    private String branchDesc;

    /**
     * 创建时间
     */
    @Column
    private Date createTime;

    /**
     * 修改时间
     */
    @Column
    private Date updateTime;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public String getParentLevel() {
        return parentLevel;
    }

    public void setParentLevel(String parentLevel) {
        this.parentLevel = parentLevel;
    }

    public String getBranchName() {
        return branchName;
    }

    public void setBranchName(String branchName) {
        this.branchName = branchName;
    }

    public String getBranchDesc() {
        return branchDesc;
    }

    public void setBranchDesc(String branchDesc) {
        this.branchDesc = branchDesc;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }
}
