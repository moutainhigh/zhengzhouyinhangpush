package com.abtnetworks.totems.credential.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;


/**
 * @author Administrator
 * @Title:
 * @Description: 请写注释类
 * @date 2020/11/25
 */
@ApiModel("凭据批量")
@Data
public class UpdateCredentialDTO {

    @ApiModelProperty("唯一uuid")
    private String uuid;
    @ApiModelProperty("组")
    private String branchLevel;


}
