package com.abtnetworks.totems.common.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * @author liuchanghao
 * @desc 自动开通工单命令行生成特殊处理DTO
 * @date 2021-11-07 16:25
 */
@Data
@ApiModel("自动开通适配地址对象特殊处理")
public class AutoRecommendSpecialDTO {

    @ApiModelProperty("已存在源地址对象列表")
    List<AddressObjectInfoDTO> existSrcAddressList = new ArrayList<>();

    @ApiModelProperty("还需要创建源地址对象列表")
    List<AddressObjectInfoDTO> restSrcAddressList = new ArrayList<>();

    @ApiModelProperty("已存在目的地址对象列表")
    List<AddressObjectInfoDTO> existDstAddressList = new ArrayList<>();

    @ApiModelProperty("还需要创建目的地址对象列表")
    List<AddressObjectInfoDTO> restDstAddressList = new ArrayList<>();

}
