package com.abtnetworks.totems.common.dto.commandline;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.util.List;

/**
 * @author lifei
 * @desc 下发pool信息(针对于f5设备)
 * @date 2021/8/2 20:22
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PushPoolInfo {

    /**
     * pool名称
     */
    private String name;

    /**
     * pool详情集合
     */
    private List<PoolDetailInfo> poolDetailInfos;

    /**
     * pool组优先级类型（disable：没有组优先级和单ip优先级这个概念,Less than:有组优先级和单ip优先级）
     */
    private String groupPriorityType;

    /**
     * pool组优先级等级
     */
    private Integer groupPriorityLevel;
}
