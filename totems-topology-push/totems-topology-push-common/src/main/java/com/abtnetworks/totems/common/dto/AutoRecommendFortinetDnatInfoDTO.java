package com.abtnetworks.totems.common.dto;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author liuchanghao
 * @desc 自动开通生成飞塔目的NAT vip对象信息
 * @date 2021-12-17 16:25
 */
@Data
@ApiModel("vip对象信息DTO")
public class AutoRecommendFortinetDnatInfoDTO {

    @ApiModelProperty("vip对象名称")
    String vipName;

    @ApiModelProperty("转换前目的IP")
    String preDstIp;

    @ApiModelProperty("转换后目的IP")
    String postDstIp;

}
