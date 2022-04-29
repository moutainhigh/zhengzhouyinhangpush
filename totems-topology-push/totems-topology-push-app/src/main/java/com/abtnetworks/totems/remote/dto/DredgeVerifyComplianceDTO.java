package com.abtnetworks.totems.remote.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @Author: Administrator
 * @Date: 2020/4/24
 * @desc: 请写类注释
 */
@ApiModel("合规检查校验开通入参模型")
@Data
public class DredgeVerifyComplianceDTO {
    @ApiModelProperty("源地址ip")
    private String srcIp;
    @ApiModelProperty("目的地址ip")
    private String dstIp;
    @ApiModelProperty("服务对象")
    private List<DredgeServiceDTO> serviceList;

    @ApiModelProperty("源区域id")
    private String srcDistrictId;
    @ApiModelProperty("目的区域id")
    private String dstDistrictId;


    public String getSrcIp() {
        return srcIp;
    }

    public void setSrcIp(String srcIp) {
        this.srcIp = srcIp;
    }

    public String getDstIp() {
        return dstIp;
    }

    public void setDstIp(String dstIp) {
        this.dstIp = dstIp;
    }

    public List<DredgeServiceDTO> getServiceList() {
        return serviceList;
    }

    public void setServiceList(List<DredgeServiceDTO> serviceList) {
        this.serviceList = serviceList;
    }

    @Override
    public String toString() {
        return super.toString();
    }
}
