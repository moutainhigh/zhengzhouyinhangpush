package com.abtnetworks.totems.common.utils;

import com.abtnetworks.totems.common.constants.PolicyConstants;
import com.abtnetworks.totems.common.constants.ReturnCode;
import org.apache.commons.lang3.StringUtils;
import sun.net.util.IPAddressUtil;

import java.math.BigInteger;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * 对输入值进行校验以及格式化的工具
 * @Author: wenjiachang
 * @Date: 2018/12/26 14:32
 */
public class InputValueUtils {

    private static final int MAX_IP_SUPORT_NUM = 254;

    /**
     * 格式化端口，将空字符串和null值都变成"any"
     * @param port 端口
     * @return 格式化后的值
     */
    public static String formatPort(String port) {
        if(AliStringUtils.isEmpty(port)) {
            return PolicyConstants.POLICY_STR_VALUE_ANY.toLowerCase();
        }

        return port;
    }

    /**
     * 格式化IP字符串，去掉无用的分隔符
     * @param ip ip字符串
     * @return
     */
    public static String formatIpAddress(String ip) {
        String[] ipAddresses = ip.split(",");

        StringBuilder sb = new StringBuilder();
        for(String ipAddress: ipAddresses) {
            if(!AliStringUtils.isEmpty(ipAddress)){
                sb.append(ipAddress);
                sb.append(",");
            }
        }

        sb.deleteCharAt(sb.lastIndexOf(","));
        return sb.toString();
    }

    /**
     * 检查端口
     * @param port 端口
     * @return
     */

    public static int checkPort(String port) {
        String[] portStrs = port.split(",");

        if (portStrs.length > 5){
            return ReturnCode.POLICY_MSG_INVALID_POST_LENGTH;
        }

        for(String p : portStrs){
            if(p.indexOf("-") != -1){
                String[] ports = p.split("-");
                String startPort = ports[0];
                String endPort = ports[1];
                if(!StringUtils.isNumeric(startPort) || !StringUtils.isNumeric(endPort)){
                    return ReturnCode.POLICY_MSG_INVALID_POST_VALUE;
                }
                if(Integer.parseInt(startPort) < 0 || Integer.parseInt(startPort) > 65535
                        || Integer.parseInt(endPort) < 0 || Integer.parseInt(endPort) > 65535
                        || Integer.parseInt(startPort) > Integer.parseInt(endPort)){
                    return ReturnCode.POLICY_MSG_INVALID_POST_VALUE;
                }
            }else{
                if(!StringUtils.isNumeric(p)){
                    return ReturnCode.POLICY_MSG_INVALID_POST_VALUE;
                }
                if(Integer.parseInt(p) < 0 || Integer.parseInt(p) > 65535){
                    return ReturnCode.POLICY_MSG_INVALID_POST_VALUE;
                }
            }
        }

        return ReturnCode.POLICY_MSG_OK;
    }

    /**
     * 检查IP地址是否为单一IP，IP范围或者IP网段
     * @param ipAddress IP地址
     * @return Return Code字符串
     */
    public static int checkIpSingleIpRangeSegment(String ipAddress) {
        if (AliStringUtils.isEmpty(ipAddress)) {
            return ReturnCode.POLICY_MSG_EMPTY_VALUE;
        }

        String[] ipStrs = ipAddress.split(",");
        if (ipStrs.length > 1){
            return ReturnCode.POLICY_MSG_INVALID_LENGTH;
        }

        if (!IpUtils.isIP(ipAddress) && !IpUtils.isIPSegment(ipAddress) && !IpUtils.isIPRange(ipAddress)) {
            return ReturnCode.POLICY_MSG_INVALID_FORMAT;
        }

        //去掉子网长度检测
//        if (ipAddress.contains("/") && Integer.parseInt(ipAddress.split("/")[1]) < 24) {
//
//            return ReturnCode.POLICY_MSG_INVALID_SUBNET_MASK;
//        }

        return ReturnCode.POLICY_MSG_OK;
    }

    /**
     * 检测IP地址是否为五个IP地址，地址段或者网段
     * @param ipAddress
     * @return
     */
    public static int checkIp(String ipAddress) {
        if (AliStringUtils.isEmpty(ipAddress)) {
            return ReturnCode.POLICY_MSG_EMPTY_VALUE;
        }

        String[] ipStrs = ipAddress.split(",");

        for(String ipStr : ipStrs) {
            if (!IpUtils.isIP(ipStr) && !IpUtils.isIPSegment(ipStr) && !IpUtils.isIPRange(ipStr)) {
                return ReturnCode.POLICY_MSG_INVALID_FORMAT;
            }
            if(IpUtils.isIPRange(ipStr)) {
                String start = IpUtils.getStartIpFromIpAddress(ipStr);
                String end = IpUtils.getEndIpFromIpAddress(ipStr);
                Long startNum = IpUtils.IPv4StringToNum(start);
                Long endNum = IpUtils.IPv4StringToNum(end);
                if (startNum > endNum) {
                    return ReturnCode.INVALID_IP_RANGE;
                }
            }
        }

        return ReturnCode.POLICY_MSG_OK;
    }

    /**
     * 检测IP地址是否为五个IP地址，地址段或者网段
     * @param ipAddress
     * @return
     */
    public static int checkIpV6(String ipAddress) {
        try {
            if (AliStringUtils.isEmpty(ipAddress)) {
                return ReturnCode.POLICY_MSG_EMPTY_VALUE;
            }

            String[] ipv6Strs = ipAddress.split(",");
            for(String ipv6Str : ipv6Strs) {

                //判断是any
                if(ipv6Str.equals(PolicyConstants.IPV6_ANY)){
                    continue;
                }

                //判断网段ipv6格式
                if (ipv6Str.contains("/")) {
                    String[] ipv6Arr = ipv6Str.split("/");
                    if (ipv6Arr.length == 2) {
                        String s_ipv6 = ipv6Arr[0];
                        String e_number = ipv6Arr[1];
                        if (IPAddressUtil.isIPv6LiteralAddress(s_ipv6) && AliStringUtils.isNumeric(e_number)
                                && Integer.valueOf(e_number) > 0 && Integer.valueOf(e_number) <= 128) {
                            continue;
                        } else {
                            return ReturnCode.POLICY_MSG_INVALID_FORMAT;
                        }
                    } else {
                        //不符合ipv6 网段
                        return ReturnCode.POLICY_MSG_INVALID_FORMAT;
                    }
                }

                //判断范围ipv6格式
                if (ipv6Str.contains("-")) {
                    String[] ipv6Arr = ipv6Str.split("-");
                    if (ipv6Arr.length == 2) {
                        String s_ipv6 = ipv6Arr[0];
                        String e_ipv6 = ipv6Arr[1];
                        if (IPAddressUtil.isIPv6LiteralAddress(s_ipv6) && IPAddressUtil.isIPv6LiteralAddress(e_ipv6)) {
                            boolean checkResult = IP6Utils.checkIpv6Range(ipv6Str);
                            if (!checkResult) {
                                return ReturnCode.INVALID_IP_RANGE;
                            } else {
                                continue;
                            }

                        } else {
                            return ReturnCode.POLICY_MSG_INVALID_FORMAT;
                        }
                    } else {
                        //不符合ipv6 范围
                        return ReturnCode.POLICY_MSG_INVALID_FORMAT;
                    }

                }
                //判断单个ipv6格式
                if (IPAddressUtil.isIPv6LiteralAddress(ipv6Str)) {
                    continue;
                } else {
                    return ReturnCode.POLICY_MSG_INVALID_FORMAT;
                }
            }

            return ReturnCode.POLICY_MSG_OK;

        } catch (Exception e) {
            return ReturnCode.POLICY_MSG_INVALID_FORMAT;
        }
    }

    /**
     * 将ip段转换成单个ipList
     * 单个ip超过最大允许数量停止转换 return null
     * @param ipAddr String ip
     * @param MAX_IP 最大分解所得ip数量
     * @return
     */
    public static List<String> ipConvert(String ipAddr, int MAX_IP) {
        try {
            List<String> ipList = new ArrayList<>();
            int num = 0;
            String[] ipArray = ipAddr.split(",");
            for (String ip : ipArray) {
                BigInteger[] bigIntegers = QuintupleUtils.ipv46ToNumRange(ip);
                if (bigIntegers[0].compareTo(bigIntegers[1]) == 0) {
                    ipList.add(ip);
                    num ++;
                } else {
                    for (BigInteger i = bigIntegers[0]; i.compareTo(bigIntegers[1]) <= 0 ; i = i.add(BigInteger.ONE)) {
                        ipList.add(QuintupleUtils.bigIntToIpv46(i));
                        num ++;
                        if (num >= MAX_IP) {
                            return null;
                        }
                    }
                }
                if (num >= MAX_IP) {
                    return null;
                }
            }
            return ipList;
        } catch (Exception e) {
            return null;
        }
    }

    public static String autoCorrect(String ipAddress) {
        String[] ipStrs = ipAddress.split(",");
        StringBuilder sb = new StringBuilder();
        for(String ipStr : ipStrs) {

            if(IpUtils.isIPRange(ipStr)) {
                String start = IpUtils.getStartIpFromIpAddress(ipStr);
                String end = IpUtils.getEndIpFromIpAddress(ipStr);
                Long startNum = IpUtils.IPv4StringToNum(start);
                Long endNum = IpUtils.IPv4StringToNum(end);
                if (startNum > endNum) {
                    sb.append(",");
                    sb.append(String.format("%s-%s", end,start));
                } else {
                    //ip范围起始小于终止，则直接添加
                    sb.append(",");
                    sb.append(ipStr);
                }
            } else {
                //其他情况直接添加
                sb.append(",");
                sb.append(ipStr);
            }
        }
        if(sb.length()>0) {
            sb.deleteCharAt(0);
        }
        return sb.toString();
    }

    public static String autoCorrectPorts(String ports) {
        String[] portStrs = ports.split(",");
        StringBuilder sb = new StringBuilder();
        for(String portString : portStrs) {
            if(PortUtils.isPortRange(portString)) {
                String start = PortUtils.getStartPort(portString);
                String end = PortUtils.getEndPort(portString);
                Integer startNum = Integer.valueOf(start);
                Integer endNum = Integer.valueOf(end);
                if (startNum > endNum) {
                    sb.append(",");
                    sb.append(String.format("%s-%s", end,start));
                } else if(startNum == endNum) {
                    //相等则填起始地址，消除输入为60-60的情况
                    sb.append(",");
                    sb.append(start);
                } else {
                    //ip范围起始小于终止，则直接添加
                    sb.append(",");
                    sb.append(portString);
                }
            } else {
                //其他情况直接添加
                sb.append(",");
                sb.append(portString);
            }
        }
        if(sb.length()>0) {
            sb.deleteCharAt(0);
        }
        return sb.toString();
    }


    public static int getProtocol(String protocol) {
        try{
            int value = Integer.valueOf(protocol);
            return value;
        } catch(Exception e) {
            e.printStackTrace();
            return -2;
        }
    }

    public static boolean validOrderName(String name) {
        String regex = "[a-zA-Z0-9-_]{0,63}";
        Pattern pattern = Pattern.compile(regex);
        return pattern.matcher(name).matches();
    }

    /**
     * 校验掩码位
     *
     * @param value
     * @return
     */
    public static boolean validMask(String value, int ipType) {
        if (value == null || value.length() == 0) {
            return false;
        }

        if (!isPositiveNumber(value)) {
            return false;
        }
        int num = Integer.parseInt(value);

        if (0 == ipType) {
            if (num < 0 || num > 32) {
                return false;
            }
        } else {
            if (num < 0 || num > 128) {
                return false;
            }
        }
        return true;
    }

    /**
     * 校验正整数
     * @param str
     * @return
     */
    public static boolean isPositiveNumber(String str){
        String regex = "^0?$|^([1-9][0-9]*)?$";
        Pattern pattern = Pattern.compile(regex);
        return pattern.matcher(str).matches();
    }

    /**
     * 校验优先级和管理距离
     *
     * @param value
     * @return
     */
    public static boolean validPriority(String value) {
        if (value == null || value.length() == 0) {
            return false;
        }

        if (!isPositiveNumber(value)) {
            return false;
        }
        int num = Integer.parseInt(value);

        if (num < 0 || num > 255) {
            return false;
        }
        return true;
    }


}
