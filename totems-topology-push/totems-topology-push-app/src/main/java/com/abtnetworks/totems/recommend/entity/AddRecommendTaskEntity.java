package com.abtnetworks.totems.recommend.entity;

import com.abtnetworks.totems.common.dto.commandline.ServiceDTO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.persistence.Transient;
import javax.validation.constraints.NotNull;
import java.util.Date;
import java.util.List;

@Data
@ApiModel("添加互联网开通任务对象信息")
public class AddRecommendTaskEntity {
    @ApiModelProperty("id")
    String id;

    @ApiModelProperty("主题")
    String theme;

    @ApiModelProperty("策略描述")
    String description;

    @ApiModelProperty("工单备注")
    String remarks;

    @ApiModelProperty("源地址")
    String srcIp;

    @ApiModelProperty("源地址所属系统")
    String srcIpSystem;

    @ApiModelProperty("入口子网")
    String entrySubnet;

    @ApiModelProperty("目的地址")
    String dstIp;

    @ApiModelProperty("目的地址所属系统")
    String dstIpSystem;

    @ApiModelProperty("出口子网")
    String exitSubnet;

    @ApiModelProperty("模拟变更场景")
    String whatIfCases;

    @ApiModelProperty("服务")
    List<ServiceDTO> serviceList;

    @ApiModelProperty("开始时间")
    @org.springframework.format.annotation.DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    private Date startTime;

    @ApiModelProperty("结束时间")
    @org.springframework.format.annotation.DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    private Date endTime;

    @ApiModelProperty("长连接")
    private Integer idleTimeout;
    @ApiModelProperty("关联nat")
    private String relevancyNat;
    @ApiModelProperty("仿真开通类型：1：明细开通，8：内-》外开通 14 外->内开通  15 大网段开通")
    @NotNull(message = "仿真开通类型不能为空")
    private Integer taskType;
    @Length(max = 512,message = "起点标签长度最大不能超过512")
    @ApiModelProperty("起点标签")
    private String startLabel;

    @ApiModelProperty("ip类型  0：ipv4; 1:ipv6; 2:url ")
    private Integer ipType;
    @ApiModelProperty("标签模式 OR，AND")
    private String labelModel;

    @ApiModelProperty("转换后源地址")
    private String postSrcIp;

    @ApiModelProperty("转换后目的地址")
    private String postDstIp;

    @ApiModelProperty("范围过滤")
    private boolean rangeFilter;
    @ApiModelProperty("合并检查")
    private boolean mergeCheck;
    @ApiModelProperty("移动冲突前")
    private boolean beforeConflict;
    @ApiModelProperty("关联的东西向工单ID")
    private Integer weTaskId;

    @ApiModelProperty("是否来自于地址映射自动开通的工单")
    @Transient
    private Boolean isAutoMappingTask;
}
