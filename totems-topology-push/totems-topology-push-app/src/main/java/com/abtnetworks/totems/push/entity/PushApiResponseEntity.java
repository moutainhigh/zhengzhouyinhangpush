package com.abtnetworks.totems.push.entity;

import com.alibaba.fastjson.JSONObject;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @desc    管理平台API 下发响应数据
 * @author liuchanghao
 * @date 2021-02-26 11:06
 */
@Data
public class PushApiResponseEntity {

    @ApiModelProperty("源地址对象名称")
    private List<String> srcNameList;

    @ApiModelProperty("目的地址对象名称")
    private List<String> dstNameList;

    @ApiModelProperty("源地址对象转换后的名称")
    private List<String> postSrcNameList;

    @ApiModelProperty("服务对象名称")
    private List<String> serviceNames;

    @ApiModelProperty("时间对象名称")
    private List<String> scheduleNames;

    @ApiModelProperty("策略id")
    private String policyId;

    @ApiModelProperty("策略名称")
    private String policyName;

    @ApiModelProperty("安装响应数据")
    private JSONObject installResult;

    @ApiModelProperty("回滚响应数据")
    private String rollbackResult;

    @ApiModelProperty("ip类型  0：ipv4; 1:ipv6; 2:url ")
    private Integer ipType;

}
