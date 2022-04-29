package com.abtnetworks.totems.common.utils;

import com.abtnetworks.totems.common.constants.PolicyConstants;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * @Author: hw
 * @Date: 2018/8/13 19:14
 */
public class IpUtils {

    protected  static  Logger logger = LoggerFactory.getLogger(IpUtils.class);

//    public static void main(String[] args) {
//        String segment="0.0.0.0/0";//网段
//
//        //获得起始IP和终止IP的方法（包含网络地址和广播地址）
//        String startIp=getStartIp(segment);
//        String endIp=getEndIp(segment);
//        System.out.println("起始IP：" + startIp + "终止IP：" + endIp);
////        System.out.println(IPv4StringToNum(endIp) - IPv4StringToNum(startIp)+1);
//        if(isIPRange("252.168.1.1-192.168.1.2")) {
//            System.out.println("是ip范围");
//        }
//        if(isIPSegment(segment)) {
//            System.out.println("是网段");
//        }
////
////        //获得起始IP和终止IP的方法（不包含网络地址和广播地址）
////        String subStart=startIp.split("\\.")[0]+"."+startIp.split("\\.")[1]+"."+startIp.split("\\.")[2]+".";
////        String subEnd=endIp.split("\\.")[0]+"."+endIp.split("\\.")[1]+"."+endIp.split("\\.")[2]+".";
////        startIp=subStart+(Integer.parseInt(startIp.split("\\.")[3])+1);
////        endIp=subEnd+(Integer.parseInt(endIp.split("\\.")[3])-1);
////        System.out.println("起始IP：" + startIp + "终止IP：" + endIp);
////        System.out.println(IPv4StringToNum(endIp) - IPv4StringToNum(startIp)+1);
//
//        //判断一个IP是否属于某个网段
////        boolean flag = isInRange("192.168.1.125", "192.168.1.0/24");
////        System.out.println(flag);
//
//        //判断是否是一个IP
////        System.out.println(isIP("192.168.1.235"));
////        System.out.println(isIPSegment("192.168.1.0/24"));
////        System.out.println(isIPRange("192.168.1.0-192.168.1.239"));
////        System.out.println(getHostNumber(32));
////        System.out.println(getHostNumber("0.0.0.0"));
//
////        System.out.println(IPv4StringToNum("192.618.1.1"));
////        System.out.println(IPv4NumToString(3232287075L));
//
////        System.out.println(getSegment("192.168.201.105", 28));
//    }

    /**
     * IP转换Long
     * @param ipaddr
     * @return
     */
    public static Long IPv4StringToNum(String ipaddr) {
        String ip[] = ipaddr.split("\\.");
        Long ipLong = 256 * 256 * 256 * Long.parseLong(ip[0]) +
                256 * 256 * Long.parseLong(ip[1]) +
                256 * Long.parseLong(ip[2]) +
                Long.parseLong(ip[3]);
        return ipLong;
    }


    /**
     * Long转换ip
     * @param ipaddr
     * @return
     */
    public static String IPv4NumToString(Long ipaddr) {
        long y = ipaddr % 256;
        long m = (ipaddr - y) / (256 * 256 * 256);
        long n = (ipaddr - 256 * 256 *256 * m - y) / (256 * 256);
        long x = (ipaddr - 256 * 256 *256 * m - 256 * 256 *n - y) / 256;
        return m + "." + n + "." + x + "." + y;
    }

    /**
     * 功能：判断一个IP是不是在一个网段下的
     * 格式：isInRange("192.168.8.3", "192.168.9.10/22");
     */
    public static boolean isInRange(String ip, String cidr) {
        String[] ips = ip.split("\\.");
        int ipAddr = (Integer.parseInt(ips[0]) << 24)
                | (Integer.parseInt(ips[1]) << 16)
                | (Integer.parseInt(ips[2]) << 8) | Integer.parseInt(ips[3]);
        int type = Integer.parseInt(cidr.replaceAll(".*/", ""));
        int mask = 0xFFFFFFFF << (32 - type);
        String cidrIp = cidr.replaceAll("/.*", "");
        String[] cidrIps = cidrIp.split("\\.");
        int cidrIpAddr = (Integer.parseInt(cidrIps[0]) << 24)
                | (Integer.parseInt(cidrIps[1]) << 16)
                | (Integer.parseInt(cidrIps[2]) << 8)
                | Integer.parseInt(cidrIps[3]);

        return (ipAddr & mask) == (cidrIpAddr & mask);
    }

    /**
     * 功能：判断是否是一个ip
     * 格式：isIP("192.192.192.1")
     */
    public static boolean isIP(String str) {
        String regex = "(25[0-5]|2[0-4]\\d|[0-1]\\d{2}|[1-9]?\\d)\\.(25[0-5]|2[0-4]\\d|[0-1]\\d{2}|[1-9]?\\d)\\.(25[0-5]|2[0-4]\\d|[0-1]\\d{2}|[1-9]?\\d)\\.(25[0-5]|2[0-4]\\d|[0-1]\\d{2}|[1-9]?\\d)";
        Pattern pattern = Pattern.compile(regex);
        return pattern.matcher(str).matches();
    }

    /**
     * 功能：判断是否是一个ip网段
     * 格式：isIP("192.192.192.1")
     */
    public static boolean isIPSegment(String str) {
        String regex = "(25[0-5]|2[0-4]\\d|[0-1]\\d{2}|[1-9]?\\d)\\.(25[0-5]|2[0-4]\\d|[0-1]\\d{2}|[1-9]?\\d)\\.(25[0-5]|2[0-4]\\d|[0-1]\\d{2}|[1-9]?\\d)\\.(25[0-5]|2[0-4]\\d|[0-1]\\d{2}|[1-9]?\\d)\\/(?:[0-9]|[12][0-9]|3[012])";
        Pattern pattern = Pattern.compile(regex);
        return pattern.matcher(str).matches();
    }

    /**
     * 功能：判断是否是一个ip范围
     * 格式：isIP("192.192.192.1")
     */
    public static boolean isIPRange(String str) {
        String regex = "(25[0-5]|2[0-4]\\d|[0-1]\\d{2}|[1-9]?\\d)\\.(25[0-5]|2[0-4]\\d|[0-1]\\d{2}|[1-9]?\\d)\\.(25[0-5]|2[0-4]\\d|[0-1]\\d{2}|[1-9]?\\d)\\.(25[0-5]|2[0-4]\\d|[0-1]\\d{2}|[1-9]?\\d)-(25[0-5]|2[0-4]\\d|[0-1]\\d{2}|[1-9]?\\d)\\.(25[0-5]|2[0-4]\\d|[0-1]\\d{2}|[1-9]?\\d)\\.(25[0-5]|2[0-4]\\d|[0-1]\\d{2}|[1-9]?\\d)\\.(25[0-5]|2[0-4]\\d|[0-1]\\d{2}|[1-9]?\\d)";
        Pattern pattern = Pattern.compile(regex);
        return pattern.matcher(str).matches();
    }




    /**
     * 根据ip和掩码位获取网段
     * @param ip
     * @param maskBit
     * @return 网段
     */
    public static String getSegment(String ip, int maskBit) {
        long a = (long) Math.pow(2, 32-maskBit);
        long startIP = IPv4StringToNum(ip) / a * a;
        return IPv4NumToString(startIP) + "/" + maskBit;
    }

    /**
     * 根据网段计算起始IP 网段格式:x.x.x.x/x
     * 包含网络地址和广播地址
     * @param segment  网段
     * @return 起始IP
     */
    public static String getStartIp(String segment) {
        StringBuffer startIp = new StringBuffer();
        if (segment == null) {
            return null;
        }
        String arr[] = segment.split("/");
        String ip = arr[0];
        String maskIndex = arr[1];
        if("0".equals(maskIndex)){
            return "0.0.0.0";
        }
        String mask = getMaskByMaskBit(maskIndex);
        if (4 != ip.split("\\.").length || mask == null) {
            return null;
        }
        int ipArray[] = new int[4];
        int netMaskArray[] = new int[4];
        for (int i = 0; i < 4; i++) {
            try {
                ipArray[i] = Integer.parseInt(ip.split("\\.")[i]);
                netMaskArray[i] = Integer.parseInt(mask.split("\\.")[i]);
                if (ipArray[i] > 255 || ipArray[i] < 0 || netMaskArray[i] > 255 || netMaskArray[i] < 0) {
                    return null;
                }
                ipArray[i] = ipArray[i] & netMaskArray[i];
                if(i==3){
                    startIp.append(ipArray[i]);
                }else{
                    startIp.append(ipArray[i]+".");
                }
            } catch (NumberFormatException e) {
                System.out.println(e.getMessage());
            }
        }
        return startIp.toString();
    }

    /**
     * 根据网段计算结束IP
     * 包含网络地址和广播地址
     * @param segment
     * @return 结束IP
     */
    public static String getEndIp(String segment) {
        StringBuffer endIp=new StringBuffer();
        String startIp = getStartIp(segment);
        if (segment == null) {
            return null;
        }
        String arr[] = segment.split("/");
        String maskIndex = arr[1];
        if("0".equals(maskIndex)){
            return "255.255.255.255";
        }
        //实际需要的IP个数
        int hostNumber = 0;
        int startIpArray[] = new int[4];
        try {
            hostNumber=1<<32-(Integer.parseInt(maskIndex));
            for (int i = 0; i <4; i++) {
                startIpArray[i] = Integer.parseInt(startIp.split("\\.")[i]);
                if(i == 3){
                    startIpArray[i] = startIpArray[i] - 1;
                    break;
                }
            }
            startIpArray[3] = startIpArray[3] + (hostNumber - 1);
        } catch (NumberFormatException e) {
            System.out.println(e.getMessage());
        }

        if(startIpArray[3] >255){
            int k = startIpArray[3] / 256;
            startIpArray[3] = startIpArray[3] % 256;
            startIpArray[2] = startIpArray[2] + k;
        }
        if(startIpArray[2] > 255){
            int  j = startIpArray[2] / 256;
            startIpArray[2] = startIpArray[2] % 256;
            startIpArray[1] = startIpArray[1] + j;
            if(startIpArray[1] > 255){
                int  k = startIpArray[1] / 256;
                startIpArray[1] = startIpArray[1] % 256;
                startIpArray[0] = startIpArray[0] + k;
            }
        }
        for(int i = 0; i < 4; i++){
            if(i == 3){
                startIpArray[i] = startIpArray[i] + 1;
            }
            if("" == endIp.toString()||endIp.length()==0){
                endIp.append(startIpArray[i]);
            }else{
                endIp.append("." + startIpArray[i]);
            }
        }
        return endIp.toString();
    }


    /**
     * 根据掩码位获取掩码
     *
     * @param maskBit
     *            掩码位数，如"28"、"30"
     * @return
     */
    public static String getMaskByMaskBit(String maskBit)
    {
        return "".equals(maskBit) ? "error, maskBit is null !" : getMaskMap(maskBit);
    }

    /**
     * 根据掩码位获取掩码 掩码int-->掩码String
     * @param maskBit
     * @return
     */
    public static String getMaskMap(String maskBit) {
        if ("0".equals(maskBit)) {
            return "0.0.0.0";
        }
        if ("1".equals(maskBit)) {
            return "128.0.0.0";
        }
        if ("2".equals(maskBit)) {
            return "192.0.0.0";
        }
        if ("3".equals(maskBit)) {
            return "224.0.0.0";
        }
        if ("4".equals(maskBit)) {
            return "240.0.0.0";
        }
        if ("5".equals(maskBit)) {
            return "248.0.0.0";
        }
        if ("6".equals(maskBit)) {
            return "252.0.0.0";
        }
        if ("7".equals(maskBit)) {
            return "254.0.0.0";
        }
        if ("8".equals(maskBit)) {
            return "255.0.0.0";
        }
        if ("9".equals(maskBit)) {
            return "255.128.0.0";
        }
        if ("10".equals(maskBit)) {
            return "255.192.0.0";
        }
        if ("11".equals(maskBit)) {
            return "255.224.0.0";
        }
        if ("12".equals(maskBit)) {
            return "255.240.0.0";
        }
        if ("13".equals(maskBit)) {
            return "255.248.0.0";
        }
        if ("14".equals(maskBit)) {
            return "255.252.0.0";
        }
        if ("15".equals(maskBit)) {
            return "255.254.0.0";
        }
        if ("16".equals(maskBit)) {
            return "255.255.0.0";
        }
        if ("17".equals(maskBit)) {
            return "255.255.128.0";
        }
        if ("18".equals(maskBit)) {
            return "255.255.192.0";
        }
        if ("19".equals(maskBit)) {
            return "255.255.224.0";
        }
        if ("20".equals(maskBit)) {
            return "255.255.240.0";
        }
        if ("21".equals(maskBit)) {
            return "255.255.248.0";
        }
        if ("22".equals(maskBit)) {
            return "255.255.252.0";
        }
        if ("23".equals(maskBit)) {
            return "255.255.254.0";
        }
        if ("24".equals(maskBit)) {
            return "255.255.255.0";
        }
        if ("25".equals(maskBit)) {
            return "255.255.255.128";
        }
        if ("26".equals(maskBit)) {
            return "255.255.255.192";
        }
        if ("27".equals(maskBit)) {
            return "255.255.255.224";
        }
        if ("28".equals(maskBit)) {
            return "255.255.255.240";
        }
        if ("29".equals(maskBit)) {
            return "255.255.255.248";
        }
        if ("30".equals(maskBit)) {
            return "255.255.255.252";
        }
        if ("31".equals(maskBit)) {
            return "255.255.255.254";
        }
        if ("32".equals(maskBit)) {
            return "255.255.255.255";
        }
        return "-1";
    }

    /**
     * 根据掩码获取掩码位 掩码String-->掩码int
     * @param mask
     * @return
     */
    public static int getMaskBit(String mask) {
        switch (mask){
            case "0.0.0.0":
                return 0;
            case "128.0.0.0":
                return 1;
            case "192.0.0.0":
                return 2;
            case "224.0.0.0":
                return 3;
            case "240.0.0.0":
                return 4;
            case "248.0.0.0":
                return 5;
            case "252.0.0.0":
                return 6;
            case "254.0.0.0":
                return 7;
            case "255.0.0.0":
                return 8;
            case "255.128.0.0":
                return 9;
            case "255.192.0.0":
                return 10;
            case "255.224.0.0":
                return 11;
            case "255.240.0.0":
                return 12;
            case "255.248.0.0":
                return 13;
            case "255.252.0.0":
                return 14;
            case "255.254.0.0":
                return 15;
            case "255.255.0.0":
                return 16;
            case "255.255.128.0":
                return 17;
            case "255.255.192.0":
                return 18;
            case "255.255.224.0":
                return 19;
            case "255.255.240.0":
                return 20;
            case "255.255.248.0":
                return 21;
            case "255.255.252.0":
                return 22;
            case "255.255.254.0":
                return 23;
            case "255.255.255.0":
                return 24;
            case "255.255.255.128":
                return 25;
            case "255.255.255.192":
                return 26;
            case "255.255.255.224":
                return 27;
            case "255.255.255.240":
                return 28;
            case "255.255.255.248":
                return 29;
            case "255.255.255.252":
                return 30;
            case "255.255.255.254":
                return 31;
            case "255.255.255.255":
                return 32;
            default:
                return -1;
        }

    }

    /**
     * 根据反掩码获取掩码位 反掩码String-->掩码int
     * @param inverseMask
     * @return
     */
    public static int getMaskBitMapByInverseMask(String inverseMask) {
        if ("255.255.255.255".equals(inverseMask)) {
            return 0;
        }
        if ("127.255.255.255".equals(inverseMask)) {
            return 1;
        }
        if ("63.255.255.255".equals(inverseMask)) {
            return 2;
        }
        if ("31.255.255.255".equals(inverseMask)) {
            return 3;
        }
        if ("15.255.255.255".equals(inverseMask)) {
            return 4;
        }
        if ("7.255.255.255".equals(inverseMask)) {
            return 5;
        }
        if ("3.255.255.255".equals(inverseMask)) {
            return 6;
        }
        if ("1.255.255.255".equals(inverseMask)) {
            return 7;
        }
        if ("0.255.255.255".equals(inverseMask)) {
            return 8;
        }
        if ("0.127.255.255".equals(inverseMask)) {
            return 9;
        }
        if ("0.63.255.255".equals(inverseMask)) {
            return 10;
        }
        if ("0.31.255.255".equals(inverseMask)) {
            return 11;
        }
        if ("0.15.255.255".equals(inverseMask)) {
            return 12;
        }
        if ("0.7.255.255".equals(inverseMask)) {
            return 13;
        }
        if ("0.3.255.255".equals(inverseMask)) {
            return 14;
        }
        if ("0.1.255.255".equals(inverseMask)) {
            return 15;
        }
        if ("0.0.255.255".equals(inverseMask)) {
            return 16;
        }
        if ("0.0.127.255".equals(inverseMask)) {
            return 17;
        }
        if ("0.0.63.255".equals(inverseMask)) {
            return 18;
        }
        if ("0.0.31.255".equals(inverseMask)) {
            return 19;
        }
        if ("0.0.15.255".equals(inverseMask)) {
            return 20;
        }
        if ("0.0.7.255".equals(inverseMask)) {
            return 21;
        }
        if ("0.0.3.255".equals(inverseMask)) {
            return 22;
        }
        if ("0.0.1.255".equals(inverseMask)) {
            return 23;
        }
        if ("0.0.0.255".equals(inverseMask)) {
            return 24;
        }
        if ("0.0.0.127".equals(inverseMask)) {
            return 25;
        }
        if ("0.0.0.63".equals(inverseMask)) {
            return 26;
        }
        if ("0.0.0.31".equals(inverseMask)) {
            return 27;
        }
        if ("0.0.0.15".equals(inverseMask)) {
            return 28;
        }
        if ("0.0.0.7".equals(inverseMask)) {
            return 29;
        }
        if ("0.0.0.3".equals(inverseMask)) {
            return 30;
        }
        if ("0.0.0.1".equals(inverseMask)) {
            return 31;
        }
        if ("0.0.0.0".equals(inverseMask)) {
            return 32;
        }
        return -1;
    }

    /**
     * 根据掩码位获取位反掩码 掩码int-->反掩码String
     * @param maskBit
     * @return
     */
    public static String getWildcardMaskMap(int maskBit) {
        switch (maskBit){
            case 0:
                return "255.255.255.255";
            case 1:
                return "127.255.255.255";
            case 2:
                return "63.255.255.255";
            case 3:
                return "31.255.255.255";
            case 4:
                return "15.255.255.255";
            case 5:
                return "7.255.255.255";
            case 6:
                return "3.255.255.255";
            case 7:
                return "1.255.255.255";
            case 8:
                return "0.255.255.255";
            case 9:
                return "0.127.255.255";
            case 10:
                return "0.63.255.255";
            case 11:
                return "0.31.255.255";
            case 12:
                return "0.15.255.255";
            case 13:
                return "0.7.255.255";
            case 14:
                return "0.3.255.255";
            case 15:
                return "0.1.255.255";
            case 16:
                return "0.0.255.255";
            case 17:
                return "0.0.127.255";
            case 18:
                return "0.0.63.255";
            case 19:
                return "0.0.31.255";
            case 20:
                return "0.0.15.255";
            case 21:
                return "0.0.7.255";
            case 22:
                return "0.0.3.255";
            case 23:
                return "0.0.1.255";
            case 24:
                return "0.0.0.255";
            case 25:
                return "0.0.0.127";
            case 26:
                return "0.0.0.63";
            case 27:
                return "0.0.0.31";
            case 28:
                return "0.0.0.15";
            case 29:
                return "0.0.0.7";
            case 30:
                return "0.0.0.3";
            case 31:
                return "0.0.0.1";
            case 32:
                return "0.0.0.0";
            default:
                return StringUtils.EMPTY;
        }
    }

    public static List<String> parseIpRange(String ipfrom, String ipto) {
        List<String> ips = new ArrayList<String>();
        String[] ipfromd = ipfrom.split("\\.");
        String[] iptod = ipto.split("\\.");
        int[] int_ipf = new int[4];
        int[] int_ipt = new int[4];
        for (int i = 0; i < 4; i++) {
            int_ipf[i] = Integer.parseInt(ipfromd[i]);
            int_ipt[i] = Integer.parseInt(iptod[i]);
        }
        for (int A = int_ipf[0]; A <= int_ipt[0]; A++) {
            for (int B = (A == int_ipf[0] ? int_ipf[1] : 0); B <= (A == int_ipt[0] ? int_ipt[1]
                    : 255); B++) {
                for (int C = (B == int_ipf[1] ? int_ipf[2] : 0); C <= (B == int_ipt[1] ? int_ipt[2]
                        : 255); C++) {
                    for (int D = (C == int_ipf[2] ? int_ipf[3] : 0); D <= (C == int_ipt[2] ? int_ipt[3]
                            : 255); D++) {
                        ips.add(A + "." + B + "." + C + "." + D);
                    }
                }
            }
        }
        return ips;
    }

    /**
     * 计算子网大小（包含网络地址和广播地址）
     * @param netmask 掩码位
     */
    public static long getHostNumber(int netmask)
    {
        if(netmask<0||netmask>=32)
        {
            return 0;
        }
        int bits=32-netmask;
        return (long) Math.pow(2,bits);
    }

    /**
     * 计算子网大小（包含网络地址和广播地址）
     * @param netMask 掩码
     * @return
     */
    private static long getHostNumber(String netMask) {
        long hostNumber = 0L;
        int netMaskArray[] = new int[4];
        for (int i = 0; i < 4 ; i++) {
            netMaskArray[i] = Integer.parseInt(netMask.split("\\.")[i]);
            if(netMaskArray[i] < 255){
                hostNumber =  (long) (Math.pow(256,3-i) * (256 - netMaskArray[i]));
                break;
            }
        }
        return hostNumber;
    }

    /**
     * 获取IP范围的起始IP
     * @param range IP范围
     * @return 终止IP
     */
    public static String getStartIpFromRange(String range) {
        if(range == null) {
            return null;
        }
        if(!range.contains("-")) {
            return null;
        }

        String[] array = range.split("-");
        return array[0];
    }


    /**
     * 获取IP范围的终止IP
     * @param range IP范围
     * @return 终止IP
     */
    public static String getEndIpFromRange(String range) {
        if(range == null) {
            return null;
        }
        if(!range.contains("-")) {
            return null;
        }
        String[] array = range.split("-");
        return array[1];
    }

    /**
     * 从IP地址获取起始IP
     * @param ipAddress Ip地址（单独IP，IP段或者IP范围）
     * @return 起始IP
     */
    public static String getStartIpFromIpAddress(String ipAddress) {

        String startIp = ipAddress;
        if(isIPSegment(ipAddress)) {
            return getStartIp(ipAddress);
        } else if (isIPRange(ipAddress)) {
            return getStartIpFromRange(ipAddress);
        }
        return startIp;
    }

    /**
     * 从IP地址组获取起始IP
     * @param ipAddress
     * @return
     */
    public static String getStartIpFromIpAddresses(String ipAddress) {
        String[] ipStrs = ipAddress.split(",");

        String ipStr = ipStrs[0];
        String startIp = ipStr;
        if(isIPSegment(ipStr)) {
            return getStartIp(ipStr);
        } else if (isIPRange(ipStr)) {
            return getStartIpFromRange(ipStr);
        }
        return startIp;
    }

    public static String getEndIpFromIpAddress(String ipAddress) {
        String endIp = ipAddress;
        if(isIPRange(ipAddress)) {
            return getEndIpFromRange(ipAddress);
        } else if (isIPSegment(ipAddress)) {
            return getEndIp(ipAddress);
        }
        return endIp;
    }

    /**
     * 检测IP是否合法
     * @param ipAddress
     * @return 合法返回true，否则返回false
     */
    public static boolean isValidIp(String ipAddress) {
        if (IpUtils.isIP(ipAddress) || IpUtils.isIPRange(ipAddress) || IpUtils.isIPSegment(ipAddress)) {
            return true;
        }
            return false;
    }

    /**
     * 根据IP网段地址获取IP
     * @param ipAddress IP地址段
     * @return IP地址
     */
    public static String getIpFromIpSegment(String ipAddress) {
        String arr[] = ipAddress.split("/");
        return arr[0];
    }

    /**
     * 二进制与计算后，转换为十进制的ip
     * @param ipAddress  11000000.10101000.11001000.00000010
     * @param segmentAddress  11111111.11111111.11111111.10000000
     * @return
     */
    public static String calcIpAndByBinary(String ipAddress, String segmentAddress){
        String result = "";
        String calcResult = "";
        //循环取每一位
        for (int i = 0; i < ipAddress.length(); i++) {
            String t1 = ipAddress.substring(i, (i + 1));
            if (t1.equals(".")) {
                calcResult += ".";
                continue;
            }
            String t2 = segmentAddress.substring(i, (i + 1));
            if (t1.equals(t2) && t1.equals("1")) {
                calcResult += "1";
            } else {
                calcResult += "0";
            }
        }
        result = getDecimalIpByBinary(calcResult);
        return result;
    }

    /**
     * 将ip地址转换为二进制
     * @param ipAddress  192.168.200.2
     * @return   11000000.10101000.11001000.00000010
     */
    public static String getBinaryIp(String ipAddress) {
        String result = "";
        String[] numArr = ipAddress.split("\\.");
        for (String str : numArr) {
            int num = Integer.valueOf(str);
            String tmp = Integer.toBinaryString(num);
            //转换二进制后，不足8位补0
            if (tmp.length() < 8) {
                tmp = "00000000" + tmp;
                tmp = tmp.substring(tmp.length()-8);
            }
            result += tmp + ".";
        }

        result = result.substring(0, result.length() - 1);
        return result;
    }

    /**
     * 将二进制ip地址转换为十进制
     * @param ipAddress  11000000.10101000.11001000.00000010
     * @return   192.168.200.2
     */
    public static String getDecimalIpByBinary(String ipAddress) {
        String result = "";
        String[] numArr = ipAddress.split("\\.");
        for (String str : numArr) {
            String num = Integer.valueOf(str, 2).toString();
            result += num + ".";
        }

        result = result.substring(0, result.length() - 1);
        return result;
    }

    /**
     * 根据IP网段地址获取子网掩码
     * @param ipAddress IP地址段
     * @return 子网掩码
     */
    public static String getMaskBitFromIpSegment(String ipAddress) {
        String arr[] = ipAddress.split("/");
        return arr[1];
    }

    /**
     * 检查IP是否在网段范围
     * @param ip 支持单个ip，网段，范围
     * @param segment ip 支持单个ip，网段，范围
     * @return
     */
    public static boolean checkIpRange(String ip, String segment){
        String startIp = getStartIpFromIpAddress(ip);
        String endIp = getEndIpFromIpAddress(ip);
        String segmentStartIp = getStartIpFromIpAddress(segment);
        String segmentEndIp = getEndIpFromIpAddress(segment);
        if(IPv4StringToNum(segmentStartIp) <= IPv4StringToNum(startIp)
                && IPv4StringToNum(segmentEndIp) >= IPv4StringToNum(endIp)){
            return true;
        }
        return false;
    }
    /**
     * 将IP地址中的IP范围转换成IP子网
     * @param ipAddress 原始IP地址字符串，可以包含多个IP地址，逗号分隔
     * @return 转换后ip地址字符串，可以包含多个IP地址，逗号分隔
     */
    public static String convertIpRangeToSegment(String ipAddress) {
        List<String> ipList = new ArrayList<>();
        String[] ips = ipAddress.split(",");
        for(String ip:ips) {
            List<String> ipSegments = IPUtil.convertRangeToSubnet(ip);
            ipList.addAll(ipSegments);
        }
        StringBuilder sb= new StringBuilder();
        for(String ip:ipList) {
            sb.append(",");
            sb.append(ip);
        }
        if(sb.length() > 0) {
            sb.deleteCharAt(0);
        }
        return sb.toString();
    }

    /**
     * IP范围转换成覆盖该范围的两个相邻子网（或一个子网）
     * 其主要算法如下：
     * 1. 算出包含ip范围的最小子网a
     * 2. 以该子网的中点分割ip范围（该中点为一个ip子网的起始点）
     * 3. 算出从ip范围起始地址到切割点前一点的ip范围的最小子网a，
     * 4. 算出从切割点开始到ip范围中点的最小子网b
     * 5. 若a和b为大小相同的子网并且可以合并，则合并为同一子网
     * 注意，该算法获得的子网大部分时间会比原IP范围稍大
     * @param ipRange 需要转换的IP地址范围
     * @return 转换后的IP子网列表
     */
    @Deprecated
    public static List<String> convertRangeToSubnet(String ipRange) {
        List<String> ipSubnetList = new ArrayList<>();
        if(!IpUtils.isIPRange(ipRange)) {
            ipSubnetList.add(ipRange);
            return ipSubnetList;
        }

        String startIpString = getStartIpFromIpAddress(ipRange);
        String endIpString = getEndIpFromIpAddress(ipRange);

        Long startIp = IPv4StringToNum(startIpString);
        Long endIp = IPv4StringToNum(endIpString);

        if(startIp.equals(endIp)) {
            ipSubnetList.add(startIpString + "/32");
            return ipSubnetList;
        }

        int maxsize = 32;
        while ( maxsize > 0) {
            long mask = CIDR2MASK[ maxsize -1 ];

            if ( (startIp & mask) == (endIp & mask)) {
//                System.out.println("子网掩码为:"+ String.valueOf(maxsize - 1));
                break;
            }

            maxsize--;
        }

        long range = 1L << (32 - (maxsize - 1));
        long nextRange = range/2;

        long subnet = CIDR2MASK[ maxsize -1 ] & startIp;
        long midSubnet = subnet + nextRange;

        int index = 0;
        {
            long count = midSubnet - startIp;
            long num = 1;
            while(num < count) {
                num = num*2;
                index ++;
            }
            long firstHalf = midSubnet - (1 << index);
            String firstSubnet = IPv4NumToString(firstHalf) + "/" + String.valueOf(32-index);
            //System.out.println(firstSubnet);
            ipSubnetList.add(firstSubnet);
        }

        int index2 = 0;
        {
            long count = endIp - midSubnet + 1;
            long num2 = 1;
            while(num2 < count) {
                num2 = num2*2;
                index2 ++;
            }
            String secondSubnet = IPv4NumToString(midSubnet) + "/" + String.valueOf(32-index2);
//            System.out.println(secondSubnet);
            ipSubnetList.add(secondSubnet);
        }

        if((index == index2) &&  (index == (32-maxsize))) {
            ipSubnetList.clear();
            String subnetString = IPv4NumToString(subnet) + "/" + String.valueOf(maxsize - 1);
            ipSubnetList.add(subnetString);
        }

        //long mid = subnet +
        return ipSubnetList;
    }


    public static long[] getIpStartEndBySubnetMask(String subnet, String mask) {
        long[] ipStartEnd = new long[2];
        long subnetLong = IPv4StringToNum(subnet);
        ipStartEnd[0] = subnetLong & -1L << (int)(32L - Long.parseLong(mask));
        ipStartEnd[1] = ipStartEnd[0] | ~(-1L << (int)(32L - Long.parseLong(mask)));
        return ipStartEnd;
    }

    public static final int[] CIDR2MASK = new int[] { 0x00000000, 0x80000000,
            0xC0000000, 0xE0000000, 0xF0000000, 0xF8000000, 0xFC000000,
            0xFE000000, 0xFF000000, 0xFF800000, 0xFFC00000, 0xFFE00000,
            0xFFF00000, 0xFFF80000, 0xFFFC0000, 0xFFFE0000, 0xFFFF0000,
            0xFFFF8000, 0xFFFFC000, 0xFFFFE000, 0xFFFFF000, 0xFFFFF800,
            0xFFFFFC00, 0xFFFFFE00, 0xFFFFFF00, 0xFFFFFF80, 0xFFFFFFC0,
            0xFFFFFFE0, 0xFFFFFFF0, 0xFFFFFFF8, 0xFFFFFFFC, 0xFFFFFFFE,
            0xFFFFFFFF };

    public static boolean hasIntersection(String srcIps, String dstIp) {
        if(AliStringUtils.isEmpty(srcIps) ||
                srcIps.equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_ANY)) {
            return true;
        }
        String[] srcIpList = srcIps.split(",");
        boolean flag = false;
        for(String srcIp : srcIpList) {
            Long srcIpStart = IPv4StringToNum(getStartIpFromIpAddress(srcIp));
            Long srcIpEnd = IPv4StringToNum(getEndIpFromIpAddress(srcIp));
            Long dstIpStart = IPv4StringToNum(getStartIpFromIpAddress(dstIp));
            Long dstIpEnd = IPv4StringToNum(getEndIpFromIpAddress(dstIp));

            if (srcIpEnd < dstIpStart) {
                continue;
            } else if (srcIpStart > dstIpEnd) {
                continue;
            }
            flag = true;
        }
        return flag;
    }
    /**
     * 检测IP地址是否为IPv6地址
     * @param str
     * @return
     */
    public static boolean isIPv6(String str) {
        return AliStringUtils.isEmpty(str)?false:str.contains(":");
    }

    public static boolean isIPv6Subnet(String str) {
        if(!isIPv6(str)) {
            return false;
        }
        return str.contains("/");
    }

    public static boolean isIPv6Range(String str) {
        if(!isIPv6(str)) {
            return false;
        }
        return str.contains("-");
    }
    /**
     * 获取ipv6范围第一个ipv6地址
     * @param ipv6
     * @return
     */
    public static String getRangeStartIPv6(String ipv6){
        String arr[] = ipv6.split("-");
        return arr[0];
    }
    /**
     *  获取ipv6范围最后一个ipv6地址
     * @param ipv6
     * @return
     */
    public static String getRangeEndIPv6(String ipv6){
        String arr[] = ipv6.split("-");
        return arr[1];
    }
    /**
     * 获取ipv6子网ipv6地址
     * @param ipv6
     * @return
     */
    public static String getIpSegmentStartIPv6(String ipv6){
        String[] ipv6s = ipv6.split("/");
        return ipv6s[0];
    }


    /**
     *  获取ipv6子网掩码
     * @param ipv6
     * @return
     */
    public static String getIpSegmentMaskIPv6(String ipv6){
        String[] ipv6s = ipv6.split("/");
        return ipv6s[1];
    }

    /**
     * 根据掩码位 获取反掩码对应的ip
     * @param maskBit
     * @return
     */
    public static String getInverseMaskIpByMaskBit(String maskBit){
        String ipMask = getMaskByMaskBit(maskBit);
        Long ipLong  = IPv4StringToNum(ipMask);
        long regularWildcard = ipLong ^ 0xFFFFFFFFL;
        return IPv4NumToString(regularWildcard);
    }

    /**
     * 判断两个IP是否有交集，支持逗号分隔的多IP
     * IP支持单IP/子网/范围
     *
     * @param firstIp
     * @param secendIp
     * @return
     */
    public static boolean isIntersection(String firstIp, String secendIp){

        String[] firstIpList = firstIp.split(",");
        String[] secendIpList = secendIp.split(",");
        for(String first : firstIpList) {
            Long firstIpStart = IPv4StringToNum(getStartIpFromIpAddress(first));
            Long firstIpEnd = IPv4StringToNum(getEndIpFromIpAddress(first));
            for(String secend : secendIpList) {
                Long secendIpStart = IPv4StringToNum(getStartIpFromIpAddress(secend));
                Long secendIpEnd = IPv4StringToNum(getEndIpFromIpAddress(secend));

                if (firstIpEnd < secendIpStart) {
                    continue;
                }
                if (firstIpStart > secendIpEnd) {
                    continue;
                }
                return true;
            }
        }
        return false;

    }

    public static void main(String[] args) {
        String ipRange = "128.0.0.0-255.255.255.255";
        String ipRange2 = "172.16.11.125-172.16.11.144";
        String ipRange3 = "192.168.1.0-192.168.1.255";

        List<String> ipSubnetList = IpUtils.convertRangeToSubnet(ipRange);
        System.out.println("ip范围是：" + ipRange);
        System.out.println("转换成最小子网为：" + JSONObject.toJSONString(ipSubnetList));
        List<String> ipSubnetList2 = IpUtils.convertRangeToSubnet(ipRange2);
        System.out.println("ip范围是：" + ipRange2);
        System.out.println("转换成最小子网为：" + JSONObject.toJSONString(ipSubnetList2));
        List<String> ipSubnetList3 = IpUtils.convertRangeToSubnet(ipRange3);
        System.out.println("ip范围是：" + ipRange3);
        System.out.println("转换成最小子网为：" + JSONObject.toJSONString(ipSubnetList3));
    }
}
