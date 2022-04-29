package com.abtnetworks.totems.push.dto.forti;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @author lifei
 * @since 2021/2/18
 **/
@Data
@ApiModel("飞塔管理平台策略创建DTO")
public class FortiPoliceDTO {

    String  name;

    @ApiModelProperty("入接口")
    List<String> srcintf;

    @ApiModelProperty("出接口")
    List<String> dstintf;

    @ApiModelProperty("源地址名称集合")
    List<String> srcaddr;

    @ApiModelProperty("目的地址名称集合")
    List<String> dstaddr;

    @ApiModelProperty("服务名称集合")
    List<String> service;

    @ApiModelProperty("时间名称")
    List<String> schedule;

    @ApiModelProperty("动作  0:拒绝 1:接受")
    Integer action;

    @ApiModelProperty("0:不记录违规流量,1:记录违规流量; 当防火墙的动作是拒绝的时候才有这个参数")
    Integer  logtraffic;

    @ApiModelProperty("0:禁用,1:启用 新增策略的时候默认启用")
    Integer  status;
}
