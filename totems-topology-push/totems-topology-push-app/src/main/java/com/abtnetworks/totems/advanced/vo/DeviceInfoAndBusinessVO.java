package com.abtnetworks.totems.advanced.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @author lifei
 * @desc 设备和业务参数VO
 * @date 2022/1/21 14:15
 */
@Data
public class DeviceInfoAndBusinessVO {

    @ApiModelProperty("设备信息集合")
    List<DeviceInfoVO> deviceInfoVOList;

    @ApiModelProperty("文件名称")
    String fileName;
}
