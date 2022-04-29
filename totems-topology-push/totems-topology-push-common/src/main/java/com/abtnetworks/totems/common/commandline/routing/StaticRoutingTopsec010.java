package com.abtnetworks.totems.common.commandline.routing;

import com.abtnetworks.totems.common.commandline.PolicyGenerator;
import com.abtnetworks.totems.common.dto.CmdDTO;
import com.abtnetworks.totems.common.dto.CommandLineStaticRoutingInfoDTO;
import com.abtnetworks.totems.common.dto.DeviceDTO;
import com.abtnetworks.totems.common.dto.PolicyDTO;
import com.abtnetworks.totems.common.enums.IpTypeEnum;
import com.abtnetworks.totems.vender.h3c.routing.StaticRoutingH3cSecPathV7Impl;
import com.abtnetworks.totems.vender.longma.routing.RoutingLongMaImpl;
import com.abtnetworks.totems.vender.topsec.routing.StaticRoutingTopsec010Impl;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class StaticRoutingTopsec010 implements PolicyGenerator {

    private StaticRoutingTopsec010Impl topsec010;

    public StaticRoutingTopsec010() {
        topsec010 = new StaticRoutingTopsec010Impl();
    }

    @Override
    public String generate(CmdDTO cmdDTO) {
        DeviceDTO device = cmdDTO.getDevice();
        PolicyDTO policy = cmdDTO.getPolicy();
        CommandLineStaticRoutingInfoDTO routingInfoDTO = cmdDTO.getCommandLineStaticRoutingInfoDTO();
        StringBuilder sb = new StringBuilder();

        try {
            sb.append(topsec010.generateRoutePreCommandline(device.isVsys(),device.getVsysName(),null,null));
            if (IpTypeEnum.IPV4.getCode() == policy.getIpType().intValue()){
                sb.append(topsec010.generateIpv4RoutingCommandLine(policy.getDstIp(),routingInfoDTO.getSubnetMask(),
                        routingInfoDTO.getNextHop(),routingInfoDTO.getOutInterface(),routingInfoDTO.getPriority(),null, policy.getDescription(),null,null));
            }else {
                sb.append(topsec010.generateIpv6RoutingCommandLine(policy.getDstIp(),routingInfoDTO.getSubnetMask(),
                        routingInfoDTO.getNextHop(),routingInfoDTO.getOutInterface(),routingInfoDTO.getPriority(),null, policy.getDescription(),null,null));
            }

            sb.append(topsec010.generateRoutePostCommandline(null,null));
        } catch (Exception e) {
            e.printStackTrace();
        }

        return sb.toString();
    }
}
