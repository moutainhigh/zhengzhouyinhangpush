package com.abtnetworks.totems.common.commandline2.dto;

import com.abtnetworks.totems.common.dto.commandline.ServiceDTO;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @author zc
 * @date 2020/01/08
 */
@Data
public class CommandDTO {

    /**
     * 型号
     */
    private String modelNumber;

    /**
     * 设备uuid
     */
    private String deviceUuid;

    /**
     * 是否为虚设备
     */
    private Boolean isVsys;

    /**
     * 虚设备名称
     */
    private String vsysName;

    /**
     * 生成策略的名字
     */
    private String name;

    /**
     * 策略允许或者禁止的关键字
     */
    private String action;

    /**
     * 用于生成对象名的关键字
     */
    private String ticket;

    /**
     * 描述
     */
    private String description;

    /**
     * 地址包含ip ipRange ipMask, ip可以是ipv4和ipv6, 中间逗号分隔
     * 1.1.1.1,2.2.2.2-2.2.2.5,3.3.3.3/24,ff::dd,aa::11-aa::22,bb::33/64
     */
    private String ipAddress;

    private String srcIp;

    private String dstIp;

    /**
     * 存在的地址对象名
     */
    private String existedAddressName;

    /**
     * 服务对象
     */
    List<ServiceDTO> serviceList;

    /**
     * 存在的服务名
     */
    private String existedServiceName;

    /**
     * 开始时间
     */
    String startTime;

    /**
     * 结束时间
     */
    String endTime;

}
