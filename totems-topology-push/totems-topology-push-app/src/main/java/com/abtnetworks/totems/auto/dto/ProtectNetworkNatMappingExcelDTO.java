package com.abtnetworks.totems.auto.dto;

import com.abtnetworks.totems.auto.enums.PushNatTypeEnum;
import com.abtnetworks.totems.common.constants.ReturnCode;
import com.abtnetworks.totems.common.tools.excel.ExcelField;
import com.abtnetworks.totems.common.utils.AliStringUtils;
import com.abtnetworks.totems.common.utils.InputValueUtils;
import com.abtnetworks.totems.common.utils.PortUtils;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

@ApiModel("防护网段导入DTO")
@Slf4j
public class ProtectNetworkNatMappingExcelDTO {

    @ApiModelProperty("主键id")
    private Long id;

    @ApiModelProperty("防火墙IP")
    private String deviceIp;

    @ApiModelProperty("防护网段配置主键ID")
    private Long configId;

    @ApiModelProperty("Nat类型（N：无Nat；S:源Nat；D:目的Nat）")
    private String natType;

    @ApiModelProperty("内网端口")
    private String insideIp;

    @ApiModelProperty("内网协议")
    private String insideProtocol;

    @ApiModelProperty("内网端口")
    private String insidePorts;

    @ApiModelProperty("外网IP")
    private String outsideIp;

    @ApiModelProperty("外网协议")
    private String outsideProtocol;

    @ApiModelProperty("外网端口")
    private String outsidePorts;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @ExcelField(title = "设备IP", type = 2, align = 2, sort = 5)
    public String getDeviceIp() {
        return deviceIp;
    }

    public void setDeviceIp(String deviceIp) {
        this.deviceIp = deviceIp;
    }

    public Long getConfigId() {
        return configId;
    }

    public void setConfigId(Long configId) {
        this.configId = configId;
    }

    @ExcelField(title = "NAT类型", type = 2, align = 2, sort = 10)
    public String getNatType() {
        return natType;
    }

    public void setNatType(String natType) {
        this.natType = natType;
    }

    @ExcelField(title = "转换后IP", type = 2, align = 2, sort = 20)
    public String getInsideIp() {
        return insideIp;
    }

    public void setInsideIp(String insideIp) {
        this.insideIp = insideIp;
    }

    public String getInsideProtocol() {
        return insideProtocol;
    }

    public void setInsideProtocol(String insideProtocol) {
        this.insideProtocol = insideProtocol;
    }

    @ExcelField(title = "转换后端口", type = 2, align = 2, sort = 35)
    public String getInsidePorts() {
        return insidePorts;
    }

    public void setInsidePorts(String insidePorts) {
        this.insidePorts = insidePorts;
    }

    @ExcelField(title = "转换前IP", type = 2, align = 2, sort = 15)
    public String getOutsideIp() {
        return outsideIp;
    }

    public void setOutsideIp(String outsideIp) {
        this.outsideIp = outsideIp;
    }

    @ExcelField(title = "协议", type = 2, align = 2, sort = 25)
    public String getOutsideProtocol() {
        return outsideProtocol;
    }

    public void setOutsideProtocol(String outsideProtocol) {
        this.outsideProtocol = outsideProtocol;
    }

    @ExcelField(title = "转换前端口", type = 2, align = 2, sort = 30)
    public String getOutsidePorts() {
        return outsidePorts;
    }

    public void setOutsidePorts(String outsidePorts) {
        this.outsidePorts = outsidePorts;
    }

    public boolean isEmpty() {
        if(AliStringUtils.isEmpty(deviceIp)) {
            return true;
        }
        return false;
    }

    /**
     * 进行数据合法性检查，并给出提示。
     * 进行 natType,outsideIp,insideIp,outsideProtocol,outsidePorts,insidePorts 格式的校验并进行处理
     *
     * @return 检查结果
     */
    public int validation() {

        int rc = 0;

        //校验nat类型
        if (StringUtils.isEmpty(natType)) {
            return ReturnCode.NATTYPE_ERROR;
        } else {
            if (PushNatTypeEnum.NAT_TYPE_S.getCode().equals(natType) && StringUtils.isEmpty(outsideIp)) {
                return ReturnCode.SRC_NATTYPE_IPEMPTY_ERROR;
            }

            if (PushNatTypeEnum.NAT_TYPE_D.getCode().equals(natType) && (StringUtils.isEmpty(outsideIp) || StringUtils.isEmpty(insideIp))) {
                return ReturnCode.DST_NATTYPE_IPEMPTY_ERROR;
            }
        }

        // 数据校验
        if (StringUtils.isNotEmpty(outsideIp)) {
            //转换前ip校验
            String[] proIpAddresses = outsideIp.split("\n");
            StringBuilder proIpSb = new StringBuilder();
            for(String proIpAddress: proIpAddresses) {
                proIpAddress = proIpAddress.trim();
                if(StringUtils.isBlank(proIpAddress)){
                    continue;
                }
                proIpSb.append(",");
                proIpSb.append(proIpAddress);
            }
            if(proIpSb.length() > 0) {
                proIpSb.deleteCharAt(0);
            }
            outsideIp = proIpSb.toString();

            rc = InputValueUtils.checkIp(outsideIp);
            if (rc != ReturnCode.POLICY_MSG_OK && rc != ReturnCode.INVALID_IP_RANGE && rc != ReturnCode.POLICY_MSG_EMPTY_VALUE) {
                log.info("转换前IP不正确：" + outsideIp);
                return ReturnCode.OUTSIDEIP_ERROR;
            }
        }

        if (StringUtils.isNotEmpty(insideIp)) {
            //转换后ip校验
            String[] proIpAddresses = insideIp.split("\n");
            StringBuilder proIpSb = new StringBuilder();
            for(String proIpAddress: proIpAddresses) {
                proIpAddress = proIpAddress.trim();
                if(StringUtils.isBlank(proIpAddress)){
                    continue;
                }
                proIpSb.append(",");
                proIpSb.append(proIpAddress);
            }
            if(proIpSb.length() > 0) {
                proIpSb.deleteCharAt(0);
            }
            insideIp = proIpSb.toString();

            rc = InputValueUtils.checkIp(insideIp);
            if (rc != ReturnCode.POLICY_MSG_OK && rc != ReturnCode.INVALID_IP_RANGE && rc != ReturnCode.POLICY_MSG_EMPTY_VALUE) {
                log.info("转换后IP不正确：" + insideIp);
                return ReturnCode.INSIDEIP_ERROR;
            }
        }

        //校验协议及端口
        if ("ICMP".equals(outsideProtocol)) {
            if (StringUtils.isNotEmpty(outsidePorts) || StringUtils.isNotEmpty(insidePorts)) {
                return ReturnCode.PROTECT_ICMP_TYPE_ERROR;
            }
        } else if ("TCP".equals(outsideProtocol) || "UDP".equals(outsideProtocol)) {
            if (tcpAndUdpPortValid(outsidePorts, insidePorts)) {
                return ReturnCode.PROTECT_TCP_UDP_PORT_ERROR;
            }

            if (!PortUtils.isValidPortString(outsidePorts) || !PortUtils.isValidPortString(insidePorts)) {
                return ReturnCode.INVALID_PORT_FORMAT;
            }
        } else if (StringUtils.isEmpty(outsideProtocol)) {
            if (StringUtils.isNotEmpty(outsidePorts) || StringUtils.isNotEmpty(insidePorts)) {
                return ReturnCode.PROTECT_EMPTY_TYPE_ERROR;
            }
        } else {
            return ReturnCode.INVALID_NAT_TYPE;
        }

        return rc;
    }


    /**
     * 校验转换前及转换后端口是否为空
     * @param outsidePorts
     * @param insidePorts
     * @return
     */
    private Boolean tcpAndUdpPortValid(String outsidePorts, String insidePorts) {
        if (StringUtils.isNotEmpty(outsidePorts) && StringUtils.isEmpty(insidePorts)) {
            return true;
        }

        if (StringUtils.isEmpty(outsidePorts) && StringUtils.isNotEmpty(insidePorts)) {
            return true;
        }

        return false;
    }

}
