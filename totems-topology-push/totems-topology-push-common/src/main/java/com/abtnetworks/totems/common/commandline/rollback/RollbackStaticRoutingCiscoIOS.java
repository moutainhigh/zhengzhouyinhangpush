package com.abtnetworks.totems.common.commandline.rollback;

import com.abtnetworks.totems.common.commandline.PolicyGenerator;
import com.abtnetworks.totems.common.dto.CmdDTO;
import com.abtnetworks.totems.common.dto.CommandLineStaticRoutingInfoDTO;
import com.abtnetworks.totems.common.dto.PolicyDTO;
import com.abtnetworks.totems.vender.cisco.routing.CiscoIOSStaticRoutingImpl;
import org.springframework.stereotype.Service;

@Service
public class RollbackStaticRoutingCiscoIOS implements PolicyGenerator {
    @Override
    public String generate(CmdDTO cmdDTO) {
        CiscoIOSStaticRoutingImpl ciscoIOSStaticRouting = new CiscoIOSStaticRoutingImpl();
        PolicyDTO policy = cmdDTO.getPolicy();
        CommandLineStaticRoutingInfoDTO routingInfoDTO = cmdDTO.getCommandLineStaticRoutingInfoDTO();
        StringBuilder sb = new StringBuilder();

        try {
            sb.append(ciscoIOSStaticRouting.generateRoutePreCommandline(null,null,null,null));
            sb.append(ciscoIOSStaticRouting.deleteIpv4RoutingCommandLine(policy.getDstIp(),routingInfoDTO.getSubnetMask(),
                    routingInfoDTO.getNextHop(),routingInfoDTO.getOutInterface(),routingInfoDTO.getManagementDistance(),null, null,null,null));
            sb.append(ciscoIOSStaticRouting.generateRoutePostCommandline(null,null));
        } catch (Exception e) {
            e.printStackTrace();
        }

        return sb.toString();
    }
}
