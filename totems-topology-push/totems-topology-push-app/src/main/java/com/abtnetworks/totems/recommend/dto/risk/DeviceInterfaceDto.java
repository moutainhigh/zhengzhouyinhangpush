package com.abtnetworks.totems.recommend.dto.risk;

import lombok.Data;

/**
 * @author lifei
 * @desc 设备接口dto
 * @date 2021/8/25 16:55
 */
@Data
public class DeviceInterfaceDto {

    /**
     * 子网uuid
     */
    private String subnetUuid;

    /**
     * Ip地址
     */
    private String ipAddress;

    /**
     * 掩码长度
     */
    private String maskLength;

    /**
     * 接口名称
     */
    private String interfaceName;
}
