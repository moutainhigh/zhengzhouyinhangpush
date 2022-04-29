package com.abtnetworks.totems.command.line.dto;

import lombok.Data;

/**
 * @Author: WangCan
 * @Description Nat 策略参数
 * @Date: 2021/6/22
 */
@Data
public class NatPolicyParamDTO extends PolicyParamDTO{

    /**
     * src转换后地址
     */
    private IpAddressParamDTO postSrcIpAddress;

    /**
     * dst转换后地址
     */
    private IpAddressParamDTO postDstIpAddress;


    /**
     * 转换后服务
     */
    private ServiceParamDTO[] postServiceParam;

    /**
     * 下一跳VRouter
     */
    private String eVr;

    /**
     * 转换后源地址对象名
     */
    private String[] postSrcRefIpAddressObject;

    /**
     * 转换后源地址组对象名
     */
    private String[] postSrcRefIpAddressObjectGroup;
    /**
     * 转换后目的地址对象名
     */
    private String[] postDstRefIpAddressObject;

    /**
     * 转换后目的地址组对象名
     */
    private String[] postDstRefIpAddressObjectGroup;

    /**
     * 转换后引用 服务对象
     */
    private String[] postRefServiceObject;

    /**
     * 转换后引用 服务组对象
     */
    private String[] postRefServiceObjectGroup;

    /**
     * 模式
     */
    private String mode;

    /**
     * 内网地址
     */
    private IpAddressParamDTO insideAddress;

    /**
     * 外网地址
     */
    private IpAddressParamDTO globalAddress;

    /**
     * 内网地址对象名
     */
    private String[] insideRefIpAddressObject;

    /**
     * 内网地址组对象名
     */
    private String[] insideRefIpAddressObjectGroup;

    /**
     * 外网地址对象名
     */
    private String[] globalRefIpAddressObject;

    /**
     * 外网地址组对象名
     */
    private String[] globalRefIpAddressObjectGroup;

    /**
     * 内网服务
     */
    private ServiceParamDTO[] insideServiceParam;

    /**
     * 外网服务
     */
    private ServiceParamDTO[] globalServiceParam;



}
