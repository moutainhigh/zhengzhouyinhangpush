package com.abtnetworks.totems.remote.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * @Author: Administrator
 * @Date: 2020/4/24
 * @desc: 请写类注释
 */
@ApiModel("服务")
public class DredgeServiceDTO {
    @ApiModelProperty("目的端口")
    private String dstPorts;
    @ApiModelProperty("协议")
    private String protocol;
    @ApiModelProperty("是否any（预留字段，不需要传值）")
    private String isAny;

    @ApiModelProperty("是否all（预留字段，不需要传值）")
    private String isAll;

    public String getIsAll() {
        return isAll;
    }

    public void setIsAll(String isAll) {
        this.isAll = isAll;
    }

    public String getIsAny() {
        return isAny;
    }

    public void setIsAny(String isAny) {
        this.isAny = isAny;
    }

    public String getDstPorts() {
        return dstPorts;
    }

    public void setDstPorts(String dstPorts) {
        this.dstPorts = dstPorts;
    }

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    @Override
    public String toString() {
        return super.toString();
    }


}
