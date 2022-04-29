package com.abtnetworks.totems.disposal.dto;

import com.abtnetworks.totems.common.tools.excel.ExcelField;

/**
 * @Author hw
 * @Description
 * @Date 17:56 2019/11/12
 */
public class ImportRoutingScenesOrderDTO extends ImportOrderDTO {

    @Override
    @ExcelField(title="工单名称",type = 2,align=2, sort=10)
    public String getOrderName() {
        return orderName;
    }

    @Override
    @ExcelField(title="场景名称",type = 2,align=2, sort=20)
    public String getScenesNames() {
        return scenesNames;
    }

    @Override
    @ExcelField(title="IP地址类型",type = 2,align=2, sort=30)
    public String getStrIpv6() {
        return strIpv6;
    }

    @Override
    @ExcelField(title="黑洞路由IP",type = 2,align=2, sort=40)
    public String getRoutingIp() {
        return routingIp;
    }

    @Override
    @ExcelField(title="动作",type = 2,align=2, sort=50)
    public String getAction() {
        return action;
    }

    @Override
    @ExcelField(title="备注",type = 2,align=2, sort=60)
    public String getRemarks() {
        return remarks;
    }

}
