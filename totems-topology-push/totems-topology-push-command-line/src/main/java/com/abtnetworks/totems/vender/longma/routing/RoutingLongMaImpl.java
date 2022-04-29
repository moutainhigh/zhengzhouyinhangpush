package com.abtnetworks.totems.vender.longma.routing;

import com.abtnetworks.totems.common.utils.IPUtil;
import com.abtnetworks.totems.vender.longma.security.SecurityLongMaImpl;
import org.apache.commons.lang3.StringUtils;

import java.util.Map;

public class RoutingLongMaImpl extends SecurityLongMaImpl {

    /**
     * @return enable\nconfigure terminal\n
     */
    @Override
    public String generateRoutePreCommandline(Boolean isVsys, String vsysName, Map<String, Object> map, String[] args) {
        return "enable\nconfigure terminal\n";
    }

    /**
     * 龙马路由策略命令行
     *
     * @param ip          IP
     * @param mask        掩码
     * @param nextHop     下一跳
     * @param netDoor     出接口
     * @param distance    优先级
     * @param weight      权重
     * @param description 描述
     * @param map
     * @param args
     * @return 两种： ip route 1.1.1.0/24 192.168.1.1 23\n || ip route 1.1.1.0 255.255.255.0 s0/0/0\n
     * @throws Exception
     */
    @Override
    public String generateIpv4RoutingCommandLine(String ip, Integer mask, String nextHop, String netDoor, String distance, String weight, String description, Map<String, Object> map, String[] args) throws Exception {
        StringBuilder sb = new StringBuilder();
        sb.append("ip route ");
        if (map.containsKey("vrf")){
            sb.append("vrf ").append(map.get("vrf")).append(StringUtils.SPACE);
        }
        sb.append(ip).append("/").append(mask).append(StringUtils.SPACE);
        if (!StringUtils.isBlank(nextHop)) {
            sb.append(nextHop).append(" ").append(distance);
        } else if (StringUtils.isNotEmpty(netDoor)) {
            sb.append(netDoor);
        }
        sb.append(StringUtils.LF);
        return sb.toString();
    }

    /**
     * @return no ip route
     * @throws Exception
     */
    @Override
    public String deleteIpv4RoutingCommandLine(String ip, Integer mask, String nextHop, String netDoor, String distance, String weight, String description, Map<String, Object> map, String[] args) throws Exception {
        StringBuilder sb = new StringBuilder();
        sb.append("no ip route ");
        if (map.containsKey("vrf")){
            sb.append("vrf ").append(map.get("vrf")).append(StringUtils.SPACE);
        }
        sb.append(ip).append("/").append(mask).append(StringUtils.SPACE);
        if (!StringUtils.isBlank(nextHop)) {
            sb.append(nextHop);
        } else if (StringUtils.isNotEmpty(netDoor)) {
            sb.append(netDoor);
        }
        sb.append(StringUtils.LF);
        return sb.toString();
    }

    @Override
    public String generateIpv6RoutingCommandLine(String ip, Integer mask, String nextHop, String netDoor, String distance, String weight, String description, Map<String, Object> map, String[] args) throws Exception {
        return null;
    }

    @Override
    public String deleteIpv6RoutingCommandLine(String ip, Integer mask, String nextHop, String netDoor, String distance, String weight, String description, Map<String, Object> map, String[] args) throws Exception {
        return null;
    }

    @Override
    public String generateRoutePostCommandline(Map<String, Object> map, String[] args) {
        return "end\nsave config\n";
    }
}
