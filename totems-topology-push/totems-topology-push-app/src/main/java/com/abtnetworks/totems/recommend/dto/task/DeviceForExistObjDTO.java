package com.abtnetworks.totems.recommend.dto.task;

import com.abtnetworks.totems.common.enums.DeviceModelNumberEnum;
import com.abtnetworks.totems.common.enums.PolicyEnum;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author Administrator
 * @Title:
 * @Description: 请写注释类
 * @date 2020/12/23
 */
@Data
public class DeviceForExistObjDTO {

    @ApiModelProperty("设备UUID")
    String deviceUuid;

    @ApiModelProperty("设备型号")
    DeviceModelNumberEnum modelNumber;

    @ApiModelProperty("策略类型")
    PolicyEnum policyType;
}
