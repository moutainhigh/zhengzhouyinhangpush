package com.abtnetworks.totems.auto.dto;

import com.abtnetworks.totems.branch.dto.UserInfoDTO;
import com.abtnetworks.totems.common.enums.ActionEnum;
import com.abtnetworks.totems.common.enums.MoveSeatEnum;
import com.abtnetworks.totems.common.enums.PolicyEnum;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/** 自动开通任务公共字段DTO
 * @desc
 * @author liuchanghao
 * @date 2021-06-09 18:48
 */
@Data
public class AutoRecommendTaskSamePartDTO {

    @ApiModelProperty("描述")
    private String description;

    @ApiModelProperty("ip类型  0：ipv4; 1:ipv6; 2:url ")
    private Integer ipType;

    @ApiModelProperty("任务类型枚举")
    private PolicyEnum type;

    @ApiModelProperty("用户信息")
    private UserInfoDTO userInfoDTO;

    @ApiModelProperty("当前登录的用户名")
    private String userName;

    @ApiModelProperty("主题")
    private String theme;

    @ApiModelProperty("源接口")
    private String inDevIf;

    @ApiModelProperty("目的接口")
    private String outDevIf;

    @ApiModelProperty("动作")
    private ActionEnum action;

    @ApiModelProperty("是否是虚墙")
    private boolean isVsys;

    @ApiModelProperty("虚墙名称")
    private String vsysName;

    @ApiModelProperty("移动位置")
    private MoveSeatEnum moveSeat;

    @ApiModelProperty("设备uuid")
    private String deviceUuid;

    @ApiModelProperty("合并检查")
    private Boolean mergeCheck;

    @ApiModelProperty("移动到冲突前")
    private Boolean beforeConflict;

    @ApiModelProperty("长连接")
    private Integer IdleTimeout;

    @ApiModelProperty("策略用户")
    private List<String> policyUserNames;

    @ApiModelProperty("策略应用")
    private List<String> policyApplications;

    @ApiModelProperty("范围过滤")
    private Boolean rangeFilter;

    @ApiModelProperty("任务类型")
    private Integer taskType;

    @ApiModelProperty("开始时间")
    private String startTimeString;

    @ApiModelProperty("结束时间")
    private String endTimeString;

}
