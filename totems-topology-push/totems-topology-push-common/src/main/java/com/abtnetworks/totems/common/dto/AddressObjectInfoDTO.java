package com.abtnetworks.totems.common.dto;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author liuchanghao
 * @desc 自动开通工单命令行生成引用地址对象信息
 * @date 2021-11-07 16:25
 */
@Data
@ApiModel("地址对象信息DTO")
public class AddressObjectInfoDTO {

    @ApiModelProperty("地址对象名称")
    String addressObjectName;

    @ApiModelProperty("地址对象对应的IP")
    String addressObjectIP;

}
