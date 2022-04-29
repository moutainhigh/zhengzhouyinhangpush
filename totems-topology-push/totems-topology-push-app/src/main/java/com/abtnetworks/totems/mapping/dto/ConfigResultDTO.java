package com.abtnetworks.totems.mapping.dto;

import com.abtnetworks.totems.mapping.enums.CustomRuleTypeEnum;
import com.abtnetworks.totems.mapping.enums.RuleTypeTaskEnum;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @desc    获取规则配置返回值DTO
 * @author liuchanghao
 * @date 2022-02-08 10:31
 */
@Data
public class ConfigResultDTO {

    @ApiModelProperty("主题/工单号")
    private CustomRuleTypeEnum customRuleTypeEnum;

    @ApiModelProperty("描述")
    private RuleTypeTaskEnum ruleTypeTaskEnum;

}
