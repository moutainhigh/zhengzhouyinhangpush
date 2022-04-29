package com.abtnetworks.totems.common.commandline.routing;

import com.abtnetworks.totems.common.commandline.PolicyGenerator;
import com.abtnetworks.totems.common.dto.CmdDTO;
import com.abtnetworks.totems.common.dto.commandline.RoutingCommandDTO;
import lombok.extern.slf4j.Slf4j;

import java.net.UnknownHostException;
import java.util.List;

/**
 * @author zc
 * @date 2019/12/17
 */
@Slf4j
public class RoutingJuniperSSG implements RoutingGenerator, PolicyGenerator {

    @Override
    public String generate(CmdDTO cmdDTO) {
        RoutingCommandDTO routingCommandDTO = getRoutingCommandDTO(cmdDTO);
        String commandline = "命令行生成失败，未知主机异常！";
        try {
            commandline = generatorRoutingCommandLine(routingCommandDTO);
        } catch (Exception e) {

        }
        return commandline;
    }

    RoutingCommandDTO getRoutingCommandDTO(CmdDTO cmdDTO) {
        RoutingCommandDTO routingCommandDTO = new RoutingCommandDTO();

        return routingCommandDTO;
    }

    @Override
    public String generatePreCommandLine(RoutingCommandDTO routingCommandDTO) {
        return "cli\nconfigure\n";
    }

    @Override
    public String generatePostCommandLine(RoutingCommandDTO routingCommandDTO) {
        return "commit\n";
    }

    /**
     * set route 1.1.1.0/24 interface null
     * @param dto
     * @return
     * @throws UnknownHostException
     */
    @Override
    public String generatorRoutingCommandLine(RoutingCommandDTO dto) throws UnknownHostException {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(generatePreCommandLine(dto));
        String ipAddr = dto.getIpAddr();
        List<String> allIpList = RoutingGenerator.ipConvert(ipAddr);
        if (dto.getRoutingType().equals(RoutingCommandDTO.RoutingType.UNREACHABLE)) {
            log.info("配置黑洞路由");
            allIpList.forEach(ip -> {
                if (ip.contains(":")) {
                    //ipv6
                    //todo
                } else {
                    //ipv4
                    stringBuilder.append("set route ").append(ip).append("/32 interface null\n");
                }
            });
        }
        stringBuilder.append(generatePostCommandLine(dto));
        return stringBuilder.toString();
    }

    /**
     * unset route 1.1.1.0/24 interface null
     * @param routingCommandDTO
     * @return
     * @throws UnknownHostException
     */
    @Override
    public String deleteRoutingCommandLine(RoutingCommandDTO routingCommandDTO) throws UnknownHostException {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(generatePreCommandLine(routingCommandDTO));
        String ipAddr = routingCommandDTO.getIpAddr();
        List<String> allIpList = RoutingGenerator.ipConvert(ipAddr);
        if (routingCommandDTO.getRoutingType().equals(RoutingCommandDTO.RoutingType.UNREACHABLE)) {
            log.info("配置黑洞路由");
            allIpList.forEach(ip -> {
                if (ip.contains(":")) {
                    //ipv6
                    //todo
                } else {
                    //ipv4
                    stringBuilder.append("unset route ").append(ip).append("/32 interface null\n");
                }
            });
        }
        stringBuilder.append(generatePostCommandLine(routingCommandDTO));
        return stringBuilder.toString();
    }
}
