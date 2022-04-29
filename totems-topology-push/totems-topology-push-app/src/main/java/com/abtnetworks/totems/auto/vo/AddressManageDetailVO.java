package com.abtnetworks.totems.auto.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @Description 对象管理详情
 * @Version --
 * @Created by zhoumuhua on '2021-07-27 14:44:39'.
 */
@Data
@ApiModel("对象管理详情表")
public class AddressManageDetailVO {

    @ApiModelProperty(value = "主键")
    private Integer id;

    @ApiModelProperty("地址校验id集合")
    private List<Integer> idList;

    @ApiModelProperty(value = "uuid")
    private String uuid;

    @ApiModelProperty(value = "对象管理任务id")
    private Integer taskId;

    @ApiModelProperty(value = "地址名称")
    private String addressName;

    @ApiModelProperty(value = "地址内容")
    private String addressEntry;

    @ApiModelProperty(value = "前端传的字段：地址类型--NETWORK_OBJECT地址对象，NETWORK_GROUP_OBJECT地址组对象，ADDRESS地址条目")
    private String addressCategory;

    @ApiModelProperty(value = "场景id")
    private String scenesUuid;

    @ApiModelProperty(value = "场景名称")
    private String scenesName;

    @ApiModelProperty(value = "对象级别,0顶级,1一级,2二级,3三级地址对象")
    private Integer addressLevel;

    @ApiModelProperty(value = "上级对象id")
    private Integer parentId;

    @ApiModelProperty(value = "所属组")
    private String parentName;

    @ApiModelProperty(value = "状态 --> AddressStatusEnum")
    private Integer status;

    @ApiModelProperty(value = "命令行及下发状态 --> AutoRecommendStatusEnum")
    private Integer pushStatus;

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

    @ApiModelProperty(value = "子级地址对象")
    private List<AddressManageDetailVO> child = new ArrayList<>();

}
