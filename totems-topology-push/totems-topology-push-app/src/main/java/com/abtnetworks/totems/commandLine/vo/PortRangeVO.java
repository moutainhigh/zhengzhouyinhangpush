package com.abtnetworks.totems.commandLine.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @Author: WangCan
 * @Description
 * @Date: 2021/8/10
 */
@ApiModel("原子化命令行HTTP接口>>范围端口")
@Data
public class PortRangeVO {

    @ApiModelProperty("开始端口")
    private Integer start;

    @ApiModelProperty("结束端口")
    private Integer end;

    @ApiModelProperty("超时时间")
    private Long idleTimeout;

    public PortRangeVO() {
    }

    public PortRangeVO(Integer start, Integer end) {
        this.start = start;
        this.end = end;
    }
}
