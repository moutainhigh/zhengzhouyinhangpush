package com.abtnetworks.totems.disposal.dto;

import com.abtnetworks.totems.common.tools.excel.ExcelField;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author luwei
 * @date 2020/1/8
 */
@ApiModel(value = "导入路由白名单")
@Data
public class ImportRoutWhiteListDTO {

    @ApiModelProperty(value = "名称")
    private String name;

    @ApiModelProperty(value = "黑洞路由IP地址")
    private String routingIp;

    @ApiModelProperty(value = "校验转换后的黑洞路由IP")
    private String routingIpText;


    @ApiModelProperty(value = "备注")
    private String remarks;

    @ExcelField(title = "名称", type = 2, align = 2, sort = 10)
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @ExcelField(title = "黑洞路由IP", type = 2, align = 2, sort = 20)
    public String getRoutingIp() {
        return routingIp;
    }

    public void setRoutingIp(String routingIp) {
        this.routingIp = routingIp;
    }

    @ExcelField(title = "备注", type = 2, align = 2, sort = 30)
    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }
}
