package com.abtnetworks.totems.recommend.vo;

import lombok.Data;

/**
 * @Author: wenjiachang
 * @Date: 2018/12/18 16:06
 */
@Data
public class PolicyRecommendSecurityPolicyVO {

    /**
     * 策略集
     */
    String policy;

    /**
     * 策略名称
     */
    String policyName;

    /**
     * 策略id
     */
    String policyId;

    /**
     * 源域
     */
    String srcDomain;

    /**
     * 源IP
     */
    String srcIp;

    /**
     * 目的域
     */
    String dstDomain;

    /**
     * 目的IP
     */
    String dstIp;

    /**
     * 服务
     */
    String service;

    /**
     * 时间
     */
    String time;

    /**
     * 老化时间
     */
    String saveIdleTimeout;

    /**
     * 行为
     */
    String action;

    /**
     * 是否启用
     */
    String isAble;

    /***
     * 描述
     */
    String description;

    /**
     * 长连接
     */
    String idleTimeout;

    /**
     * 策略Uuid
     */
    private String policyUuid;

    /**
     * 设备UUID
     */
    private String deviceUuid;
}
