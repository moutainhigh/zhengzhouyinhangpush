package com.abtnetworks.totems.auto.dto;

import com.abtnetworks.totems.common.dto.commandline.ServiceDTO;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * @desc    自动开通任务Nat信息DTO
 * @author liuchanghao
 * @date 2021-06-09 18:48
 */
@Data
public class AutoRecommendTaskNatInfoDTO {

    @ApiModelProperty("主题/工单号")
    private String theme;

    @ApiModelProperty("设备IP")
    private String deviceIp;

    @ApiModelProperty("设备名称")
    private String deviceName;

    @ApiModelProperty("源域")
    private String srcZone;

    @ApiModelProperty("目的域")
    private String dstZone;

    @ApiModelProperty("源接口")
    private String inDevIf;

    @ApiModelProperty("目的接口")
    private String outDevIf;

    @ApiModelProperty("源IP转换前")
    private String preSrcIp;

    @ApiModelProperty("源IP转换后")
    private String postSrcIp;

    @ApiModelProperty("目的IP转换前")
    private String preDstIp;

    @ApiModelProperty("目的IP转换后")
    private String postDstIp;

    @ApiModelProperty("创建时间")
    private Date createTime;

    @ApiModelProperty("转换前端口")
    private String prePorts;

    @ApiModelProperty("转换后协议")
    private String postProtocol;

    @ApiModelProperty("转换后端口")
    private String postPorts;

    @ApiModelProperty("开始时间-时间戳格式")
    private Date startTime;

    @ApiModelProperty("结束时间-时间戳格式")
    private Date endTime;

    @ApiModelProperty("当前登录的用户名")
    private String userName;

    @ApiModelProperty("服务列表")
    private String serviceList;

    @ApiModelProperty("转换后服务列表")
    private List<ServiceDTO> postServiceList;

    @ApiModelProperty("动作")
    private String action;

    @ApiModelProperty("下发状态")
    private Integer pushStatus;

}
