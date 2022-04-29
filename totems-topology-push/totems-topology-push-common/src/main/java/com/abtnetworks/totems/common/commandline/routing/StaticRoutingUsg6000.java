package com.abtnetworks.totems.common.commandline.routing;

import com.abtnetworks.totems.common.commandline.PolicyGenerator;
import com.abtnetworks.totems.common.dto.CmdDTO;
import com.abtnetworks.totems.common.dto.CommandLineStaticRoutingInfoDTO;
import com.abtnetworks.totems.common.dto.DeviceDTO;
import com.abtnetworks.totems.common.dto.PolicyDTO;
import com.abtnetworks.totems.common.enums.IpTypeEnum;
import com.abtnetworks.totems.vender.Juniper.routing.JuniperSRXStaticRoutingImpl;
import com.abtnetworks.totems.vender.Usg.routing.StaticRoutingUsg6000Impl;
import com.abtnetworks.totems.vender.longma.routing.RoutingLongMaImpl;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class StaticRoutingUsg6000 implements PolicyGenerator {

    private StaticRoutingUsg6000Impl routingUsg6000;

    public StaticRoutingUsg6000() {
        routingUsg6000 = new StaticRoutingUsg6000Impl();
    }

    @Override
    public String generate(CmdDTO cmdDTO) {
        DeviceDTO device = cmdDTO.getDevice();
        PolicyDTO policy = cmdDTO.getPolicy();
        CommandLineStaticRoutingInfoDTO routingInfoDTO = cmdDTO.getCommandLineStaticRoutingInfoDTO();
        StringBuilder sb = new StringBuilder();
        Map map = new HashMap();

        if (StringUtils.isNotEmpty(routingInfoDTO.getSrcVirtualRouter())){
            map.put("srcVpn",routingInfoDTO.getSrcVirtualRouter());
        }
        if (StringUtils.isNotEmpty(routingInfoDTO.getDstVirtualRouter())){
            map.put("dstVpn",routingInfoDTO.getDstVirtualRouter());
        }
        if (device.isVsys()){
            map.put("hasVsys",device.isHasVsys());
        }

        try {
            if (IpTypeEnum.IPV4.getCode() == policy.getIpType().intValue()){
                sb.append(routingUsg6000.generateRoutePreCommandline(device.isVsys(),device.getVsysName(),null,null));
                sb.append(routingUsg6000.generateIpv4RoutingCommandLine(policy.getDstIp(),routingInfoDTO.getSubnetMask(),
                        routingInfoDTO.getNextHop(),routingInfoDTO.getOutInterface(),routingInfoDTO.getPriority(),null, policy.getDescription(),map,null));
                sb.append(routingUsg6000.generateRoutePostCommandline(null,null));
            }else if (!device.isVsys()){
                sb.append(routingUsg6000.generateRoutePreCommandline(device.isVsys(),device.getVsysName(),null,null));
                sb.append(routingUsg6000.generateIpv6RoutingCommandLine(policy.getDstIp(),routingInfoDTO.getSubnetMask(),
                        routingInfoDTO.getNextHop(),routingInfoDTO.getOutInterface(),routingInfoDTO.getPriority(),null, policy.getDescription(),map,null));
                sb.append(routingUsg6000.generateRoutePostCommandline(null,null));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return sb.toString();
    }
}
