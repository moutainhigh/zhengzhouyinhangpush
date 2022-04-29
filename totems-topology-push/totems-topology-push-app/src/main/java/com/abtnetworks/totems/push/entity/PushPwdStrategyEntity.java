package com.abtnetworks.totems.push.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

/**
 * @Description
 * @Author guanduo.su
 * @Date: 2021/4/23 19:40
 **/
@Data
@ApiModel("密码策略实体")
public class PushPwdStrategyEntity {


    @ApiModelProperty("主键id")
    private Integer id;

    @ApiModelProperty("密码不能包含用户名")
    private String pwdCannotUsers ;

    @ApiModelProperty("新密码不能与旧密码相同")
    private String npwdCannotOpwd;

    @ApiModelProperty("默认最小密码长度是否勾选")
    private String pwdDefaultMinLengthType;

    @ApiModelProperty("默认最小密码长度")
    private Integer pwdDefaultMinLength;

    @ApiModelProperty("密码重置，密码过期天数是否勾选")
    private String pwdDaysType ;

    @ApiModelProperty("重置密码天数")
    private Integer pwdResetDays ;

    @ApiModelProperty("密码过期提醒天数")
    private Integer pwdOverdueDays ;

    @ApiModelProperty("用户必须修改初始密码")
    private String pwdModifyInitial;

    @ApiModelProperty("密码必须包含")
    private String pwdContainType;

    @ApiModelProperty("密码包含数字")
    private String pwdContainMum ;

    @ApiModelProperty("密码包含字母")
    private String pwdContainLetter;

    @ApiModelProperty("密码包含特殊字符")
    private String pwdContainSpelChar;

    @ApiModelProperty("密码登录失败锁定次数")
    private int pwdFailNum;

    @ApiModelProperty("密码登录失败锁定时长")
    private int pwdLockTime;

    @ApiModelProperty("是否启用密码校验 0: 否  1: 是")
    private String pwdEnable ;

    @ApiModelProperty("创建时间")
    private Date createTime ;

    @ApiModelProperty("创建人")
    private String createBy;

    @ApiModelProperty("ip白名单")
    private String ipWhiteList ;

}