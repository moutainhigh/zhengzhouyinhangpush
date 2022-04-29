package com.abtnetworks.totems.recommend.dto.task;

import com.abtnetworks.totems.whale.policy.dto.PathAnalyzeDTO;
import com.abtnetworks.totems.whale.policy.ro.PathAnalyzeRO;
import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * @Author: wenjiachang
 * @Date: 2018/12/19 11:14
 */
@Data
public class RecommendTaskDTO extends BaseTaskDTO {

    /**
     * 任务ID，数据库自动生成
     */
    private int id;

    /**
     * 主题（工单号）
     */
    private String name;

    /**
     * 流水号
     */
    private String orderNO;

    /**
     * 描述
     */
    private String description;

    /**
     * 源IP
     */
    private String srcIp;

    /**
     * 源端口
     */
    private String srcPort;

    /**
     * 目的IP
     */
    private String dstIp;

    /**
     * 目的端口
     */
    private String dstPort;

    /**
     * 协议
     */
    private String protocol;

    /**
     * ICMP类型
     */
    private Integer icmpType;

    /**
     * ICMP码
     */
    private Integer icmpCode;

    /**
     * 开始时间
     */
    private Date startTime;

    /**
     * 结束时间
     */
    private Date endTime;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 路径查询结果数据
     */
    private PathAnalyzeRO pathAnalyzeRO;

    /**
     * 相关生成策略
     */
    List<RecommendTaskPolicyDTO> policyList;
}
