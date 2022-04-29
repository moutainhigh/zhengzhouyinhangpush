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
public class RoutingJuniperRouter implements RoutingGenerator , PolicyGenerator {

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
        return "cli\nconfiguration\n";
    }

    @Override
    public String generatePostCommandLine(RoutingCommandDTO routingCommandDTO) {
        return "commit\n";
    }

    /**
     * set routing-options static route 1.1.1.1/32 discard
     * set routing-options rib inet6.0 static route 1111:1111:2222::2222/128 discard
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
                    stringBuilder.append("set routing-options rib inet6.0 static route ").append(ip).append("/128 discard\n");
                } else {
                    //ipv4
                    stringBuilder.append("set routing-options static route ").append(ip).append("/32 discard\n");
                }
            });
        }
        stringBuilder.append(generatePostCommandLine(dto));
        return stringBuilder.toString();
    }

    /**
     * delete routing-options static route 1.1.1.1/32 discard
     * delete routing-options rib inet6.0 static route 1111:1111:2222::2222/128 discard
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
                    stringBuilder.append("delete routing-options rib inet6.0 static route ").append(ip).append("/128 discard\n");
                } else {
                    //ipv4
                    stringBuilder.append("delete routing-options static route ").append(ip).append("/32 discard\n");
                }
            });
        }
        stringBuilder.append(generatePostCommandLine(routingCommandDTO));
        return stringBuilder.toString();
    }
}
