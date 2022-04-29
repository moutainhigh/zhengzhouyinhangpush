package com.abtnetworks.totems.common.dto.generate;

import com.abtnetworks.totems.common.enums.DeviceNetworkTypeEnum;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author Administrator
 * @Title:
 * @Description: 请写注释类
 * @date 2021/3/1
 */
@Api("存在的地址对象")
@Data
public class ExistAddressObjectDTO {
    @ApiModelProperty("对象类型：服务，地址，地址组")
    private String deviceObjectType;
    @ApiModelProperty("对象细分类类型：地址池，地址池组")
    private DeviceNetworkTypeEnum deviceNetworkTypeEnum;
    @ApiModelProperty("复用对象名")
    private String existName;
}
