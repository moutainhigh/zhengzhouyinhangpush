package com.abtnetworks.totems.push.service.executor.impl;

import com.abtnetworks.totems.common.constants.ReturnCode;
import com.abtnetworks.totems.push.dto.PushCmdDTO;
import com.abtnetworks.totems.push.dto.PushResultDTO;
import com.abtnetworks.totems.push.service.executor.Executor;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.List;

public class TelnetExec implements Executor {
    private static Logger logger = Logger.getLogger(TelnetExec.class);

    @Override
    public PushResultDTO exec(PushCmdDTO pushCmdDTO){
        PushResultDTO pushResultDTO = new PushResultDTO();
        try {
            initSession(pushCmdDTO.getDeviceManagerIp(), pushCmdDTO.getUsername(), pushCmdDTO.getPassword(),
                    pushCmdDTO.getPort());

            if(canConnection()) {
                List<String> resultCmdList = execCommand(pushCmdDTO);

                StringBuilder sb = new StringBuilder();
                for(String str: resultCmdList) {
//                    logger.info(str);
                    sb.append(str).append("\n");
                }
                pushResultDTO.setResult(ReturnCode.POLICY_MSG_OK);
                pushResultDTO.setCmdEcho(sb.toString());
            } else {
                pushResultDTO.setResult(ReturnCode.CONNECT_FAILED);
            }
        } catch(Exception e) {
            logger.error("下发异常：", e);
            pushResultDTO.setResult(ReturnCode.PUSH_TASK_ERROR);
        }
        return pushResultDTO;
    }

    void initSession(String ip, String userName, String password, int port) {

    }

    boolean canConnection() {
        return true;
    }

    private List<String> execCommand(PushCmdDTO dto) {
        return null;
    }
}
