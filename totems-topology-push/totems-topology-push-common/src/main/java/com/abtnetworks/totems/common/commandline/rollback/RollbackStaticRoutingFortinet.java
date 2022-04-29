package com.abtnetworks.totems.common.commandline.rollback;

import com.abtnetworks.totems.common.commandline.PolicyGenerator;
import com.abtnetworks.totems.common.dto.*;
import com.abtnetworks.totems.common.enums.IpTypeEnum;
import com.abtnetworks.totems.vender.fortinet.routing.StaticRoutingFortinetImpl;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class RollbackStaticRoutingFortinet implements PolicyGenerator {

    private StaticRoutingFortinetImpl fortinet;

    public RollbackStaticRoutingFortinet() {
        fortinet = new StaticRoutingFortinetImpl();
    }

    @Override
    public String generate(CmdDTO cmdDTO) {
        DeviceDTO device = cmdDTO.getDevice();
        PolicyDTO policy = cmdDTO.getPolicy();
        SettingDTO setting = cmdDTO.getSetting();
        CommandLineStaticRoutingInfoDTO routingInfoDTO = cmdDTO.getCommandLineStaticRoutingInfoDTO();
        StringBuilder sb = new StringBuilder();
        Map map = new HashMap();

        map.put("hasVsys",device.isHasVsys());
        map.put("id",setting.getPolicyId());

        try {
            sb.append(fortinet.generateRoutePreCommandline(device.isVsys(),device.getVsysName(),map,null));
            if (IpTypeEnum.IPV4.getCode() == policy.getIpType().intValue()){
                sb.append(fortinet.deleteIpv4RoutingCommandLine(policy.getDstIp(),routingInfoDTO.getSubnetMask(),
                        routingInfoDTO.getNextHop(),routingInfoDTO.getOutInterface(),routingInfoDTO.getPriority(),null, policy.getDescription(),map,null));
            }else {
                sb.append(fortinet.deleteIpv6RoutingCommandLine(policy.getDstIp(),routingInfoDTO.getSubnetMask(),
                        routingInfoDTO.getNextHop(),routingInfoDTO.getOutInterface(),routingInfoDTO.getPriority(),null, policy.getDescription(),map,null));
            }

            sb.append(fortinet.generateRoutePostCommandline(null,null));
        } catch (Exception e) {
            e.printStackTrace();
        }

        return sb.toString();
    }
}
