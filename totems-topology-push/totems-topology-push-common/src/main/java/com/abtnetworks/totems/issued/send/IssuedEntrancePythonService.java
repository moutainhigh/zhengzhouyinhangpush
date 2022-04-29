package com.abtnetworks.totems.issued.send;

import com.abtnetworks.totems.push.dto.PushCmdDTO;
import com.abtnetworks.totems.push.dto.PushResultDTO;

/**
 * @author lifei
 * @desc XXXX
 * @date 2022/1/21 14:56
 */
public interface IssuedEntrancePythonService {


    /**
     * 下发特例-python下发入口
     * @param pushCmdDTO
     * @return
     */
    PushResultDTO commandExecuteByPython(PushCmdDTO pushCmdDTO);
}
