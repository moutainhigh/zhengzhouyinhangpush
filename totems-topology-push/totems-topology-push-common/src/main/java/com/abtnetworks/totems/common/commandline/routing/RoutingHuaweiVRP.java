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
 * @date 2019/12/17
 */
@Slf4j
@Service
public class RoutingHuaweiVRP implements RoutingGenerator , PolicyGenerator {

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
    public String generatePreCommandLine(RoutingCommandDTO dto) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("system-view")
                .append("\n");
        if(dto.getIsVsys() != null && dto.getIsVsys()) {
            stringBuilder.append("switch vsys ")
                    .append(dto.getVsysName())
                    .append("\n");
        }
        return stringBuilder.toString();
    }

    @Override
    public String generatePostCommandLine(RoutingCommandDTO routingCommandDTO) {
        return "\n" +
                "quit" +
                "\n" +
                "return" +
                "\n";
    }

    /**
     * ip route-static 1.1.1.0 24 Null0
     * ipv6 route-static ff:a::2:d 128 Null0
     * @param dto
     * @return
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
                    stringBuilder.append("ipv6 route-static ").append(ip).append(" 128 Null0\n");
                } else {
                    //ipv4
                    stringBuilder.append("ip route-static ").append(ip).append(" 32 Null0\n");
                }
            });
        }
        stringBuilder.append(generatePostCommandLine(dto));
        return stringBuilder.toString();
    }

    /**
     * undo ip route-static 1.1.1.0 24 Null0
     * undo ipv6 route-static ff:a::2:d 128 Null0
     * @param dto
     * @return
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
                    stringBuilder.append("undo ipv6 route-static ").append(ip).append(" 128 Null0\n");
                } else {
                    //ipv4
                    stringBuilder.append("undo ip route-static ").append(ip).append(" 32 Null0\n");
                }
            });
        }
        stringBuilder.append(generatePostCommandLine(dto));
        return stringBuilder.toString();
    }
}
