package com.abtnetworks.totems.auto.utils;

import com.abtnetworks.totems.common.constants.PolicyConstants;
import com.abtnetworks.totems.common.enums.IpTypeEnum;
import com.abtnetworks.totems.common.utils.QuintupleUtils;
import com.abtnetworks.totems.common.utils.TotemsIp4Utils;
import com.abtnetworks.totems.common.utils.TotemsIp6Utils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import sun.net.util.IPAddressUtil;

import java.math.BigInteger;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.UnknownHostException;
import java.util.Enumeration;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 用正则表达式判断是否为IP
 * Created by hanyu on 2017/5/26.
 */
@Slf4j
public class IpAddress {
    /**
     * IP格式正则
     */
    private static final String REGEX_IP = "(\\d|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])(\\.(\\d|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])){3}";

    /**
     * IP范围格式正则
     */
    private static final String REGEX_IP_RANGE = REGEX_IP + "-" + REGEX_IP;

    /**
     * IP子网格式正则
     */
    private static final String REGEX_IP_SUBNET = REGEX_IP + "/(3[0-2]|[1-2]\\d|[1-9])";

    public static boolean isIP(String addr) {
        if(addr==null || addr.equals("")){
            return false;
        }
        if (addr.length() < 7 || addr.length() > 15 || "".equals(addr)) {
            return false;
        }
        /**
         * 判断IP格式和范围
         */
        String rexp = "^(1\\d{2}|2[0-4]\\d|25[0-5]|[1-9]\\d|[1-9]|0)\\."
                +"(1\\d{2}|2[0-4]\\d|25[0-5]|[1-9]\\d|\\d|0)\\."
                +"(1\\d{2}|2[0-4]\\d|25[0-5]|[1-9]\\d|\\d|0)\\."
                +"(1\\d{2}|2[0-4]\\d|25[0-5]|[1-9]\\d|\\d|0)$";

        Pattern pat = Pattern.compile(rexp);

        Matcher mat = pat.matcher(addr);

        boolean ipAddress = mat.find();

        return ipAddress;
    }

    /**
     * 判断是IPv6地址
     * @param ip
     * @return
     */
    public static boolean isIPv6(String ip) {
        if (StringUtils.isBlank(ip)) {
            return false;
        }
        return IPAddressUtil.isIPv6LiteralAddress(ip);
    }

    public static boolean isSubnetMask(String subnetMask) {
        return subnetMask == null ? false : subnetMask.matches(REGEX_IP_SUBNET);
    }

    public static boolean isIpRange(String ipRange) {
        boolean isIpRange = ipRange == null ? false : ipRange.matches(REGEX_IP_RANGE);
        if (isIpRange) {
            long[] ipStartEnd = getIpStartEnd(ipRange);
            isIpRange = ipStartEnd[0] < ipStartEnd[1];
        }
        return isIpRange;
    }

    /**
     * 数字子网掩码校验
     * @param value
     * @return
     */
    public static boolean isNumNetmask(String value){
        if(value==null || value.length()==0){
            return false;
        }

        if(!isNumeric(value)){
            return false;
        }
        int num = Integer.parseInt(value);

        if(num < 8 || num > 32){
            return false;
        }
        return true;
    }

    public static boolean isNumeric(String str){
        for (int i = 0; i < str.length(); i++){
            System.out.println(str.charAt(i));
            if (!Character.isDigit(str.charAt(i))){
                return false;
            }
        }
        return true;
    }


    public static boolean isDottedNetmaskV4(String value){
        if(value==null || value.equals("")){
            return false;
        }
        String rexp = "(\\d{1,3})\\.(\\d{1,3})\\.(\\d{1,3})\\.(\\d{1,3})";
        boolean result = value.matches(rexp);
        String[] split = value.split("\\.");
        if(result){
            final boolean b = (split[0].equals("0")) || ((split[0].equals("0")) && (split[1].equals("0")) && (split[2].equals("0")) && (split[3].equals("0")));
            if (b) {
                return false;
            }
            int lastBit = 1; // accept 255.255.255.255
            int cnt = 0;
            for (int i=0; i <4; i++) {
                int mask = Integer.parseInt(split[i], 10);
                for (int j=7; j >= 0; j--) {
                    double pow = Math.pow(2, j);
                    int n = (int) pow;
                    int i1 = mask & n;
                    int bitOn = i1 > 0 ? 1 : 0;
                    if (lastBit != bitOn) {
                        lastBit = bitOn;
                        cnt++;    //no change or Just change once,from 1 to 0  : subnet mask
                    }
                }
            }
            if(cnt > 1) {
                return false;
            } else {
                return true;
            }
        }
        return false;
    }

    /**
     * ip地址转成long型数字
     * 将IP地址转化成整数的方法如下：
     * 1、通过String的split方法按.分隔得到4个长度的数组
     * 2、通过左移位操作（<<）给每一段的数字加权，第一段的权为2的24次方，第二段的权为2的16次方，第三段的权为2的8次方，最后一段的权为1
     * @param strIp
     * @return
     */
    public static long ipToLong(String strIp) {
        String[]ip = strIp.split("\\.");
        return (Long.parseLong(ip[0]) << 24) + (Long.parseLong(ip[1]) << 16) + (Long.parseLong(ip[2]) << 8) + Long.parseLong(ip[3]);
    }


    /**
     * 将十进制整数形式转换成127.0.0.1形式的ip地址
     * 将整数形式的IP地址转化成字符串的方法如下：
     * 1、将整数值进行右移位操作（>>>），右移24位，右移时高位补0，得到的数字即为第一段IP。
     * 2、通过与操作符（&）将整数值的高8位设为0，再右移16位，得到的数字即为第二段IP。
     * 3、通过与操作符吧整数值的高16位设为0，再右移8位，得到的数字即为第三段IP。
     * 4、通过与操作符吧整数值的高24位设为0，得到的数字即为第四段IP。
     * @param longIp
     * @return
     */
    public static String longToIP(long longIp) {
        StringBuffer sb = new StringBuffer("");
        // 直接右移24位
        sb.append(String.valueOf((longIp >>> 24)));
        sb.append(".");
        // 将高8位置0，然后右移16位
        sb.append(String.valueOf((longIp & 0x00FFFFFF) >>> 16));
        sb.append(".");
        // 将高16位置0，然后右移8位
        sb.append(String.valueOf((longIp & 0x0000FFFF) >>> 8));
        sb.append(".");
        // 将高24位置0
        sb.append(String.valueOf((longIp & 0x000000FF)));
        return sb.toString();
    }

    /**
     * 根据子网和掩码，返回起点和终点ip
     *
     * @param subnet
     * @param mask
     * @return
     */
    public static long[] getIpStartEndBySubnetMask(String subnet, String mask) {
        long[] ipStartEnd = new long[2];
        long subnetLong = ipToLong(subnet);
        ipStartEnd[0] = subnetLong & (0xFFFFFFFF << (32 - Long.parseLong(mask)));
        ipStartEnd[1] = (ipStartEnd[0] | ~(0xFFFFFFFF << (32 - Long.parseLong(mask))));
        return ipStartEnd;
    }

    /**
     * 根据ip范围或子网掩码，返回ip的起点和终点
     *
     * @param ipSection
     * @return
     */
    public static long[] getIpStartEnd(String ipSection) {
    	//String strSub=ipSection.substring(0, ipSection.lastIndexOf("("));
        long[] ipStartEnd = new long[2];
        String[] ipSectionSplit;
        if (ipSection.matches(REGEX_IP_RANGE)) {
            ipSectionSplit = ipSection.split("-");
            ipStartEnd[0] = ipToLong(ipSectionSplit[0]);
            ipStartEnd[1] = ipToLong(ipSectionSplit[1]);
        } else if (ipSection.matches(REGEX_IP_SUBNET)) {
            ipSectionSplit = ipSection.split("/");
            ipStartEnd = getIpStartEndBySubnetMask(ipSectionSplit[0], ipSectionSplit[1]);
        } else if(ipSection.endsWith(")")&&ipSection.startsWith("(")){
        	String  startIp=ipSection.substring(1, ipSection.lastIndexOf("."))+".0";
        	String  endIp=ipSection.substring(1, ipSection.lastIndexOf("."))+".255";
        	ipStartEnd[0]=ipToLong(startIp);
        	ipStartEnd[1]=ipToLong(endIp);
        } else if((ipSection.substring(0, ipSection.lastIndexOf("("))).length()>0){//mg(129.17.100.0/24)
        	String st=ipSection.substring(ipSection.indexOf("("), ipSection.lastIndexOf("."));
			String  startIp=st.substring(1, st.length())+".0";
        	String  endIp=st.substring(1, st.length())+".255";
        	ipStartEnd[0]=ipToLong(startIp);
        	ipStartEnd[1]=ipToLong(endIp);
        }else {
        	throw new IllegalArgumentException("ipSection:" + ipSection + "得格式不正确");
        }
        return ipStartEnd;
    }

    public static String getIpSection(long start, long end) {
        if(start == end) {
            return longToIP(start);
        }
        return longToIP(start)+"-"+longToIP(end);
    }

    /**
     * 根据ipv6范围或子网掩码，返回ip的起点和终点
     *
     * @param ip6  ff::0  ff::0/64   ff::0-ff::ff
     * @return 解析异常返回null
     */
    public static Pair<byte[], byte[]> getIp6StartEndBytes(String ip6) {
        Pair<byte[], byte[]> pair = null;
        try {
            if (StringUtils.contains(ip6, ":")) {
                String[] ipSectionSplit;
                if (StringUtils.contains(ip6, "-")) {
                    ipSectionSplit = ip6.split("-");
                    if (ipSectionSplit.length == 2 && isIPv6(ipSectionSplit[0]) && isIPv6(ipSectionSplit[1])) {
                        byte[] startByte = InetAddress.getByName(ipSectionSplit[0]).getAddress();

                        byte[] endByte = InetAddress.getByName(ipSectionSplit[1]).getAddress();
                        pair = Pair.of(startByte, endByte);
                    }
                } else if (StringUtils.contains(ip6, "/")) {
                    ipSectionSplit = ip6.split("/");
                    if (ipSectionSplit.length == 2 && isIPv6(ipSectionSplit[0]) && isNumeric(ipSectionSplit[1])) {
                        int mask = Integer.parseInt(ipSectionSplit[1]);
                        int shift = 128 - mask;
                        byte[] ip6Byte = InetAddress.getByName(ipSectionSplit[0]).getAddress();
                        BigInteger ip6BigInteger = new BigInteger(1, ip6Byte);
                        byte[] startByte = ip6BigInteger.shiftRight(shift).shiftLeft(shift).toByteArray();
                        byte[] endByte = ip6BigInteger.shiftRight(shift).add(BigInteger.ONE).shiftLeft(shift).subtract(BigInteger.ONE).toByteArray();
                        pair = Pair.of(getIpv6Byte(startByte), getIpv6Byte(endByte));
                    }
                } else if(isIPv6(ip6)) {
                    byte[] ip6Byte = InetAddress.getByName(ip6).getAddress();
                    pair = Pair.of(ip6Byte, ip6Byte);
                }
            }
        } catch (UnknownHostException e) {
            log.error("ipv6转换bytes异常", e);
            return null;
        }
        return pair;
    }

    /**
     * 获取ipv6字符串
     * @param ip
     * @return
     */
    public static Pair<String, String> getIp6StartEndString(String ip) {
        try {
            Pair<byte[], byte[]> pair = getIp6StartEndBytes(ip);
            if (pair != null) {
                String startIpv6 = InetAddress.getByAddress(pair.getLeft()).getHostAddress();
                String endIpv6 = InetAddress.getByAddress(pair.getRight()).getHostAddress();
                return Pair.of(startIpv6,endIpv6);
            }
        } catch (Exception e) {
            log.error("ipv6转换String异常", e);
        }
        return null;
    }

    public static String bytesToIp6(byte[] bytes) {
        try {
            return InetAddress.getByAddress(bytes).getHostAddress();
        } catch (UnknownHostException e) {
            log.error("ipv6转换String异常", e);
            return null;
        }
    }


    public static byte[] getIpv6Byte(byte[] bytes) {
        int length = bytes.length;
        int a = 0;
        if (length < 16) {
            a = 16-length;
            byte[] bytes1 = new byte[a];
            bytes = ArrayUtils.insert(0, bytes, bytes1);
        } else if (length == 17) {
            bytes = ArrayUtils.remove(bytes, 0);
        }
        return bytes;
    }

    /**
     * 判断ip是否在ipSection的范围中
     *
     * @param ip
     * @param ipSection
     * @return
     */
    public static boolean isIpInRange(String ip, String ipSection) {
        long[] ipStartEnd = getIpStartEnd(ipSection);
        long ipLong = ipToLong(ip);
        return ipLong >= ipStartEnd[0] && ipLong <= ipStartEnd[1];
    }

    /**
     * 判断两段ip是否有交集，有则为false
     *
     * @param ipRange
     * @param ipSection
     * @return
     */
    public static boolean isIpRangeOutRange(String ipRange, String ipSection) {
        long[] ipStartEnd1 = getIpStartEnd(ipRange);
        long[] ipStartEnd2 = getIpStartEnd(ipSection);
        return ipStartEnd1[0] > ipStartEnd2[1] || ipStartEnd1[1] < ipStartEnd2[0];
    }

    /**
     * 判断ipRange的ip范围是否在ipSession范围内，是则返回true
     *
     * @param ipRange
     * @param ipSection
     * @return
     */
    public static boolean isIpRangeInRange(String ipRange, String ipSection) {
        long[] ipStartEnd1 = getIpStartEnd(ipRange);
        long[] ipStartEnd2 = getIpStartEnd(ipSection);
        return ipStartEnd1[0] >= ipStartEnd2[0] && ipStartEnd1[1] <= ipStartEnd2[1];
    }

    /**
     * 尽量获取本机非127.0.0.1的IP
     *
     * @return
     * @throws Exception
     */
    public static InetAddress getLocalHostLANAddress() throws Exception {
        try {
            InetAddress candidateAddress = null;
            // 遍历所有的网络接口
            Enumeration ifaces = NetworkInterface.getNetworkInterfaces();
            while (ifaces.hasMoreElements()) {
                NetworkInterface iface = (NetworkInterface) ifaces.nextElement();
                // 在所有的接口下再遍历IP
                Enumeration inetAddrs = iface.getInetAddresses();
                while (inetAddrs.hasMoreElements()) {
                    InetAddress inetAddr = (InetAddress) inetAddrs.nextElement();
                    if (!inetAddr.isLoopbackAddress()) {// 排除loopback类型地址
                        if (inetAddr.isSiteLocalAddress()) {
                            // 如果是site-local地址，就是它了
                            return inetAddr;
                        } else if (candidateAddress == null) {
                            // site-local类型的地址未被发现，先记录候选地址
                            candidateAddress = inetAddr;
                        }
                    }
                }
            }
            if (candidateAddress != null) {
                return candidateAddress;
            }
            // 如果没有发现 non-loopback地址.只能用最次选的方案
            return InetAddress.getLocalHost();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 计算并比较2个IP是否相同
     * @param srcIp
     * @param dstIp
     * @return
     */
    public static boolean isSameIp(String srcIp, String dstIp, Integer ipType){
        boolean result = false;
        if(StringUtils.isBlank(srcIp) && StringUtils.isBlank(dstIp)){
            return true;
        }
        if(StringUtils.equalsAnyIgnoreCase(srcIp, PolicyConstants.POLICY_STR_VALUE_ANY) && StringUtils.equalsAnyIgnoreCase(dstIp, PolicyConstants.POLICY_STR_VALUE_ANY)){
            return true;
        }
        if(StringUtils.containsIgnoreCase(srcIp, PolicyConstants.POLICY_STR_VALUE_ANY) || StringUtils.containsIgnoreCase(dstIp, PolicyConstants.POLICY_STR_VALUE_ANY)){
            return false;
        }
        if((StringUtils.isBlank(srcIp) && StringUtils.isNotBlank(dstIp)) || (StringUtils.isBlank(dstIp) && StringUtils.isNotBlank(srcIp))){
            return false;
        }
        if(IpTypeEnum.IPV4.getCode().equals(ipType)){
            Pair<String, String> srcPair =  TotemsIp4Utils.getIpStartEndString(srcIp);
            Pair<String, String> dstPair =  TotemsIp4Utils.getIpStartEndString(dstIp);
            if(srcPair.compareTo(dstPair) == 0 ){
                result = true;
            }
        } else if(IpTypeEnum.IPV6.getCode().equals(ipType)){
            Pair<String, String> srcPair = TotemsIp6Utils.getIp6StartEndString(srcIp);
            Pair<String, String> dstPair = TotemsIp6Utils.getIp6StartEndString(dstIp);
            if(srcPair.compareTo(dstPair) == 0 ){
                result = true;
            }
        } else {
            log.error("当前IP类型不支持比较");
        }
        return result;
    }

    public static void main(String[] args) throws Exception{

        Pair<String, String> ip6StartEnd = IpAddress.getIp6StartEndString("2001:0db8:3c4d:0015:0000:0000:1a2f:1a2b/18");
//        System.out.println(ip6StartEnd.getLeft());
//        System.out.println(ip6StartEnd.getRight());
//        Pair<byte[], byte[]> byteString = getIp6StartEndBytes("2001:0:0:0:0:0:0:0");
//        Pair<byte[], byte[]> byteString2 = getIp6StartEndBytes("2001:3fff:ffff:ffff:ffff:ffff:ffff:ffff");
//        String start = InetAddress.getByAddress(byteString.getLeft()).getHostAddress();
//        String start2 = InetAddress.getByAddress(byteString2.getLeft()).getHostAddress();
//
//        System.out.println(start);
//
//        System.out.println(start2);

        System.out.println(QuintupleUtils.ipv46ToNumRange("2001:0db8:3c4d:0015:0000:0000:1a2f:1a2b/18")[0]);
        System.out.println(QuintupleUtils.ipv46ToNumRange("2001:0db8:3c4d:0015:0000:0000:1a2f:1a2b/18")[1]);


        System.out.println(QuintupleUtils.bigIntToIpv6(new BigInteger("42540488161975842760550356425300246528")));
        System.out.println(QuintupleUtils.bigIntToIpv6(new BigInteger("42541786236190476467457489049382551551")));


    }

}