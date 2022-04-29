package com.abtnetworks.totems.vender.Juniper.routing;

import com.abtnetworks.totems.vender.Juniper.security.SecurityJuniperSRXImpl;
import org.apache.commons.lang3.StringUtils;

import java.util.Map;

public class JuniperSRXStaticRoutingImpl extends SecurityJuniperSRXImpl {

    /**
     *
     * @param isVsys
     * @param vsysName
     * @param map
     * @param args
     * @return
     * 进入命令行：
     * configure
     */
    @Override
    public String generateRoutePreCommandline(Boolean isVsys, String vsysName, Map<String, Object> map, String[] args) {
        StringBuilder sb = new StringBuilder();
        sb.append("configure").append(StringUtils.LF);
        return sb.toString();
    }

    /**
     *
     * @param map
     * @param args
     * @return 保存配置：
     *          commit
     *      退到根视图：
     *      quit
     */
    @Override
    public String generateRoutePostCommandline(Map<String, Object> map, String[] args) {
        StringBuilder sb = new StringBuilder();
        sb.append("commit").append(StringUtils.LF);
        sb.append("quit").append(StringUtils.LF);
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
     * @return set routing-instances 路由实例名称 routing-options static route 目的地址/掩码 next-hop ipv4地址 preference 编号
     *         set routing-instances vrf2 routing-options static route 192.168.1.0/24 next-hop 10.1.1.1 preference 2
     * @throws Exception
     */
    @Override
    public String generateIpv4RoutingCommandLine(String ip, Integer mask, String nextHop, String netDoor, String distance, String weight, String description, Map<String, Object> map, String[] args) throws Exception {
        StringBuilder sb = new StringBuilder();
        sb.append("set ");
        if (map.containsKey("routeInstance")){
            sb.append(String.format("routing-instances %s ",map.get("routeInstance")));
        }
        sb.append(String.format("routing-options static route %s/%d ",ip,mask));
        if (StringUtils.isNotEmpty(nextHop)){
            sb.append(String.format("next-hop %s ",nextHop));
        }
        if (StringUtils.isNotEmpty(netDoor)){
            sb.append(String.format("next-hop %s ",netDoor));
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
     * @return delete routing-instances 路由实例名称 routing-options static route 目的地址/掩码 下一跳  (删除可带下一跳但不带优先级)
     *         delete routing-instances vrf2 routing-options static route 192.168.1.0/24 next-hop 10.1.1.1
     * @throws Exception
     */
    @Override
    public String deleteIpv4RoutingCommandLine(String ip, Integer mask, String nextHop, String netDoor, String distance, String weight, String description, Map<String, Object> map, String[] args) throws Exception {
        StringBuilder sb = new StringBuilder();
        sb.append("delete ");
        if (map.containsKey("routeInstance")){
            sb.append(String.format("routing-instances %s ",map.get("routeInstance")));
        }
        sb.append(String.format("routing-options static route %s/%d ",ip,mask));
        if (StringUtils.isNotEmpty(nextHop)){
            sb.append(String.format("next-hop %s ",nextHop));
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
     * @return set routing-options rib inet6.0 static route 2409:8C20:0A11:0103::/64 next-hop 2409:8C20:0A11:0103::1
     * @throws Exception
     */
    @Override
    public String generateIpv6RoutingCommandLine(String ip, Integer mask, String nextHop, String netDoor, String distance, String weight, String description, Map<String, Object> map, String[] args) throws Exception {
        StringBuilder sb = new StringBuilder();
        sb.append("set ");
        if (map.containsKey("routeInstance")){
            sb.append(String.format("routing-instances %s ",map.get("routeInstance")));
        }
        sb.append("routing-options rib ");
        if (map.containsKey("routeInstance")){
            sb.append(String.format("%s.",map.get("routeInstance")));
        }
        sb.append(String.format("inet6.0 static route %s/%d ",ip,mask));
        if (StringUtils.isNotEmpty(nextHop)){
            sb.append(String.format("next-hop %s ",nextHop));
        }
        if (StringUtils.isNotEmpty(netDoor)){
            sb.append(String.format("next-hop %s ",netDoor));
        }
        if (StringUtils.isNotEmpty(distance)){
            sb.append(String.format("preference %s ",distance));
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
     * @return delete routing-options static route 192.168.1.0/24 next-hop 10.1.1.1
     * @throws Exception
     */
    @Override
    public String deleteIpv6RoutingCommandLine(String ip, Integer mask, String nextHop, String netDoor, String distance, String weight, String description, Map<String, Object> map, String[] args) throws Exception {
        StringBuilder sb = new StringBuilder();
        sb.append("delete ");
        if (map.containsKey("routeInstance")){
            sb.append(String.format("routing-instances %s ",map.get("routeInstance")));
        }
        sb.append(String.format("routing-options static route %s/%d ",ip,mask));
        if (StringUtils.isNotEmpty(nextHop)){
            sb.append(String.format("next-hop %s ",nextHop));
        }
        sb.append(StringUtils.LF);
        return sb.toString();
    }
}
