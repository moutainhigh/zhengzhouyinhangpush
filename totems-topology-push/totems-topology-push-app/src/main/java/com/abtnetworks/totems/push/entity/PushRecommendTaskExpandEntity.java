package com.abtnetworks.totems.push.entity;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;


/**
 * F5策略生成表实体
 *
 * @author lifei
 * @since 2021年08月02日
 */
@Data
public class PushRecommendTaskExpandEntity {

    @ApiModelProperty("主键")
    private Integer id;

    @ApiModelProperty("工单id")
    private Integer taskId;

    @ApiModelProperty("设备id")
    private String deviceUuid;

    @ApiModelProperty("设备名称")
    private String deviceName;

    @ApiModelProperty("场景uuid")
    private String sceneUuid;

    @ApiModelProperty("场景名称")
    private String sceneName;

    @ApiModelProperty("pool信息")
    private String poolInfo;

    @ApiModelProperty("snat类型")
    private Integer snatType;

    @ApiModelProperty("snatPool信息")
    private String snatPoolInfo;

    @ApiModelProperty("http_profile")
    private String httpProfile;

    @ApiModelProperty("证书名称")
    private String sslProfile;

    @ApiModelProperty("任务类型 19：F5botnNat  18：F5dnat")
    private Integer taskType;

    @ApiModelProperty("备注")
    private String mark;

    @ApiModelProperty("创建人")
    private String createUser;

    @ApiModelProperty("创建时间")
    private Date createTime;

    @ApiModelProperty("修改时间")
    private Date updateTime;

    @ApiModelProperty("静态路由所需信息")
    private String staticRoutingInfo;

}