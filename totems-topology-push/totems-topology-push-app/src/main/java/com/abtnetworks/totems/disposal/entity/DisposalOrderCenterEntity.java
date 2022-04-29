package com.abtnetworks.totems.disposal.entity;

import com.abtnetworks.totems.disposal.BaseEntity;

/**
 * @Author hw
 * @Description
 * @Date 16:57 2019/11/11
 */
public class DisposalOrderCenterEntity extends BaseEntity {

    private static final long serialVersionUID = 529625018663990657L;

    /**
     * 主键id，自增
     */
    private Long id;

    /**
     * UUID
     */
    private String uuid;

    /**
     * 工单名称，用户自定义
     */
    private String orderName;

    /**
     * 工单号，系统生成
     */
    private String orderNo;

    /**
     * 分类：策略，路由
     */
    private Integer category;

    /**
     * 工单类型：1手动、2黑IP、3路径
     */
    private Integer type;

    /**
     * 派发审核类型：0手动审核、1自动
     */
    private Integer sendType;

    /**
     * 源IP
     */
    private String srcIp;

    /**
     * 目的ip
     */
    private String dstIp;

    /**
     * 服务组
     */
    private String serviceList;

    /**
     * 黑洞路由IP
     */
    private String routingIp;

    /**
     * 动作
     */
    private String action;

    /**
     * 需求来源描述
     */
    private String origin;

    /**
     * 原因
     */
    private String reason;

    /**
     * 来源分类，是否上级派发
     */
    private Integer sourceClassification;


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

    public Integer getSourceClassification() {
        return sourceClassification;
    }

    public void setSourceClassification(Integer sourceClassification) {
        this.sourceClassification = sourceClassification;
    }

}
