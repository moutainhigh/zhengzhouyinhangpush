package com.abtnetworks.totems.mapping.entity;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

/**
 * @desc    IP匹配表实体
 * @author liuchanghao
 * @date 2022-01-21 10:31
 */

@Data
public class PushAutoMappingIpEntity {

    @ApiModelProperty("主键id")
    private Integer id;

    @ApiModelProperty("UUID")
    private String uuid;

    @ApiModelProperty("工单类型（0：源nat；1：目的nat；）")
    private Integer natType;

    @ApiModelProperty("ip地址，查询使用")
    private String ip;

    @ApiModelProperty("转换前地址")
    private String preIp;

    @ApiModelProperty("转换后地址")
    private String postIp;

    @ApiModelProperty("设备uuid")
    private String deviceUuid;

    @ApiModelProperty("设备IP")
    private String deviceIp;

    @ApiModelProperty("设备名称")
    private String deviceName;

    @ApiModelProperty("引用地址池ID")
    private Integer mappingNatId;

    @ApiModelProperty("创建人")
    private String createUser;

    @ApiModelProperty("创建时间")
    private Date createTime;

    @ApiModelProperty("更新人")
    private String updateUser;

    @ApiModelProperty("更新时间")
    private Date updateTime;


}