package com.abtnetworks.totems.push.entity;

import com.abtnetworks.totems.disposal.BaseEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;
/**
 * @desc
 * @author liuchanghao
 * @date 2020-09-10 15:52
 */
@Data
@ApiModel("封禁IP命令行实体类")
public class PushForbidCommandLineEntity extends BaseEntity {

    private static final long serialVersionUID = 1325334070248208435L;

    @ApiModelProperty("主键id")
    private Integer id;

    @ApiModelProperty("源ip")
    private String srcIp;

    @ApiModelProperty("封禁IP UUID")
    private String forbidIpUuid;

    @ApiModelProperty("设备 UUID")
    private String deviceUuid;

    @ApiModelProperty("命令行类型")
    private String commandType;

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

    @ApiModelProperty("下发时间")
    private Date pushTime;

    @ApiModelProperty("回滚状态")
    private Integer revertStatus;

    @ApiModelProperty("回滚时间")
    private Date revertTime;

    @ApiModelProperty("回滚命令行下发回显")
    private String commandlineRevertEcho;

    @ApiModelProperty("回滚修改时间")
    private Date revertModifiedTime;

    @ApiModelProperty("编辑回滚用户")
    private String editRevertUserName;

}