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
@ApiModel("封禁IP实体类")
public class PushForbidIpEntity extends BaseEntity {

    private static final long serialVersionUID = 6126359349628549540L;

    @ApiModelProperty("主键id")
    private Integer id;

    @ApiModelProperty("uuid")
    private String uuid;

    @ApiModelProperty("流水号")
    private String serialNumber;

    @ApiModelProperty("源IP")
    private String srcIp;

    @ApiModelProperty("状态（0：初始化；1：命令行生成中；2：生成失败；3：待下发；4：下发中；5：下发失败；6：下发成功；7：已更新；8：已启用；9：已禁用）")
    private Integer status;

    @ApiModelProperty("启用状态状态（Y：已启用；N：已禁用；）")
    private String enableStatus;

    @ApiModelProperty("创建人")
    private String createUser;

    @ApiModelProperty("创建时间")
    private Date createDate;

    @ApiModelProperty("更新人")
    private String updateUser;

    @ApiModelProperty("更新时间")
    private Date updateTime;

    @ApiModelProperty("场景uuid 集合")
    private String[] scenesUuidArray;

    @ApiModelProperty("场景名称")
    private String scenesName;

    @ApiModelProperty("场景uuid")
    private String scenesUuid;
}