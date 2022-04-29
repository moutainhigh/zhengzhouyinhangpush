package com.abtnetworks.totems.recommend.dto.task;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

@Data
public class NatInfoDTO {

    @ApiModelProperty("NAT位置")
    private String natField;

    @ApiModelProperty("NAT类型")
    private String natType;

    @ApiModelProperty("转换前地址对象名称")
    public List<String> preNatName;

    @ApiModelProperty("转换前对象内容")
    private List<String> preNatValue;

    @ApiModelProperty("转换前排除地址")
    private List<String> preNatExclusive;

    @ApiModelProperty("转换后地址对象名称")
    private List<String> postNatName;

    @ApiModelProperty("转换后对象内容")
    private List<String> postNatValue;

    @ApiModelProperty("转换后排除地址")
    private List<String> postNatExclusive;
}
