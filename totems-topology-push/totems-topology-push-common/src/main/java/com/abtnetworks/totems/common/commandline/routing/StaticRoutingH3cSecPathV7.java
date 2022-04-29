package com.abtnetworks.totems.common.commandline.routing;

import com.abtnetworks.totems.common.commandline.PolicyGenerator;
import com.abtnetworks.totems.common.dto.CmdDTO;
import com.abtnetworks.totems.common.dto.CommandLineStaticRoutingInfoDTO;
import com.abtnetworks.totems.common.dto.DeviceDTO;
import com.abtnetworks.totems.common.dto.PolicyDTO;
import com.abtnetworks.totems.common.enums.IpTypeEnum;
import com.abtnetworks.totems.vender.Usg.routing.StaticRoutingUsg6000Impl;
import com.abtnetworks.totems.vender.h3c.routing.StaticRoutingH3cSecPathV7Impl;
import com.abtnetworks.totems.vender.hillstone.routing.StaticRoutingHillStoneV5Impl;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class StaticRoutingH3cSecPathV7 implements PolicyGenerator {

    private StaticRoutingH3cSecPathV7Impl secPathV7;

    public StaticRoutingH3cSecPathV7() {
        secPathV7 = new StaticRoutingH3cSecPathV7Impl();
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

        try {
            sb.append(secPathV7.generateRoutePreCommandline(device.isVsys(),device.getVsysName(),null,null));
            if (IpTypeEnum.IPV4.getCode() == policy.getIpType().intValue()){
                sb.append(secPathV7.generateIpv4RoutingCommandLine(policy.getDstIp(),routingInfoDTO.getSubnetMask(),
                        routingInfoDTO.getNextHop(),routingInfoDTO.getOutInterface(),routingInfoDTO.getPriority(),null, policy.getDescription(),map,null));
            }else {
                sb.append(secPathV7.generateIpv6RoutingCommandLine(policy.getDstIp(),routingInfoDTO.getSubnetMask(),
                        routingInfoDTO.getNextHop(),routingInfoDTO.getOutInterface(),routingInfoDTO.getPriority(),null, policy.getDescription(),map,null));
            }

            sb.append(secPathV7.generateRoutePostCommandline(null,null));
        } catch (Exception e) {
            e.printStackTrace();
        }

        return sb.toString();
    }
}

