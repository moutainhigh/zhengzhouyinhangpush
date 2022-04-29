package com.abtnetworks.totems.vender.fortinet.routing;

import com.abtnetworks.totems.common.utils.IPUtil;
import com.abtnetworks.totems.vender.fortinet.security.SecurityFortinetImpl;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.Map;

public class StaticRoutingFortinetImpl extends SecurityFortinetImpl {

    /**
     *
     * @param isVsys
     * @param vsysName
     * @param map
     * @param args
     * @return 如果设备存在虚系统，即使是主墙，也需要增加
     * config vdom
     * edit 虚墙名称（如果是主墙，则为root）
     * 如果设备不存在虚系统，则不需要。
     */
    @Override
    public String generateRoutePreCommandline(Boolean isVsys, String vsysName, Map<String, Object> map, String[] args) {

        StringBuilder sb = new StringBuilder();
        if (isVsys) {
            sb.append("config vdom ").append(StringUtils.LF);
            sb.append(String.format(EDIT_STR,vsysName)).append(StringUtils.LF);
        }else{
            if(ObjectUtils.isNotEmpty(map)) {
                boolean hasVsys = (boolean) map.get("hasVsys");
                if (hasVsys) {
                    sb.append("config vdom ").append(StringUtils.LF);
                    vsysName = "root";
                    sb.append(String.format(EDIT_STR, vsysName)).append(StringUtils.LF);
                }
            }
        }
        return sb.toString();
    }

    /**
     *
     * @param map
     * @param args
     * @return end
     */
    @Override
    public String generateRoutePostCommandline(Map<String, Object> map, String[] args) {
        StringBuilder sb = new StringBuilder();
        sb.append("end").append(StringUtils.LF);
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
     * @return config router static             //进入静态路由配置视图
     *   edit 1                       //路由条目ID，取值范围0-4294967295（必填项）
     *   set device "port1"            //出接口，（必填项）
     *   set dst 192.168.0.0 255.255.0.0   //目的地址 子网掩码 （必填项）
     *   set gateway 192.168.201.1      //下一跳     （非必填，不填默认为0.0.0.0）
     *   set comment 静态路由1       //描述信息  （非必填，支持中文，字符格式，取值为0-255）
     *   set priority 1
     * @throws Exception
     */
    @Override
    public String generateIpv4RoutingCommandLine(String ip, Integer mask, String nextHop, String netDoor, String distance, String weight, String description, Map<String, Object> map, String[] args) throws Exception {
        StringBuilder sb = new StringBuilder();
        sb.append("config router static").append(StringUtils.LF);
        sb.append(String.format("edit %s",map.get("id"))).append(StringUtils.LF);
        sb.append(String.format("set device %s",netDoor)).append(StringUtils.LF);
        String maskIpByMask = IPUtil.getMaskByMaskBit(String.valueOf(mask));
        sb.append(String.format("set dst %s %s",ip,maskIpByMask)).append(StringUtils.LF);
        if (StringUtils.isNotEmpty(nextHop)){
            sb.append(String.format("set gateway %s",nextHop)).append(StringUtils.LF);
        }else {
            sb.append("set gateway 0.0.0.0").append(StringUtils.LF);
        }
        if (StringUtils.isNotEmpty(distance)){
            sb.append(String.format("set priority %s",distance)).append(StringUtils.LF);
        }
        if (StringUtils.isNotEmpty(description)){
            sb.append(String.format("set comment %s",description)).append(StringUtils.LF);
        }
        sb.append("next").append(StringUtils.LF);

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
     * @return delete 路由ID
     * @throws Exception
     */
    @Override
    public String deleteIpv4RoutingCommandLine(String ip, Integer mask, String nextHop, String netDoor, String distance, String weight, String description, Map<String, Object> map, String[] args) throws Exception {
        StringBuilder sb = new StringBuilder();
        sb.append("config route static").append(StringUtils.LF);
        sb.append(String.format("delete %s",map.get("id"))).append(StringUtils.LF);
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
     * @return config router static6
     * edit 1
     *         set device "port4"
     *         set dst 2001:db8:1::1/128
     *         set gateway 2001:db8:2001::1
     *         set priority 0
     *         set comment ''
     *     next
     * @throws Exception
     */
    @Override
    public String generateIpv6RoutingCommandLine(String ip, Integer mask, String nextHop, String netDoor, String distance, String weight, String description, Map<String, Object> map, String[] args) throws Exception {
        StringBuilder sb = new StringBuilder();
        sb.append("config router static6").append(StringUtils.LF);
        sb.append(String.format("edit %s",map.get("id"))).append(StringUtils.LF);
        sb.append(String.format("set device %s",netDoor)).append(StringUtils.LF);
        sb.append(String.format("set dst %s/%d",ip,mask)).append(StringUtils.LF);
        if (StringUtils.isNotEmpty(nextHop)){
            sb.append(String.format("set gateway %s",nextHop)).append(StringUtils.LF);
        }else {
            sb.append("set gateway 0.0.0.0").append(StringUtils.LF);
        }
        if (StringUtils.isNotEmpty(distance)){
            sb.append(String.format("set priority %s",distance)).append(StringUtils.LF);
        }
        if (StringUtils.isNotEmpty(description)){
            sb.append(String.format("set comment %s",description)).append(StringUtils.LF);
        }
        sb.append("next").append(StringUtils.LF);

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
     * @return delete 路由ID
     * @throws Exception
     */
    @Override
    public String deleteIpv6RoutingCommandLine(String ip, Integer mask, String nextHop, String netDoor, String distance, String weight, String description, Map<String, Object> map, String[] args) throws Exception {
        StringBuilder sb = new StringBuilder();
        sb.append("config route static").append(StringUtils.LF);
        sb.append(String.format("delete %s",map.get("id"))).append(StringUtils.LF);
        return sb.toString();
    }
}
