package com.abtnetworks.totems.issued.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author lifei
 * @desc py下发DTO
 * @date 2022/1/20 17:41
 */
@Data
public class PythonPushDTO {

    @ApiModelProperty("文件路径")
    private String filePath;

    @ApiModelProperty("文件名称")
    private String pythonFileName;

    @ApiModelProperty("备用设备信息")
    private StandbyDeviceInfoDTO standbyDeviceInfoDTO;
}
