package com.abtnetworks.totems.disposal.dto;

import java.util.Date;

/**
 * @Author hw
 * @Description
 * @Date 17:56 2019/11/12
 */
public class ImportOrderDTO {

    /**
     * 主键id，自增
     */
    public Long id;

    /**
     * 工单内容UUID
     */
    public String centerUuid;

    /**
     * 备注
     */
    public String remarks;

    /**
     * 状态
     */
    public Integer status;

    /**
     * 创建人员
     */
    public String createUser;

    /**
     * 创建时间
     */
    public Date createTime;

    /**
     * 工单名称，用户自定义
     */
    public String orderName;

    /**
     * 工单号，系统生成
     */
    public String orderNo;

    /**
     * 分类：策略，路由
     */
    public Integer category;

    /**
     * 工单类型：1手动、2黑IP、3路径
     */
    public Integer type;

    /**
     * 派发审核类型：0手动审核、1自动
     */
    public Integer sendType;

    /**
     * 源IP
     */
    public String srcIp;

    /**
     * 目的ip
     */
    public String dstIp;

    /**
     * 服务组
     */
    public String serviceList;

    /**
     * 黑洞路由IP
     */
    public String routingIp;

    /**
     * 动作
     */
    public String action;

    /**
     * 需求来源描述
     */
    public String origin;

    /**
     * 原因
     */
    public String reason;

    /**
     * 场景名称
     */
    public String scenesNames;

    /**
     * IP地址类型（Excel模板数据：IPV4,IPV6），是否 ipv6
     */
    public String strIpv6;

    public ImportOrderDTO() {
    }

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

    public String getOrderName() {
        return orderName;
    }

    public void setOrderName(String orderName) {
        this.orderName = orderName;
    }

    public String getOrderNo() {
        return orderNo;
    }

    public void setOrderNo(String orderNo) {
        this.orderNo = orderNo;
    }

    public Integer getCategory() {
        return category;
    }

    public void setCategory(Integer category) {
        this.category = category;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public Integer getSendType() {
        return sendType;
    }

    public void setSendType(Integer sendType) {
        this.sendType = sendType;
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

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getOrigin() {
        return origin;
    }

    public void setOrigin(String origin) {
        this.origin = origin;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getScenesNames() {
        return scenesNames;
    }

    public void setScenesNames(String scenesNames) {
        this.scenesNames = scenesNames;
    }

    public String getStrIpv6() {
        return strIpv6;
    }

    public void setStrIpv6(String strIpv6) {
        this.strIpv6 = strIpv6;
    }
}
