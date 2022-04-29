package com.abtnetworks.totems.auto.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

/**
 * topo_node领域模型
 *
 * @author zc
 * @date 2018/11/20
 */
@Data
public class TopoNodeDO {
    private Integer id;
    private String ip;
    private String uuid;
    private String type;
    private String deviceName;
    private String deviceTemplateId;
    private String importedName;
    private String hostname;
    private Date createdTime;
    private String createdUser;
    private Date modifiedTime;
    private String modifiedUser;
    private String credentialUuid;
    private String gatherId;
    private String taskType;
    private String taskUuid;
    private String deviceLogUuid;
    private String controllerId;
    private Integer portNumber;
    private Integer timeout;
    private String customPythonPath;
    private Byte gatherType;
    private String gatherCycle;
    private String vendorName;
    private String vendorId;
    private Integer version;
    private String bundleId;
    private String pluginId;
    private String logDetailId;
    private Byte origin;
    private Byte state;
    private String filePath;
    private Integer nodeGroup;
    private String modelNumber;
    private String description;
    private Boolean includeRouting;
    private Integer accountId;
    private String probeIp;
    private String probeToken;
    @ApiModelProperty("配置文件中的设备名字")
    private String name;
    @ApiModelProperty("是否是虚墙")
    private String isVsys;
    @ApiModelProperty("是否跳过路径分析")
    private String skipAnalysis;
}