package com.abtnetworks.totems.common.commandline.rollback;

import com.abtnetworks.totems.common.commandline.PolicyGenerator;
import com.abtnetworks.totems.common.commandline.routing.RoutingJuniperSrx;
import com.abtnetworks.totems.common.dto.CmdDTO;
import com.abtnetworks.totems.common.dto.commandline.RoutingCommandDTO;
import org.springframework.stereotype.Service;

@Service
public class RollbackRoutingJuniperSrx implements PolicyGenerator {
    @Override
    public String generate(CmdDTO cmdDTO) {
        RoutingJuniperSrx generator = new RoutingJuniperSrx();
        RoutingCommandDTO routingCommandDTO = getRoutingCommandDTO(cmdDTO);
        String commandline = "命令行生成失败，未知主机异常！";
        try{
            commandline = generator.deleteRoutingCommandLine(routingCommandDTO);
        } catch (Exception e) {

        }
        return commandline;
    }

    RoutingCommandDTO getRoutingCommandDTO(CmdDTO cmdDTO) {
        RoutingCommandDTO routingCommandDTO = new RoutingCommandDTO();

        return routingCommandDTO;
    }
}