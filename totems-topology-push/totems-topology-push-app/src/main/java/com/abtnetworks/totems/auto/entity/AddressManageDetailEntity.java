package com.abtnetworks.totems.auto.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

/**
 * @Description 对象管理详情
 * @Version --
 * @Created by zhoumuhua on '2021-07-27 14:44:39'.
 */
@Data
@ApiModel("对象管理详情表")
public class AddressManageDetailEntity {

    @ApiModelProperty(value = "主键")
    private Integer id;

    @ApiModelProperty(value = "uuid")
    private String uuid;

    @ApiModelProperty(value = "对象管理任务id")
    private Integer taskId;

    @ApiModelProperty(value = "地址名称")
    private String addressName;

    @ApiModelProperty(value = "场景id")
    private String scenesUuid;

    @ApiModelProperty(value = "对象级别,0顶级,1一级,2二级,3三级地址对象")
    private Integer addressLevel;

    @ApiModelProperty(value = "上级对象id")
    private Integer parentId;

    @ApiModelProperty(value = "状态 --> AddressStatusEnum")
    private Integer status;

    @ApiModelProperty(value = "删除值")
    private String addressDel;

    @ApiModelProperty(value = "新增值")
    private String addressAdd;

    @ApiModelProperty(value = "创建时间")
    private Date createTime;

    @ApiModelProperty(value = "下发id")
    private String pushId;

    @ApiModelProperty(value = "地址对象类型 --> DeviceObjectTypeEnum")
    private String addressType;

}
