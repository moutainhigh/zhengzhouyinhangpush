package com.abtnetworks.totems.common.commandline2.constant;

/**
 * @author zc
 * @date 2019/12/31
 */
public class CommandConstant {

    public static final String FIREWALL = "FIREWALL";
    public static final String ROUTER = "ROUTER";
    public static final String LOAD_BALANCE = "LOAD_BALANCE";

//-----------------------  ip相关的常量 start  -----------------------------------

    /**
     * 掩码
     * 1.1.1.1/24 的掩码为24
     */
    public static final String MASK = "<mask>";

    /**
     * 反掩码
     * 1.1.1.1/24 的反掩码为8
     */
    public static final String WILDCARD_MASK = "<wildcard-mask>";

    /**
     * ip形式的掩码
     * 255.255.255.0
     */
    public static final String IP_MASK = "<ip-mask>";

    /**
     * ip形式的反掩码
     * 0.0.0.255
     */
    public static final String IP_WILDCARD_MASK = "<ip-wildcard-mask>";

    /**
     * 1.1.1.1
     */
    public static final String IPV4_HOST = "<ipv4-host>";

    /**
     * 1.1.1.1-2.2.2.2
     */
    public static final String IPV4_RANGE = "<ipv4-range>";

    /**
     * 1.1.1.1/24
     */
    public static final String IPV4_MASK = "<ipv4-mask>";

    /**
     * 11::11
     */
    public static final String IPV6_HOST = "<ipv6-host>";

    /**
     * 11::11-11::22
     */
    public static final String IPV6_RANGE = "<ipv6-range>";

    /**
     * 11::11/64
     */
    public static final String IPV6_MASK = "<ipv6-mask>";

//-----------------------  ip相关的常量 end  -----------------------------------

    public static final String PORT_START = "<port-start>";

    public static final String PORT = "<port>";

    public static final String PORT_END = "<port-end>";

    public static final String ICMP_TYPE = "<icmp-type>";

    public static final String ICMP_CODE = "<icmp-code>";



    public static final String INDEX = "<index>";

    public static final String VSYS_NAME = "<vsys-name>";

    public static final String POLICY_NAME = "<policy-name>";

    public static final String ADDRESS_NAME = "<address-name>";

    public static final String SRC_ADDRESS_NAME = "<src-address-name>";

    public static final String DST_ADDRESS_NAME = "<dst-address-name>";

    public static final String ADDRESS_GROUP_NAME = "<address-group-name>";

    public static final String SERVICE_NAME = "<service-name>";

    public static final String SERVICE_GROUP_NAME = "<service-group-name>";

    public static final String TIME_NAME = "<time-name>";

    public static final String TIME_FROM = "<time-from>";

    public static final String TIME_TO = "<time-to>";

    public static final String ACTION = "<action>";

    public static final String DESCRIPTION = "<description>";
}
