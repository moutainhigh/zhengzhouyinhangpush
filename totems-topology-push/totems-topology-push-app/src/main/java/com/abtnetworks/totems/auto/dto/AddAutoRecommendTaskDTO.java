package com.abtnetworks.totems.auto.dto;

import com.abtnetworks.totems.common.dto.commandline.ServiceDTO;
import com.abtnetworks.totems.common.entity.NodeEntity;
import com.abtnetworks.totems.common.enums.ActionEnum;
import com.abtnetworks.totems.common.enums.MoveSeatEnum;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**新增自动开通任务DTO
 * @desc
 * @author liuchanghao
 * @date 2021-06-09 18:48
 */
@Data
public class AddAutoRecommendTaskDTO {

    @ApiModelProperty("主题/工单号")
    private String theme;

    @ApiModelProperty("描述")
    private String description;

    @ApiModelProperty("访问类型（0：内网互访；1：内网访问互联网；2：互联网访问内网） ")
    private Integer accessType;

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

    @ApiModelProperty("源地址所属系统")
    String srcIpSystem;

    @ApiModelProperty("目的地址所属系统")
    String dstIpSystem;

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

    @ApiModelProperty("范围过滤")
    private Boolean rangeFilter;
    @ApiModelProperty("合并检查")
    private Boolean mergeCheck;
    @ApiModelProperty("移动到冲突前")
    private Boolean beforeConflict;

    @ApiModelProperty("入接口别名 ")
    private String inDevItfAlias;

    @ApiModelProperty("出接口别名 ")
    private String outDevItfAlias;

    @ApiModelProperty("策略用户")
    private List<String> policyUserNames;

    @ApiModelProperty("策略应用")
    private List<String> policyApplications;

    @ApiModelProperty("源IP转换前")
    private String preSrcIp;

    @ApiModelProperty("源IP转换后")
    private String postSrcIp;

    @ApiModelProperty("目的IP转换前")
    private String preDstIp;

    @ApiModelProperty("目的IP转换后")
    private String postDstIp;

    @ApiModelProperty("转换前协议")
    private String preProtocol;

    @ApiModelProperty("转换前端口")
    private String prePorts;

    @ApiModelProperty("转换后协议")
    private String postProtocol;

    @ApiModelProperty("转换后端口")
    private String postPorts;

    @ApiModelProperty("转换后服务")
    private String postService;

    @ApiModelProperty("设备信息")
    private NodeEntity nodeEntity;

    @ApiModelProperty("查询源标志")
    private Boolean srcFlag;

    @ApiModelProperty("动作")
    private ActionEnum action;

    @ApiModelProperty("是否是虚墙")
    private boolean isVsys;

    @ApiModelProperty("虚墙名称")
    private String vsysName;

    @ApiModelProperty("移动位置")
    private MoveSeatEnum moveSeat;

    @ApiModelProperty("交集-源IP")
    private String srcRangeIp;

    @ApiModelProperty("交集-目的IP")
    private String dstRangeIp;

    @ApiModelProperty("Nat类型（N：无Nat；S:源Nat；D:目的Nat）")
    private String natType;


}
