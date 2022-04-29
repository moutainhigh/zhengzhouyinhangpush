package com.abtnetworks.totems.issued.send.impl;

import com.abtnetworks.totems.issued.send.ClientExecuteAfterService;
import com.abtnetworks.totems.push.dto.PushCmdDTO;
import com.abtnetworks.totems.push.dto.PushResultDTO;
import org.springframework.stereotype.Service;

/**
 * @author zakyoung
 * @Title:
 * @Description: 客户端执行后
 * @date 2020-03-22
 */
@Service
public class ClientExecuteAfterServiceImpl implements ClientExecuteAfterService {

    @Override
    public String repairEchoCommand(PushCmdDTO pushCmdDTO, PushResultDTO pushResultDTO) {
        switch (pushCmdDTO.getDeviceModelNumberEnum()) {
            case SRX:
                String newCommand = pushResultDTO.getCmdEcho().replaceAll("\b", "");
                return newCommand;
            default:
                return pushResultDTO.getCmdEcho();
        }

    }
}
