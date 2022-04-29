package com.abtnetworks.totems.issued.dto;

import com.abtnetworks.totems.common.enums.DeviceModelNumberEnum;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author zakyoung
 * @Title:
 * @Description: 请写注释类
 * @date 2020-03-15
 */
@Data
@ApiModel("远程连接登陆用户的信息")
public class RemoteConnectUserDTO {
    @ApiModelProperty("ip或者hostname")
    private String deviceManagerIp;
    @ApiModelProperty("用户名")
    private String username;
    @ApiModelProperty("密码")
    private String password;
    @ApiModelProperty("端口")
    private Integer port;
    @ApiModelProperty("连接超时时间")
    private Integer timeOut;
    @ApiModelProperty("连接类型例如ssh，telnet")
    private String executorType;

    @ApiModelProperty("设备型号")
    DeviceModelNumberEnum deviceModelNumberEnum;
    @ApiModelProperty("设备字符集")
    private String charset;
    @ApiModelProperty("间隔时间")
    private Integer interval;
}
