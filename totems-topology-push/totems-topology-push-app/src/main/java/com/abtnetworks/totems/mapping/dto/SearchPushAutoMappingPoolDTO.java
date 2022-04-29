package com.abtnetworks.totems.mapping.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author liuchanghao
 * @desc
 * @date 2022-02-16 10:46
 */
@ApiModel("搜索关联信息实体")
@Data
public class SearchPushAutoMappingPoolDTO {
    @ApiModelProperty("页面大小")
    private Integer pageSize;
    @ApiModelProperty("当前页")
    private Integer currentPage;
    @ApiModelProperty("设备名称")
    private String deviceName;
    @ApiModelProperty("转换前ip")
    private String preIp;
    @ApiModelProperty("转换后ip")
    private String postIp;
}
