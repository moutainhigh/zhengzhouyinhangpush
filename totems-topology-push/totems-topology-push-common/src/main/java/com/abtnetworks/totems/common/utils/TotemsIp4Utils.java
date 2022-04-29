package com.abtnetworks.totems.common.utils;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TotemsIp4Utils {
    private static final Logger log = LoggerFactory.getLogger(TotemsIp4Utils.class);
    private static final String REGEX_IP = "(\\d|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])(\\.(\\d|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])){3}";
    private static final String REGEX_IP_RANGE = "(\\d|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])(\\.(\\d|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])){3}-(\\d|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])(\\.(\\d|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])){3}";
    private static final String REGEX_IP_SUBNET = "(\\d|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])(\\.(\\d|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])){3}/(3[0-2]|[1-2]\\d|\\d)";
    private static final Pattern IP4_PATTERN = Pattern.compile("^(\\d|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])(\\.(\\d|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])){3}$");
    private static final Pattern IP4_RANGE_PATTERN = Pattern.compile("^(\\d|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])(\\.(\\d|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])){3}-(\\d|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])(\\.(\\d|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])){3}$");
    private static final Pattern IP4_SUBNET_PATTERN = Pattern.compile("^(\\d|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])(\\.(\\d|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])){3}/(3[0-2]|[1-2]\\d|\\d)$");
    private static final Pattern IP4_MATCH_PATTERN = Pattern.compile("(\\d|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])(\\.(\\d|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])){3}");
    private static final Map<Integer, String> MASK_IP4_MAP = new HashMap(33);
    private static final Map<String, Integer> IP4_MASK_MAP = new HashMap(33);
    private static final Map<Integer, String> WILDCARD_MASK_IP4_MAP;
    private static final Map<String, Integer> IP4_WILDCARD_MASK_MAP;

    private TotemsIp4Utils() {
    }

    public static boolean isIp4(String ip4) {
        return IP4_PATTERN.matcher(ip4).matches();
    }

    public static boolean isIp4Mask(String ipMask) {
        return IP4_SUBNET_PATTERN.matcher(ipMask).matches();
    }

    public static boolean isIp4Range(String ipRange) {
        boolean isIpRange = IP4_RANGE_PATTERN.matcher(ipRange).matches();
        if (isIpRange) {
            String[] ipSectionSplit = ipRange.split("-");
            isIpRange = ipToLong(ipSectionSplit[0]) < ipToLong(ipSectionSplit[1]);
        }

        return isIpRange;
    }

    public static String getMaskIpByMask(int mask) {
        if (MASK_IP4_MAP.containsKey(mask)) {
            return (String)MASK_IP4_MAP.get(mask);
        } else {
            throw new IllegalArgumentException("请求参数: [" + mask + "]不在范围0-32之间");
        }
    }

    public static String getMaskIpByMask(String mask) {
        if (StringUtils.isBlank(mask)) {
            throw new IllegalArgumentException("请求参数: [" + mask + "]不能为空");
        } else {
            int maskInt = Integer.parseInt(mask);
            return getMaskIpByMask(maskInt);
        }
    }

    public static int getMaskByMaskIp(String maskIp4) {
        if (StringUtils.isBlank(maskIp4)) {
            throw new IllegalArgumentException("请求参数: [" + maskIp4 + "]不能为空");
        } else if (IP4_MASK_MAP.containsKey(maskIp4)) {
            return (Integer)IP4_MASK_MAP.get(maskIp4);
        } else {
            throw new IllegalArgumentException("请求参数: [" + maskIp4 + "]不是ipv4掩码");
        }
    }

    public static String getWildcardMaskIpByMask(int wildcardMask) {
        if (WILDCARD_MASK_IP4_MAP.containsKey(wildcardMask)) {
            return (String)WILDCARD_MASK_IP4_MAP.get(wildcardMask);
        } else {
            throw new IllegalArgumentException("请求参数: [" + wildcardMask + "]不在范围0-32之间");
        }
    }

    public static int getWildcardMaskByMaskIp(String wildcardMaskIp4) {
        if (StringUtils.isBlank(wildcardMaskIp4)) {
            throw new IllegalArgumentException("请求参数: [" + wildcardMaskIp4 + "]不能为空");
        } else if (IP4_WILDCARD_MASK_MAP.containsKey(wildcardMaskIp4)) {
            return (Integer)IP4_WILDCARD_MASK_MAP.get(wildcardMaskIp4);
        } else {
            throw new IllegalArgumentException("请求参数: [" + wildcardMaskIp4 + "]不是ipv4反掩码");
        }
    }

    public static long ipToLong(String strIp) {
        String[] ip = strIp.split("\\.");
        return Long.parseLong(ip[0]) << 24 | Long.parseLong(ip[1]) << 16 | Long.parseLong(ip[2]) << 8 | Long.parseLong(ip[3]);
    }

    public static String longToIp(long longIp) {
        return (longIp >>> 24) + "." + ((longIp & 16777215L) >>> 16) + "." + ((longIp & 65535L) >>> 8) + "." + (longIp & 255L);
    }

    public static int ipToUnsignedInt(String strIp) {
        String[] ip = strIp.split("\\.");
        return Integer.parseInt(ip[0]) << 24 | Integer.parseInt(ip[1]) << 16 | Integer.parseInt(ip[2]) << 8 | Integer.parseInt(ip[3]);
    }

    public static String unsignedIntToIp(int intIp) {
        return (intIp >>> 24) + "." + ((intIp & 16777215) >>> 16) + "." + ((intIp & '\uffff') >>> 8) + "." + (intIp & 255);
    }

    public static long unsignedIntToLong(int a) {
        return (long)a & 4294967295L;
    }

    public static int compareUnsignedInt(int x, int y) {
        return Integer.compareUnsigned(x, y);
    }

    public static int[] getIpStartEndInt(String ipSection) {
        int[] ipStartEnd = new int[2];
        if (isIp4Range(ipSection)) {
            String[] ipSectionSplit = ipSection.split("-");
            ipStartEnd[0] = ipToUnsignedInt(ipSectionSplit[0]);
            ipStartEnd[1] = ipToUnsignedInt(ipSectionSplit[1]);
        } else if (isIp4Mask(ipSection)) {
            ipStartEnd = getIpStartEndIntByIpMask(ipSection);
        } else {
            if (!isIp4(ipSection)) {
                throw new IllegalArgumentException("ipSection:" + ipSection + "得格式不正确");
            }

            ipStartEnd[0] = ipToUnsignedInt(ipSection);
            ipStartEnd[1] = ipStartEnd[0];
        }

        return ipStartEnd;
    }

    public static Pair<String, String> getIpStartEndString(String ipSection) {
        if (isIp4Range(ipSection)) {
            String[] ipStartEndString = ipSection.split("-");
            return Pair.of(ipStartEndString[0], ipStartEndString[1]);
        } else if (isIp4Mask(ipSection)) {
            int[] ipStartEndInt = getIpStartEndIntByIpMask(ipSection);
            return Pair.of(unsignedIntToIp(ipStartEndInt[0]), unsignedIntToIp(ipStartEndInt[1]));
        } else if (isIp4(ipSection)) {
            return Pair.of(ipSection, ipSection);
        } else {
            throw new IllegalArgumentException("ipSection:" + ipSection + "得格式不正确");
        }
    }

    public static int[] getIpStartEndIntByIpMask(String ipMask) {
        int[] ipStartEnd = new int[2];
        String[] ipMaskSplit = ipMask.split("/");
        int ip = ipToUnsignedInt(ipMaskSplit[0]);
        int mask = -1 << 32 - Integer.parseInt(ipMaskSplit[1]);
        ipStartEnd[0] = ip & mask;
        ipStartEnd[1] = ipStartEnd[0] | ~mask;
        return ipStartEnd;
    }

    public static boolean isIpInIpSection(String ip, String ipSection) {
        int[] ipStartEnd = getIpStartEndInt(ipSection);
        int ipInt = ipToUnsignedInt(ip);
        return Integer.compareUnsigned(ipInt, ipStartEnd[0]) >= 0 && Integer.compareUnsigned(ipInt, ipStartEnd[1]) <= 0;
    }

    public static String getSegment(String ip, int mask) {
        int ipInt = ipToUnsignedInt(ip);
        int shift = 32 - mask;
        int startIP = ipInt >>> shift << shift;
        return unsignedIntToIp(startIP) + "/" + mask;
    }

    public static List<String> convertRangeToSubnet(String ipRange) {
        String[] ipStartEnd = ipRange.split("-");
        long start = ipToLong(ipStartEnd[0]);
        long end = ipToLong(ipStartEnd[1]);

        ArrayList pairs;
        int suffixLength;
        for(pairs = new ArrayList(); end >= start; start = (long)((double)start + Math.pow(2.0D, (double)suffixLength))) {
            suffixLength = 0;
            int lowestSetBit = (int)Long.lowestOneBit(start) - 1;
            if (lowestSetBit != 0) {
                if (lowestSetBit == -1) {
                    lowestSetBit = 32;
                }

                double x = Math.log((double)(end - start + 1L)) / Math.log(2.0D);
                int maxDiff = (int)Math.floor(x);
                suffixLength = Math.min(lowestSetBit, maxDiff);
            }

            String ip = longToIp(start);
            pairs.add(ip + "/" + (32 - suffixLength));
        }

        return pairs;
    }

    public static List<String> getWildcardRanges(String baseIp, String wildcardMask) {
        int wildcardMaskBit;
        if (IP4_WILDCARD_MASK_MAP.containsKey(wildcardMask)) {
            log.debug("通配符掩码为反掩码");
            wildcardMaskBit = (Integer)IP4_WILDCARD_MASK_MAP.get(wildcardMask);
            return Collections.singletonList(getSegment(baseIp, wildcardMaskBit));
        } else {
            wildcardMaskBit = ipToUnsignedInt(wildcardMask);
            int maxRangeCount = 256;
            long realRangeCount = getWildcardRangeCount(wildcardMask);
            if (realRangeCount > (long)maxRangeCount) {
                log.warn("Too many wildcard ranges: {}. Limited to {}. Base IP/wildcardMask: {}/{}", new Object[]{realRangeCount, Integer.valueOf(maxRangeCount), baseIp, wildcardMask});
            }

            int resultCount = maxRangeCount;
            if (realRangeCount < (long)maxRangeCount) {
                resultCount = (int)realRangeCount;
            }

            List<String> resultList = new ArrayList(resultCount);
            int maskBit = ~wildcardMaskBit;
            int baseBit = ipToUnsignedInt(baseIp) & maskBit;
            int trailingBitCount = Integer.numberOfTrailingZeros(maskBit);
            long myGap = 1L;
            if (realRangeCount > (long)maxRangeCount) {
                myGap = realRangeCount / (long)maxRangeCount;
            }

            for(long nextIndex = 0L; nextIndex < realRangeCount; nextIndex += myGap) {
                int result = baseBit;
                int indexOneCount = Integer.bitCount((int)nextIndex);
                int indexMask = 1;
                int baseMask = 1 << trailingBitCount;

                for(int myOneCount = 0; myOneCount < indexOneCount; baseMask <<= 1) {
                    if (baseMask == 0) {
                        throw new RuntimeException("Coding error. oldBase should have enough zero bit.");
                    }

                    if ((baseMask & maskBit) == 0) {
                        if ((indexMask & (int)nextIndex) != 0) {
                            ++myOneCount;
                            result |= baseMask;
                        }

                        indexMask <<= 1;
                    }
                }

                resultList.add(unsignedIntToIp(result) + "/" + (32 - trailingBitCount));
            }

            return resultList;
        }
    }

    public static long getWildcardRangeCount(String wildcardMask) {
        int mask = ipToUnsignedInt(wildcardMask);
        int bitCount = Integer.bitCount(mask);
        int trailingBitCount = Integer.numberOfTrailingZeros(~mask);
        int rangeBitCount = bitCount - trailingBitCount;
        return 1L << rangeBitCount;
    }

    public static String getMatcherIP4(String ipStr) {
        String ip4 = null;
        Matcher m = IP4_MATCH_PATTERN.matcher(ipStr);
        if (m.find()) {
            ip4 = m.group();
        }

        return ip4;
    }

    static {
        MASK_IP4_MAP.put(0, "0.0.0.0");
        MASK_IP4_MAP.put(1, "128.0.0.0");
        MASK_IP4_MAP.put(2, "192.0.0.0");
        MASK_IP4_MAP.put(3, "224.0.0.0");
        MASK_IP4_MAP.put(4, "240.0.0.0");
        MASK_IP4_MAP.put(5, "248.0.0.0");
        MASK_IP4_MAP.put(6, "252.0.0.0");
        MASK_IP4_MAP.put(7, "254.0.0.0");
        MASK_IP4_MAP.put(8, "255.0.0.0");
        MASK_IP4_MAP.put(9, "255.128.0.0");
        MASK_IP4_MAP.put(10, "255.192.0.0");
        MASK_IP4_MAP.put(11, "255.224.0.0");
        MASK_IP4_MAP.put(12, "255.240.0.0");
        MASK_IP4_MAP.put(13, "255.248.0.0");
        MASK_IP4_MAP.put(14, "255.252.0.0");
        MASK_IP4_MAP.put(15, "255.254.0.0");
        MASK_IP4_MAP.put(16, "255.255.0.0");
        MASK_IP4_MAP.put(17, "255.255.128.0");
        MASK_IP4_MAP.put(18, "255.255.192.0");
        MASK_IP4_MAP.put(19, "255.255.224.0");
        MASK_IP4_MAP.put(20, "255.255.240.0");
        MASK_IP4_MAP.put(21, "255.255.248.0");
        MASK_IP4_MAP.put(22, "255.255.252.0");
        MASK_IP4_MAP.put(23, "255.255.254.0");
        MASK_IP4_MAP.put(24, "255.255.255.0");
        MASK_IP4_MAP.put(25, "255.255.255.128");
        MASK_IP4_MAP.put(26, "255.255.255.192");
        MASK_IP4_MAP.put(27, "255.255.255.224");
        MASK_IP4_MAP.put(28, "255.255.255.240");
        MASK_IP4_MAP.put(29, "255.255.255.248");
        MASK_IP4_MAP.put(30, "255.255.255.252");
        MASK_IP4_MAP.put(31, "255.255.255.254");
        MASK_IP4_MAP.put(32, "255.255.255.255");
        MASK_IP4_MAP.forEach((k, v) -> {
            Integer var10000 = (Integer)IP4_MASK_MAP.put(v, k);
        });
        WILDCARD_MASK_IP4_MAP = new HashMap(33);
        IP4_WILDCARD_MASK_MAP = new HashMap(33);
        WILDCARD_MASK_IP4_MAP.put(0, "255.255.255.255");
        WILDCARD_MASK_IP4_MAP.put(1, "127.255.255.255");
        WILDCARD_MASK_IP4_MAP.put(2, "63.255.255.255");
        WILDCARD_MASK_IP4_MAP.put(3, "31.255.255.255");
        WILDCARD_MASK_IP4_MAP.put(4, "15.255.255.255");
        WILDCARD_MASK_IP4_MAP.put(5, "7.255.255.255");
        WILDCARD_MASK_IP4_MAP.put(6, "3.255.255.255");
        WILDCARD_MASK_IP4_MAP.put(7, "1.255.255.255");
        WILDCARD_MASK_IP4_MAP.put(8, "0.255.255.255");
        WILDCARD_MASK_IP4_MAP.put(9, "0.127.255.255");
        WILDCARD_MASK_IP4_MAP.put(10, "0.63.255.255");
        WILDCARD_MASK_IP4_MAP.put(11, "0.31.255.255");
        WILDCARD_MASK_IP4_MAP.put(12, "0.15.255.255");
        WILDCARD_MASK_IP4_MAP.put(13, "0.7.255.255");
        WILDCARD_MASK_IP4_MAP.put(14, "0.3.255.255");
        WILDCARD_MASK_IP4_MAP.put(15, "0.1.255.255");
        WILDCARD_MASK_IP4_MAP.put(16, "0.0.255.255");
        WILDCARD_MASK_IP4_MAP.put(17, "0.0.127.255");
        WILDCARD_MASK_IP4_MAP.put(18, "0.0.63.255");
        WILDCARD_MASK_IP4_MAP.put(19, "0.0.31.255");
        WILDCARD_MASK_IP4_MAP.put(20, "0.0.15.255");
        WILDCARD_MASK_IP4_MAP.put(21, "0.0.7.255");
        WILDCARD_MASK_IP4_MAP.put(22, "0.0.3.255");
        WILDCARD_MASK_IP4_MAP.put(23, "0.0.1.255");
        WILDCARD_MASK_IP4_MAP.put(24, "0.0.0.255");
        WILDCARD_MASK_IP4_MAP.put(25, "0.0.0.127");
        WILDCARD_MASK_IP4_MAP.put(26, "0.0.0.63");
        WILDCARD_MASK_IP4_MAP.put(27, "0.0.0.31");
        WILDCARD_MASK_IP4_MAP.put(28, "0.0.0.15");
        WILDCARD_MASK_IP4_MAP.put(29, "0.0.0.7");
        WILDCARD_MASK_IP4_MAP.put(30, "0.0.0.3");
        WILDCARD_MASK_IP4_MAP.put(31, "0.0.0.1");
        WILDCARD_MASK_IP4_MAP.put(32, "0.0.0.0");
        WILDCARD_MASK_IP4_MAP.forEach((k, v) -> {
            Integer var10000 = (Integer)IP4_WILDCARD_MASK_MAP.put(v, k);
        });
    }
}