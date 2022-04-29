package com.abtnetworks.totems.mapping.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;


@Data
public class AutoMappingSceneRuleVO {

    @ApiModelProperty("主键id")
    private Integer id;

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

    @ApiModelProperty("描述")
    private String description;

    /**
     * 页数
     */
    private Integer page = 1;

    /**
     * 每页条数
     */
    private Integer limit = 20;

}
