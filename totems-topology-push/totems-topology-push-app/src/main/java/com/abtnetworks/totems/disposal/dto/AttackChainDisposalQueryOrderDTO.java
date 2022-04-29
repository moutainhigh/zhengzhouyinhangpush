package com.abtnetworks.totems.disposal.dto;

/**
 * @Author hw
 * @Description
 * @Date 17:56 2019/11/12
 */
public class AttackChainDisposalQueryOrderDTO {

    /**
     * 状态
     */
    private Integer status;

    /**
     * 分类：策略，路由
     */
    private Integer category;

    /**
     * 工单类型：1手动、2黑IP、3路径
     */
    private Integer type;

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

    public AttackChainDisposalQueryOrderDTO() {
    }

    public AttackChainDisposalQueryOrderDTO(String srcIp, String dstIp, String serviceList) {
        this.srcIp = srcIp;
        this.dstIp = dstIp;
        this.serviceList = serviceList;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
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
}
