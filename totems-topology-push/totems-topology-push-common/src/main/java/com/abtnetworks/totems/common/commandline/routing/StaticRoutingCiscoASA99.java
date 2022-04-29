package com.abtnetworks.totems.common.commandline.routing;

import com.abtnetworks.totems.common.commandline.PolicyGenerator;
import com.abtnetworks.totems.common.dto.CmdDTO;
import com.abtnetworks.totems.common.dto.CommandLineStaticRoutingInfoDTO;
import com.abtnetworks.totems.common.dto.DeviceDTO;
import com.abtnetworks.totems.common.dto.PolicyDTO;
import com.abtnetworks.totems.common.enums.IpTypeEnum;
import com.abtnetworks.totems.vender.cisco.routing.StaticRoutingCiscoASA99Impl;
import com.abtnetworks.totems.vender.fortinet.routing.StaticRoutingFortinetImpl;
import com.abtnetworks.totems.vender.h3c.routing.StaticRoutingH3cSecPathV7Impl;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class StaticRoutingCiscoASA99 implements PolicyGenerator {

    private StaticRoutingCiscoASA99Impl ciscoASA99;

    public StaticRoutingCiscoASA99() {
        ciscoASA99 = new StaticRoutingCiscoASA99Impl();
    }

    @Override
    public String generate(CmdDTO cmdDTO) {
        DeviceDTO device = cmdDTO.getDevice();
        PolicyDTO policy = cmdDTO.getPolicy();
        CommandLineStaticRoutingInfoDTO routingInfoDTO = cmdDTO.getCommandLineStaticRoutingInfoDTO();
        StringBuilder sb = new StringBuilder();

        try {
            sb.append(ciscoASA99.generateRoutePreCommandline(device.isVsys(),device.getVsysName(),null,null));
            if (IpTypeEnum.IPV4.getCode() == policy.getIpType().intValue()){
                sb.append(ciscoASA99.generateIpv4RoutingCommandLine(policy.getDstIp(),routingInfoDTO.getSubnetMask(),
                        routingInfoDTO.getNextHop(),routingInfoDTO.getOutInterface(),routingInfoDTO.getManagementDistance(),null, policy.getDescription(),null,null));
            }else {
                sb.append(ciscoASA99.generateIpv6RoutingCommandLine(policy.getDstIp(),routingInfoDTO.getSubnetMask(),
                        routingInfoDTO.getNextHop(),routingInfoDTO.getOutInterface(),routingInfoDTO.getManagementDistance(),null, policy.getDescription(),null,null));
            }

            sb.append(ciscoASA99.generateRoutePostCommandline(null,null));
        } catch (Exception e) {
            e.printStackTrace();
        }

        return sb.toString();
    }
}
