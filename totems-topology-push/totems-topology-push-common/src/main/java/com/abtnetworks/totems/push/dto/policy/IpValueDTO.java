package com.abtnetworks.totems.push.dto.policy;

import com.abtnetworks.totems.common.utils.IpUtils;
import lombok.Data;

/**
 * @author Administrator
 * @Title:
 * @Description: 请写注释类
 * @date 2021/7/13
 */
@Data
public class IpValueDTO {

    long start;
    long end;
    String ipString;

    @Override
    public String toString() {
        if(start != end) {
            return String.format("%s-%s", IpUtils.IPv4NumToString(start), IpUtils.IPv4NumToString(end));
        } else {
            return String.format("%s", IpUtils.IPv4NumToString(start));
        }
    }

    IpValueDTO() {}

    public IpValueDTO(String ip) {
        ipString = ip;
        String startIp = IpUtils.getStartIpFromIpAddress(ip);
        String endIp = IpUtils.getEndIpFromIpAddress(ip);
        start = IpUtils.IPv4StringToNum(startIp);
        end = IpUtils.IPv4StringToNum(endIp);
    }
}
