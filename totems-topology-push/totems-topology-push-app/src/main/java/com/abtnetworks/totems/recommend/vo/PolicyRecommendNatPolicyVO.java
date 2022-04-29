package com.abtnetworks.totems.recommend.vo;

import lombok.Data;

/**
 * @Author: wenjiachang
 * @Date: 2018/12/18 16:29
 */
@Data
public class PolicyRecommendNatPolicyVO {

    /**
     * NAT类型
     */
    String natType;

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
     * 源IP（转换前）
     */
    String preSrcIp;

    /**
     * 源IP（转换后）
     */
    String postSrcIp;

    /**
     * 目的域
     */
    String dstDomain;

    /**
     * 目的IP（转换前）
     */
    String preDstIp;

    /**
     * 目的IP（转换后）
     */
    String postDstIp;

    /**
     * 转换前服务
     */
    String preService;

    /**
     * 转换后服务
     */
    String postService;

    /**
     * 时间
     */
    String time;

    /**
     * 是否生效
     */
    String isAble;

    /**
     * 描述
     */
    String description;

}
