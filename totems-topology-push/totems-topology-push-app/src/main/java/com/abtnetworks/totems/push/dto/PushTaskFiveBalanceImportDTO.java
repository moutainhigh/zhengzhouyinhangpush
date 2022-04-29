package com.abtnetworks.totems.push.dto;

import com.abtnetworks.totems.common.constants.ReturnCode;
import com.abtnetworks.totems.common.utils.IpUtils;
import com.abtnetworks.totems.common.utils.excel.annotation.TotemsExcelField;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.apache.commons.lang3.StringUtils;

/**
 * @author lifei
 * @desc F5设备批量导入DTO
 * @date 2021/8/3 20:39
 */
@ApiModel(value = "F5设备批量导入DTO")
public class PushTaskFiveBalanceImportDTO {

    @ApiModelProperty("序号")
    String id;

    @ApiModelProperty("设备ip")
    String deviceIp;

    @ApiModelProperty("用户发布F5地址 即转换前目的地址")
    String dstIp;

    @ApiModelProperty("用户发布F5端口 即转换前协议")
    String preProtocol;

    @ApiModelProperty("用户发布F5端口 即转换前的端口")
    String prePort;

    @ApiModelProperty("pool_member地址_端口")
    String postDstIpAndPort;

    @ApiModelProperty("场景名称")
    private String applySystemName;

    @ApiModelProperty("备注")
    String mark;

    @ApiModelProperty("设备uuid")
    String deviceUuid;

    @ApiModelProperty("设备名称")
    String deviceName;


    @TotemsExcelField(title = "序号", type = TotemsExcelField.Type.IMPORT, align = TotemsExcelField.Align.CENTER, sort = 0)
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @TotemsExcelField(title = "设备ip", type = TotemsExcelField.Type.IMPORT, align = TotemsExcelField.Align.CENTER, sort = 10)
    public String getDeviceIp() {
        return deviceIp;
    }

    public void setDeviceIp(String deviceIp) {
        this.deviceIp = deviceIp;
    }

    @TotemsExcelField(title = "用户发布F5地址", type = TotemsExcelField.Type.IMPORT, align = TotemsExcelField.Align.CENTER, sort = 20)
    public String getDstIp() {
        return dstIp;
    }

    public void setDstIp(String dstIp) {
        this.dstIp = dstIp;
    }

    @TotemsExcelField(title = "用户发布F5协议", type = TotemsExcelField.Type.IMPORT, align = TotemsExcelField.Align.CENTER, sort = 30)
    public String getPreProtocol() {
        return preProtocol;
    }

    public void setPreProtocol(String preProtocol) {
        this.preProtocol = preProtocol;
    }

    @TotemsExcelField(title = "用户发布F5端口", type = TotemsExcelField.Type.IMPORT, align = TotemsExcelField.Align.CENTER, sort = 40)
    public String getPrePort() {
        return prePort;
    }

    public void setPrePort(String prePort) {
        this.prePort = prePort;
    }

    @TotemsExcelField(title = "pool_member地址_端口", type = TotemsExcelField.Type.IMPORT, align = TotemsExcelField.Align.CENTER, sort = 50)
    public String getPostDstIpAndPort() {
        return postDstIpAndPort;
    }

    public void setPostDstIpAndPort(String postDstIpAndPort) {
        this.postDstIpAndPort = postDstIpAndPort;
    }

    @TotemsExcelField(title = "场景名称", type = TotemsExcelField.Type.IMPORT, align = TotemsExcelField.Align.CENTER, sort = 60)
    public String getApplySystemName() {
        return applySystemName;
    }

    public void setApplySystemName(String applySystemName) {
        this.applySystemName = applySystemName;
    }

    @TotemsExcelField(title = "备注", type = TotemsExcelField.Type.IMPORT, align = TotemsExcelField.Align.CENTER, sort = 70)
    public String getMark() {
        return mark;
    }

    public void setMark(String mark) {
        this.mark = mark;
    }

    public String getDeviceUuid() {
        return deviceUuid;
    }

    public void setDeviceUuid(String deviceUuid) {
        this.deviceUuid = deviceUuid;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    @Override
    public String toString() {
        return "PushTaskFiveBalanceImportDTO{" +
                "id='" + id + '\'' +
                ", deviceIp='" + deviceIp + '\'' +
                ", dstIp='" + dstIp + '\'' +
                ", preProtocol='" + preProtocol + '\'' +
                ", prePort='" + prePort + '\'' +
                ", postDstIpAndPort='" + postDstIpAndPort + '\'' +
                ", applySystemName='" + applySystemName + '\'' +
                ", mark='" + mark + '\'' +
                ", deviceUuid='" + deviceUuid + '\'' +
                ", deviceName='" + deviceName + '\'' +
                '}';
    }

    public int validation() {
        if (StringUtils.isBlank(dstIp)) {
            return ReturnCode.POLICY_MSG_EMPTY_VALUE;
        }else{
            dstIp = dstIp.trim();
        }
        if (!IpUtils.isIP(dstIp)) {
            return ReturnCode.ADDRESS_NOT_IP_HOST;
        }

        if (StringUtils.isBlank(postDstIpAndPort)) {
            return ReturnCode.POLICY_MSG_EMPTY_VALUE;
        }else {
            postDstIpAndPort = postDstIpAndPort.trim();
            String[] postDstIpAndPorts = postDstIpAndPort.split("\n");
            for (String str : postDstIpAndPorts) {
                String[] items = str.split(":");
                if (!IpUtils.isIP(items[0])) {
                    return ReturnCode.ADDRESS_NOT_IP_HOST;
                }
            }
        }


        return ReturnCode.POLICY_MSG_OK;
    }
}
