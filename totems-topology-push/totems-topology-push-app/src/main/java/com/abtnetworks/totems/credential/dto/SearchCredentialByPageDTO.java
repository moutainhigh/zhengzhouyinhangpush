package com.abtnetworks.totems.credential.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author Administrator
 * @Title:
 * @Description: 请写注释类
 * @date 2020/11/23
 */
@ApiModel("查询节点配置")
@Data
public class SearchCredentialByPageDTO {
    @ApiModelProperty("当前页")
    private  Integer pageIndex;
    @ApiModelProperty("页面大小")
    private  Integer pageSize;
    @ApiModelProperty("页面凭据名")
    private String name;
    @ApiModelProperty("组")
    private String branchLevel;
    @ApiModelProperty("是否选项框")
    private Boolean selectBox;

}
