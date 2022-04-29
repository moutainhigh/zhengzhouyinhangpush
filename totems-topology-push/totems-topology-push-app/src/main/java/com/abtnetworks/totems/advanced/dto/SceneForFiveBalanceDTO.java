package com.abtnetworks.totems.advanced.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotNull;
import java.util.Date;


/**
* 策略生成F5负载场景实体类实体
*
* @author lifei
* @since 2021年07月30日
*/
@ApiModel("策略生成F5负载场景DTO")
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SceneForFiveBalanceDTO {

    @ApiModelProperty("主键")
    private Integer id;

    @ApiModelProperty("场景uuid")
    private String sceneUuid;

    @NotNull(message = "场景名称不能为空")
    @Length(max = 128 ,message = "场景名称长度不超过128")
    @ApiModelProperty("场景名称")
    private String sceneName;

    @NotNull(message = "应用发布类型不能为空")
    @Length(max = 64 ,message = "应用发布类型长度不超过64")
    @ApiModelProperty("应用发布类型")
    private String applyType;

    @NotNull(message = "节点负载模式不能为空")
    @Length(max = 64 ,message = "应用发布类型长度不超过64")
    @ApiModelProperty("节点负载模式")
    private String loadBlanaceMode;

    @Length(max = 64 ,message = "节点回话保持长度不超过64")
    @ApiModelProperty("节点回话保持")
    private String persist;

    @ApiModelProperty("健康检查")
    private String monitor;

    @ApiModelProperty("备注")
    private String mark;

    @ApiModelProperty("创建人")
    private String createUser;

    @ApiModelProperty("创建时间")
    private Date createTime;

    @ApiModelProperty("描述")
    private String description;

    @ApiModelProperty("页面大小")
    private Integer pageSize;

    @ApiModelProperty("当前页")
    private Integer currentPage;


}