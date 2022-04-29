package com.abtnetworks.totems.mapping.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

/**
 * @desc
 * @author liuchanghao
 * @date 2021-06-11 10:52
 */
@Data
@ApiModel("工单任务检测查询VO")
public class AutoMappingTaskSearchVO {

    @ApiModelProperty("源IP组")
    private String srcIp;

    @ApiModelProperty("主题")
    private String theme;

    @ApiModelProperty("目的IP组")
    private String dstIp;

    @ApiModelProperty("描述")
    private String description;

    /**
     * 页数
     */
    private Integer page = 1;

    /**
     * 每页条数
     */
    private Integer limit = 20;

}