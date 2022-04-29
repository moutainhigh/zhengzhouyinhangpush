package com.abtnetworks.totems.disposal.dto;

import com.abtnetworks.totems.common.tools.excel.ExcelField;
import com.abtnetworks.totems.common.dto.commandline.ServiceDTO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @author luwei
 * @date 2020/1/8
 */
@ApiModel(value = "导入策略白名单")
@Data
public class ImportPolicyWhiteListDTO {

    @ApiModelProperty(value = "名称")
    private String name;

    @ApiModelProperty(value = "是否IPv6")
    private String ipType;

    @ApiModelProperty(value = "源地址")
    private String srcIp;

    @ApiModelProperty(value = "校验转换后的源地址")
    private String srcIpText;

    @ApiModelProperty(value = "目的地址")
    private String dstIp;

    @ApiModelProperty(value = "校验转换后的目的地址")
    private String dstIpText;

    @ApiModelProperty(value = "服务")
    private String service;

    @ApiModelProperty(value = "校验转换后的服务")
    List<ServiceDTO> serviceList;

    @ApiModelProperty(value = "备注")
    private String remarks;

    @ExcelField(title = "名称", type = 2, align = 2, sort = 10)
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @ExcelField(title = "IP类型", type = 2, align = 2, sort = 20)
    public String getIpType() {
        return ipType;
    }

    public void setIpType(String ipType) {
        this.ipType = ipType;
    }

    @ExcelField(title = "源地址", type = 2, align = 2, sort = 30)
    public String getSrcIp() {
        return srcIp;
    }

    public void setSrcIp(String srcIp) {
        this.srcIp = srcIp;
    }

    @ExcelField(title = "目的地址", type = 2, align = 2, sort = 40)
    public String getDstIp() {
        return dstIp;
    }

    public void setDstIp(String dstIp) {
        this.dstIp = dstIp;
    }

    @ExcelField(title = "服务", type = 2, align = 2, sort = 50)
    public String getService() {
        return service;
    }

    public void setService(String service) {
        this.service = service;
    }

    @ExcelField(title = "备注", type = 2, align = 2, sort = 60)
    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }


}
