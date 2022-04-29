package com.abtnetworks.totems.common.commandline.rollback;

import com.abtnetworks.totems.common.commandline.PolicyGenerator;
import com.abtnetworks.totems.common.dto.CmdDTO;
import com.abtnetworks.totems.common.dto.CommandLineStaticRoutingInfoDTO;
import com.abtnetworks.totems.common.dto.DeviceDTO;
import com.abtnetworks.totems.common.dto.PolicyDTO;
import com.abtnetworks.totems.common.enums.IpTypeEnum;
import com.abtnetworks.totems.vender.topsec.routing.StaticRoutingTopsec010Impl;
import org.springframework.stereotype.Service;

@Service
public class RollbackStaticRoutingTopsec010 implements PolicyGenerator {

    private StaticRoutingTopsec010Impl topsec010;

    public RollbackStaticRoutingTopsec010() {
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
                sb.append(topsec010.deleteIpv4RoutingCommandLine(policy.getDstIp(),routingInfoDTO.getSubnetMask(),
                        routingInfoDTO.getNextHop(),routingInfoDTO.getOutInterface(),routingInfoDTO.getPriority(),null, policy.getDescription(),null,null));
            }else {
                sb.append(topsec010.deleteIpv6RoutingCommandLine(policy.getDstIp(),routingInfoDTO.getSubnetMask(),
                        routingInfoDTO.getNextHop(),routingInfoDTO.getOutInterface(),routingInfoDTO.getPriority(),null, policy.getDescription(),null,null));
            }

            sb.append(topsec010.generateRoutePostCommandline(null,null));
        } catch (Exception e) {
            e.printStackTrace();
        }

        return sb.toString();
    }
}
