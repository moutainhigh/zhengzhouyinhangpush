package com.abtnetworks.totems.common.dto.commandline;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
@ApiModel("命令行生成任务对象")
public class CommandlineTaskDTO {

    @ApiModelProperty("命令行下发任务主键id")
    private Integer id;

    @ApiModelProperty("任务id")
    private Integer taskId;

    @ApiModelProperty("主题")
    private String theme;

    @ApiModelProperty("用户名")
    private String userName;

    @ApiModelProperty("描述")
    private String description;

    @ApiModelProperty("源地址")
    private String srcIp;

    @ApiModelProperty("目的地址")
    private String dstIp;

    @ApiModelProperty("服务列表")
    private List<ServiceDTO> serviceList;

    @ApiModelProperty("开始时间")
    Date startTime;

    @ApiModelProperty("结束时间")
    Date endTime;

    @ApiModelProperty("行为")
    String action;

    @ApiModelProperty("设备uuid")
    private String deviceUuid;

    @ApiModelProperty("源域")
    private String srcZone;

    @ApiModelProperty("目的域")
    private String dstZone;

    @ApiModelProperty("入接口")
    private String inDevItf;

    @ApiModelProperty("出接口")
    private String outDevItf;

    @ApiModelProperty("入接口别名")
    private String inDevItfAlias;

    @ApiModelProperty("出接口别名")
    private String outDevItfAlias;

    @ApiModelProperty("是否置顶")
    private boolean setTop;

    @ApiModelProperty("是否为虚设备")
    private boolean isVsys;

    @ApiModelProperty("主设备UUID")
    private String rootDeviceUuid;

    @ApiModelProperty("虚设备名称")
    private String vsysName;
}
