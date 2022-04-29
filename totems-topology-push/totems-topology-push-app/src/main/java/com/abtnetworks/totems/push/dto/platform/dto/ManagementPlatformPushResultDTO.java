package com.abtnetworks.totems.push.dto.platform.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @desc    管理平台下发结果
 * @author liuchanghao
 * @date 2021-02-24 11:16
 */
@Data
@ApiModel("管理平台下发结果")
public class ManagementPlatformPushResultDTO {

    @ApiModelProperty("策略包名称")
    String packageName;

    @ApiModelProperty("飞塔install响应数据")
    String fortinetInstallResponse;

    public ManagementPlatformPushResultDTO(String packageName, String fortinetInstallResponse) {
        this.packageName = packageName;
        this.fortinetInstallResponse = fortinetInstallResponse;
    }
}