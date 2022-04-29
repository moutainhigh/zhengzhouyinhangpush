package com.abtnetworks.totems.common.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class IpAddressDTO {

    @ApiModelProperty("起始ip")
    String start;

    @ApiModelProperty("结束ip")
    String end;
}
