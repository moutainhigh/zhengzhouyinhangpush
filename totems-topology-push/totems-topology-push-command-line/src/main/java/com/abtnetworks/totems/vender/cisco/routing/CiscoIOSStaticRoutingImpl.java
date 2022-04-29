package com.abtnetworks.totems.vender.cisco.routing;

import com.abtnetworks.totems.common.network.TotemsIp4Utils;
import com.abtnetworks.totems.common.utils.IPUtil;
import com.abtnetworks.totems.vender.cisco.security.SecurityCiscoIOSImpl;
import org.apache.commons.lang3.StringUtils;

import java.util.Map;

public class CiscoIOSStaticRoutingImpl extends SecurityCiscoIOSImpl {
    /**
     *
     * @param isVsys
     * @param vsysName
     * @param map
     * @param args
     * @return
     * 进入命令行：
     *      #进入特权模式
     *      enable
     * #进入全局配置模式
     * configure terminal
     */
    @Override
    public String generateRoutePreCommandline(Boolean isVsys, String vsysName, Map<String, Object> map, String[] args) {
        StringBuilder sb = new StringBuilder();
//        sb.append("enable").append(StringUtils.LF);
        sb.append("configure terminal").append(StringUtils.LF);
        return sb.toString();
    }

    /**
     *
     * @param map
     * @param args
     * @return  保存配置：
     *          write
     *          退到根视图：
     *          end
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
     * @param map
     * @param args
     * @return ip route 目的地址   子网掩码     出接口 管理距离
     *         ip route 2.2.2.0 255.255.255.0 s0/0/0 2
     * @throws Exception
     */
    @Override
    public String generateIpv4RoutingCommandLine(String ip, Integer mask, String nextHop, String netDoor, String distance, String weight, String description, Map<String, Object> map, String[] args) throws Exception {
        StringBuilder sb = new StringBuilder();
//        String maskIpByMask = TotemsIp4Utils.getMaskIpByMask(mask);
        String maskIpByMask = IPUtil.getMaskByMaskBit(String.valueOf(mask));
        sb.append(String.format("ip route %s %s ",ip,maskIpByMask));
        if (StringUtils.isNotEmpty(nextHop)){
            sb.append(String.format(" %s ",nextHop));
        }
        if (StringUtils.isNotEmpty(netDoor)){
            sb.append(String.format(" %s ",netDoor));
        }
        if (StringUtils.isNotEmpty(distance)){
            sb.append(String.format(" %s ",distance));
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
     * @param distance 管理距离
     * @param weight 权重
     * @param description 描述
     * @param map
     * @param args
     * @return no ip route 目的地址    子网掩码    出接口 管理距离
     *         no ip route 2.2.2.0 255.255.255.0 s0/0/0 2
     * @throws Exception
     */
    @Override
    public String deleteIpv4RoutingCommandLine(String ip, Integer mask, String nextHop, String netDoor, String distance, String weight, String description, Map<String, Object> map, String[] args) throws Exception {
        StringBuilder sb = new StringBuilder();
//        String maskIpByMask = TotemsIp4Utils.getMaskIpByMask(mask);
        String maskIpByMask = IPUtil.getMaskByMaskBit(String.valueOf(mask));
        sb.append(String.format("no ip route %s %s ",ip,maskIpByMask));
        if (StringUtils.isNotEmpty(nextHop)){
            sb.append(String.format(" %s ",nextHop));
        }
        if (StringUtils.isNotEmpty(netDoor)){
            sb.append(String.format(" %s ",netDoor));
        }
        if (StringUtils.isNotEmpty(distance)){
            sb.append(String.format(" %s ",distance));
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
     * @return
     * @throws Exception
     */
    @Override
    public String generateIpv6RoutingCommandLine(String ip, Integer mask, String nextHop, String netDoor, String distance, String weight, String description, Map<String, Object> map, String[] args) throws Exception {
        if(mask == null){
            mask = 128;
        }
        return String.format("set routing-options static route %s/%s reject\n",ip,mask);
    }

    @Override
    public String deleteIpv6RoutingCommandLine(String ip, Integer mask, String nextHop, String netDoor, String distance, String weight, String description, Map<String, Object> map, String[] args) throws Exception {
        if(mask == null){
            mask = 128;
        }
        return String.format("delete set routing-options static route %s/%s reject\n",ip,mask);
    }
}
