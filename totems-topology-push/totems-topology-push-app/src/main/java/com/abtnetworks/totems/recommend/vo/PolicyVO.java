package com.abtnetworks.totems.recommend.vo;

import com.abtnetworks.totems.common.dto.commandline.ServiceDTO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;
import java.util.List;


@Data
@ApiModel("策略")
public class PolicyVO {
    @ApiModelProperty("主键")
    private Integer id;

    @ApiModelProperty("路径信息id")
    private Integer pathInfoId;

    @ApiModelProperty("源地址")
    private String srcIp;

    @ApiModelProperty("目的地址")
    private String dstIp;

    @ApiModelProperty("服务")
    private List<ServiceDTO> service;

    @ApiModelProperty("开始时间")
    private Date startTime;

    @ApiModelProperty("结束时间")
    private Date endTime;

    @ApiModelProperty("源域")
    private String srcZone;

    @ApiModelProperty("目的域")
    private String dstZone;

    @ApiModelProperty("入接口")
    private String inDevIf;

    @ApiModelProperty("出接口")
    private String outDevIf;

    @ApiModelProperty("老化时间")
    private Integer idleTimeout;
}
