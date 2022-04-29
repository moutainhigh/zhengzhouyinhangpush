package com.abtnetworks.totems.issued.dto;

import com.abtnetworks.totems.common.enums.DeviceModelNumberEnum;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author lifei
 * @desc 备用设备信息DTO
 * @date 2022/1/20 17:49
 */
@Data
public class StandbyDeviceInfoDTO {

    @ApiModelProperty("设备类型枚举")
    DeviceModelNumberEnum deviceModelNumberEnum;

    @ApiModelProperty("设备登陆用户")
    String username;

    @ApiModelProperty("设备登陆密码")
    String password;

    @ApiModelProperty("设备登陆二次验证用户")
    String enableUsername;

    @ApiModelProperty("设备登陆二次验证密码")
    String enablePassword;

    @ApiModelProperty("设备ip")
    String deviceManagerIp;

    @ApiModelProperty("设备名称")
    String deviceName;

    @ApiModelProperty("设备连接端口")
    Integer port;

    @ApiModelProperty("连接设备的协议类型  如ssh和telnet")
    String executorType;

    @ApiModelProperty("凭据名称")
    String credentialName;

    @ApiModelProperty("编码格式")
    String charset;

    @ApiModelProperty("间隔时间")
    Integer interval;

    @ApiModelProperty("支持移动策略")
    MoveParamDTO moveParamDTO;

    @ApiModelProperty("是否虚设备")
    Boolean isVSys ;

    @ApiModelProperty("虚墙名称")
    String vSysName;
}
