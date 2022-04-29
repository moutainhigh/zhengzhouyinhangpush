package com.abtnetworks.totems.recommend.dto.recommend;

import com.abtnetworks.totems.common.dto.commandline.ServiceDTO;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * @author lifei
 * @desc 仿真开通关联场景
 * @date 2021/12/27 11:28
 */
@Data
public class RecommendRelevanceSceneDTO {


    @ApiModelProperty("主键")
    private Integer id;

    @ApiModelProperty("任务id")
    private String taskId;

    @ApiModelProperty("场景名称")
    private String name;

    @ApiModelProperty("设备uuid")
    String deviceUuid;

    @ApiModelProperty("设备IP")
    String deviceIp;

    @ApiModelProperty("设备名称")
    String deviceName;

    @ApiModelProperty("源IP组")
    private String srcIp;

    @ApiModelProperty("目的IP组")
    private String dstIp;

    @ApiModelProperty("ip类型  0：ipv4; 1:ipv6; 2:url")
    private Integer ipType;

    @ApiModelProperty("分组")
    String branchLevel;

    @ApiModelProperty("服务(页面传入入参)")
    List<ServiceDTO> serviceList;

    @ApiModelProperty("转换前服务")
    String serviceListJson;

    @ApiModelProperty("附加信息")
    private String additionInfo;

    @ApiModelProperty("创建人")
    private String createUser;

    @ApiModelProperty("创建时间")
    private Date createTime;

    @ApiModelProperty("修改时间")
    private Date updateTime;

    @ApiModelProperty("源IP转换前")
    private String preSrcIp;

    @ApiModelProperty("源IP转换后")
    private String postSrcIp;

    @ApiModelProperty("目的IP转换前")
    private String preDstIp;

    @ApiModelProperty("目的IP转换后")
    private String postDstIp;

    @ApiModelProperty("转换前服务")
    private String postService;

    @ApiModelProperty("源域所有")
    private String srcDomain;

    @ApiModelProperty("目的域所有")
    private String dstDomain;

    @ApiModelProperty("转换后端口")
    String postPort;

    @ApiModelProperty("源域")
    String srcZone;

    @ApiModelProperty("入接口")
    String srcItf;

    @ApiModelProperty("目的域")
    String dstZone;

    @ApiModelProperty("出接口")
    String dstItf;
}
