package com.abtnetworks.totems.common.dto.commandline;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

/**
 * @author lifei
 * @desc pool详情
 * @date 2021/8/2 20:25
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PoolDetailInfo {

    /**
     * ip地址 ,多个用逗号分隔
     */
    private String  ip;

    /**
     * 端口
     */
    private String  port;

    /**
     * 单ip的优先级
     */
    private Integer  priorityLevel;
}
