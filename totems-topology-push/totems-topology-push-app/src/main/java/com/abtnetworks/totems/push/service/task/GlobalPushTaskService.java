package com.abtnetworks.totems.push.service.task;

import com.abtnetworks.totems.push.dto.CommandTaskDTO;

import java.util.List;

//物理/云 策略下发/回滚
public interface GlobalPushTaskService {
    List<CommandTaskDTO> getCommandTaskDTOListByTaskid(List<Integer> idList, boolean revert, StringBuilder errMsg, List<String> themeList, List<Integer> weTaskIds);

    int getWePushTaskListByTaskId(List<Integer> weTaskIds);

    void pushWeTask(List<Integer> weTaskIds, boolean revert);
}
