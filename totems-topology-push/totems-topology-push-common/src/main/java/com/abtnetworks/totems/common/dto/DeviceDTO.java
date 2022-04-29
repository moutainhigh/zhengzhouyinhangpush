package com.abtnetworks.totems.common.dto;

import com.abtnetworks.totems.common.enums.DeviceModelNumberEnum;
import com.abtnetworks.totems.common.entity.NodeEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel("设备数据")
public class DeviceDTO {
    @ApiModelProperty("设备UUID")
    String deviceUuid;

    @ApiModelProperty("设备型号")
    DeviceModelNumberEnum modelNumber;

    @ApiModelProperty("是否有虚墙")
    boolean hasVsys;

    @ApiModelProperty("是否为虚墙")
    boolean isVsys;

    @ApiModelProperty("虚墙名称")
    String vsysName;

    @ApiModelProperty("策略列表UUID")
    String ruleListUuid;

    @ApiModelProperty("设备信息")
    NodeEntity nodeEntity;

    @ApiModelProperty("是否灾备设备")
    Boolean isDisasterDevice;

    @ApiModelProperty("相关策略集名称")
    private String ruleListName;

    @ApiModelProperty("匹配到的ruleId")
    private String matchRuleId;

}
