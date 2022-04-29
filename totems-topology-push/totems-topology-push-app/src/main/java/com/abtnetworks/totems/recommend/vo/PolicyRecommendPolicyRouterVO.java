package com.abtnetworks.totems.recommend.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @Author: wenjiachang
 * @Date: 2018/12/18 16:47
 */
@Data
@ApiModel("路由策略前端显示数据对象")
public class PolicyRecommendPolicyRouterVO {

    @ApiModelProperty("序号")
    String number;

    @ApiModelProperty("IP")
    String srcIp;

    @ApiModelProperty("掩码")
    String mask;

    @ApiModelProperty("下一跳")
    String nextHop;

    @ApiModelProperty("出接口")
    String netDoor;

    @ApiModelProperty("优先级")
    String distance;

    @ApiModelProperty("权重")
    String weight;

    @ApiModelProperty("协议")
    String protocol;

    @ApiModelProperty("描述")
    String description;


}
