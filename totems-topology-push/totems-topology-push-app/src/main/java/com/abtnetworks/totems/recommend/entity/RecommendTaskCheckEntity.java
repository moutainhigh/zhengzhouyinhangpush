package com.abtnetworks.totems.recommend.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

@Data
@ApiModel("工单任务对象")
public class RecommendTaskCheckEntity {
    @ApiModelProperty("主键id")
    private Integer id;

    @ApiModelProperty("工单名称")
    private String orderNumber;

    @ApiModelProperty("任务id")
    private String taskId;

    @ApiModelProperty("工单状态")
    private Integer status;

    @ApiModelProperty("工单文件名称")
    private String fileName;

    @ApiModelProperty("工单创建时间")
    private Date createTime;

    @ApiModelProperty("工单创建人")
    private String userName;

    @ApiModelProperty("批量工单类型：0，Api接口提交；1，本地上传")
    private Integer batchType;

    @ApiModelProperty("工单回传结果")
    private String result;
}