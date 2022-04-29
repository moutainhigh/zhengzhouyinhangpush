package com.abtnetworks.totems.common.utils;

import com.abtnetworks.totems.common.constants.PolicyConstants;

/**
 * @Author: wenjiachang
 * @Date: 2018/12/28 16:11
 */
public class ProtocolUtils {

    /**
     * 根据数值获取协议名称
     * @param protocol 协议号
     * @return 协议名称
     */
    public static String getProtocolByValue(int protocol) {
        switch(protocol) {
            case 0:
                return PolicyConstants.POLICY_STR_VALUE_ANY;
            case 1:
                return PolicyConstants.POLICY_STR_VALUE_ICMP;
            case 6:
                return PolicyConstants.POLICY_STR_VALUE_TCP;
            case 17:
                return PolicyConstants.POLICY_STR_VALUE_UDP;
            case 58:
                return PolicyConstants.POLICY_STR_VALUE_ICMPV6;
            default:
                return String.valueOf(protocol);
        }
    }

    /**
     * 根据字符串数值获取协议名称
     * @param protocol 协议号字符串
     * @return 协议名称
     */
    public static String getProtocolByString(String protocol) {
        switch(protocol) {
            case "0":
                return PolicyConstants.POLICY_STR_VALUE_ANY;
            case "1":
                return PolicyConstants.POLICY_STR_VALUE_ICMP;
            case "6":
                return PolicyConstants.POLICY_STR_VALUE_TCP;
            case "17":
                return PolicyConstants.POLICY_STR_VALUE_UDP;
            case "58":
                return PolicyConstants.POLICY_STR_VALUE_ICMPV6;
            default:
                return protocol;
        }
    }

    /**
     * 根据字符串数值获取协议名称
     * @param protocolName 协议号字符串
     * @return 协议名称
     */
    public static String getProtocolNumberByName(String protocolName) {
        switch(protocolName) {
            case PolicyConstants.POLICY_STR_VALUE_ANY:
                return "0";
            case PolicyConstants.POLICY_STR_VALUE_ICMP:
                return  "1";
            case PolicyConstants.POLICY_STR_VALUE_TCP:
                return  "6";
            case PolicyConstants.POLICY_STR_VALUE_UDP:
                return "17";

            case PolicyConstants.POLICY_STR_VALUE_ICMPV6:
                return "58";
            default:
                return protocolName;
        }
    }

    /**
     * 是否超出协议范围
     * @param protocol
     * @return
     */
    public static boolean isBeyondProtocol(String protocol){
        if("0".equalsIgnoreCase(protocol)||"1".equalsIgnoreCase(protocol) || "58".equalsIgnoreCase(protocol)){
            return false;
        }else if("6".equalsIgnoreCase(protocol)||"17".equalsIgnoreCase(protocol)) {
            return false;
        }else{
            return true;
        }
    }
}
