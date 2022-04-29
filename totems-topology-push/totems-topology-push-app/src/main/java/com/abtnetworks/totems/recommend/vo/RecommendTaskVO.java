package com.abtnetworks.totems.recommend.vo;

import com.abtnetworks.totems.common.dto.commandline.ServiceDTO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
@ApiModel("策略开通任务显示数据对象")
public class RecommendTaskVO {
    @ApiModelProperty("主键")
    private Integer id;

    @ApiModelProperty("主题（工单号）")
    private String theme;

    @ApiModelProperty("流水号")
    private String orderNumber;

    @ApiModelProperty("用户名")
    private String userName;

    @ApiModelProperty("描述")
    private String description;

    @ApiModelProperty("源地址")
    private String srcIp;

    @ApiModelProperty("目的地址")
    private String dstIp;

    @ApiModelProperty("服务列表")
    private List<ServiceDTO> serviceList;

    @ApiModelProperty("创建时间")
    private Date createTime;

    @ApiModelProperty("开始时间")
    private Date startTime;

    @ApiModelProperty("结束时间")
    private Date endTime;

    @ApiModelProperty("任务状态")
    private Integer status;

    @ApiModelProperty("任务类型")
    private Integer taskType;
}
