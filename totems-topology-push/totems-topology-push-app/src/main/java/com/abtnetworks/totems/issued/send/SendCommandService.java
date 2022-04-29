package com.abtnetworks.totems.issued.send;

import com.abtnetworks.totems.push.dto.PushCmdDTO;
import com.abtnetworks.totems.push.dto.PushResultDTO;

/**
 * @author zakyoung
 * @Title:
 * @Description: 请写注释类
 * @date 2020-03-18
 */
public interface SendCommandService {

    /**
     * 路由两种下发
     *
     * @param pushCmdDTO
     * @return
     */
    PushResultDTO routeNewOrOldExecuteByRegular(PushCmdDTO pushCmdDTO);

    PushResultDTO routeNewOrOldExecuteByRegular(PushCmdDTO pushCmdDTO, String pushKey);
}
