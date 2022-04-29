package com.abtnetworks.totems.common.enums;

import net.minidev.json.JSONUtil;

/**
 * 设备类型
 * @author lifei
 * @date 2021/4/13
 */
public enum DeviceTypeEnum {

    //防火墙
    FIREWALL,
    //路由
    ROUTER,
    //负载
    LOAD_BALANCER,
    //交换机
    SWITCH,
    /**网关防火墙**/
    GATEWAY_FIREWALL,
    LAYER2_FIREWALL,
    /**linux IPTables**/
    LINUX,
    ANTI_DDOS, IPS,
    /**流控**/
    QOS,
    UNKNOWN,
    //主机  -攻击面使用
    HOST,

    /**网闸**/
    GAP,
    ;

    private DeviceTypeEnum() {
    }


    /**
     * 是防火墙
     * @return
     */
    public boolean isFirewall() {
        if (this == FIREWALL || this == GATEWAY_FIREWALL || this == LAYER2_FIREWALL) {
            return true;
        }
        return false;
    }

}
