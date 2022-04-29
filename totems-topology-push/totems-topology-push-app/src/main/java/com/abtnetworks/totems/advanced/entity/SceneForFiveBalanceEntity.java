package com.abtnetworks.totems.advanced.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;



/**
* 策略生成F5负载场景实体类实体
*
* @author lifei
* @since 2021年07月30日
*/
@ApiModel("策略生成F5负载场景实体类实体")
@Data
public class SceneForFiveBalanceEntity {

    @ApiModelProperty("主键")
    private Integer id;

    @ApiModelProperty("场景uuid")
    private String sceneUuid;

    @ApiModelProperty("场景名称")
    private String sceneName;

    @ApiModelProperty("应用发布类型")
    private String applyType;

    @ApiModelProperty("节点负载模式")
    private String loadBlanaceMode;

    @ApiModelProperty("节点回话保持")
    private String persist;

    @ApiModelProperty("健康检查")
    private String monitor;

    @ApiModelProperty("备注")
    private String mark;

    @ApiModelProperty("创建人")
    private String createUser;

    @ApiModelProperty("描述")
    private String description;

    @ApiModelProperty("创建时间")
    private Date createTime;

    @ApiModelProperty("修改时间")
    private Date updateTime;

}