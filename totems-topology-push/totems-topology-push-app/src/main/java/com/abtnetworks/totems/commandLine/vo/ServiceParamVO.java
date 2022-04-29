package com.abtnetworks.totems.commandLine.vo;

import com.abtnetworks.totems.command.line.dto.PortRangeDTO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @Author: WangCan
 * @Description
 * @Date: 2021/8/10
 */
@ApiModel("原子化命令行HTTP接口>>服务详情")
@Data
public class ServiceParamVO {

    @ApiModelProperty("协议code")
    Integer protocolTypeEnumCode;

    @ApiModelProperty("协议附加type")
    String[] protocolAttachTypeArray;

    @ApiModelProperty("协议附加code")
    String[] protocolAttachCodeArray;

    @ApiModelProperty("源端口： 数字 单个端口类型")
    Integer[] srcSinglePortArray;

    @ApiModelProperty("源端口： Str 单个端口类型")
    String[] srcSinglePortStrArray;

    @ApiModelProperty("源端口：数字 范围端口类型")
    PortRangeVO[] srcRangePortVoArray;

    @ApiModelProperty("目的端口： 数字 单个端口类型")
    Integer[] dstSinglePortArray;

    @ApiModelProperty("目的端口： Str 单个端口类型")
    String[] dstSinglePortStrArray;

    @ApiModelProperty("目的端口：数字 范围端口类型")
    PortRangeVO[] dstRangePortVoArray;

    @ApiModelProperty("超时时间")
    String[] timeOutArray;
}
