package com.abtnetworks.totems.common.vo;

import com.abtnetworks.totems.external.vo.PolicyDetailVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @author Administrator
 * @Title:
 * @Description: 请写注释类
 * @date 2021/3/23
 */
@Api("push中命令行返回的重要信息")
@Data
public class PushCommandInfoVO {
    @ApiModelProperty("策略名字")
    private String policyName;
    @ApiModelProperty("0合并源IP，1,合并目的IP, 2合并服务,3新增策略")
    private Integer commandType;

    @ApiModelProperty("匹配策略")
    private List<PolicyDetailVO> ruleList;

}
