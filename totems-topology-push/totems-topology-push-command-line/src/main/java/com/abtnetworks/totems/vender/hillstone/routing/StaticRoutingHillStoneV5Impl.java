package com.abtnetworks.totems.vender.hillstone.routing;

import com.abtnetworks.totems.vender.hillstone.security.SecurityHillStoneV5Impl;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.Map;

public class StaticRoutingHillStoneV5Impl extends SecurityHillStoneV5Impl {
    /**
     *
     * @param isVsys
     * @param vsysName
     * @param map
     * @param args
     * @return #进入全局模式
     * configure
     * enter-vsys 虚系统名称
     * ip vrouter trust-vr   //进入VRouter配置模式
     */
    @Override
    public String generateRoutePreCommandline(Boolean isVsys, String vsysName, Map<String, Object> map, String[] args) {

        StringBuilder sb = new StringBuilder();
        sb.append("configure").append(StringUtils.LF);
        if (isVsys != null && isVsys && StringUtils.isNotEmpty(vsysName)) {
            sb.append(String.format("enter-vsys %s",vsysName)).append(StringUtils.LF);
        }
        if (map.containsKey("srcVpn")){
            sb.append(String.format("ip vrouter %s",map.get("srcVpn"))).append(StringUtils.LF);
        }else {
            sb.append("ip vrouter trust-vr").append(StringUtils.LF);
        }
        return sb.toString();
    }

    /**
     *
     * @param map
     * @param args
     * @return 保存配置
     * save
     * yy
     * 退到根视图
     * end
     */
    @Override
    public String generateRoutePostCommandline(Map<String, Object> map, String[] args) {
        StringBuilder sb = new StringBuilder();
        sb.append("end").append(StringUtils.LF);
        sb.append("save").append(StringUtils.LF);
        sb.append("yy").append(StringUtils.LF);
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
     * @return ip route 1.5.1.0/24 1.5.1.1 32 description ads
     * @throws Exception
     */
    @Override
    public String generateIpv4RoutingCommandLine(String ip, Integer mask, String nextHop, String netDoor, String distance, String weight, String description, Map<String, Object> map, String[] args) throws Exception {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("ip route %s/%d ",ip,mask));

        if (StringUtils.isNotEmpty(netDoor)){
            sb.append(String.format("%s ",netDoor));
        }

        if (StringUtils.isNotEmpty(nextHop)){
            sb.append(String.format("%s ",nextHop));
        }

        if (StringUtils.isNotEmpty(distance)){
            sb.append(String.format("%s ",distance));
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
     * @return no ip route 1.5.1.0/24 1.5.1.1 description
     * @throws Exception
     */
    @Override
    public String deleteIpv4RoutingCommandLine(String ip, Integer mask, String nextHop, String netDoor, String distance, String weight, String description, Map<String, Object> map, String[] args) throws Exception {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("no ip route %s/%d ",ip,mask));

        if (StringUtils.isNotEmpty(netDoor)){
            sb.append(String.format("%s ",netDoor));
        }

        if (StringUtils.isNotEmpty(nextHop)){
            sb.append(String.format("%s ",nextHop));
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
     * @return ipv6 route 2001:db8:1::1/128 2001:db8:2001::1 32
     * @throws Exception
     */
    @Override
    public String generateIpv6RoutingCommandLine(String ip, Integer mask, String nextHop, String netDoor, String distance, String weight, String description, Map<String, Object> map, String[] args) throws Exception {
        StringBuilder sb = new StringBuilder();

        sb.append(String.format("ipv6 route %s/%d ",ip,mask));

        if (StringUtils.isNotEmpty(netDoor)){
            sb.append(String.format("%s ",netDoor));
        }

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
     * @param ip
     * @param mask
     * @param nextHop
     * @param netDoor
     * @param distance
     * @param weight
     * @param description
     * @param map
     * @param args
     * @return no ip route 1.5.1.0/24 1.5.1.1 description
     * @throws Exception
     */
    @Override
    public String deleteIpv6RoutingCommandLine(String ip, Integer mask, String nextHop, String netDoor, String distance, String weight, String description, Map<String, Object> map, String[] args) throws Exception {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("no ip route %s/%d ",ip,mask));

        if (StringUtils.isNotEmpty(netDoor)){
            sb.append(String.format("%s ",netDoor));
        }

        if (StringUtils.isNotEmpty(nextHop)){
            sb.append(String.format("%s ",nextHop));
        }

        sb.append(StringUtils.LF);
        return sb.toString();
    }
}
