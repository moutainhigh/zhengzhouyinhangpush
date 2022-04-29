package com.abtnetworks.totems.common.commandline.routing;

import com.abtnetworks.totems.common.commandline.PolicyGenerator;
import com.abtnetworks.totems.common.dto.CmdDTO;
import com.abtnetworks.totems.common.dto.commandline.RoutingCommandDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.net.UnknownHostException;
import java.util.List;

/**
 * @author zc
 * @date 2019/12/09
 */
@Slf4j
@Service
public class RoutingCiscoASA implements RoutingGenerator, PolicyGenerator {

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
        return "configure terminal\n";
    }

    @Override
    public String generatePostCommandLine(RoutingCommandDTO routingCommandDTO) {
        return "exit\n";
    }

    /**
     * ip route destination 1.1.1.0 255.255.255.0 null0
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
                    //todo 未支持
                } else {
                    //ipv4
                    stringBuilder.append("ip route destination ").append(ip).append(" 255.255.255.255 null0\n");
                }
            });
        }
        stringBuilder.append(generatePostCommandLine(dto));
        return stringBuilder.toString();
    }

    /**
     * no ip route destination 1.1.1.0 255.255.255.0 null0
     * @param dto
     * @return
     * @throws UnknownHostException
     */
    @Override
    public String deleteRoutingCommandLine(RoutingCommandDTO dto) throws UnknownHostException {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(generatePreCommandLine(dto));
        String ipAddr = dto.getIpAddr();
        List<String> allIpList = RoutingGenerator.ipConvert(ipAddr);
        if (dto.getRoutingType().equals(RoutingCommandDTO.RoutingType.UNREACHABLE)) {
            log.info("删除黑洞路由");
            allIpList.forEach(ip -> {
                if (ip.contains(":")) {
                    //ipv6
                    //todo 未支持
                } else {
                    //ipv4
                    stringBuilder.append("no ip route destination ").append(ip).append(" 255.255.255.255 null0\n");
                }
            });
        }
        stringBuilder.append(generatePostCommandLine(dto));
        return stringBuilder.toString();
    }
}
