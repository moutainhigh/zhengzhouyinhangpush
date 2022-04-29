package com.abtnetworks.totems.command.line.inf.ip;

import com.abtnetworks.totems.command.line.dto.IpAddressRangeDTO;
import com.abtnetworks.totems.command.line.dto.IpAddressSubnetIntDTO;
import com.abtnetworks.totems.command.line.enums.StatusTypeEnum;
import com.abtnetworks.totems.command.line.inf.BasicInterface;

import java.util.Map;

/**
 *
 * ipv6地址一共128位，用十六进制表示，中间用“:”隔开，每一部分是16位。子网掩码只有一种表示（ipv6已经不叫子网掩码，叫前缀，前缀表示网络位，现在为了和ipv4表示对比，姑且这么叫）
 * ipv6的地址，同一ip可以有多种表示形式，不过意义都是一样的，后面的前缀“64”表示128位中的前64位表示网络位，后面的64位表示主机位，只要ip的前64位一样，就说明子网一样，ipv6默认的前缀是64.
 *
 * IPV6 没有子网掩码的概念，页面没有网络号与主机号的概念
 * 取而代之的是 前缀长度 和 接口ID
 * 前缀长度就可以当作子网掩码来理解，接口ID可以当作主机号来理解
 * 比如地址2001:1234:2234:abcd::1/64就表示前缀长度为64位
 *
 * 其实ipv4和ipv6的划分子网方式是一样的，都是用位表示，前n位为网络位，则说明ip只要前n位一样，则子网一样
 *
 * @Description
 * @Version
 * @Created by hw on '2021/4/15 17:27'.
 */
public interface IpAddressV6Interface extends BasicInterface {

    /**
     * 单个 ip
     * ABCD:EF01:2345:6789:ABCD:EF01:2345:6789
     * @param singleIpArray
     * @param map
     * @param args
     * @return
     */
    public String generateSingleIpV6CommandLine(StatusTypeEnum statusTypeEnum, String[] singleIpArray, Map<String, Object> map, String[] args) throws Exception;

    /**
     * Ip 范围
     * @param rangIpArray X:X:X:X:X:X:X:X - X:X:X:X:X:X:X:X 例如：ABCD:EF01:2345:6789:ABCD:EF01:2345:6789 - ADCD:EF01:2125:6189:ABCD:EF01:2345:6789
     * @param map
     * @param args
     * @return
     */
    public String generateRangeIpV6CommandLine(StatusTypeEnum statusTypeEnum,IpAddressRangeDTO[] rangIpArray, Map<String, Object> map, String[] args) throws Exception;

    /**
     * 子网 前缀
     * @param subnetIpArray 子网ip 集合
     * @param sub sub 子接口
     * @param map
     * @param args
     * @return
     * @throws Exception
     */
    public String generateSubnetIntIpV6CommandLine(StatusTypeEnum statusTypeEnum,IpAddressSubnetIntDTO[] subnetIpArray, String sub, Map<String, Object> map, String[] args) throws Exception;

}
