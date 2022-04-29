package com.abtnetworks.totems.common.commandline.rollback;

import com.abtnetworks.totems.common.commandline.PolicyGenerator;
import com.abtnetworks.totems.common.dto.CmdDTO;
import com.abtnetworks.totems.common.dto.CommandLineStaticRoutingInfoDTO;
import com.abtnetworks.totems.common.dto.PolicyDTO;
import com.abtnetworks.totems.vender.Juniper.routing.JuniperSRXStaticRoutingImpl;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;


@Service
public class RollbackStaticRoutingJuniperSRX implements PolicyGenerator {

    private JuniperSRXStaticRoutingImpl juniperSRXStaticRouting;

    public RollbackStaticRoutingJuniperSRX() {
        juniperSRXStaticRouting = new JuniperSRXStaticRoutingImpl();
    }

    @Override
    public String generate(CmdDTO cmdDTO) {
        PolicyDTO policy = cmdDTO.getPolicy();
        CommandLineStaticRoutingInfoDTO routingInfoDTO = cmdDTO.getCommandLineStaticRoutingInfoDTO();
        StringBuilder sb = new StringBuilder();
        Map map = new HashMap();
        if (StringUtils.isNotEmpty(routingInfoDTO.getDstVirtualRouter())){
            map.put("routeInstance",routingInfoDTO.getDstVirtualRouter());
        }

        try {
            sb.append(juniperSRXStaticRouting.generateRoutePreCommandline(null,null,null,null));
            sb.append(juniperSRXStaticRouting.deleteIpv4RoutingCommandLine(policy.getDstIp(),routingInfoDTO.getSubnetMask(),
                    routingInfoDTO.getNextHop(),routingInfoDTO.getOutInterface(),routingInfoDTO.getPriority(),null, null,map,null));
            sb.append(juniperSRXStaticRouting.generateRoutePostCommandline(null,null));
        } catch (Exception e) {
            e.printStackTrace();
        }

        return sb.toString();
    }
}
