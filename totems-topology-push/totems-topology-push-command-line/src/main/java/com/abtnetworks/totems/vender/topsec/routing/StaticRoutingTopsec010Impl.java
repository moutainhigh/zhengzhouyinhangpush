package com.abtnetworks.totems.vender.topsec.routing;

import com.abtnetworks.totems.common.utils.IPUtil;
import com.abtnetworks.totems.vender.topsec.TOS_010.SecurityTopsec010Impl;
import org.apache.commons.lang3.StringUtils;

import java.util.Map;

public class StaticRoutingTopsec010Impl extends SecurityTopsec010Impl {

    /**
     *
     * @param isVsys
     * @param vsysName
     * @param map
     * @param args
     * @return
     */
    @Override
    public String generateRoutePreCommandline(Boolean isVsys, String vsysName, Map<String, Object> map, String[] args) {

        return "";
    }

    /**
     *
     * @param map
     * @param args
     * @return save
     */
    @Override
    public String generateRoutePostCommandline(Map<String, Object> map, String[] args) {
        StringBuilder sb = new StringBuilder();
        sb.append("save").append(StringUtils.LF);
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
     * @param map 路由实例名称
     * @param args
     * @return network route add dst 202.103.96.0/24 gw 192.168.90.1 dev feth2 metric 1
     * @throws Exception
     */
    @Override
    public String generateIpv4RoutingCommandLine(String ip, Integer mask, String nextHop, String netDoor, String distance, String weight, String description, Map<String, Object> map, String[] args) throws Exception {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("network route add dst %s/%d ",ip,mask));
        if (StringUtils.isNotEmpty(nextHop)){
            sb.append(String.format("gw %s ",nextHop));
        }
        if (StringUtils.isNotEmpty(netDoor)){
            sb.append(String.format("dev %s ",netDoor));
        }
        if (StringUtils.isNotEmpty(distance)){
            sb.append(String.format("metric %s ",distance));
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
     * @return network route delete dst 202.103.96.0/24 gw 192.168.90.1 dev ipsec1 metric 23
     * @throws Exception
     */
    @Override
    public String deleteIpv4RoutingCommandLine(String ip, Integer mask, String nextHop, String netDoor, String distance, String weight, String description, Map<String, Object> map, String[] args) throws Exception {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("network route delete dst %s/%d ",ip,mask));
        if (StringUtils.isNotEmpty(nextHop)){
            sb.append(String.format("gw %s ",nextHop));
        }else {
            sb.append("gw 0.0.0.0 ");
        }
        if (StringUtils.isNotEmpty(netDoor)){
            sb.append(String.format("dev %s ",netDoor));
        }
        if (StringUtils.isNotEmpty(distance)){
            sb.append(String.format("metric %s ",distance));
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
     * @return network route add family ipv6 dst 2fbb:aabb::/64 gw 3faa::aaaa dev eth4 metric 1
     * @throws Exception
     */
    @Override
    public String generateIpv6RoutingCommandLine(String ip, Integer mask, String nextHop, String netDoor, String distance, String weight, String description, Map<String, Object> map, String[] args) throws Exception {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("network route add family ipv6 dst %s/%d ",ip,mask));
        if (StringUtils.isNotEmpty(nextHop)){
            sb.append(String.format("gw %s ",nextHop));
        }
        if (StringUtils.isNotEmpty(netDoor)){
            sb.append(String.format("dev %s ",netDoor));
        }
        if (StringUtils.isNotEmpty(distance)){
            sb.append(String.format("metric %s ",distance));
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
     * @return network route delete dst 202.103.96.0/24 gw 192.168.90.1 dev ipsec1 metric 23
     * @throws Exception
     */
    @Override
    public String deleteIpv6RoutingCommandLine(String ip, Integer mask, String nextHop, String netDoor, String distance, String weight, String description, Map<String, Object> map, String[] args) throws Exception {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("network route delete family ipv6 dst %s/%d ",ip,mask));
        if (StringUtils.isNotEmpty(nextHop)){
            sb.append(String.format("gw %s ",nextHop));
        }else {
            sb.append("gw 0.0.0.0 ");
        }
        if (StringUtils.isNotEmpty(netDoor)){
            sb.append(String.format("dev %s ",netDoor));
        }
        if (StringUtils.isNotEmpty(distance)){
            sb.append(String.format("metric %s ",distance));
        }
        sb.append(StringUtils.LF);
        return sb.toString();
    }
}
