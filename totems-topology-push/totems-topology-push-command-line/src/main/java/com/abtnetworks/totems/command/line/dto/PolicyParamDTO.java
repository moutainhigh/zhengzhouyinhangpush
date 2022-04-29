package com.abtnetworks.totems.command.line.dto;

import com.abtnetworks.totems.command.line.enums.MoveSeatEnum;
import lombok.Data;

/**
 * @Description
 * @Version
 * @Created by hw on '2021/4/19 17:37'.
 */
@Data
public class PolicyParamDTO {

    /**
     * 策略集
     */
    private String groupName;

    /**
     * 策略名称
     */
    private String name;

    /**
     * 策略id
     */
    private String id;
    /**
     * 动作
     */
    private String action;

    /**
     * 备注说明
     */
    private String description;

    /**
     * 开启日志
     */
    private String logFlag;
    /**
     * 老化时间
     */
    private String ageingTime;
    /**
     * 引用病毒库
     */
    private String refVirusLibrary;

    /**
     * 移动位置
     */
    private MoveSeatEnum moveSeatEnum;

    /**
     * 交换位置的规则名或id
     */
    private String swapRuleNameId;

    //五元组

    /**
     * 源ip ，需生成命令行
     */
    private IpAddressParamDTO srcIp;

    /**
     * 目的ip 需生成命令行
     */
    private IpAddressParamDTO dstIp;

    /**
     * 服务（端口和协议），需要生成命令行
     */
    private ServiceParamDTO[] serviceParam;

    /**
     * 绝对计划时间对象 需要生成命令行
     */
    private AbsoluteTimeParamDTO absoluteTimeParamDTO;

    /**
     * 周期计划时间对象
     */
    private PeriodicTimeParamDTO periodicTimeParamDTO;

    //域

    /**
     * 源域
     */
    private ZoneParamDTO srcZone;
    /**
     * 目的域
     */
    private ZoneParamDTO dstZone;

    //接口

    /**
     * 进接口
     */
    private InterfaceParamDTO inInterface;

    /**
     * 出接口
     */
    private InterfaceParamDTO outInterface;

    //引用对象

    /**
     * 引用 源地址对象
     */
    private String[] srcRefIpAddressObject;

    /**
     * 引用 目的地址对象
     */
    private String[] dstRefIpAddressObject;

    /**
     * 引用 源地址组对象
     */
    private String[] srcRefIpAddressObjectGroup;

    /**
     * 引用 目的地址组对象
     */
    private String[] dstRefIpAddressObjectGroup;

    /**
     * 引用 服务对象
     */
    private String[] refServiceObject;

    /**
     * 引用 服务组对象
     */
    private String[] refServiceObjectGroup;

    /**
     * 引用 时间对象
     */
    private String[] refTimeObject;

}
