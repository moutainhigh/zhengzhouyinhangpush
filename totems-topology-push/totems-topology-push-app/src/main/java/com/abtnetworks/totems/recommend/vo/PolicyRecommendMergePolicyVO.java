package com.abtnetworks.totems.recommend.vo;

/**
 * @Author: wenjiachang
 * @Date: 2018/12/18 17:00
 */
public class PolicyRecommendMergePolicyVO {

    String recommendPolicyId;

    String id;

    String type;

    String lineNumber;

    String policy;

    String policyId;

    String policyName;

    String srcDomain;

    String srcIp;

    String dstDomain;

    String dstIp;

    String service;

    String time;

    String saveIdleTimeout;

    String action;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getLineNumber() {
        return lineNumber;
    }

    public void setLineNumber(String lineNumber) {
        this.lineNumber = lineNumber;
    }

    public String getPolicy() {
        return policy;
    }

    public void setPolicy(String policy) {
        this.policy = policy;
    }

    public String getPolicyId() {
        return policyId;
    }

    public void setPolicyId(String policyId) {
        this.policyId = policyId;
    }

    public String getPolicyName() {
        return policyName;
    }

    public void setPolicyName(String policyName) {
        this.policyName = policyName;
    }

    public String getSrcDomain() {
        return srcDomain;
    }

    public void setSrcDomain(String srcDomain) {
        this.srcDomain = srcDomain;
    }

    public String getSrcIp() {
        return srcIp;
    }

    public void setSrcIp(String srcIp) {
        this.srcIp = srcIp;
    }

    public String getDstDomain() {
        return dstDomain;
    }

    public void setDstDomain(String dstDomain) {
        this.dstDomain = dstDomain;
    }

    public String getDstIp() {
        return dstIp;
    }

    public void setDstIp(String dstIp) {
        this.dstIp = dstIp;
    }

    public String getService() {
        return service;
    }

    public void setService(String service) {
        this.service = service;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getSaveIdleTimeout() {
        return saveIdleTimeout;
    }

    public void setSaveIdleTimeout(String saveIdleTimeout) {
        this.saveIdleTimeout = saveIdleTimeout;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getRecommendPolicyId() {
        return recommendPolicyId;
    }

    public void setRecommendPolicyId(String recommendPolicyId) {
        this.recommendPolicyId = recommendPolicyId;
    }
}
