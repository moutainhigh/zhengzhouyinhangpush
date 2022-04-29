package com.abtnetworks.totems.vender.cisco.routing;

import com.abtnetworks.totems.common.utils.IPUtil;
import com.abtnetworks.totems.vender.cisco.security.SecurityCiscoASA99Impl;
import org.apache.commons.lang3.StringUtils;

import java.util.Map;

public class StaticRoutingCiscoASA99Impl extends SecurityCiscoASA99Impl {
    /**
     *
     * @param isVsys
     * @param vsysName
     * @param map
     * @param args
     * @return #进入全局模式
     * enable
     * #进入配置模式
     * configure terminal  changeto context system
     */
    @Override
    public String generateRoutePreCommandline(Boolean isVsys, String vsysName, Map<String, Object> map, String[] args) {
        StringBuilder sb = new StringBuilder();
//        sb.append("enable").append(StringUtils.LF);
        sb.append("configure terminal").append(StringUtils.LF);
        if (isVsys != null && isVsys && StringUtils.isNotEmpty(vsysName)) {
            sb.append(String.format("changeto context %s",vsysName)).append(StringUtils.LF);
        }
        return sb.toString();
    }

    /**
     *
     * @param map
     * @param args
     * @return write
     * 退到根视图
     * end
     */
    @Override
    public String generateRoutePostCommandline(Map<String, Object> map, String[] args) {
        StringBuilder sb = new StringBuilder();
        sb.append("end").append(StringUtils.LF);
        sb.append("write").append(StringUtils.LF);
        return sb.toString();
    }

    /**
     *
     * @param ip IP
     * @param mask 掩码
     * @param nextHop 下一跳
     * @param netDoor 出接口
     * @param distance 管理距离
     * @param weight 权重
     * @param description 描述
     * @param map 名称
     * @param args
     * @return route outside 10.10.10.0 255.255.255.0 192.168.1.1 3
     * @throws Exception
     */
    @Override
    public String generateIpv4RoutingCommandLine(String ip, Integer mask, String nextHop, String netDoor, String distance, String weight, String description, Map<String, Object> map, String[] args) throws Exception {
        StringBuilder sb = new StringBuilder();
//        String maskIpByMask = TotemsIp4Utils.getMaskIpByMask(mask);
        String maskIpByMask = IPUtil.getMaskByMaskBit(String.valueOf(mask));
        sb.append(String.format("route %s %s %s ",netDoor,ip,maskIpByMask));
        if (StringUtils.isNotEmpty(nextHop)){
            sb.append(String.format("%s ",nextHop));
        }
        if (StringUtils.isNotEmpty(distance)){
            sb.append(String.format("%s ",distance));
        }
        sb.append(StringUtils.LF);
        return sb.toString();
    }

    /**
     *
     * @param ip IP
     * @param mask 掩码
     * @param nextHop 下一跳
     * @param netDoor 出接口
     * @param distance 优先级
     * @param weight 权重
     * @param description 描述
     * @param map
     * @param args
     * @return no route outside 10.10.10.0 255.255.255.0 192.168.1.1 3
     * @throws Exception
     */
    @Override
    public String deleteIpv4RoutingCommandLine(String ip, Integer mask, String nextHop, String netDoor, String distance, String weight, String description, Map<String, Object> map, String[] args) throws Exception {
        StringBuilder sb = new StringBuilder();
//        String maskIpByMask = TotemsIp4Utils.getMaskIpByMask(mask);
        String maskIpByMask = IPUtil.getMaskByMaskBit(String.valueOf(mask));
        sb.append(String.format("no route %s %s %s ",netDoor,ip,maskIpByMask));
        if (StringUtils.isNotEmpty(nextHop)){
            sb.append(String.format("%s ",nextHop));
        }
        if (StringUtils.isNotEmpty(distance)){
            sb.append(String.format("%s ",distance));
        }
        sb.append(StringUtils.LF);
        return sb.toString();
    }

    /**
     *
     * @param ip IP
     * @param mask 掩码
     * @param nextHop 下一跳
     * @param netDoor 出接口
     * @param distance 优先级
     * @param weight 权重
     * @param description 描述
     * @param map
     * @param args
     * @return ipv6 route if_name ::/0 next_hop_ipv6_addr
     * @throws Exception
     */
    @Override
    public String generateIpv6RoutingCommandLine(String ip, Integer mask, String nextHop, String netDoor, String distance, String weight, String description, Map<String, Object> map, String[] args) throws Exception {
        StringBuilder sb = new StringBuilder();
//        String maskIpByMask = TotemsIp4Utils.getMaskIpByMask(mask);
//        String maskIpByMask = IPUtil.getMaskByMaskBit(String.valueOf(mask));
        sb.append(String.format("ipv6 route %s %s/%s ",netDoor,ip,mask));
        if (StringUtils.isNotEmpty(nextHop)){
            sb.append(String.format("%s ",nextHop));
        }
        sb.append(StringUtils.LF);
        return sb.toString();
    }

    /**
     *
     * @param ip
     * @param mask
     * @param nextHop
     * @param netDoor
     * @param distance
     * @param weight
     * @param description
     * @param map
     * @param args
     * @return no route outside 10.10.10.0 255.255.255.0 192.168.1.1 3
     * @throws Exception
     */
    @Override
    public String deleteIpv6RoutingCommandLine(String ip, Integer mask, String nextHop, String netDoor, String distance, String weight, String description, Map<String, Object> map, String[] args) throws Exception {
        StringBuilder sb = new StringBuilder();
//        String maskIpByMask = TotemsIp4Utils.getMaskIpByMask(mask);
//        String maskIpByMask = IPUtil.getMaskByMaskBit(String.valueOf(mask));
        sb.append(String.format("no ipv6 route %s %s/%s ",netDoor,ip,mask));
        if (StringUtils.isNotEmpty(nextHop)){
            sb.append(String.format("%s ",nextHop));
        }
        sb.append(StringUtils.LF);
        return sb.toString();
    }
}
