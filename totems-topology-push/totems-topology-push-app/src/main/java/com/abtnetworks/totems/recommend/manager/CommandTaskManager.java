package com.abtnetworks.totems.recommend.manager;

import com.abtnetworks.totems.recommend.dto.recommend.EditCommandDTO;
import com.abtnetworks.totems.recommend.entity.CommandTaskEditableEntity;
import com.abtnetworks.totems.recommend.vo.CommandVO;

import java.util.List;

public interface CommandTaskManager {
    /**
     * 添加命令行任务
     * @param entity 命令行任务实例对象
     * @return 添加数量
     */
    int addCommandEditableEntityTask(CommandTaskEditableEntity entity);

    int insertCommandEditableEntityTask(CommandTaskEditableEntity entity);

    /**
     * 批量添加命令行任务
     * @param list 命令航任务列表
     */
    int insertCommandEditableEntityList(List<CommandTaskEditableEntity> list);

    /**
     * 根据taskId查找命令行
     * @param taskId 任务id
     * @return 命令行实例对象数据
     */
    CommandTaskEditableEntity getCommandEditableEntityByTaskIdAndDeviceUuid(int taskId, String deviceUuid);

    List<CommandVO> getCommandByTaskId(int taskId);

    List<CommandTaskEditableEntity> getCommandTaskByTaskId(int taskId);


    /**
     * 通过id查询命令行下发对象
     * @param id
     * @return
     */
    CommandTaskEditableEntity selectByPrimaryKey(int id);
    /**
     * 编辑命令行
     * @param editCommandDTO
     * @param userName

     * @return
     */
    int editCommandEditableEntity(EditCommandDTO editCommandDTO, String userName);

    /**
     * 更新命令行下发回显结果
     * @param id 主键Id
     * @param commandlineEcho 下发回显结果
     * @return 更新结果
     */
    int updateCommandEcho(int id, String commandlineEcho);

    int update(CommandTaskEditableEntity entity);

    int removeByTaskId(int taskId);

    List<CommandTaskEditableEntity> getScheduledCommand();

    List<CommandTaskEditableEntity> getExecuteTask();

    int setPushSchedule(CommandTaskEditableEntity entity);
}
