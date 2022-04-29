package com.abtnetworks.totems.issued.dto;

import io.swagger.annotations.ApiModelProperty;

/**
 * @author Administrator
 * @Title:
 * @Description: 下发特例参数提供
 * @date 2020/8/19
 */
public class SpecialParamDTO {


    @ApiModelProperty("checkPoint策略包名称")
    String policyPackage;
    @ApiModelProperty("checkPointMGMT服务管理ip或者深信服防火墙采集路径")
    String webUrl;
    @ApiModelProperty("checkpoint集群名")
    String cpMiGatewayClusterName;
    @ApiModelProperty("山石回滚命令行根据策略名称还是策略id，true：策略名称，false：策略id")
    Boolean rollbackType = true;

    public String getCpMiGatewayClusterName() {
        return cpMiGatewayClusterName;
    }

    public void setCpMiGatewayClusterName(String cpMiGatewayClusterName) {
        this.cpMiGatewayClusterName = cpMiGatewayClusterName;
    }

    public String getWebUrl() {
        return webUrl;
    }

    public void setWebUrl(String webUrl) {
        this.webUrl = webUrl;
    }


    public String getPolicyPackage() {
        return policyPackage;
    }

    public void setPolicyPackage(String policyPackage) {
        this.policyPackage = policyPackage;
    }

    public Boolean getRollbackType() {
        return rollbackType;
    }

    public void setRollbackType(Boolean rollbackType) {
        this.rollbackType = rollbackType;
    }

    @Override
    public String toString() {
        return super.toString();
    }
}
