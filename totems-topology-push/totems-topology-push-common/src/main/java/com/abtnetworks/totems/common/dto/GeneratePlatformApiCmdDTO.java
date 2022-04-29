package com.abtnetworks.totems.common.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @desc    管理平台命令行生成数据对象
 * @author liuchanghao
 * @date 2021-02-23 09:21
 */
@Data
@ApiModel("管理平台命令行生成数据对象")
public class GeneratePlatformApiCmdDTO {

    @ApiModelProperty("任务相关数据")
    TaskDTO task = new TaskDTO();

    @ApiModelProperty("设备信息")
    DeviceDTO device = new DeviceDTO();

    @ApiModelProperty("策略数据")
    PolicyDTO policy = new PolicyDTO();

    @ApiModelProperty("高级设置")
    SettingDTO setting = new SettingDTO();

}
