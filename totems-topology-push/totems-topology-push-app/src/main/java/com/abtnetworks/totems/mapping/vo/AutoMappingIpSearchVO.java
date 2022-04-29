package com.abtnetworks.totems.mapping.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @desc
 * @author liuchanghao
 * @date 2021-06-11 10:52
 */
@Data
@ApiModel("工单任务检测查询VO")
public class AutoMappingIpSearchVO {

    @ApiModelProperty("工单类型（0：源nat；1：目的nat；）")
    private Integer natType;

    @ApiModelProperty("IP地址，查询时使用")
    private String ip;

    @ApiModelProperty("转换前地址")
    private String preIp;

    @ApiModelProperty("转换后地址")
    private String postIp;

    @ApiModelProperty("设备uuid")
    private String deviceUuid;

    /**
     * 页数
     */
    private Integer page = 1;

    /**
     * 每页条数
     */
    private Integer limit = 20;

}