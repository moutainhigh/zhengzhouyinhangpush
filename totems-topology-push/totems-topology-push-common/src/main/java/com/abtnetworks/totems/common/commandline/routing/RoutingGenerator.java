package com.abtnetworks.totems.common.commandline.routing;

import com.abtnetworks.totems.common.utils.QuintupleUtils;
import com.abtnetworks.totems.common.dto.commandline.RoutingCommandDTO;

import java.math.BigInteger;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author zc
 * @date 2019/11/15
 */
public interface RoutingGenerator {
    /**
     * 最大分解所得ip数量
     */
    int MAX_IP = 256;

    /**
     * 前置命令行
     * @param routingCommandDTO
     * @return
     */
    String generatePreCommandLine(RoutingCommandDTO routingCommandDTO);

    /**
     * 后置命令行
     * @param routingCommandDTO
     * @return
     */
    String generatePostCommandLine(RoutingCommandDTO routingCommandDTO);

    /**
     * 生成路由命令
     * @param routingCommandDTO
     * @return
     */
    String generatorRoutingCommandLine(RoutingCommandDTO routingCommandDTO) throws UnknownHostException;

    /**
     * 删除路由命令
     * @param routingCommandDTO
     * @return
     */
    String deleteRoutingCommandLine(RoutingCommandDTO routingCommandDTO) throws UnknownHostException;

    /**
     * 将ip段转换成单个ipList
     * 单个ip超过最大允许数量停止转换
     * @param ipAddr
     * @return
     */
    static List<String> ipConvert(String ipAddr) throws UnknownHostException {
        List<String> ipList = new ArrayList<>();
        int num = 0;
        String[] ipArray = ipAddr.split(",");
        for (String ip : ipArray) {
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
        }
        return ipList;
    }
}
