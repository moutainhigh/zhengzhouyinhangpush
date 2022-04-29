package com.abtnetworks.totems.common.commandline.rollback;

import com.abtnetworks.totems.common.commandline.PolicyGenerator;
import com.abtnetworks.totems.common.dto.CmdDTO;
import com.abtnetworks.totems.common.dto.CommandLineStaticRoutingInfoDTO;
import com.abtnetworks.totems.common.dto.PolicyDTO;
import com.abtnetworks.totems.vender.cisco.routing.CiscoIOSStaticRoutingImpl;
import com.abtnetworks.totems.vender.longma.routing.RoutingLongMaImpl;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class RollbackStaticRoutingLongMa implements PolicyGenerator {
    @Override
    public String generate(CmdDTO cmdDTO) {
        RoutingLongMaImpl routingLongMa = new RoutingLongMaImpl();
        PolicyDTO policy = cmdDTO.getPolicy();
        CommandLineStaticRoutingInfoDTO routingInfoDTO = cmdDTO.getCommandLineStaticRoutingInfoDTO();
        StringBuilder sb = new StringBuilder();
        Map<String, Object> map = new HashMap<>();
        if (StringUtils.isNotEmpty(routingInfoDTO.getDstVirtualRouter())){
            map.put("vrf",routingInfoDTO.getDstVirtualRouter());
        }
        try {
            sb.append(routingLongMa.generateRoutePreCommandline(null,null,null,null));
            sb.append(routingLongMa.deleteIpv4RoutingCommandLine(policy.getDstIp(),routingInfoDTO.getSubnetMask(),
                    routingInfoDTO.getNextHop(),routingInfoDTO.getOutInterface(),routingInfoDTO.getManagementDistance(),null, null,map,null));
            sb.append(routingLongMa.generateRoutePostCommandline(null,null));
        } catch (Exception e) {
            e.printStackTrace();
        }

        return sb.toString();
    }
}
