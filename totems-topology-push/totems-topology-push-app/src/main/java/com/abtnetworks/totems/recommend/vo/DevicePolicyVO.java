package com.abtnetworks.totems.recommend.vo;

import com.abtnetworks.totems.external.vo.PolicyDetailVO;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @Author: wenjiachang
 * @Date: 2018/12/21 10:10
 */
@Data
public class DevicePolicyVO {
    List<PolicyRecommendSecurityPolicyVO> securityPolicyList;

    List<PolicyRecommendNatPolicyVO> natPolicList;

    List<PolicyRecommendPolicyRouterVO> routerList;

    @ApiModelProperty("ACL策略")
    List<PolicyDetailVO> aclList;
    @ApiModelProperty("策略路由")
    List<PolicyDetailVO> policyRoutList;
}
