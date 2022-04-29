package com.abtnetworks.totems.common.commandline.rollback;

import com.abtnetworks.totems.common.commandline.PolicyGenerator;
import com.abtnetworks.totems.common.dto.CmdDTO;
import com.abtnetworks.totems.common.dto.CommandLineStaticRoutingInfoDTO;
import com.abtnetworks.totems.common.dto.DeviceDTO;
import com.abtnetworks.totems.common.dto.PolicyDTO;
import com.abtnetworks.totems.common.enums.IpTypeEnum;
import com.abtnetworks.totems.vender.Usg.routing.StaticRoutingUsg6000Impl;
import com.abtnetworks.totems.vender.longma.routing.RoutingLongMaImpl;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class RollbackStaticRoutingUsg6000 implements PolicyGenerator {

    private StaticRoutingUsg6000Impl routingUsg6000;

    public RollbackStaticRoutingUsg6000() {
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

        try {
            sb.append(routingUsg6000.generateRoutePreCommandline(device.isVsys(),device.getVsysName(),null,null));
            if (IpTypeEnum.IPV4.getCode() == policy.getIpType().intValue()){
                sb.append(routingUsg6000.deleteIpv4RoutingCommandLine(policy.getDstIp(),routingInfoDTO.getSubnetMask(),
                        routingInfoDTO.getNextHop(),routingInfoDTO.getOutInterface(),routingInfoDTO.getPriority(),null, policy.getDescription(),map,null));
            }else {
                sb.append(routingUsg6000.deleteIpv6RoutingCommandLine(policy.getDstIp(),routingInfoDTO.getSubnetMask(),
                        routingInfoDTO.getNextHop(),routingInfoDTO.getOutInterface(),routingInfoDTO.getPriority(),null, policy.getDescription(),map,null));
            }

            sb.append(routingUsg6000.generateRoutePostCommandline(null,null));
        } catch (Exception e) {
            e.printStackTrace();
        }

        return sb.toString();
    }
}
