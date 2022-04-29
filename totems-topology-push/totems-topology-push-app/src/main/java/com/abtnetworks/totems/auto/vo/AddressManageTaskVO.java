package com.abtnetworks.totems.auto.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * @Description 对象管理任务
 * @Version --
 * @Created by zhoumuhua on '2021-07-27 11:45:15'.
 */
@Data
@ApiModel("对象管理任务表")
public class AddressManageTaskVO {

    @ApiModelProperty(value = "主键")
    private Integer id;

    @ApiModelProperty(value = "uuid")
    private String uuid;

    @ApiModelProperty(value = "初始化类型--new新建，init初始化")
    private String initCategory;

    @ApiModelProperty(value = "地址类型--NETWORK_OBJECT地址对象，NETWORK_GROUP_OBJECT地址组对象，ADDRESS地址条目")
    private String addressCategory;

    @ApiModelProperty(value = "地址名称")
    private String addressName;

    @ApiModelProperty(value = "地址内容")
    private String addressEntry;

    @ApiModelProperty(value = "场景id")
    private String scenesUuid;

    @ApiModelProperty(value = "场景名称")
    private String scenesName;

    @ApiModelProperty(value = "设备id")
    private String deviceUuid;

    @ApiModelProperty(value = "申请人")
    private String userName;

    @ApiModelProperty(value = "申请描述")
    private String description;

    @ApiModelProperty(value = "创建时间")
    private Date createTime;

    @ApiModelProperty("地址校验id集合")
    private List<Integer> idList;


    /**
     * 页数
     */
    private Integer page = 1;

    /**
     * 每页条数
     */
    private Integer limit = 20;
}
