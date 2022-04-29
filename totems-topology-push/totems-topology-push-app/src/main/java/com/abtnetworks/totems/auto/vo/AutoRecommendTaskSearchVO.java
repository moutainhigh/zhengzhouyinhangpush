package com.abtnetworks.totems.auto.vo;

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
@ApiModel("防护网段配置查询VO")
public class AutoRecommendTaskSearchVO {

    @ApiModelProperty("查询IP")
    private String ip;

    @ApiModelProperty("ip类型  0：ipv4; 1:ipv6; 2:url")
    private Integer ipType;

    @ApiModelProperty("源IP组")
    private String srcIp;

    @ApiModelProperty("主题")
    private String theme;

    @ApiModelProperty("目的IP组")
    private String dstIp;

    @ApiModelProperty("任务状态")
    private Integer status;

    @ApiModelProperty("创建时间-开始时间")
    private Date createStartTime;

    @ApiModelProperty("创建时间-结束时间")
    private Date createEndTime;

    @ApiModelProperty("申请人")
    private String applicant;

    @ApiModelProperty("当前操作人")
    private String userName;

    /**
     * 页数
     */
    private Integer page = 1;

    /**
     * 每页条数
     */
    private Integer limit = 20;

}