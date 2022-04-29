package com.abtnetworks.totems.push.service.executor;

import com.abtnetworks.totems.push.dto.PushCmdDTO;
import com.abtnetworks.totems.push.dto.PushResultDTO;

public interface Executor {

    /**
     * 下发命令行
     * @param pushCmdDTO 命令行下发任务对象
     * @return 下发结果对象
     */
    PushResultDTO exec(PushCmdDTO pushCmdDTO);
}
