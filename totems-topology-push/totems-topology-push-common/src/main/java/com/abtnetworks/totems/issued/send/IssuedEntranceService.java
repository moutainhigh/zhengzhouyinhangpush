package com.abtnetworks.totems.issued.send;

import com.abtnetworks.totems.issued.dto.PushCommandRegularParamDTO;
import com.abtnetworks.totems.push.dto.PushCmdDTO;
import com.abtnetworks.totems.push.dto.PushResultDTO;

/**
 * @author Administrator
 * @Title:
 * @Description: 下发入口类
 * @date 2021/6/30
 */
public interface IssuedEntranceService {
    /**
     * 下发入口方法
     * @param pushCmdDTO
     * @param pushCommandRegularParamDTO
     * @return
     */
    PushResultDTO routeNewExecuteByRegular(PushCmdDTO pushCmdDTO, PushCommandRegularParamDTO pushCommandRegularParamDTO);

    PushResultDTO routeNewExecuteByRegular(PushCmdDTO pushCmdDTO, PushCommandRegularParamDTO pushCommandRegularParamDTO, String pushKey);
}
