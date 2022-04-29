package com.abtnetworks.totems.common.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author lifei
 * @desc 命令行业务信息返回参数
 * @date 2021/5/7 20:32
 */
@Data
@ApiModel("命令行业务信息字段")
public class CommandLineBusinessInfoDTO {

    @ApiModelProperty("acl策略ruleId不够信息(错误信息拼接到命令行里面 错误命令行正常生成)")
    private String  ruleIdNotEnoughMsg;

    @ApiModelProperty("其他错误信息(直接提示在页面不生成命令行)")
    private String  otherErrorMsg;

    @ApiModelProperty("计算出来的该设备需要生成的策略数量")
    private Integer policyNums;


}
