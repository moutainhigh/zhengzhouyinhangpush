package com.abtnetworks.totems.common.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * @author liuchanghao
 * @desc 自动开通飞塔NAT特殊处理DTO
 * @date 2021-12-16 16:25
 */
@Data
@ApiModel("自动开通飞塔NAT特殊处理")
public class AutoRecommendFortinetDnatSpecialDTO {

    @ApiModelProperty("已存在VIP列表")
    List<AutoRecommendFortinetDnatInfoDTO> existVipList = new ArrayList<>();

    @ApiModelProperty("还需要创建VIP列表")
    List<AutoRecommendFortinetDnatInfoDTO> restVipList = new ArrayList<>();

}
