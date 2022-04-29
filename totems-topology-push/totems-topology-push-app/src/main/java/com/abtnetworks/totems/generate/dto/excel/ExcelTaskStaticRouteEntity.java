package com.abtnetworks.totems.generate.dto.excel;

import com.abtnetworks.totems.common.constants.ReturnCode;
import com.abtnetworks.totems.common.enums.IpTypeEnum;
import com.abtnetworks.totems.common.network.TotemsIp4Utils;
import com.abtnetworks.totems.common.network.TotemsIp6Utils;
import com.abtnetworks.totems.common.tools.excel.ExcelField;
import com.abtnetworks.totems.common.utils.AliStringUtils;
import com.abtnetworks.totems.common.utils.InputValueUtils;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;

/**
 * @author lifei
 * @desc XXXX
 * @date 2021/8/27 15:07
 */
@ApiModel("静态路由策略Excel工单模型")
@Log4j2
public class ExcelTaskStaticRouteEntity {

    @ApiModelProperty("ID")
    String id;

    @ApiModelProperty("主题（工单号）")
    String theme;

    @ApiModelProperty("设备IP")
    String deviceIp;

    @ApiModelProperty("ip类型 0:ipv4 1:ipv6")
    String ipType;

    @ApiModelProperty("目的地址")
    String dstIp;

    @ApiModelProperty("子网掩码")
    String subnetMask;

    @ApiModelProperty("所属虚拟路由器")
    String srcVirtualRouter;

    @ApiModelProperty("目的虚拟路由器")
    String dstVirtualRouter;

    @ApiModelProperty("出接口")
    String outInterface;

    @ApiModelProperty("下一跳")
    String nextHop;

    @ApiModelProperty("优先级")
    String priority;

    @ApiModelProperty("管理距离")
    String managementDistance;

    @ApiModelProperty("申请人")
    String userName;

    @ApiModelProperty("备注")
    String mark;


    @ExcelField(title="序号", type=2, align = 2, sort= 0)
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @ExcelField(title="主题（工单号）", type=2, align = 2, sort= 10)
    public String getTheme() {
        return theme;
    }

    public void setTheme(String theme) {
        this.theme = theme;
    }

    @ExcelField(title="防火墙IP",type = 2,align=2, sort=20)
    public String getDeviceIp() {
        return deviceIp;
    }

    public void setDeviceIp(String deviceIp) {
        this.deviceIp = deviceIp;
    }

    @ExcelField(title="IP类型",type = 2,align=2, sort=30)
    public String getIpType() {
        return ipType;
    }

    public void setIpType(String ipType) {
        this.ipType = ipType;
    }

    @ExcelField(title="目的主机地址",type = 2,align=2, sort=40)
    public String getDstIp() {
        return dstIp;
    }

    public void setDstIp(String dstIp) {
        this.dstIp = dstIp;
    }

    @ExcelField(title="子网掩码", type=2, align = 2, sort= 50)
    public String getSubnetMask() {
        return subnetMask;
    }

    public void setSubnetMask(String subnetMask) {
        this.subnetMask = subnetMask;
    }

    @ExcelField(title="所属虚拟路由器", type=2, align = 2, sort= 60)
    public String getSrcVirtualRouter() {
        return srcVirtualRouter;
    }

    public void setSrcVirtualRouter(String srcVirtualRouter) {
        this.srcVirtualRouter = srcVirtualRouter;
    }

    @ExcelField(title="目的虚拟路由器", type=2, align = 2, sort= 70)
    public String getDstVirtualRouter() {
        return dstVirtualRouter;
    }

    public void setDstVirtualRouter(String dstVirtualRouter) {
        this.dstVirtualRouter = dstVirtualRouter;
    }

    @ExcelField(title="出接口", type=2, align = 2, sort= 80)
    public String getOutInterface() {
        return outInterface;
    }

    public void setOutInterface(String outInterface) {
        this.outInterface = outInterface;
    }

    @ExcelField(title="下一跳", type=2, align = 2, sort= 90)
    public String getNextHop() {
        return nextHop;
    }

    public void setNextHop(String nextHop) {
        this.nextHop = nextHop;
    }

    @ExcelField(title="优先级", type=2, align = 2, sort= 100)
    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    @ExcelField(title="管理距离", type=2, align = 2, sort= 110)
    public String getManagementDistance() {
        return managementDistance;
    }

    public void setManagementDistance(String managementDistance) {
        this.managementDistance = managementDistance;
    }

    @ExcelField(title="申请人", type=2, align = 2, sort= 120)
    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    @ExcelField(title="策略描述", type=2, align = 2, sort= 130)
    public String getMark() {
        return mark;
    }

    public void setMark(String mark) {
        this.mark = mark;
    }


    public boolean isEmpty() {
        if(AliStringUtils.isEmpty(id) && AliStringUtils.isEmpty(deviceIp) && AliStringUtils.isEmpty(ipType)
                && AliStringUtils.isEmpty(dstIp) && AliStringUtils.isEmpty(subnetMask) && AliStringUtils.isEmpty(srcVirtualRouter)
                && AliStringUtils.isEmpty(dstVirtualRouter) && AliStringUtils.isEmpty(outInterface) && AliStringUtils.isEmpty(nextHop)) {
            return true;
        }
        return false;
    }


    public int validation() {

        if (AliStringUtils.isEmpty(id)) {
            return ReturnCode.EMPTY_ID;
        } else if (!StringUtils.isNumeric(id)) {
            return ReturnCode.INVALID_NUMBER;
        }

        Integer ipTypeNumber = 0;
        if (StringUtils.equalsAnyIgnoreCase(ipType, IpTypeEnum.IPV4.getDesc())) {
            ipTypeNumber = IpTypeEnum.IPV4.getCode();
        } else if (StringUtils.equalsAnyIgnoreCase(ipType, IpTypeEnum.IPV6.getDesc())) {
            ipTypeNumber = IpTypeEnum.IPV6.getCode();
        } else {
            ipTypeNumber = IpTypeEnum.URL.getCode();
        }


        int rc = 0;
        if (IpTypeEnum.IPV6.getCode().equals(ipTypeNumber)) {
            // 如果是ipv6,检查ipv6格式
            if (!TotemsIp6Utils.isIp6(dstIp)) {
                rc = ReturnCode.ADDRESS_NOT_IP_HOST;
            }

            if (StringUtils.isNotBlank(nextHop)) {
                if (!TotemsIp6Utils.isIp6(nextHop)) {
                    rc = ReturnCode.NEXTHOP_FORMAT_ERROR;
                }
            }

            if (!InputValueUtils.validMask(subnetMask, 1)) {
                rc = ReturnCode.SUBNET_MASK_ERROR;
            }

        } else if (IpTypeEnum.IPV4.getCode().equals(ipTypeNumber)) {
            // 如果是ipv4,检查ipv4格式
            if (!TotemsIp4Utils.isIp4(dstIp)) {
                rc = ReturnCode.ADDRESS_NOT_IP_HOST;
            }

            if (StringUtils.isNotBlank(nextHop)) {
                if (!TotemsIp4Utils.isIp4(nextHop)) {
                    rc = ReturnCode.NEXTHOP_FORMAT_ERROR;
                }
            }

            if (!InputValueUtils.validMask(subnetMask, 0)) {
                rc = ReturnCode.SUBNET_MASK_ERROR;
            }
        } else if (IpTypeEnum.URL.getCode().equals(ipTypeNumber)) {
            log.info("url的校验放过");
        } else {
            log.info("ipType类型不对：" + ipType);
            rc = ReturnCode.POLICY_MSG_INVALID_FORMAT;
            return rc;
        }

        // 如果出接口和下一跳同时不填则异常提示
        if (StringUtils.isBlank(nextHop) && StringUtils.isBlank(outInterface)) {
            rc = ReturnCode.OUTINTERFACE_AND_NEXTHOP_ALL_EMPTY;
        }

        if(StringUtils.isNotBlank(priority)){
            if(!InputValueUtils.validPriority(priority)){
                rc = ReturnCode.PRIORITY_OR_MANAGERDISTANCE_ERROR;
            }
        }

        if(StringUtils.isNotBlank(managementDistance)){
            if(!InputValueUtils.validPriority(managementDistance)){
                rc = ReturnCode.PRIORITY_OR_MANAGERDISTANCE_ERROR;
            }
        }

        if (ReturnCode.POLICY_MSG_OK != rc) {
            return rc;
        }
        return ReturnCode.POLICY_MSG_OK;
    }



}
