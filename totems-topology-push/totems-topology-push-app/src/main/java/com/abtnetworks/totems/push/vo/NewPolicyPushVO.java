package com.abtnetworks.totems.push.vo;

import com.abtnetworks.totems.common.dto.commandline.ServiceDTO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @author luwei
 * @date 2019/4/11
 */
@ApiModel("新建策略进行下发时，页面传递到后台")
@Data
public class NewPolicyPushVO {
    @ApiModelProperty("主题/工单号")
    private String theme;

    @ApiModelProperty("策略描述")
    private String description;

    @ApiModelProperty("工单备注")
    private String remarks;

    @ApiModelProperty("场景UUID")
    String scenesUuid;

    @ApiModelProperty("设备uuid")
    private String deviceUuid;

    @ApiModelProperty("源ip")
    private String srcIp;

    @ApiModelProperty("目的ip")
    private String dstIp;

    @ApiModelProperty("源域")
    private String srcZone;

    @ApiModelProperty("目的域")
    private String dstZone;

    @ApiModelProperty("协议")
    private List<ServiceDTO> serviceList;

    @ApiModelProperty("源接口")
    private String inDevIf;

    @ApiModelProperty("目的接口")
    private String outDevIf;

    @ApiModelProperty("开始时间-时间戳格式")
    private Long startTime;

    @ApiModelProperty("结束时间-时间戳格式")
    private Long endTime;

    @ApiModelProperty("当前登录的用户名")
    private String userName;

    @ApiModelProperty("动作")
    private String action;

    @ApiModelProperty("源地址所属系统")
    String srcIpSystem;

    @ApiModelProperty("目的地址所属系统")
    String dstIpSystem;

    @ApiModelProperty("自定义命令行")
    String commandLine;

    /**
     * 页面传参不需要
     * 主要用来存储返回 入库后及时返回命令下发任务id
     */
    private List<Integer> pushTaskId;
    /**
     * 页面传参不需要
     * 主要用来存储返回 入库后及时返回 策略生成的任务id
     */
    private int taskId;

    @ApiModelProperty("ip类型  0：ipv4; 1:ipv6; 2:url ")
    private Integer ipType;

    @ApiModelProperty("选择域名策略的ip类型用于生成命令行  0：ipv4; 1:ipv6; ")
    private Integer urlType;

    @ApiModelProperty("范围过滤")
    private Boolean rangeFilter;
    @ApiModelProperty("合并检查")
    private Boolean mergeCheck;
    @ApiModelProperty("移动到冲突前")
    private Boolean beforeConflict;
    @ApiModelProperty("长连接")
    private Integer IdleTimeout;

    @ApiModelProperty("入接口别名 ")
    private String inDevItfAlias;

    @ApiModelProperty("出接口别名 ")

    private String outDevItfAlias;
    @ApiModelProperty("策略用户")
    private List<String> policyUserNames;
    @ApiModelProperty("策略应用")
    private List<String> policyApplications;
}
