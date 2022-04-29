package com.abtnetworks.totems.mapping.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;


@Data
@ApiModel("路由查询VO")
public class AutoMappingRouteSearchVO {

    @ApiModelProperty("申请主题（工单号）")
    private String theme;

    @ApiModelProperty("描述")
    private String description;

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

    @ApiModelProperty("任务类型")
    private Integer taskType;

    @ApiModelProperty("静态路由信息")
    private String staticRoutingInfo;

    /**
     * 页数
     */
    private Integer page = 1;

    /**
     * 每页条数
     */
    private Integer limit = 20;

}