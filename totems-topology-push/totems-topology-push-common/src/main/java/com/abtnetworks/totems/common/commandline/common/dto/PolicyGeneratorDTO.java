package com.abtnetworks.totems.common.commandline.common.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author lifei
 * @desc 策略DTO(包括 策略命令行,回滚策略命令行,回滚对象命令行)
 * @date 2021/7/8 14:29
 */
@Data
public class PolicyGeneratorDTO {

    @ApiModelProperty("策略命令行")
    private String policyCommandLine;

    @ApiModelProperty("策略回滚命令行")
    private String policyRollbackCommandLine;

    @ApiModelProperty("对象回滚命令行")
    private String objectRollbackCommandLine;

}
