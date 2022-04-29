package com.abtnetworks.totems.disposal.common;

import org.junit.Test;

import java.math.BigInteger;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.List;

/**
 * @author zc
 * @date 2019/11/20
 */
public class QuintupleUtilsTest {

    @Test
    public void ipFilter() throws Exception {
        List<String> preList = Arrays.asList("0.0.1.2","0.0.1.4-0.0.1.8", "1.1.1.1/24","2.2.2.2-2.2.2.6",
                "3.3.3.3-3.3.3.6","f:f::3:a/66");
        List<String> filterList = Arrays.asList("0.0.1.2-0.0.1.6", "1.1.1.16/28","1.1.1.156/28","2.2.2.4-2.2.2.8",
                "3.3.3.2-3.3.3.7","f:f::3:a/68");
        List<String> postList = QuintupleUtils.ipListFilter(preList, filterList).getPostFilterData();
        postList.forEach(System.out::println);
    }

    @Test
    public void portListFilter() throws Exception {
        List<String> preList = Arrays.asList("11","22-33", "44-55");
        List<String> filterList = Arrays.asList("14", "16-25","46-50");
        List<String> postList = QuintupleUtils.portListFilter(preList, filterList).getPostFilterData();
        postList.forEach(System.out::println);
    }

    @Test
    public void ipv46ListToLongList() throws UnknownHostException {
        List<String> ipList = Arrays.asList("192.168.1.2-192.168.1.3", "1.1.1.1/24","2.2.2.2-2.2.2.6","192.168.1.4-192.168.1.5");
        List<BigInteger[]> result = QuintupleUtils.ipv46ListToBigIntList(ipList);
        for (BigInteger[] longs : result) {
            System.out.println(QuintupleUtils.bigIntToIpv46(longs[0]));
            System.out.println(QuintupleUtils.bigIntToIpv46(longs[1]));
        }
    }

    @Test
    public void getIpv46Range() throws UnknownHostException {
//        String ip = "192.168.1.0/24";
//        String ip = "0.0.0.0/0";
//        String ip = "ff::3d/66";
//        String ip = "::/0";
        String ip = "0.0.0.0-ff::0";
        BigInteger[] bigIntegers = QuintupleUtils.ipv46ToNumRange(ip);
        System.out.println(QuintupleUtils.getIpv46Range(bigIntegers[0], bigIntegers[1]));
    }


    @Test
    public void ipToNumTransfer() throws UnknownHostException {
        String ip = "ff::f";
        BigInteger[] bigIntegers = QuintupleUtils.ipv46ToNumRange(ip);
        System.out.println(bigIntegers[0].toString(2));
        System.out.println(bigIntegers[1].toString(2));
    }

}