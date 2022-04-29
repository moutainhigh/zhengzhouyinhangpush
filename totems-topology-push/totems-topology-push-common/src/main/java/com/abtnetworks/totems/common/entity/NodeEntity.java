package com.abtnetworks.totems.common.entity;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @Author: wenjiachang
 * @Date: 2018/12/18 11:07
 */
@Data
public class NodeEntity {

    int id;

    @ApiModelProperty("设备UUID")
    String uuid;

    @ApiModelProperty("设备ip")
    private String ip;

    @ApiModelProperty("厂商名称")
    private String vendorName;

    @ApiModelProperty("厂商英文名称")
    private String vendorId;

    @ApiModelProperty("设备名称")
    private String deviceName;

    @ApiModelProperty("设备型号")
    private String modelNumber;

    @ApiModelProperty("设备端口")
    int portNumber;

    private Byte state;

    @ApiModelProperty("采集id")
    String gatherId;

    @ApiModelProperty("设备凭证uuid")
    String credentialUuid;

    @ApiModelProperty("设备来源：1手工导入2采集")
    int origin;

    @ApiModelProperty("设备类型，0表示防火墙")
    String type;

    @ApiModelProperty("控制器类型")
    String controllerId;
    @ApiModelProperty("字符编码")
    private String charset;
    @ApiModelProperty("深信服与checkPoint地址标记")
    private String webUrl;
    @ApiModelProperty("下发凭据uuid")
    private String pushCredentialUuid;
}
