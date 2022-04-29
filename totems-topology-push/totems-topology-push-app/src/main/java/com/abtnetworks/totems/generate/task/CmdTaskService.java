package com.abtnetworks.totems.generate.task;

import com.abtnetworks.totems.branch.dto.UserInfoDTO;
import com.abtnetworks.totems.common.dto.CmdDTO;
import com.abtnetworks.totems.common.dto.GeneratedDeviceDTO;

import java.util.List;


public interface CmdTaskService {

    /**
     *
     * @param cmdDTO
     * @param userInfoDTO
     */
    void getRuleMatchFlow2Generate(CmdDTO cmdDTO,UserInfoDTO userInfoDTO);

    /**
     * 添加命令行生成任务
     * @param cmdDTOList 命令行生成数据
     * @param userInfoDTO
     * @param cmdDTO
     * @return 返回结果
     */
    int addGenerateCmdTask(List<CmdDTO> cmdDTOList, UserInfoDTO userInfoDTO, CmdDTO cmdDTO, List<GeneratedDeviceDTO> deviceDTOS);
}
