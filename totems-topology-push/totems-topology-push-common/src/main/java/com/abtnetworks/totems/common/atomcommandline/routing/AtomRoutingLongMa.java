package com.abtnetworks.totems.common.atomcommandline.routing;

import com.abtnetworks.totems.common.commandline.PolicyGenerator;
import com.abtnetworks.totems.common.commandline.routing.RoutingGenerator;
import com.abtnetworks.totems.common.dto.CmdDTO;
import com.abtnetworks.totems.common.dto.CommandLineStaticRoutingInfoDTO;
import com.abtnetworks.totems.common.dto.DeviceDTO;
import com.abtnetworks.totems.common.dto.PolicyDTO;
import com.abtnetworks.totems.common.dto.commandline.RoutingCommandDTO;
import com.abtnetworks.totems.vender.abt.security.SecurityAbtImpl;
import com.abtnetworks.totems.vender.longma.routing.RoutingLongMaImpl;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

@Service
public class AtomRoutingLongMa implements RoutingGenerator, PolicyGenerator {

    private RoutingLongMaImpl generatorBean;

    public AtomRoutingLongMa() {
        generatorBean = new RoutingLongMaImpl();
    }

    @Override
    public String generate(CmdDTO cmdDTO) {
        PolicyDTO policy = cmdDTO.getPolicy();
        DeviceDTO device = cmdDTO.getDevice();
        CommandLineStaticRoutingInfoDTO routingInfoDTO = cmdDTO.getCommandLineStaticRoutingInfoDTO();
        Map<String, Object> map = new HashMap<>();
        if (StringUtils.isNotEmpty(routingInfoDTO.getDstVirtualRouter())){
            map.put("vrf",routingInfoDTO.getDstVirtualRouter());
        }
        String commandline = "命令行生成失败，未知主机异常！";
        try {
            commandline = generatorBean.generateRoutePreCommandline(device.isVsys(),device.getVsysName(),null,null)
                    +generatorBean.generateIpv4RoutingCommandLine(policy.getDstIp(),routingInfoDTO.getSubnetMask(),routingInfoDTO.getNextHop(),routingInfoDTO.getOutInterface(),routingInfoDTO.getManagementDistance(),null,null,map,null)
                    +generatorBean.generateRoutePostCommandline(null,null);
        } catch (Exception e) {

        }
        return commandline;
    }

    @Override
    public String generatePreCommandLine(RoutingCommandDTO routingCommandDTO) {
        return null;
    }

    @Override
    public String generatePostCommandLine(RoutingCommandDTO routingCommandDTO) {
        return null;
    }

    @Override
    public String generatorRoutingCommandLine(RoutingCommandDTO routingCommandDTO) throws UnknownHostException {
        return null;
    }

    @Override
    public String deleteRoutingCommandLine(RoutingCommandDTO routingCommandDTO) throws UnknownHostException {
        return null;
    }

    public static void main(String[] args) {
        AtomRoutingLongMa atomRoutingLongMa = new AtomRoutingLongMa();
        CmdDTO cmdDTO = new CmdDTO();
        PolicyDTO policyDTO = new PolicyDTO();
        policyDTO.setDstIp("1.1.1.1");
        DeviceDTO deviceDTO = new DeviceDTO();
        CommandLineStaticRoutingInfoDTO routingInfoDTO = new CommandLineStaticRoutingInfoDTO();
        routingInfoDTO.setManagementDistance("32");
        //routingInfoDTO.setNextHop("2.2.2.2");
        routingInfoDTO.setOutInterface("S0/0/0");
        routingInfoDTO.setSubnetMask(32);
        routingInfoDTO.setDstVirtualRouter("333");
        cmdDTO.setDevice(deviceDTO);
        cmdDTO.setPolicy(policyDTO);
        cmdDTO.setCommandLineStaticRoutingInfoDTO(routingInfoDTO);
        String generate = atomRoutingLongMa.generate(cmdDTO);
        System.out.println(generate);
    }
}
