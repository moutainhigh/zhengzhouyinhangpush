package com.abtnetworks.totems.auto.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

/**
 * @Description 对象管理任务
 * @Version --
 * @Created by zhoumuhua on '2021-07-27 11:45:15'.
 */
@Data
@ApiModel("对象管理任务表")
public class AddressManageTaskEntity {

    @ApiModelProperty(value = "主键")
    private Integer id;

    @ApiModelProperty(value = "uuid")
    private String uuid;

    @ApiModelProperty(value = "地址名称")
    private String addressName;

    @ApiModelProperty(value = "场景id")
    private String scenesUuid;

    @ApiModelProperty(value = "申请人")
    private String userName;

    @ApiModelProperty(value = "申请描述")
    private String description;

    @ApiModelProperty(value = "创建时间")
    private Date createTime;

    @ApiModelProperty(value = "场景名称")
    private String scenesName;
}
