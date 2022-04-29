package com.abtnetworks.totems.disposal.entity;

import com.abtnetworks.totems.disposal.BaseEntity;
import java.util.Date;

/**
 * @Author hw
 * @Description
 * @Date 14:20 2019/11/26
 */
public class DisposalDeleteCommandLineRecordEntity extends BaseEntity {

    private static final long serialVersionUID = -2368721925092252433L;

    /**
     * 主键id，自增
     */
    private Long id;

    /**
     * UUID
     */
    private String uuid;

    /**
     * disposal_create_command_line_record表的UUID
     */
    private String createUuid;

    /**
     * push_command_task_editable表的主键id
     */
    private Integer taskEditableId;

    /**
     * 工单UUID
     */
    private String centerUuid;

    /**
     * 工单编号
     */
    private String orderNo;

    /**
     * 0封堵，1解封，2回滚
     */
    private Integer type;

    /**
     * 源ip
     */
    private String srcIp;

    /**
     * 目的ip
     */
    private String dstIp;

    /**
     * ip类型（0：ipv4，1：ipv6）
     */
    private Integer ipType;

    /**
     * 服务json
     */
    private String serviceList;

    /**
     * 黑洞路由ip
     */
    private String routingIp;

    /**
     * 是否虚墙
     */
    private Integer vsys;

    /**
     * 虚墙的主设备UUID
     */
    private String pDeviceUuid;

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

    public String getCreateUuid() {
        return createUuid;
    }

    public void setCreateUuid(String createUuid) {
        this.createUuid = createUuid;
    }

    public Integer getTaskEditableId() {
        return taskEditableId;
    }

    public void setTaskEditableId(Integer taskEditableId) {
        this.taskEditableId = taskEditableId;
    }

    public String getCenterUuid() {
        return centerUuid;
    }

    public void setCenterUuid(String centerUuid) {
        this.centerUuid = centerUuid;
    }

    public String getOrderNo() {
        return orderNo;
    }

    public void setOrderNo(String orderNo) {
        this.orderNo = orderNo;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public String getSrcIp() {
        return srcIp;
    }

    public void setSrcIp(String srcIp) {
        this.srcIp = srcIp;
    }

    public String getDstIp() {
        return dstIp;
    }

    public void setDstIp(String dstIp) {
        this.dstIp = dstIp;
    }

    public Integer getIpType() {
        return ipType;
    }

    public void setIpType(Integer ipType) {
        this.ipType = ipType;
    }

    public String getServiceList() {
        return serviceList;
    }

    public void setServiceList(String serviceList) {
        this.serviceList = serviceList;
    }

    public String getRoutingIp() {
        return routingIp;
    }

    public void setRoutingIp(String routingIp) {
        this.routingIp = routingIp;
    }

    public Integer getVsys() {
        return vsys;
    }

    public void setVsys(Integer vsys) {
        this.vsys = vsys;
    }

    public String getPDeviceUuid() {
        return pDeviceUuid;
    }

    public void setPDeviceUuid(String pDeviceUuid) {
        this.pDeviceUuid = pDeviceUuid;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

}
