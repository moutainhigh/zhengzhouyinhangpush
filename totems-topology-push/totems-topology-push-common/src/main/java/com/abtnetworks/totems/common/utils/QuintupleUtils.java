package com.abtnetworks.totems.common.utils;

import com.abtnetworks.totems.common.constants.PolicyConstants;
import com.alibaba.fastjson.JSON;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import java.math.BigInteger;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.*;
import java.util.stream.Collectors;


/**
 * 五元组相关的处理工具
 * @author zc
 * @date 2019/11/20
 */
@Slf4j
public class QuintupleUtils {

    /**
     * 端口0-65535正则
     */
    private static final String REGEX_PORT = "(\\d|[1-9]\\d{1,3}|[1-5]\\d{4}|6[0-4]\\d{3}|65[0-4]\\d{2}|655[0-2]\\d|6553[0-5])";

    /**
     * 端口范围正则
     */
    private static final String REGEX_PORT_RANGE = REGEX_PORT + "-" + REGEX_PORT;

    private QuintupleUtils() {
    }

    /**
     * 整数转ip
     * @param bigInteger
     * @return
     */
    public static String bigIntToIpv46(BigInteger bigInteger) throws UnknownHostException {
        byte[] bytes = bigInteger.toByteArray();
        int length = bytes.length;
        if (length < 4) {
            byte[] bytes1 = new byte[4-length];
            bytes = ArrayUtils.insert(0, bytes, bytes1);
        } else if (length >4 && length < 16) {
            if (length == 5 && bytes[0] == 0) {
                bytes = ArrayUtils.remove(bytes, 0);
            } else {
                byte[] bytes1 = new byte[16-length];
                bytes = ArrayUtils.insert(0, bytes, bytes1);
            }
        } else if (length == 17) {
            bytes = ArrayUtils.remove(bytes, 0);
        }
        return InetAddress.getByAddress(bytes).getHostAddress();
    }

    /**
     * 整数转ipv6
     * @param bigInteger
     * @return
     */
    public static String bigIntToIpv6(BigInteger bigInteger) throws UnknownHostException {
        byte[] bytes = bigInteger.toByteArray();
        int length = bytes.length;
        if (length < 16) {
            byte[] bytes1 = new byte[16-length];
            bytes = ArrayUtils.insert(0, bytes, bytes1);
        } else if (length == 17) {
            bytes = ArrayUtils.remove(bytes, 0);
        }
        return InetAddress.getByAddress(bytes).getHostAddress();
    }

    /**
     * ip过滤
     * @param preIpList 过滤前的ip集合
     * @param filterIpList 需要排除掉的ip集合
     * @return
     * @throws Exception 请求数据异常
     */
    public static Tmp<String> ipListFilter(List<String> preIpList, List<String> filterIpList) throws Exception {
        if (preIpList == null || preIpList.size() == 0 || filterIpList == null || filterIpList.size() == 0) {
            throw new Exception("数据异常，preIpList和filterIpList的size必须大于0");
        }

        List<BigInteger[]> preIpBigIntList = ipv46ListToBigIntList(preIpList);
        List<BigInteger[]> filterIpBigIntList = ipv46ListToBigIntList(filterIpList);
        Tmp<BigInteger[]> filterIpTmp = excludeNumRepeat(preIpBigIntList, filterIpBigIntList);

        Tmp<String> result = new Tmp<>();
        result.setPreFilterData(preIpList);
        result.setPostFilterData(ipTypeTransfer(filterIpTmp.getPostFilterData()));
        result.setFilterOutData(ipTypeTransfer(filterIpTmp.getFilterOutData()));
        return result;
    }

    private static List<String> ipTypeTransfer(List<BigInteger[]> preList) throws UnknownHostException {
        List<String> postList = new ArrayList<>();
        for (BigInteger[] bigIntegers : preList) {
            if (bigIntegers[0].compareTo(bigIntegers[1]) == 0) {
                postList.add(bigIntToIpv46(bigIntegers[0]));
            } else {
                postList.add(getIpv46Range(bigIntegers[0], bigIntegers[1]));
            }
        }
        return postList;
    }

    /**
     * 端口过滤
     * @param prePortList  过滤前的port集合
     * @param filterPortList  需要排除掉的port集合
     * @return
     * @throws Exception 请求数据异常
     */
    public static Tmp<String> portListFilter(List<String> prePortList, List<String> filterPortList) throws Exception {
        if (prePortList == null || prePortList.size() == 0 || filterPortList == null || filterPortList.size() == 0) {
            throw new Exception("数据异常，prePortList和filterPortList的size必须大于0");
        }
        boolean checkPrePort = prePortList.stream()
                .allMatch(port -> port.matches(REGEX_PORT) || port.matches(REGEX_PORT_RANGE));
        if (!checkPrePort) {
            throw new Exception("prePortList数据异常:[" + JSON.toJSONString(prePortList) + "]");
        }
        boolean checkFilterPort = filterPortList.stream()
                .allMatch(port -> port.matches(REGEX_PORT) || port.matches(REGEX_PORT_RANGE));
        if (!checkFilterPort) {
            throw new Exception("filterPortList数据异常:[" + JSON.toJSONString(filterPortList) + "]");
        }

        List<BigInteger[]> prePortLongList = portListToBigIntList(prePortList);
        List<BigInteger[]> filterPortLongList = portListToBigIntList(filterPortList);
        Tmp<BigInteger[]> filterPortTmp = excludeNumRepeat(prePortLongList, filterPortLongList);

        Tmp<String> result = new Tmp<>();
        result.setPreFilterData(prePortList);
        result.setPostFilterData(portTypeTransfer(filterPortTmp.getPostFilterData()));
        result.setFilterOutData(portTypeTransfer(filterPortTmp.getFilterOutData()));
        return result;
    }

    private static List<String> portTypeTransfer(List<BigInteger[]> preList) {
        return preList.stream()
                .map(bigIntegers -> {
                    if (bigIntegers[0].compareTo(bigIntegers[1]) == 0) {
                        return bigIntegers[0].toString();
                    } else {
                        return bigIntegers[0] + "-" + bigIntegers[1];
                    }
                })
                .collect(Collectors.toList());
    }

    /**
     * 去除后一个在前一个中重复的元素
     * @param preBigIntList
     * @param filterBigIntList
     * @return
     */
    private static Tmp<BigInteger[]> excludeNumRepeat(List<BigInteger[]> preBigIntList, List<BigInteger[]> filterBigIntList) {
        Tmp<BigInteger[]> tmp = new Tmp<>();
        List<BigInteger[]> postBigIntList = new ArrayList<>(preBigIntList);
        List<BigInteger[]> filterOutList = new ArrayList<>();
        tmp.setPreFilterData(preBigIntList);
        tmp.setPostFilterData(postBigIntList);
        tmp.setFilterOutData(filterOutList);
        int j = 0;
        for (int i = 0; i < postBigIntList.size(); i++) {
            for (; j < filterBigIntList.size();) {
                BigInteger[] preIpBigInt = postBigIntList.get(i);
                BigInteger[] filterIpBigInt = filterBigIntList.get(j);
                if (preIpBigInt[0].compareTo(filterIpBigInt[1]) > 0) {
                    log.trace("不重复，pre的范围大于filter的范围");
                    j++;
                } else {
                    if (preIpBigInt[0].compareTo(filterIpBigInt[0]) >= 0) {
                        log.trace("pre[0]在范围filter中");
                        if (preIpBigInt[1].compareTo(filterIpBigInt[1]) > 0) {
                            log.trace("相交");
                            filterOutList.add(new BigInteger[]{preIpBigInt[0], filterIpBigInt[1]});
                            preIpBigInt[0] = filterIpBigInt[1].add(BigInteger.ONE);
                            j++;
                        } else {
                            log.trace("pre被filter包含");
                            filterOutList.add(preIpBigInt);
                            postBigIntList.remove(i);
                            i--;
                            break;
                        }
                    } else {
                        if (preIpBigInt[1].compareTo(filterIpBigInt[1]) > 0) {
                            log.trace("pre包含filter");
                            filterOutList.add(filterIpBigInt);
                            BigInteger[] add = new BigInteger[]{filterIpBigInt[1].add(BigInteger.ONE), preIpBigInt[1]};
                            preIpBigInt[1] = filterIpBigInt[0].subtract(BigInteger.ONE);
                            postBigIntList.add(i + 1, add);
                            i++;
                        } else {
                            if (preIpBigInt[1].compareTo(filterIpBigInt[0]) >= 0) {
                                log.trace("pre[1]在范围filter中");
                                filterOutList.add(new BigInteger[]{filterIpBigInt[0], preIpBigInt[1]});
                                preIpBigInt[1] = filterIpBigInt[0].subtract(BigInteger.ONE);
                                break;
                            } else {
                                log.trace("不重复，pre的范围小于filter的范围");
                                break;
                            }
                        }
                    }
                }
            }
        }
        return tmp;
    }

    /**
     * 处理子网列表：转换为BigInteger[] -> 根据BigInteger[0]排序 -> 合并重复的段
     * @param ipList
     * @return
     * @throws UnknownHostException
     */
    public static List<BigInteger[]> ipv46ListToBigIntList(List<String> ipList) throws UnknownHostException {
        List<BigInteger[]> bigIntegersList = new ArrayList<>();
        for (String ip : ipList) {
            bigIntegersList.add(ipv46ToNumRange(ip));
        }
        bigIntegersList = bigIntegersList.stream()
                .sorted(Comparator.comparing(bigIntegers -> bigIntegers[0]))
                .collect(Collectors.toList());
        return numMergeRepeat(bigIntegersList);
    }

    /**
     * ip范围转BigInteger数组
     * @param ip
     * @return
     * @throws UnknownHostException
     */
    public static BigInteger[] ipv46ToNumRange(String ip) throws UnknownHostException {
        BigInteger[] startEnd = new BigInteger[2];
        if (ip.contains("/")) {
            String[] ipMask = ip.split("/");
            BigInteger ipNum = ipv46ToNum(ipMask[0]);
            int mask = Integer.parseInt(ipMask[1]);
            int i;
            if (!ipMask[0].contains(":")) {
                //ipv4
                i = 32 - mask;
            } else {
                //ipv6
                i = 128 - mask;
            }
            startEnd[0] = ipNum.shiftRight(i).shiftLeft(i);
            BigInteger num2 = new BigInteger("2");
            startEnd[1] = num2.pow(i).subtract(BigInteger.ONE).add(startEnd[0]);
        } else if (ip.contains("-")) {
            String[] ipMask = ip.split("-");
            startEnd[0] = ipv46ToNum(ipMask[0]);
            startEnd[1] = ipv46ToNum(ipMask[1]);
        } else {
            startEnd[0] = startEnd[1] = ipv46ToNum(ip);
        }
        return startEnd;
    }

    /**
     * 单个ip转BigInteger
     * @param ip
     * @return
     * @throws UnknownHostException
     */
    public static BigInteger ipv46ToNum(String ip) throws UnknownHostException {
        return new BigInteger(1, InetAddress.getByName(ip).getAddress());
    }

    /**
     * 查询ip类型
     * @param ip
     * @return true为ipv4, false为ipv6
     * @throws UnknownHostException
     */
    public static boolean isIpv4(String ip) throws UnknownHostException {
        BigInteger ipBigInt = ipv46ToNum(ip);
        int result = ipBigInt.compareTo(BigInteger.valueOf(0xFFFFFFFFL));
        if (result > 0) {
            return false;
        } else {
            return true;
        }
    }

    public static List<BigInteger[]> portListToBigIntList(List<String> portList) {
        log.trace("转化为long[]并排序");
        List<BigInteger[]> ipToLong = portList.stream()
                .map(port -> {
                    BigInteger[] startEnd = new BigInteger[2];
                    if (port.matches(REGEX_PORT)) {
                        startEnd[0] = startEnd[1] = new BigInteger(port);
                    } else {
                        //port.matches(REGEX_PORT_RANGE)
                        String[] numArray = port.split("-");
                        startEnd[0] = new BigInteger(numArray[0]);
                        startEnd[1] = new BigInteger(numArray[1]);
                    }
                    return startEnd;
                })
                .sorted(Comparator.comparing(startEnd -> startEnd[0]))
                .collect(Collectors.toList());
        return numMergeRepeat(ipToLong);
    }

    /**
     * 合并重复的数字
     * @param bigIntegersList
     * @return
     */
    public static List<BigInteger[]> numMergeRepeat(List<BigInteger[]> bigIntegersList) {
        for (int i = 0; i < bigIntegersList.size() - 1; i++) {
            BigInteger[] before = bigIntegersList.get(i);
            BigInteger[] after = bigIntegersList.get(i + 1);
            if (before[0].compareTo(after[0]) < 0) {
                log.trace("当起始ip不同时");
                if (before[1].compareTo(after[0]) < 0) {
                    log.trace("不重复");
                    if (before[1].add(BigInteger.ONE).compareTo(after[0]) == 0) {
                        log.trace("两个范围刚好相互接壤");
                        before[1] = after[1];
                        bigIntegersList.remove(i + 1);
                        i--;
                    }
                } else if (before[1].compareTo(after[1]) < 0) {
                    log.trace("相交");
                    before[1] = after[1];
                    bigIntegersList.remove(i + 1);
                    i--;
                } else {
                    log.trace("包含");
                    bigIntegersList.remove(i + 1);
                    i--;
                }
            } else if (before[0].compareTo(after[0]) == 0) {
                log.trace("当起始ip相同时");
                if (before[1].compareTo(after[1]) < 0) {
                    bigIntegersList.remove(i);
                } else {
                    bigIntegersList.remove(i + 1);
                }
                i--;
            }
        }
        return bigIntegersList;
    }

    /**
     * ip范围转换
     * @param start
     * @param end
     * @return 当范围刚好是一个子网掩码范围时返回192.168.1.1/24，否则返回192.168.1.1-192.168.1.5
     */
    public static String getIpv46Range(BigInteger start, BigInteger end) throws UnknownHostException {
        int startTrailingZeros = start.getLowestSetBit();
        int endTrailingZeros = end.add(BigInteger.ONE).getLowestSetBit();
        if (startTrailingZeros > 0 && endTrailingZeros > 0) {
            int wildcardMask = startTrailingZeros;
            if (startTrailingZeros > endTrailingZeros) {
                wildcardMask = endTrailingZeros;
            }
            BigInteger convertEnd = end.shiftRight(wildcardMask).shiftLeft(wildcardMask);
            if (convertEnd.compareTo(start) == 0) {
                int bitLength = start.bitLength();
                int mask;
                if (bitLength <=32) {
                    mask = 32 - wildcardMask;
                } else {
                    mask = 128 - wildcardMask;
                }
                return bigIntToIpv46(start) + "/" + mask;
            }
        } else if (startTrailingZeros == -1) {
            if (endTrailingZeros == 32) {
                return PolicyConstants.IPV4_ANY;
            }
            if (endTrailingZeros == 128) {
                return PolicyConstants.IPV6_ANY;
            }
        }

        int result = end.compareTo(BigInteger.valueOf(0xFFFFFFFFL));
        if (result > 0) {
            log.debug("ipv6");
            return bigIntToIpv6(start) + "-" + bigIntToIpv46(end);
        } else {
            log.debug("ipv4");
            return bigIntToIpv46(start) + "-" + bigIntToIpv46(end);
        }
    }

    /**
     * 五元组过滤
     * @param preQuintuple 过滤前的五元组
     * @param filterQuintuple 被过滤的五元组
     * @return 过滤后的五元组集合
     * @throws Exception 请求参数有问题
     */
    public static List<Quintuple> quintupleFilter(Quintuple preQuintuple, Quintuple filterQuintuple) throws Exception {
        List<String> preProtocolList = preQuintuple.getProtocolList();
        Tmp<String> postProtocolTmp = portListFilter(preProtocolList, filterQuintuple.getProtocolList());
        if (postProtocolTmp.getFilterOutData().size() == 0) {
            log.debug("协议不重复");
            return Collections.singletonList(preQuintuple);
        }
        List<String> preSrcPortList = preQuintuple.getSrcPortList();
        Tmp<String> postSrcPortTmp = portListFilter(preSrcPortList, filterQuintuple.getSrcPortList());
        if (postSrcPortTmp.getFilterOutData().size() == 0) {
            log.debug("源端口不重复");
            return Collections.singletonList(preQuintuple);
        }
        List<String> preDstPortList = preQuintuple.getDstPortList();
        Tmp<String> postDstPortTmp = portListFilter(preDstPortList, filterQuintuple.getDstPortList());
        if (postDstPortTmp.getFilterOutData().size() == 0) {
            log.debug("目的端口不重复");
            return Collections.singletonList(preQuintuple);
        }
        List<String> preSrcIpList = preQuintuple.getSrcIpList();
        Tmp<String> postSrcIpTmp = ipListFilter(preSrcIpList, filterQuintuple.getSrcIpList());
        if (postSrcIpTmp.getFilterOutData().size() == 0) {
            log.debug("源ip不重复");
            return Collections.singletonList(preQuintuple);
        }
        List<String> preDstIpList = preQuintuple.getDstIpList();
        Tmp<String> postDstIpTmp = ipListFilter(preDstIpList, filterQuintuple.getDstIpList());
        if (postDstIpTmp.getFilterOutData().size() == 0) {
            log.debug("目的ip不重复");
            return Collections.singletonList(preQuintuple);
        }

        List<Quintuple> quintupleList = new ArrayList<>();
        if (postSrcIpTmp.getPostFilterData().size() != 0) {
            quintupleList.add(new Quintuple(postSrcIpTmp.getPostFilterData(), preDstIpList, preProtocolList, preSrcPortList, preDstPortList));
        }
        if (postDstIpTmp.getPostFilterData().size() != 0) {
            quintupleList.add(new Quintuple(postSrcIpTmp.getFilterOutData(), postDstIpTmp.getPostFilterData(),
                    preProtocolList, preSrcPortList, preDstPortList));
        }
        if (postProtocolTmp.getPostFilterData().size() != 0) {
            quintupleList.add(new Quintuple(postSrcIpTmp.getFilterOutData(), postDstIpTmp.getFilterOutData(),
                    postProtocolTmp.getPostFilterData(), preSrcPortList, preDstPortList));
        }
        if (postSrcPortTmp.getPostFilterData().size() != 0) {
            quintupleList.add(new Quintuple(postSrcIpTmp.getFilterOutData(), postDstIpTmp.getFilterOutData(),
                    postProtocolTmp.getFilterOutData(), postSrcPortTmp.getPostFilterData(), preDstPortList));
        }
        if (postDstPortTmp.getPostFilterData().size() != 0) {
            quintupleList.add(new Quintuple(postSrcIpTmp.getFilterOutData(), postDstIpTmp.getFilterOutData(),
                    postProtocolTmp.getFilterOutData(), postSrcPortTmp.getFilterOutData(), postDstPortTmp.getPostFilterData()));
        }
        return quintupleList;
    }

    /**
     * 多个五元组过滤
     * @param preQuintupleList 过滤前的五元组集合
     * @param filterQuintupleList 被过滤的五元组集合
     * @return 过滤后的五元组集合
     * @throws Exception
     */
    public static List<Quintuple> quintupleFilter(List<Quintuple> preQuintupleList, List<Quintuple> filterQuintupleList) throws Exception {
        List<Quintuple> preQuintupleDistinctList = preQuintupleList.stream().distinct().collect(Collectors.toList());
        List<Quintuple> filterQuintupleDistinctList = filterQuintupleList.stream().distinct().collect(Collectors.toList());

        for (Quintuple filterQuintuple : filterQuintupleDistinctList) {
            List<Quintuple> temporaryQuintupleList = new ArrayList<>();
            for (Quintuple preQuintuple : preQuintupleDistinctList) {
                temporaryQuintupleList.addAll(QuintupleUtils.quintupleFilter(preQuintuple, filterQuintuple));
            }
            preQuintupleDistinctList = temporaryQuintupleList;
        }

        return preQuintupleDistinctList.stream().distinct().collect(Collectors.toList());
    }

    /**
     * 将五元组标准化
     * @param srcIps 例：1.1.1.1,2.2.2.2-3.3.3.3,4.4.4.4/24
     * @param dstIps 同srcIps
     * @param protocols 例：2,3-5,8
     * @param srcPorts 同protocols
     * @param dstPorts 同protocols
     * @return
     */
    public static Quintuple convertQuintuple(String srcIps,
                                             String dstIps,
                                             String protocols,
                                             String srcPorts,
                                             String dstPorts) {
        List<String> srcIpList = new ArrayList<>();
        if (StringUtils.isEmpty(srcIps)) {
            srcIpList.add(PolicyConstants.IPV4_ANY);
        } else {
            srcIpList.addAll(Arrays.asList(srcIps.split(",")));
        }

        List<String> dstIpList = new ArrayList<>();
        if (StringUtils.isEmpty(dstIps)) {
            dstIpList.add(PolicyConstants.IPV4_ANY);
        } else {
            dstIpList.addAll(Arrays.asList(dstIps.split(",")));
        }

        List<String> protocolList = new ArrayList<>();
        if (StringUtils.isEmpty(protocols)) {
            protocolList.add(PolicyConstants.PROTOCOL_ANY);
        } else {
            protocolList.addAll(Arrays.asList(protocols.split(",")));
        }

        List<String> srcPortList = new ArrayList<>();
        if (StringUtils.isEmpty(srcPorts)) {
            srcPortList.add(PolicyConstants.PORT_ANY);
        } else {
            srcPortList.addAll(Arrays.asList(srcPorts.split(",")));
        }

        List<String> dstPortList = new ArrayList<>();
        if (StringUtils.isEmpty(dstPorts)) {
            dstPortList.add(PolicyConstants.PORT_ANY);
        } else {
            dstPortList.addAll(Arrays.asList(dstPorts.split(",")));
        }
        return new Quintuple(srcIpList, dstIpList, protocolList, srcPortList, dstPortList);
    }

    /**
     *  IP地址转换成IP起始字符串数组，固定长度为【2】
     *
     * @param ip ip地址
     * @return IP地址起始字符串数组
     * @throws UnknownHostException
     */
    public static String[] ipv46toIpStartEnd(String ip) throws UnknownHostException {
        String[] startEndStringArray = new String[2];
        BigInteger[] startEndBigInteger = ipv46ToNumRange(ip);
        if (ip.contains(":")) {
            startEndStringArray[0] = bigIntToIpv6(startEndBigInteger[0]);
        } else {
            startEndStringArray[0] = bigIntToIpv46(startEndBigInteger[0]);
        }
        startEndStringArray[1] = bigIntToIpv46(startEndBigInteger[1]);
        return startEndStringArray;
    }


    /**
     * 五元组
     * @author zc
     * @date 2019/11/22
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Quintuple {

        @ApiModelProperty("源IP：元素可以为1.1.1.1， 2.2.2.2/24， 3.3.3.3-4.4.4.4")
        private List<String> srcIpList;

        @ApiModelProperty("目的ip")
        private List<String> dstIpList;

        @ApiModelProperty("协议：元素可以为1， 2-5, 协议非(6、17)时目的端口为0-65535")
        private List<String> protocolList;

        @ApiModelProperty("源端口：元素可以为1， 2-5， srcPort一般是0-65535")
        private List<String> srcPortList;

        @ApiModelProperty("目的端口：元素可以为1， 2-5")
        private List<String> dstPortList;
    }

    @Data
    public static class Tmp<T> {

        @ApiModelProperty("原始数据")
        private List<T> preFilterData;

        @ApiModelProperty("过滤后剩余的数据")
        private List<T> postFilterData;

        @ApiModelProperty("被过滤掉的数据")
        private List<T> filterOutData;
    }

}
