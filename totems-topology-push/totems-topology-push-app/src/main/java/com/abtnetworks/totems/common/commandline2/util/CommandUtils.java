package com.abtnetworks.totems.common.commandline2.util;

import com.abtnetworks.totems.common.commandline2.constant.CommandConstant;
import com.abtnetworks.totems.common.constants.PolicyConstants;
import com.abtnetworks.totems.common.utils.IpUtils;
import com.abtnetworks.totems.common.utils.PortUtils;
import com.abtnetworks.totems.common.utils.ProtocolUtils;
import com.abtnetworks.totems.disposal.common.QuintupleUtils;
import com.abtnetworks.totems.common.dto.commandline.ServiceDTO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;

import java.math.BigInteger;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author zc
 * @date 2020/01/08
 */
@Slf4j
public class CommandUtils {

    /**
     * 最大分解所得ip数量
     */
    private static final int MAX_IP = 256;

    /**
     * 最大名字的长度
     */
    public static final int MAX_NAME_LENGTH = 24;

    /**
     * 将ip段转换成单个ipList
     * 单个ip超过最大允许数量停止转换
     * @param ipAddr
     * @return
     */
    public static List<String> ipConvert(String ipAddr) {
        List<String> ipList = new ArrayList<>();
        int num = 0;
        String[] ipArray = ipAddr.split(",");
        for (String ip : ipArray) {
            try {
                BigInteger[] bigIntegers = QuintupleUtils.ipv46ToNumRange(ip);
                if (bigIntegers[0].compareTo(bigIntegers[1]) == 0) {
                    ipList.add(QuintupleUtils.bigIntToIpv46(bigIntegers[0]));
                    num ++;
                } else {
                    for (BigInteger i = bigIntegers[0]; i.compareTo(bigIntegers[1]) <= 0 ; i = i.add(BigInteger.ONE)) {
                        ipList.add(QuintupleUtils.bigIntToIpv46(i));
                        num ++;
                        if (num >= MAX_IP) {
                            break;
                        }
                    }
                }
                if (num >= MAX_IP) {
                    break;
                }
            } catch (UnknownHostException e) {
                throw new IllegalArgumentException("ip格式不规范：" + ip, e);
            }
        }
        return ipList;
    }

    /**
     * 指定ip类型
     * @param ipList
     * @return
     */
    public static List<Pair<String, String>> assignIpType(List<String> ipList) {
        return ipList.stream()
                .map(address -> {
                    String type;
                    if(IpUtils.isIPRange(address)) {
                        type = CommandConstant.IPV4_RANGE;
                    } else if (IpUtils.isIPSegment(address)) {
                        type = CommandConstant.IPV4_MASK;
                    } else if (IpUtils.isIP(address)) {
                        type = CommandConstant.IPV4_HOST;
                    } else if (address.contains(":")) {
                        if (address.contains("-")) {
                            type = CommandConstant.IPV6_RANGE;
                        } else if (address.contains("/")) {
                            type = CommandConstant.IPV6_MASK;
                        } else {
                            type = CommandConstant.IPV6_HOST;
                        }
                    } else {
                        log.error("错误的ip[{}]",address);
                        throw new IllegalArgumentException("ip格式不规范：" + address);
                    }
                    return Pair.of(type, address);
                })
                .collect(Collectors.toList());
    }

    /**
     * ip list 按照类型分组
     * @param ipList
     * @return
     */
    public static Map<String, List<String>> groupByIpType(List<String> ipList) {
        Map<String, List<String>> typeIpList = new HashMap<>(6);
        assignIpType(ipList).stream()
                .collect(Collectors.groupingBy(pair -> (String) pair.getKey()))
                .forEach((k,v) -> {
                    List<String> ips = v.stream()
                            .map(Pair::getValue)
                            .collect(Collectors.toList());
                    typeIpList.put(k,ips);
                });
        return typeIpList;
    }

    /**获取服务名称***/
    public static String getServiceName(List<ServiceDTO> serviceDTOList){
        StringBuilder nameSb = new StringBuilder();
        int number = 0;
        for (ServiceDTO dto : serviceDTOList) {
            if (number != 0) {
                nameSb.append("_");
            }
            nameSb.append(getServiceName(dto));
            number++;
        }

        String name = nameSb.toString();
        if(name.length() > MAX_NAME_LENGTH) {
            String shortName = name.substring(0, MAX_NAME_LENGTH-4);
            name = String.format("%s_etc", shortName.substring(0, shortName.lastIndexOf("_")));
        }
        return name;
    }


    public static String getServiceName(ServiceDTO dto) {
        StringBuilder sb = new StringBuilder();
        String protocolString = ProtocolUtils.getProtocolByValue(Integer.valueOf(dto.getProtocol()));
        sb.append(protocolString.toLowerCase());
        if (StringUtils.isBlank(dto.getDstPorts())) {
            return sb.toString();
        }
        if(protocolString.equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_ICMP)){
            return sb.toString();
        }
        if(dto.getDstPorts().equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_ANY) || dto.getDstPorts().equals(PolicyConstants.PORT_ANY)){
            return sb.toString();
        }
        String[] dstPorts = dto.getDstPorts().split(",");
        for (String dstPort : dstPorts) {
            if (PortUtils.isPortRange(dstPort)) {
                String startPort = PortUtils.getStartPort(dstPort);
                String endPort = PortUtils.getEndPort(dstPort);
                sb.append(String.format("_%s_%s", startPort, endPort));
            } else {
                sb.append(String.format("_%s", dstPort));
            }
        }
        return sb.toString().toLowerCase();
    }

}
