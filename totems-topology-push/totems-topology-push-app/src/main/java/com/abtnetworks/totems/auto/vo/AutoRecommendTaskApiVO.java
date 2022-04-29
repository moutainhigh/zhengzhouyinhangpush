package com.abtnetworks.totems.auto.vo;

import com.abtnetworks.totems.common.dto.commandline.ServiceDTO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.List;


/**
 * @desc    自动开通工单VO
 * @author liuchanghao
 * @date 2021-06-09 14:01
 */
@ApiModel("新建自动开通工单时，页面传递到后台")
@Data
public class AutoRecommendTaskApiVO {

    @ApiModelProperty("主题/工单号")
    @NotNull(message = "主题/工单号不能为空")
    private String theme;

    @ApiModelProperty("当前登录的用户名")
    private String userName;

    @ApiModelProperty("描述")
    private String description;

    @ApiModelProperty("五元组需求集合")
    private List<AutoRecommendBatchTaskVO> batchTaskVOList;

    @ApiModelProperty("开始时间-时间戳格式")
    private Long startTime;

    @ApiModelProperty("结束时间-时间戳格式")
    private Long endTime;

    @ApiModelProperty("ip类型  0：ipv4; 1:ipv6; 2:url ")
    private Integer ipType;

    @ApiModelProperty("主键id集合")
    private List<Integer> idList;

    @ApiModelProperty("主键id，查询报告详情页时需要")
    private Integer id;

    @ApiModelProperty("是否回滚")
    private Boolean isRevert;

    @ApiModelProperty("申请人")
    private String applicant;

    @ApiModelProperty("申请人邮箱")
    private String applicantEmail;

    @ApiModelProperty("是否是虚墙")
    private boolean isVsys;

    @ApiModelProperty("虚墙名称")
    private String vsysName;
}
