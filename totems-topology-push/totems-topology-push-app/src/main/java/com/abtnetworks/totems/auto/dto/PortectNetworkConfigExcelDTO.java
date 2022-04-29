package com.abtnetworks.totems.auto.dto;

import com.abtnetworks.totems.auto.entity.ProtectNetworkNatMappingEntity;
import com.abtnetworks.totems.auto.enums.PushNatFlagEnum;
import com.abtnetworks.totems.common.constants.ReturnCode;
import com.abtnetworks.totems.common.tools.excel.ExcelField;
import com.abtnetworks.totems.common.utils.AliStringUtils;
import com.abtnetworks.totems.common.utils.InputValueUtils;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

@ApiModel("防护网段导入DTO")
@Slf4j
public class PortectNetworkConfigExcelDTO {

    @ApiModelProperty("主键id")
    private Long id;

    @ApiModelProperty("uuid")
    private String uuid;

    @ApiModelProperty("防火墙IP")
    private String deviceIp;

    @ApiModelProperty("设备uuid")
    private String deviceUuid;

    @ApiModelProperty("设备名称")
    private String deviceName;

    @ApiModelProperty("防护网段")
    private String protectNetwork;

    @ApiModelProperty("是否打开Nat映射（Y：打开；N：未打开）")
    private String natFlag;

    @ApiModelProperty("IPV4防护网段IP起始数据")
    private Long ipv4Start;

    @ApiModelProperty("IPV4防护网段IP终止数据")
    private Long ipv4End;

    @ApiModelProperty("IPV6防护网段IP起始数据")
    private String ipv6Start;

    @ApiModelProperty("IPV6防护网段IP终止数据")
    private String ipv6End;

    @ApiModelProperty("ip类型  0：ipv4; 1:ipv6; 2:url")
    private Integer ipType;

    @ApiModelProperty("Nat类型（N：无Nat；S:源Nat；D:目的Nat）")
    private String natType;

    @ApiModelProperty("nat映射关系数据")
    private List<ProtectNetworkNatMappingEntity> natMappingDTOList;

    @ApiModelProperty("比较并取交集之后转换的IP,子网形式")
    private String convertIp;

    @ApiModelProperty("比较并取交集之后转换的IP,范围形式")
    private String convertRangeIp;

    @ApiModelProperty("转换前ip")
    private String outsideIp;

    @ApiModelProperty("转换后ip")
    private String insideIp;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    @ExcelField(title = "设备IP", type = 2, align = 2, sort = 5)
    public String getDeviceIp() {
        return deviceIp;
    }

    public void setDeviceIp(String deviceIp) {
        this.deviceIp = deviceIp;
    }

    public String getDeviceUuid() {
        return deviceUuid;
    }

    public void setDeviceUuid(String deviceUuid) {
        this.deviceUuid = deviceUuid;
    }

    @ExcelField(title = "设备名称", type = 2, align = 2, sort = 10)
    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    @ExcelField(title = "防护网段", type = 2, align = 2, sort = 15)
    public String getProtectNetwork() {
        return protectNetwork;
    }

    public void setProtectNetwork(String protectNetwork) {
        this.protectNetwork = protectNetwork;
    }

    @ExcelField(title = "是否存在NAT映射", type = 2, align = 2, sort = 20)
    public String getNatFlag() {
        return natFlag;
    }

    public void setNatFlag(String natFlag) {
        this.natFlag = natFlag;
    }

    public Long getIpv4Start() {
        return ipv4Start;
    }

    public void setIpv4Start(Long ipv4Start) {
        this.ipv4Start = ipv4Start;
    }

    public Long getIpv4End() {
        return ipv4End;
    }

    public void setIpv4End(Long ipv4End) {
        this.ipv4End = ipv4End;
    }

    public String getIpv6Start() {
        return ipv6Start;
    }

    public void setIpv6Start(String ipv6Start) {
        this.ipv6Start = ipv6Start;
    }

    public String getIpv6End() {
        return ipv6End;
    }

    public void setIpv6End(String ipv6End) {
        this.ipv6End = ipv6End;
    }

    public Integer getIpType() {
        return ipType;
    }

    public void setIpType(Integer ipType) {
        this.ipType = ipType;
    }

    public String getNatType() {
        return natType;
    }

    public void setNatType(String natType) {
        this.natType = natType;
    }

    public List<ProtectNetworkNatMappingEntity> getNatMappingDTOList() {
        return natMappingDTOList;
    }

    public void setNatMappingDTOList(List<ProtectNetworkNatMappingEntity> natMappingDTOList) {
        this.natMappingDTOList = natMappingDTOList;
    }

    public String getConvertIp() {
        return convertIp;
    }

    public void setConvertIp(String convertIp) {
        this.convertIp = convertIp;
    }

    public String getConvertRangeIp() {
        return convertRangeIp;
    }

    public void setConvertRangeIp(String convertRangeIp) {
        this.convertRangeIp = convertRangeIp;
    }

    public String getOutsideIp() {
        return outsideIp;
    }

    public void setOutsideIp(String outsideIp) {
        this.outsideIp = outsideIp;
    }

    public String getInsideIp() {
        return insideIp;
    }

    public void setInsideIp(String insideIp) {
        this.insideIp = insideIp;
    }

    public boolean isEmpty() {
        if(AliStringUtils.isEmpty(deviceIp)) {
            return true;
        }
        return false;
    }


    /**
     * 进行数据合法性检查，并给出提示。
     * 进行protectNetwork,natFlag格式的校验并进行处理
     *
     * @return 检查结果
     */
    public int validation() {

        int rc = 0;

        // 数据校验
        if (StringUtils.isNotEmpty(protectNetwork)) {
            //源ip校验
            String[] proIpAddresses = protectNetwork.split("\n");
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
            protectNetwork = proIpSb.toString();

            rc = InputValueUtils.checkIp(protectNetwork);
            if (rc != ReturnCode.POLICY_MSG_OK && rc != ReturnCode.INVALID_IP_RANGE && rc != ReturnCode.POLICY_MSG_EMPTY_VALUE) {
                log.info("防护网段不正确：" + protectNetwork);
                return ReturnCode.PROTECT_NEWWORK_ERROR;
            }
        }

        if ("是".equals(natFlag)) {
            natFlag = PushNatFlagEnum.NAT_FLAG_Y.getCode();
        } else {
            natFlag = PushNatFlagEnum.NAT_FLAG_N.getCode();
        }

        return rc;
    }
}
