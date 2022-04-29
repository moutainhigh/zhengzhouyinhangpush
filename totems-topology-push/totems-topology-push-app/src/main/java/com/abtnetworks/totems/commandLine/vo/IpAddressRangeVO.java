package com.abtnetworks.totems.commandLine.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @Author: WangCan
 * @Description
 * @Date: 2021/8/10
 */
@ApiModel("原子化命令行HTTP接口>>范围IP")
@Data
public class IpAddressRangeVO {

    @ApiModelProperty("开始IP")
    private String start;

    @ApiModelProperty("结束IP")
    private String end;
}
