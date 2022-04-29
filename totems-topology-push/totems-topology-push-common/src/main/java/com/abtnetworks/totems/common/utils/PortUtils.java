package com.abtnetworks.totems.common.utils;

import com.abtnetworks.totems.common.constants.PolicyConstants;
import org.apache.commons.lang3.StringUtils;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @Author: wenjiachang
 * @Date: 2019/1/7 11:52
 */
public class PortUtils {

    private static Pattern pattern = Pattern.compile("[0-9]*");

    public static final int DASH_FORMAT = 1;

    public static final int TO_FOMRAT = 2;

    public static final int BLANK_FORMAT = 3;

    public static final  int UNDERLINE_FORMAT = 4;

    public static final int COLON_FORMAT = 5;

    public static String getPortString(String port, int format) {
        String start = "";
        String end = "";
        if (port.equals(PolicyConstants.POLICY_STR_VALUE_ANY)) {
            start = "0";
            end = "65535";
        } else if (isPortRange(port)) {
            start = getStartPort(port);
            end = getEndPort(port);
        } else {
            return port;
        }

        switch (format) {
            case DASH_FORMAT:
                return String.format("%s-%s", start, end);
            case TO_FOMRAT:
                return String.format("%s to %s", start, end);
            case BLANK_FORMAT:
                return String.format("%s %s", start, end);
            case UNDERLINE_FORMAT:
                return String.format("%s_%s", start, end);
            case COLON_FORMAT:
                return String.format("%s:%s", start, end);
        }
        return port;
    }

    public static boolean isValidPort(String portStr) {
        Matcher isNum = pattern.matcher(portStr);
        if (isNum.matches() && portStr.length() < 6 && Integer.valueOf(portStr) >= 0 && Integer.valueOf(portStr) <= 65535) {
            return true;
        }
        return false;
    }


    public static boolean isPortRange(String port) {
        String[] arr = port.split("-");
        if (arr.length == 2) {
            return true;
        }
        return false;
    }

    public static String getStartPort(String portRange) {
        String[] arr = portRange.split("-");
        return arr[0];
    }

    public static String getEndPort(String portRange) {
        String[] arr = portRange.split("-");
        return arr[1];
    }

    public static boolean isValidPortString(String portString) {
        if(!AliStringUtils.isEmpty(portString)){
            String[] ports = portString.split(",");
            for (String port : ports) {
                if(port.contains("-")) {
                    String startPort = getStartPort(port);
                    if (!PortUtils.isValidPort(startPort)) {
                        return false;
                    }
                    String endPort = getEndPort(port);
                    if (!PortUtils.isValidPort(endPort)) {
                        return false;
                    }
                } else {
                    if (!PortUtils.isValidPort(port)) {
                        return false;
                    }
                }
            }
        }
        return true;
    }
    public static Set<String> getSinglePortByRange(String portRange) {
        String startPort = getStartPort(portRange);
        String endPort = getEndPort(portRange);
        Set<String> stringSet = new HashSet<>();
        if(org.apache.commons.lang3.StringUtils.isNumeric(startPort) && StringUtils.isNumeric(endPort)){
            int port = Integer.parseInt(startPort);
            int port2 = Integer.parseInt(endPort);
            if(port2 >= port){
                for (int i = port; i <= port2; i++) {
                    stringSet.add(String.valueOf(i));
                }
            }

        }
        return stringSet;
    }
}

