package com.abtnetworks.totems.common.commandline.routing;

import com.abtnetworks.totems.common.commandline.PolicyGenerator;
import com.abtnetworks.totems.common.dto.CmdDTO;
import com.abtnetworks.totems.common.dto.commandline.RoutingCommandDTO;

/**
 * @author zc
 * @date 2019/11/21
 */
public class DefaultRouting implements RoutingGenerator , PolicyGenerator {

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
        return null;
    }

    @Override
    public String generatePostCommandLine(RoutingCommandDTO routingCommandDTO) {
        return null;
    }

    @Override
    public String generatorRoutingCommandLine(RoutingCommandDTO routingCommandDTO) {
        return null;
    }

    @Override
    public String deleteRoutingCommandLine(RoutingCommandDTO routingCommandDTO) {
        return null;
    }
}
