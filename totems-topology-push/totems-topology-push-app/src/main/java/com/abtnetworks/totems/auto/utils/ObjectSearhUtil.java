package com.abtnetworks.totems.auto.utils;

import com.abtnetworks.totems.common.utils.IPUtil;
import com.abtnetworks.totems.whale.common.CommonRangeStringDTO;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 对象搜索工具类
 *
 * @author luwei
 * @date 2019/10/8
 */
public class ObjectSearhUtil {

    /**
     * IP4转换
     * @param content_text
     * @return
     */
    public static List<CommonRangeStringDTO> getRangeByContent(String content_text) {

        if (StringUtils.isBlank(content_text)) {
            return Collections.emptyList();
        }
        List<CommonRangeStringDTO> addressRanges = new ArrayList<>();
        String[] arr = content_text.split(",");
        for (int i = 0; i < arr.length; i++) {
            String content = arr[i].trim();
            if (!IPUtil.isIP(content) && !IPUtil.isIPRange(content) && !IPUtil.isIPSegment(content)) {
                continue;
            }
            CommonRangeStringDTO dto = new CommonRangeStringDTO();

            if (IPUtil.isIP(content)) {
                dto.setStart(content);
                dto.setEnd(content);
            } else if (IPUtil.isIPRange(content)) {
                String startIp = IPUtil.getStartIpFromRange(content);
                String endIp = IPUtil.getEndIpFromRange(content);
                dto.setStart(startIp);
                dto.setEnd(endIp);
            } else if (IPUtil.isIPSegment(content)) {
                //获取ip
                String ip = IPUtil.getIpFromIpSegment(content);
                //获取网段数
                String maskBit = IPUtil.getMaskBitFromIpSegment(content);
                long[] ipArr = IPUtil.getIpStartEndBySubnetMask(ip, maskBit);
                String startIp = IPUtil.longToIP(ipArr[0]);
                String endIp = IPUtil.longToIP(ipArr[1]);
                dto.setStart(startIp);
                dto.setEnd(endIp);
            }
            addressRanges.add(dto);
        }

        return addressRanges;
    }

    public static List<CommonRangeStringDTO> getIP6RangeByContent(String content_text) {
        if (StringUtils.isBlank(content_text)) {
            return Collections.emptyList();
        }
        List<CommonRangeStringDTO> addressRanges = new ArrayList<>();
        String[] arr = content_text.split(",");
        for (int i = 0; i < arr.length; i++) {
            String content = arr[i].trim();
            if (!IPUtil.isIPv6(content) && !IPUtil.isIPv6Range(content) && !IPUtil.isIPv6Segment(content)) {
                continue;
            }
            CommonRangeStringDTO dto = new CommonRangeStringDTO();

            if (IPUtil.isIPv6(content)) {
                dto.setStart(content);
                dto.setEnd(content);
            } else if (IPUtil.isIPv6Range(content)) {
                String[] ip6Arr = content.split("-");
                String startIp = ip6Arr[0];
                String endIp = ip6Arr[1];
                dto.setStart(startIp);
                dto.setEnd(endIp);
            } else if (IPUtil.isIPv6Segment(content)) {
                String[] ipv6Arr = IPUtil.getIPv6RangeBySubnet(content);
                dto.setStart(ipv6Arr[0]);
                dto.setEnd(ipv6Arr[1]);
            }
            addressRanges.add(dto);
        }

        return addressRanges;
    }
}
