package com.abtnetworks.totems.auto.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * 分页查询条件对象
 */
@Data
public class AutoRecommendConflictVo {

    @ApiModelProperty("自动开通工单主键id")
    private Integer autoTaskId;

    @ApiModelProperty("设备uuid ")
    private String deviceUuid;

    @ApiModelProperty("策略类型")
    private String policyType;

    /**
     * 批量添加的数据
     */
    @ApiModelProperty("自动开通策略相关策略管理集合 ")
    List<AutoRecommendConflictPolicyVo> policyVoList;

    @ApiModelProperty("主题工单号 ")
    private String theme;

    /**
     * 页数
     */
    private Integer page = 1;

    /**
     * 每页条数
     */
    private Integer limit = 20;
}