package com.abtnetworks.totems.mapping.entity;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;
/**
 * @desc    场景规则表实体
 * @author liuchanghao
 * @date 2022-01-21 10:31
 */
@Data
public class PushAutoMappingSceneRuleEntity {

    @ApiModelProperty("主键id")
    private Integer id;

    @ApiModelProperty("UUID")
    private String uuid;

    @ApiModelProperty("规则名称")
    private String ruleName;

    @ApiModelProperty("规则类型,类型可多选（0：源nat多对一；1：源nat一对一；2：目的nat一对一；3：静态路由；）")
    private String ruleType;

    @ApiModelProperty("源IP组")
    private String srcIp;

    @ApiModelProperty("目的IP组")
    private String dstIp;

    @ApiModelProperty("创建人")
    private String createUser;

    @ApiModelProperty("创建时间")
    private Date createTime;

    @ApiModelProperty("更新人")
    private String updateUser;

    @ApiModelProperty("更新时间")
    private Date updateTime;

    @ApiModelProperty("描述")
    private String description;


}