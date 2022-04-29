package com.abtnetworks.totems.common.dto.commandline;

import com.alibaba.fastjson.JSONObject;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @desc   被合并的策略dto
 * @author liuchanghao
 * @date 2021-01-06 10:44
 */
@Data
public class PolicyRecommendSecurityPolicyDTO {

    @ApiModelProperty("规则id")
    String id;

    @ApiModelProperty("规则UUID")
    String uuid;

    @ApiModelProperty("规则名称")
    String name;

    @ApiModelProperty("创建时间")
    String createTime;

    @ApiModelProperty("修改时间")
    String lastModifiedTime;

    @ApiModelProperty("乐观锁")
    String version;

    @ApiModelProperty("IP类型")
    String ipType;

    @ApiModelProperty("策略匹配条款模型")
    JSONObject matchClause;

    @ApiModelProperty("nat策略")
    JSONObject natClause;

    @ApiModelProperty("规则匹配动作")
    String action;

    @ApiModelProperty("策略集uuid")
    String ruleListUuid;

    @ApiModelProperty("源域")
    List<String> inInterfaceGroupRefs;

    @ApiModelProperty("目的域列表")
    List<String> outInterfaceGroupRefs;

    @ApiModelProperty("规则id")
    String ruleId;

    @ApiModelProperty("老化时间")
    String idleTimeout;

    @ApiModelProperty("时间对象")
    String filterTimeGroupName;


}
