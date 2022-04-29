package com.abtnetworks.totems.common.dto;

import com.abtnetworks.totems.common.enums.DeviceObjectTypeEnum;
import io.swagger.annotations.ApiModel;
import lombok.Data;

/**
 * 思科8.6版本 生成命令行时，区分引用的是地址对象还是地址组对象，服务同理
 * @author luwei
 * @date 2020/7/21
 */
@Data
@ApiModel("引用对象信息")
public class RefObjectDTO {

    private String refName;

    private DeviceObjectTypeEnum objectTypeEnum;
}
