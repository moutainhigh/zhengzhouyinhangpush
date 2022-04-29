package com.abtnetworks.totems.common.commandline.util;

import com.abtnetworks.totems.common.constants.PolicyConstants;
import com.abtnetworks.totems.common.enums.IpTypeEnum;
import com.abtnetworks.totems.common.utils.IdGen;
import com.abtnetworks.totems.common.utils.IpUtils;
import com.abtnetworks.totems.common.utils.PortUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;

import java.util.HashMap;
import java.util.Map;

public class LineNameUtils {

    private static Logger logger = Logger.getLogger(LineNameUtils.class);

//    private static String HILLSTONE_IP = "host_,range_,net_,1";
//
//    private static String HILLSTONE_Service = "tcp_,udp_,icmp,-";
//
//    private static String FORTINET_Service = "host_,range_,net_,1";
//
//    private static String FORTINET_IP = "tcp_,udp_,icmp,-";
//
//    private static Map map = new HashMap<String,String>();
//
//    static {
//        map.put("HILLSTONE_IP", HILLSTONE_IP);
//        map.put("HILLSTONE_Service", HILLSTONE_Service);
//        map.put("FORTINET_IP", FORTINET_IP);
//        map.put("FORTINET_Service", FORTINET_Service);
//    }

    public static String getServiceNameKevin(String key, String protocolString,String dstPorts,Integer idleTimeout) {
//        logger.info("进入utils获取ip名称");
        String name = null;
        String servicePrefix = "TCP,UDP,ICMP,-";
        String[] serviceArr = servicePrefix.split(",");
        String prefix;
        String line = "-";
        if (serviceArr.length == 4) {
            line = serviceArr[3];
        }
        if (protocolString.equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_TCP)) {
            prefix = serviceArr[0];
        } else if (protocolString.equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_UDP)) {
            prefix = serviceArr[1];
        } else {
            prefix = serviceArr[2];
        }
        String[] dstPortsArr = dstPorts.split(",");
        StringBuilder namesb = new StringBuilder();
        for (String dstPort : dstPortsArr) {
            namesb.append("_");
            if (PortUtils.isPortRange(dstPort)) {
                String start = PortUtils.getStartPort(dstPort);
                String end = PortUtils.getEndPort(dstPort);
                namesb.append(String.format("%s%s%s", start, line, end));
            }else {
                namesb.append(String.format("%s", dstPort));
            }
        }
        if (idleTimeout != null) {
            name = String.format("\"%s%sL\"", prefix, namesb);
        }else {
            name = String.format("\"%s%s\"", prefix, namesb);
        }

        return name;
    }

    public static String getIPNameKevin(Integer ipType, String address, boolean isSrcIp, String srcIpSystem, String dstIpSystem, int length, int index){
//        logger.info("进入utils获取service名称,ipType:" + ipType);
        String ipPrefix = ",,,3";
        String[] arr = ipPrefix.split(",");
        String isIP = arr[0];//单个ip前缀
        String iPRange = arr[1];//ip范围前缀
        String iPSegment = arr[2];//ip子网前缀
        String random = "";//使用自定义名称时，是否使用随机数
        if (arr.length == 4) {
            random = arr[3];
        }
        if (isSrcIp && StringUtils.isNotEmpty(srcIpSystem)) {
            //名称加随机数
            if ("1".equals(random)) {
                return srcIpSystem + "_" + IdGen.getRandomNumberString();
            } else if ("2".equals(random)) {//名称加ip
                return srcIpSystem + "_" + address;
            } else {
                //多个IP，名称加序号
                if (length == 1) {
                    return srcIpSystem;
                } else {
                    return srcIpSystem + "_" + index;
                }
            }
        } else if (!isSrcIp && StringUtils.isNotEmpty(dstIpSystem)) {
            if ("1".equals(random)) {
                return dstIpSystem + "_" + IdGen.getRandomNumberString();
            } else if ("2".equals(random)) {
                return dstIpSystem + "_" + address;
            } else {
                if (length == 1) {
                    return dstIpSystem;
                } else {
                    return dstIpSystem + "_" + index;
                }
            }
        }
        if (ipType.intValue() == IpTypeEnum.IPV4.getCode()) {
            if (IpUtils.isIPSegment(address)) {
                return iPSegment + address;
            } else if (IpUtils.isIPRange(address)) {
                String startIp = IpUtils.getStartIpFromIpAddress(address);
                String endIp = IpUtils.getEndIpFromIpAddress(address);
                String[] startArr = startIp.split("\\.");
                String[] endArr = endIp.split("\\.");
                if (startArr[0].equals(endArr[0])
                        && startArr[1].equals(endArr[1])
                        && startArr[2].equals(endArr[2])) {
                    return iPRange + startIp + "-" + endArr[3];
                }else {
                    return iPRange + address;
                }
            } else {
                return isIP + address;
            }
        } else if (ipType.intValue() == IpTypeEnum.IPV6.getCode()) {
            // ipv6
            if (address.contains("/")) {
                return iPSegment + address;
            } else if (address.contains("-")) {
                return iPRange + address;
            } else {
                return isIP + address;
            }
        } else {
            // 目的地址是URL类型
            return address;
        }
    }
}
