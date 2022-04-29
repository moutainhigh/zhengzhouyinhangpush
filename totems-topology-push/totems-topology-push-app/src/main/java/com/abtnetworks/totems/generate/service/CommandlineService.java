package com.abtnetworks.totems.generate.service;

import com.abtnetworks.totems.branch.dto.UserInfoDTO;
import com.abtnetworks.totems.common.dto.CmdDTO;
import com.abtnetworks.totems.common.dto.DeviceDTO;
import com.abtnetworks.totems.common.dto.generate.GenerateCommandDTO;
import com.abtnetworks.totems.common.entity.NodeEntity;
import com.abtnetworks.totems.recommend.entity.CommandTaskEditableEntity;

import java.util.List;

public interface CommandlineService {

    String generate(CmdDTO cmdDTO);

    /**
     * 生成命令行
     * @param cmdDTO
     * @return
     */
    GenerateCommandDTO generateCommand(CmdDTO cmdDTO);


    /**
     * 生成命令行
     * @param cmdDTO
     * @return
     */
    GenerateCommandDTO generateCommandForFiveBalance(CmdDTO cmdDTO);


    /**
     * 生成灾备设备命令行
     * @param cmdDTO
     * @param userInfoDTO
     * @param anotherDeviceByIp
     * @param disasterRecoveryDevice

     * @return
     */
    CmdDTO generateDisasterRecovery(CmdDTO cmdDTO, UserInfoDTO userInfoDTO,NodeEntity anotherDeviceByIp,DeviceDTO disasterRecoveryDevice);

    /**
     * 仿真，生成和设置有效路径
     * @param cmdDTO
     * @return
     */
    boolean generateCommon(CmdDTO cmdDTO);

    /**
     * 添加命令行和下发列表状态
     * @param id
     * @param commandLine
     * @param revertCommandline
     * @param status
     */
    void updateCommandStatus(Integer id, String commandLine, String revertCommandline, Integer status,String editPolicyTotalInfo,String errorMsg);

    /**
     * 更新下发命令行状态和匹配信息
     * @param entity
     */
    void updateCommandByEntity(CommandTaskEditableEntity entity);

    /**
     * 下发任务状态
     * @param taskId
     * @param status
     */
    void updateTaskStatus(Integer taskId, Integer status);

    /**
     * 保存
     * @param cmdDTO
     * @param anotherDeviceByIp
     * @param userInfoDTO
     * @return
     */
    List<CommandTaskEditableEntity> saveDisasterDeviceCommandline(CmdDTO cmdDTO, List<NodeEntity> anotherDeviceByIp, UserInfoDTO userInfoDTO  );

    /**
     * 是否存在灾备设备
     * @param cmdDTO
     * @param disasterRecoveryDevice
     * @return
     */
    List<NodeEntity> isDisasterRecoveryDevice(CmdDTO cmdDTO, DeviceDTO disasterRecoveryDevice);
}
