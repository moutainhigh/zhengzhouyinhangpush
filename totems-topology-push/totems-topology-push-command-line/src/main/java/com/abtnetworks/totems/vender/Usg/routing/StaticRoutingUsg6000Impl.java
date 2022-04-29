package com.abtnetworks.totems.vender.Usg.routing;

import com.abtnetworks.totems.common.lang.TotemsStringUtils;
import com.abtnetworks.totems.vender.Usg.security.SecurityUsg6000Impl;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.Map;

public class StaticRoutingUsg6000Impl extends SecurityUsg6000Impl {

    /**
     *
     * @param isVsys
     * @param vsysName
     * @param map
     * @param args
     * @return system-view  switch vsys 虚系统名称
     */
    @Override
    public String generateRoutePreCommandline(Boolean isVsys, String vsysName, Map<String, Object> map, String[] args) {
        StringBuilder sb = new StringBuilder();
        sb.append("system-view").append(StringUtils.LF);
        if (isVsys != null && isVsys && StringUtils.isNotEmpty(vsysName)) {
            sb.append(String.format("switch vsys %s",vsysName)).append(StringUtils.LF);
            sb.append("system-view").append(StringUtils.LF);
        }
        return sb.toString();
    }

    /**
     *
     * @param map
     * @param args
     * @return save
     * y
     */
    @Override
    public String generateRoutePostCommandline(Map<String, Object> map, String[] args) {
        StringBuilder sb = new StringBuilder();
        sb.append("return").append(StringUtils.LF);
        sb.append("save").append(StringUtils.LF);
        sb.append("y").append(StringUtils.LF);
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
     * @return ip route-static 源VPN实例 目的地址 子网掩码 出接口 优先级 描述信息
     * [sysname] ip route-static vpn-instance vpn1 10.1.1.1 32 Vlanif1 preference 32 description ads
     * @throws Exception
     */
    @Override
    public String generateIpv4RoutingCommandLine(String ip, Integer mask, String nextHop, String netDoor, String distance, String weight, String description, Map<String, Object> map, String[] args) throws Exception {
        StringBuilder sb = new StringBuilder();
        sb.append("ip route-static ");

        if (map.containsKey("srcVpn") && !map.containsKey("hasVsys")){
            sb.append(String.format("vpn-instance %s ",map.get("srcVpn")));
        }

        sb.append(String.format("%s %d ",ip,mask));

        if (map.containsKey("dstVpn") && !map.containsKey("hasVsys")){
            sb.append(String.format("vpn-instance %s ",map.get("dstVpn")));
        }

        if (StringUtils.isNotEmpty(netDoor)){
            sb.append(String.format("%s ",netDoor));
        }

        if (StringUtils.isNotEmpty(nextHop)){
            sb.append(String.format("%s ",nextHop));
        }

        if (StringUtils.isNotEmpty(distance)){
            sb.append(String.format("preference %s ",distance));
        }

        if (StringUtils.isNotEmpty(description)){
            sb.append(String.format("description %s ",description));
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
     * @return undo ip route-static 源VPN实例 目的地址 子网掩码 出接口 优先级
     * [sysname] undo ip route-static vpn-instance vpn1 10.1.1.1 32 Vlanif1 preference 32
     * @throws Exception
     */
    @Override
    public String deleteIpv4RoutingCommandLine(String ip, Integer mask, String nextHop, String netDoor, String distance, String weight, String description, Map<String, Object> map, String[] args) throws Exception {
        StringBuilder sb = new StringBuilder();
        sb.append("undo ip route-static ");

        if (ObjectUtils.isNotEmpty(map.get("srcVpn"))){
            sb.append(String.format("vpn-instance %s ",map.get("srcVpn")));
        }

        sb.append(String.format("%s %d ",ip,mask));

        if (StringUtils.isNotEmpty(netDoor)){
            sb.append(String.format("%s ",netDoor));
        }

        if (StringUtils.isNotEmpty(nextHop)){
            sb.append(String.format("%s ",nextHop));
        }

        if (StringUtils.isNotEmpty(distance)){
            sb.append(String.format("preference %s ",distance));
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
     * @return ipv6 route-static 目的地址 掩码长度 出接口 优先级 描述信息
     * [sysname]ipv6 route-static 2001:db8:1::1 128 Vlanif1 preference 32 description ads
     * @throws Exception
     */
    @Override
    public String generateIpv6RoutingCommandLine(String ip, Integer mask, String nextHop, String netDoor, String distance, String weight, String description, Map<String, Object> map, String[] args) throws Exception {
        StringBuilder sb = new StringBuilder();
        sb.append("ipv6 route-static ");

        if (ObjectUtils.isNotEmpty(map.get("srcVpn"))){
            sb.append(String.format("vpn-instance %s ",map.get("srcVpn")));
        }

        sb.append(String.format("%s %d ",ip,mask));

        if (ObjectUtils.isNotEmpty(map.get("dstVpn"))){
            sb.append(String.format("vpn-instance %s ",map.get("dstVpn")));
        }

        if (StringUtils.isNotEmpty(netDoor)){
            sb.append(String.format("%s ",netDoor));
        }

        if (StringUtils.isNotEmpty(nextHop)){
            sb.append(String.format("%s ",nextHop));
        }

        if (StringUtils.isNotEmpty(distance)){
            sb.append(String.format("preference %s ",distance));
        }

        if (StringUtils.isNotEmpty(description)){
            sb.append(String.format("description %s ",description));
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
     * @return undo ip route-static 源VPN实例 目的地址 子网掩码 出接口 优先级
     * [sysname] undo ip route-static vpn-instance vpn1 10.1.1.1 32 Vlanif1 preference 32
     * @throws Exception
     */
    @Override
    public String deleteIpv6RoutingCommandLine(String ip, Integer mask, String nextHop, String netDoor, String distance, String weight, String description, Map<String, Object> map, String[] args) throws Exception {
        StringBuilder sb = new StringBuilder();
        sb.append("undo ipv6 route-static ");

        if (ObjectUtils.isNotEmpty(map.get("srcVpn"))){
            sb.append(String.format("vpn-instance %s ",map.get("srcVpn")));
        }
        sb.append(String.format("%s %d ",ip,mask));

        if (StringUtils.isNotEmpty(netDoor)){
            sb.append(String.format("%s ",netDoor));
        }

        if (StringUtils.isNotEmpty(nextHop)){
            sb.append(String.format("%s ",nextHop));
        }

        if (StringUtils.isNotEmpty(distance)){
            sb.append(String.format("preference %s ",distance));
        }
        sb.append(StringUtils.LF);
        return sb.toString();
    }
}
