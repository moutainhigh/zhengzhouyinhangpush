package com.abtnetworks.totems.auto.dto;

import com.abtnetworks.totems.external.vo.PolicyDetailVO;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Set;

/**
 * @desc    自动开通-冲突策略
 * @author liuchanghao
 * @date 2021-08-03 10:59
 */
@Data
public class AutoRecommendConflictPolicyDTO {

    @ApiModelProperty("安全策略-冲突策略集合")
    private Set<PolicyDetailVO> securityConflictPolicyDTOSet;

    @ApiModelProperty("nat策略-冲突策略集合")
    private Set<PolicyDetailVO> natConflictPolicyDTOSet;

}
