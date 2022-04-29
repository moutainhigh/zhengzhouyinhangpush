package com.abtnetworks.totems.recommend.dto.risk;

import com.abtnetworks.totems.whale.policy.ro.AccessQueryDataRO;
import lombok.Data;

import java.util.List;

/**
 * @Author: wenjiachang
 * @Date: 2018/12/14 18:20
 */
@Data
public class PolicyRecommendRiskDTO {

    /**
     * 任务id
     */
    int taskId;

    /**
     * 源域UUID
     */
    String srcZoneUuid;

    /**
     * 目的域UUID
     */
    String dstZoneUuid;

    /**
     * 数据流对象
     */
    List<AccessQueryDataRO> data;
}
