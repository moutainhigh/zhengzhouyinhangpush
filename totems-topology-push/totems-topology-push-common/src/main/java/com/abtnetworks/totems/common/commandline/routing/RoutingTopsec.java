package com.abtnetworks.totems.common.commandline.routing;

import com.abtnetworks.totems.common.commandline.PolicyGenerator;
import com.abtnetworks.totems.common.dto.CmdDTO;
import com.abtnetworks.totems.common.dto.DeviceDTO;
import com.abtnetworks.totems.common.dto.commandline.RoutingCommandDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.net.UnknownHostException;
import java.util.List;

/**
 * 天融信路由命令行
 * @author luwei
 * @date 2020/7/9
 */
@Slf4j
@Service
public class RoutingTopsec implements RoutingGenerator , PolicyGenerator {


    @Override
    public String generate(CmdDTO cmdDTO) {
        RoutingCommandDTO routingCommandDTO = new RoutingCommandDTO();

        DeviceDTO deviceDTO = cmdDTO.getDevice();

        routingCommandDTO.setDeviceUuid(deviceDTO.getDeviceUuid());
        routingCommandDTO.setIsVsys(deviceDTO.isVsys());
        routingCommandDTO.setVsysName(deviceDTO.getVsysName());

        String commandline = "命令行生成失败，未知主机异常！";
        try {
            commandline = generatorRoutingCommandLine(routingCommandDTO);
        } catch (Exception e) {

        }
        return commandline;
    }

    @Override
    public String generatePreCommandLine(RoutingCommandDTO routingCommandDTO) {
        return "";
    }

    @Override
    public String generatePostCommandLine(RoutingCommandDTO routingCommandDTO) {
        return "";
    }

    @Override
    public String generatorRoutingCommandLine(RoutingCommandDTO routingCommandDTO) throws UnknownHostException {

        StringBuilder sb = new StringBuilder();
        sb.append(generatePreCommandLine(routingCommandDTO));
        String ipAddr = routingCommandDTO.getIpAddr();
        List<String> allIpList = RoutingGenerator.ipConvert(ipAddr);
        if (routingCommandDTO.getRoutingType().equals(RoutingCommandDTO.RoutingType.UNREACHABLE)) {
            log.info("配置黑洞路由");
            allIpList.forEach(ip -> {
                if (ip.contains(":")) {
                    //ipv6
                    sb.append("network route add family ipv6 dst ").append(ip).append("/128 null 0\n");
                } else {
                    //ipv4
                    sb.append("network route add dst ").append(ip).append(" 32 null 0\n");
                }
            });
        }
        sb.append(generatePostCommandLine(routingCommandDTO));
        return sb.toString();
    }

    @Override
    public String deleteRoutingCommandLine(RoutingCommandDTO routingCommandDTO) throws UnknownHostException {
        StringBuilder sb = new StringBuilder();
        sb.append(generatePreCommandLine(routingCommandDTO));
        String ipAddr = routingCommandDTO.getIpAddr();
        List<String> allIpList = RoutingGenerator.ipConvert(ipAddr);
        if (routingCommandDTO.getRoutingType().equals(RoutingCommandDTO.RoutingType.UNREACHABLE)) {
            log.info("删除黑洞路由");
            allIpList.forEach(ip -> {
                if (ip.contains(":")) {
                    //ipv6
                    sb.append("network route delete family ipv6 dst ").append(ip).append("/128 null 0\n");
                } else {
                    //ipv4
                    sb.append("network route delete dst ").append(ip).append(" 32 null 0\n");
                }
            });
        }
        sb.append(generatePostCommandLine(routingCommandDTO));
        return sb.toString();
    }

    public static void main(String[] args) throws Exception{
        RoutingCommandDTO dto = new RoutingCommandDTO();
        dto.setIpAddr("192.168.1.1-192.168.1-5,172.171.2/24,192.168.2.6");
        dto.setRoutingType(RoutingCommandDTO.RoutingType.UNREACHABLE);
        RoutingTopsec topsec = new RoutingTopsec();
        String command = topsec.generatorRoutingCommandLine(dto);
        System.out.println(command);
    }
}
