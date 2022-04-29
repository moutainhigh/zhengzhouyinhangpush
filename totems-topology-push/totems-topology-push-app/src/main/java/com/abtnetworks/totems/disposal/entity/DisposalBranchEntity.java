package com.abtnetworks.totems.disposal.entity;

import com.abtnetworks.totems.disposal.BaseEntity;
import java.io.Serializable;
import java.util.Date;

/**
 * @Author hw
 * @Description
 * @Date 16:57 2019/11/11
 */
public class DisposalBranchEntity extends BaseEntity {

    private static final long serialVersionUID = -6259215541083742305L;

    /**
     * 主键id，自增
     */
    private Long id;

    /**
     * UUID
     */
    private String uuid;

    /**
     * 下级单位编码
     */
    private String code;

    /**
     * 下级单位名称
     */
    private String name;

    /**
     * 下级单位主机IP
     */
    private String ip;

    /**
     * 备注
     */
    private String remarks;

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

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

}
