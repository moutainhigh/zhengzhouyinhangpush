package com.abtnetworks.totems.translation.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * @Author: WangCan
 * @Description 预定义服务对象
 * @Date: 2021/8/27
 */
@ApiModel("预定义服务对象")
public class PredefinedService {

    private Integer id;

    @ApiModelProperty(value = "协议")
    private String protocol;

    @ApiModelProperty(value = "源端口/类型")
    private String sourcePortType;

    @ApiModelProperty(value = "目的端口/code")
    private String destinationPortCode;

    @ApiModelProperty(value = "端口")
    private String port;

    @ApiModelProperty(value = "各厂家服务对象名")
    private String venderObjName;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public String getSourcePortType() {
        return sourcePortType;
    }

    public void setSourcePortType(String sourcePortType) {
        this.sourcePortType = sourcePortType;
    }

    public String getDestinationPortCode() {
        return destinationPortCode;
    }

    public void setDestinationPortCode(String destinationPortCode) {
        this.destinationPortCode = destinationPortCode;
    }

    public String getPort() {
        return port;
    }

    public void setPort(String port) {
        this.port = port;
    }

    public String getVenderObjName() {
        return venderObjName;
    }

    public void setVenderObjName(String venderObjName) {
        this.venderObjName = venderObjName;
    }
}
