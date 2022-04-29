package com.abtnetworks.totems.common.constants;

/**
 * @author Administrator
 * @Title:
 * @Description: <p> 封堵常量 {@link DisposalConstants }
 *  策略仿真常量 {@link PolicyConstants}
 *  生成常量 {@link GenerateConstants}
 *  高级设置常量 {@link AdvancedSettingsConstants}
 *  公共类常量{@link CommonConstants}
 *  凭据常量{@link CredentialConstants}
 *  下发常量{@link PushConstants}
 *
 * </p>
 * @date 2020/12/22
 */
public class GenerateConstants {
    /**
     * 服务对象
     */
    public static final String SERVER_OBJECT_TYPE = "SERVICE_OBJECT";
    /**
     * 服务对象组
     */
    public static final String SERVICE_GROUP_OBJECT_TYPE = "SERVICE_GROUP_OBJECT";
    /**
     * 预定义服务对象
     */
    public static final String PREDEFINED_SERVICE_OBJECT_TYPE = "PREDEFINED_SERVICE_OBJECT";

    /**
     * 已经开通不需要在开了
     */
    public static final String OPENED_POLICY_NOT_GENERATE = "该设备不生成命令行[已存在开通策略]";
    /**
     * 这些预定义对于天融信设备来讲都要过滤的预定义服务
     */
    public static final String PRE_TOP_SERVICE_REUSE_OBJECT_FILTER_NAME = "dhcp,l2tp,pptp,rip,snmp,ssh,tp,monitor,ping,telnet,tosids,auth,cgi_auth,webui,ipsecvpn,ntp,ids,ripng,dns,bgp,userauth-portal";

    /**
     * 思科服务对象复用过滤掉类型为PORT_SERVICE的服务
     */
    public static final String PORT_SERVICE_TYPE = "PORT_SERVICE";

}
