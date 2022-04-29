package com.abtnetworks.totems.push.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

/**
 * @desc    下发封禁dto（封禁业务下发特殊处理）
 * @author liuchanghao
 * @date 2020-09-12 15:58
 */
@Data
public class PushForbidDTO {

    @ApiModelProperty("是否拼接命令行")
    private Boolean isAppendCommandLine;

}
