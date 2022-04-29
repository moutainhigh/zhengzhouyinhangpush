package com.abtnetworks.totems.common.dto.commandline;

import io.swagger.annotations.ApiModel;
import lombok.Data;

/**
 * @author luwei
 * @date 2019/5/30
 */
@ApiModel(value = "策略合并对象")
@Data
public class PolicyMergeDTO {

    private String ruleId;

    private String ruleName;

    private String mergeField;

}
