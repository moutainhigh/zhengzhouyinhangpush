package com.abtnetworks.totems.recommend.dto.task;

import lombok.Data;

import java.util.List;


/**
 * @Author: wenjiachang
 * @Date: 2018/12/19 9:38
 */
@Data
public class VerifyTaskDTO extends BaseTaskDTO {

    int id;

    String taskUuid;

    private String srcIp;

    private String srcPort;

    private String dstIp;

    private String dstPort;

    private String protocol;

    List<RecommendTaskPolicyDTO> policyList;
}
