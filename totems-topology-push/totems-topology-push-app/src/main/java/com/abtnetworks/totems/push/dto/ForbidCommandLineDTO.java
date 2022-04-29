package com.abtnetworks.totems.push.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

/**
 * 封禁IP 下发记录
 * @author luwei
 * @date 2020/9/12
 */
@Data
public class ForbidCommandLineDTO {

    @ApiModelProperty("主键流水ID")
    private Long id;

    @ApiModelProperty("源IP")
    private String srcIp;

    @ApiModelProperty("工单uuid")
    private String forbidIpUuid;

    @ApiModelProperty("Ip")
    private String deviceUuid;

    @ApiModelProperty("Ip")
    private String deviceIp;

    @ApiModelProperty("设备类型")
    private String deviceType;

    @ApiModelProperty("设备名字")
    private String deviceName;

    @ApiModelProperty("厂商名字")
    private String vendorName;

    @ApiModelProperty("厂商ID")
    private String vendorId;

    @ApiModelProperty("型号")
    private String modelNumber;


    @ApiModelProperty("策略名")
    private String policyName;

    @ApiModelProperty("源地址对象")
    private String srcObjectName;

    @ApiModelProperty("命令行")
    private String commandline;

    @ApiModelProperty("回滚命令行")
    private String commandlineRevert;

    @ApiModelProperty("命令行下发结果")
    private String commandlineEcho;

    @ApiModelProperty("下发状态")
    private Integer pushStatus;

    @ApiModelProperty("下发状态描述")
    private String pushStatusDesc;

    @ApiModelProperty("下发时间")
    private Date pushTime;

    @ApiModelProperty("下发时间-字符串格式")
    private String pushTimeStr;

}
