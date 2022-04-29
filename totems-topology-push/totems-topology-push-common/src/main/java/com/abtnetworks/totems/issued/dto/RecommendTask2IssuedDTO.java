package com.abtnetworks.totems.issued.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author Administrator
 * @Title:
 * @Description: 将工单表中有用的信息放在这里给下发用
 * @date 2020/9/7
 */
@Data
public class RecommendTask2IssuedDTO {
    @ApiModelProperty("设备源域")
    String srcZone;

    @ApiModelProperty("设备目的域")
    String dstZone;

    @ApiModelProperty("长连接 工单中的长连接 为高级设置提供使用")
    String idleTime;

    @ApiModelProperty("合并的字段属性")
    private Integer mergeProperty;

    @ApiModelProperty("合并的字段属性")
    private String ruleListName;

    @ApiModelProperty("匹配到的deny策略的id")
    private String matchRuleId;

    @ApiModelProperty("是否是合并策略")
    private boolean isMergePolicy = false;

}
