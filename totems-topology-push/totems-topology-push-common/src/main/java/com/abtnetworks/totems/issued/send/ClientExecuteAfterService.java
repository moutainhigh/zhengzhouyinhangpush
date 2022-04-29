package com.abtnetworks.totems.issued.send;

import com.abtnetworks.totems.push.dto.PushCmdDTO;
import com.abtnetworks.totems.push.dto.PushResultDTO;

/**
 * @author zakyoung
 * @Title:
 * @Description: 请写注释类
 * @date 2020-03-22
 */
public interface ClientExecuteAfterService {
    /**
     * 修补Srx回显命令行不友好问题，比如转码问题
     *
     * @param pushCmdDTO
     * @param pushResultDTO
     * @return
     */
    String repairEchoCommand(PushCmdDTO pushCmdDTO, PushResultDTO pushResultDTO);
}
