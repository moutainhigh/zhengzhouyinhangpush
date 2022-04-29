package com.abtnetworks.totems.auto.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

/**
 * @Description 对象管理地址条目表
 * @Version --
 * @Created by zhoumuhua on '2021-10-28'.
 */
@Data
@ApiModel("对象管理地址条目表")
public class AddressDetailEntryEntity {

    @ApiModelProperty(value = "主键")
    private Integer id;

    @ApiModelProperty(value = "uuid")
    private String uuid;

    @ApiModelProperty(value = "对象管理详情id")
    private Integer detailId;

    @ApiModelProperty(value = "对象管理任务id")
    private Integer taskId;

    @ApiModelProperty(value = "地址内容")
    private String addressName;

    @ApiModelProperty(value = "地址对象类型")
    private String addressType;

    @ApiModelProperty(value = "创建时间")
    private Date createTime;

}