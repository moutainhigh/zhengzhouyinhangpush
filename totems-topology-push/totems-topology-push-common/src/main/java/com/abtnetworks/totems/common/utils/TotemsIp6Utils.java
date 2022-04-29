package com.abtnetworks.totems.common.utils;


import com.googlecode.ipv6.IPv6Address;
import com.googlecode.ipv6.IPv6Network;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sun.net.util.IPAddressUtil;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

public class TotemsIp6Utils {
    private static final Logger log = LoggerFactory.getLogger(TotemsIp6Utils.class);

    public static boolean isIp6(String ip6) {
        return IPAddressUtil.isIPv6LiteralAddress(ip6);
    }

    public static boolean isIp6Mask(String ip6Mask) {
        if (ip6Mask != null && ip6Mask.contains("/")) {
            String[] ip6MaskSplit = ip6Mask.split("/");
            if (ip6MaskSplit.length == 2 && isIp6(ip6MaskSplit[0]) && StringUtils.isNumeric(ip6MaskSplit[1])) {
                int prefix = Integer.parseInt(ip6MaskSplit[1]);
                return prefix >= 0 && prefix <= 128;
            }
        }

        return false;
    }

    public static boolean isIp6Range(String ip6Range) {
        if (ip6Range != null && ip6Range.contains("-")) {
            String[] ip6RangeSplit = ip6Range.split("-");
            if (ip6RangeSplit.length == 2) {
                return isIp6(ip6RangeSplit[0]) && isIp6(ip6RangeSplit[1]);
            }
        }

        return false;
    }

    public static Pair<String, String> getIp6StartEndString(String ip6Section) {
        if (isIp6(ip6Section)) {
            return Pair.of(ip6Section, ip6Section);
        } else if (isIp6Range(ip6Section)) {
            String[] ip6StartEnd = ip6Section.split("-");
            return Pair.of(ip6StartEnd[0], ip6StartEnd[1]);
        } else if (isIp6Mask(ip6Section)) {
            IPv6Network iPv6Network = IPv6Network.fromString(ip6Section);
            String start = iPv6Network.getFirst().toLongString();
            String end = iPv6Network.getLast().toLongString();
            return Pair.of(start, end);
        } else {
            throw new IllegalArgumentException("ip6Section:" + ip6Section + "得格式不正确");
        }
    }

    public static BigInteger ip6ToBigInteger(String ipv6) {
        return IPv6Address.fromString(ipv6).toBigInteger();
    }

    public static String bigIntegerToIp6(BigInteger bigInteger) {
        return IPv6Address.fromBigInteger(bigInteger).toString();
    }

    public static List<String> convertRangeToSubnet(String ip6Range) {
        Pair<String, String> startEndIpFromIpv6Address = getIp6StartEndString(ip6Range);
        String startIp = (String)startEndIpFromIpv6Address.getLeft();
        String endIp = (String)startEndIpFromIpv6Address.getRight();
        BigInteger start = ip6ToBigInteger(startIp);
        BigInteger end = ip6ToBigInteger(endIp);

        ArrayList pairs;
        int suffixLength;
        for(pairs = new ArrayList(); end.compareTo(start) >= 0; start = start.add(BigInteger.valueOf(2L).pow(suffixLength))) {
            suffixLength = 0;
            int lowestSetBit = start.getLowestSetBit();
            if (lowestSetBit != 0) {
                if (lowestSetBit == -1) {
                    lowestSetBit = 128;
                }

                double x = Math.log(end.subtract(start).add(BigInteger.ONE).doubleValue()) / Math.log(2.0D);
                int maxDiff = (int)Math.floor(x);
                suffixLength = Math.min(lowestSetBit, maxDiff);
            }

            String ip = bigIntegerToIp6(start);
            pairs.add(ip + "/" + (128 - suffixLength));
        }

        return pairs;
    }
}
