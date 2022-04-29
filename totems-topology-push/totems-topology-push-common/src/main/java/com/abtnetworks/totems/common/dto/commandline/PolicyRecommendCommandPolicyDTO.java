package com.abtnetworks.totems.common.dto.commandline;

/**
 * @Author: wenjiachang
 * @Date: 2018/12/17 12:15
 */
public class PolicyRecommendCommandPolicyDTO {
    /**
     * 策略id
     */
    int policyId;

    /**
     * 策略名称
     */
    String policyName;

    /**
     * 设备ip
     */
    String ip;

    /**
     *  设备厂商ID
     */
    String vendorId;

    /**
     * 设备厂商名称
     */
    String vendorName;

    /**
     * 设备UUID
     */
    String uuid;

    /**
     * 设备名称
     */
    String name;

    /**
     * 策略描述信息
     */
    String description;

    /**
     * 策略行为，1为permit，2位deny
     */
    int action;

    /**
     * 是否启用，1为启用，2为不启用
     */
    int enable;

    /**
     * 源域
     */
    String srcZone;

    /**
     * 源接口
     */
    String srcInf;

    /**
     * 源ip
     */
    String srcIp;

    /**
     * 目的域
     */
    String dstZone;

    /**
     * 目的接口
     */
    String dstInf;

    /**
     * 目的ip
     */
    String dstIp;

    /**
     * 协议
     */
    String protocol;

    /**
     * 源端口
     */
    String srcPort;

    /**
     * 目的端口
     */
    String dstPort;

    /**
     * 开始时间
     */
    String startTime;

    /**
     * 结束时间
     */
    String endTime;

    public int getPolicyId() {
        return policyId;
    }

    public void setPolicyId(int policyId) {
        this.policyId = policyId;
    }

    public String getPolicyName() {
        return policyName;
    }

    public void setPolicyName(String policyName) {
        this.policyName = policyName;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getVendorId() {
        return vendorId;
    }

    public void setVendorId(String vendorId) {
        this.vendorId = vendorId;
    }

    public String getVendorName() {
        return vendorName;
    }

    public void setVendorName(String vendorName) {
        this.vendorName = vendorName;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getAction() {
        return action;
    }

    public void setAction(int action) {
        this.action = action;
    }

    public int getEnable() {
        return enable;
    }

    public void setEnable(int enable) {
        this.enable = enable;
    }

    public String getSrcZone() {
        return srcZone;
    }

    public void setSrcZone(String srcZone) {
        this.srcZone = srcZone;
    }

    public String getSrcInf() {
        return srcInf;
    }

    public void setSrcInf(String srcInf) {
        this.srcInf = srcInf;
    }

    public String getSrcIp() {
        return srcIp;
    }

    public void setSrcIp(String srcIp) {
        this.srcIp = srcIp;
    }

    public String getDstZone() {
        return dstZone;
    }

    public void setDstZone(String dstZone) {
        this.dstZone = dstZone;
    }

    public String getDstInf() {
        return dstInf;
    }

    public void setDstInf(String dstInf) {
        this.dstInf = dstInf;
    }

    public String getDstIp() {
        return dstIp;
    }

    public void setDstIp(String dstIp) {
        this.dstIp = dstIp;
    }

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public String getSrcPort() {
        return srcPort;
    }

    public void setSrcPort(String srcPort) {
        this.srcPort = srcPort;
    }

    public String getDstPort() {
        return dstPort;
    }

    public void setDstPort(String dstPort) {
        this.dstPort = dstPort;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }
}
